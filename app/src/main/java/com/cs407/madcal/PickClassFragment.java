package com.cs407.madcal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;


public class PickClassFragment extends Fragment {

    private ArrayList<String> classNames;
    private ArrayList<Integer> classIds;
    View view;
    private String wiscId; // Variable to store WISC ID

    private int[] weekdayIds = {R.id.monday_dot, R.id.tuesday_dot, R.id.wednesday_dot,
            R.id.thursday_dot, R.id.friday_dot, R.id.saturday_dot, R.id.sunday_dot};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_pickclass, container, false);

        classNames = new ArrayList<>();
        classIds = new ArrayList<>();

        if (getArguments() != null) {
            wiscId = getArguments().getString("WISC_ID");
        }

        DatabaseHelper db = new DatabaseHelper(getActivity());
        List<String[]> classes = db.getClassesByWiscId(wiscId);

        // Populate the class names and IDs
        for (String[] classInfo : classes) {
            classIds.add(Integer.parseInt(classInfo[0])); // Add class ID
            classNames.add(classInfo[1]); // Add class name
        }

        ListView listView = view.findViewById(R.id.class_listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, classNames);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                int classId = classIds.get(position);

                //  Start of Dialog Code
                new AlertDialog.Builder(getActivity())
                        .setMessage("Would you like to edit or delete this class?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                new AlertDialog.Builder(getActivity())
                                        .setMessage("Are you sure you want to delete?")
                                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                db.deleteClass(classId);
                                                classNames.remove(position);
                                                classIds.remove(position);
                                                adapter.notifyDataSetChanged();

                                                Bundle result = new Bundle();
                                                result.putBoolean("deleted_class", true);
                                                getParentFragmentManager().setFragmentResult("class_delete_key", result);
                                                getFragmentManager().popBackStack();
                                            }
                                        })
                                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                            }
                        })
                        .setNeutralButton("DO NOTHING", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("EDIT", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                int classId = (int)classIds.get(position);

                                Fragment editClassFragment = new NewClassFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("WISC_ID", wiscId);
                                bundle.putInt("CLASS_ID", classId);
                                editClassFragment.setArguments(bundle);;

                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, editClassFragment)
                                        .addToBackStack(null)
                                        .commit();
                            }
                        }).show();
                //  End of Dialog Code

            }
        });

        return view;
    }
}
