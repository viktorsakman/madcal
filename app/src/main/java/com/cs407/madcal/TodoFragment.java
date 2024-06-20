package com.cs407.madcal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodoFragment extends Fragment {

    private EditText taskEditText;
    private ListView listView;
    private Button addTaskButton;

    private ArrayList<String> taskList;
    private ArrayAdapter<String> taskAdapter;
    private String wiscId; // Variable to store WISC ID
    private View view;
    ArrayAdapter<String> adapter;
    DatabaseHelper db;
    ArrayList<Integer> taskIdList;
    ArrayList<String> taskDescriptions;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_todo, container, false);

        // Retrieve the WISC ID from the fragment's arguments
        if (getArguments() != null) {
            wiscId = getArguments().getString("WISC_ID");
        }

        TextView todaysDateTextView = view.findViewById(R.id.todays_date);
        String currentDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date());
        todaysDateTextView.setText("Today's Date: " + currentDate);

        db = new DatabaseHelper(getActivity());
        createTaskList(view);

        Button addTaskButton = view.findViewById(R.id.add_task_button);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment newTaskFragment = new NewTaskFragment();
                Bundle bundle = new Bundle();
                bundle.putString("WISC_ID", wiscId);
                newTaskFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, newTaskFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                int taskId = (int)taskIdList.get(position);

                //  Start of Dialog Code
                new AlertDialog.Builder(getActivity())
                        .setMessage("Would you like to edit or delete this assignment?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                new AlertDialog.Builder(getActivity())
                                        .setMessage("Are you sure you want to delete?")
                                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                db.deleteTask(taskId);
                                                createTaskList(getView());
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
                                int taskId = (int)taskIdList.get(position);

                                Fragment editTaskFragment = new NewTaskFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("WISC_ID", wiscId);
                                bundle.putInt("TASK_ID", taskId);
                                editTaskFragment.setArguments(bundle);;

                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, editTaskFragment)
                                        .addToBackStack(null)
                                        .commit();
                            }
                        }).show();
                //  End of Dialog Code
            }
        });

        getParentFragmentManager().setFragmentResultListener("task_add_key", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                if (result.getBoolean("added")) {
                    createTaskList(getView());
                }
            }
        });

        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        createTaskList(view);
    }

    private void createTaskList(View view) {
        listView = view.findViewById(R.id.list_item);
        List<String[]> tasks = db.getTasksByWiscId(wiscId);

        taskDescriptions = new ArrayList<>();
        taskIdList = new ArrayList<>();

        for (String[] task : tasks) {
            taskDescriptions.add(task[0]);
            taskIdList.add(Integer.parseInt(task[1]));
        }

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, taskDescriptions);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}