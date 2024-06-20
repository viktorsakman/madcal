package com.cs407.madcal;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import java.util.ArrayList;
import java.util.List;

public class ScheduleFragment extends Fragment {

    private String wiscId; // Variable to store WISC ID
    DatabaseHelper db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        // Retrieve the WISC ID from the fragment's arguments
        if (getArguments() != null) {
            wiscId = getArguments().getString("WISC_ID");
        }

        db = new DatabaseHelper(getActivity());
        createClassSchedule(view);

        Button scheduleButton = view.findViewById(R.id.schedule_button);
        scheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment newClassFragment = new NewClassFragment();
                Bundle bundle = new Bundle();
                bundle.putString("WISC_ID", wiscId);
                newClassFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, newClassFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        Button editButton = view.findViewById(R.id.edit_class_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment pickClassFragment = new PickClassFragment();
                Bundle bundle = new Bundle();
                bundle.putString("WISC_ID", wiscId);
                pickClassFragment .setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, pickClassFragment )
                        .addToBackStack(null)
                        .commit();
            }
        });

        getParentFragmentManager().setFragmentResultListener("class_add_key", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                if (result.getBoolean("new_class")) {
                    createClassSchedule(getView());
                }
            }
        });

        getParentFragmentManager().setFragmentResultListener("class_delete_key", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                if (result.getBoolean("deleted_class")) {
                    createClassSchedule(getView());
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        createClassSchedule(getView());
    }

    private void createClassSchedule(View view) {
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        ListView listView = view.findViewById(R.id.class_listview);

        List<String> classSchedules = new ArrayList<>();

        for (String day : daysOfWeek) {
            List<String> classesForDay = db.getClassesByDay(day, wiscId);
            StringBuilder daySchedule = new StringBuilder(day + ":\n");
            for (String classInfo : classesForDay) {
                daySchedule.append("\n").append(classInfo);
            }
            classSchedules.add(daySchedule.toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, classSchedules);
        listView.setAdapter(adapter);
    }
}
