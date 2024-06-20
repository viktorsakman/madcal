package com.cs407.madcal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;


public class NewTaskFragment extends Fragment {

    private ArrayList<String> taskList;
    private ArrayAdapter<String> taskAdapter;
    View view;
    private String wiscId; // Variable to store WISC ID
    private int taskId;
    private boolean updating;

    public static boolean isValidDate(String dateString, String format) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalDate.parse(dateString, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_newtask, container, false);
        updating = false;

        // Retrieve the WISC ID from the fragment's arguments
        if (getArguments() != null) {
            wiscId = getArguments().getString("WISC_ID");
            taskId = getArguments().getInt("TASK_ID");
        }
        if (taskId != 0) {
            updating = true;
            populateEntries();
        }

        Button saveTaskButton = view.findViewById(R.id.save_button);
        saveTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try {
                    String task = ((EditText)view.findViewById(R.id.the_task)).getText().toString().trim();
                    String month = ((EditText) view.findViewById(R.id.the_month)).getText().toString().trim();
                    String day = ((EditText) view.findViewById(R.id.the_day)).getText().toString().trim();
                    String year = ((EditText) view.findViewById(R.id.the_year)).getText().toString().trim();
                    String hour = ((EditText) view.findViewById(R.id.the_hour)).getText().toString().trim();
                    String minute = ((EditText) view.findViewById(R.id.the_minute)).getText().toString().trim();

                    RadioGroup radioGroup = view.findViewById(R.id.dotSelectionGroup);
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    String meridiem = "";
                    if (selectedId == R.id.am_dot) {
                        meridiem = "AM";
                    } else if (selectedId == R.id.pm_dot) {
                        meridiem = "PM";
                    } else {
                        new AlertDialog.Builder(getActivity())
                                .setMessage("You must select either AM or PM.")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                        return;
                    }

                    if (Integer.valueOf(month) < 10 && month.length() == 1) {
                        month = "0" + month;
                    }

                    if (Integer.valueOf(day) < 10 && day.length() == 1) {
                        day = "0" + day;
                    }

                    if (Integer.valueOf(hour) < 10 && hour.length() == 1) {
                        hour = "0" + hour;
                    }

                    if (Integer.valueOf(minute) < 10 && minute.length() == 1) {
                        minute = "0" + minute;
                    }

                    if(!isValidDate(year + "-" + month + "-" + day, "yyyy-MM-dd")) {
                        //  Start of Dialog Code
                        new AlertDialog.Builder(getActivity())
                                .setMessage("You've entered an invalid date. Try again.")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                        //  End of Dialog Code
                    } else if ((Integer.valueOf(hour) < 1 || Integer.valueOf(hour) > 12)
                            || (Integer.valueOf(minute) < 0 || Integer.valueOf(minute) > 59)) {
                        //  Start of Dialog Code
                        new AlertDialog.Builder(getActivity())
                                .setMessage("You've entered an invalid (AM/PM) time. Try again.")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                        //  End of Dialog Code
                    } else if (Integer.valueOf(year) < 2000 || Integer.valueOf(year) > 2200) {
                        //  Start of Dialog Code
                        new AlertDialog.Builder(getActivity())
                                .setMessage("Make sure that the year isn't too far into the future or past.")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                        //  End of Dialog Code
                    } else {
                        // Construct date and time strings
                        String taskDate = year + "-" + month + "-" + day;
                        String taskTime = hour + ":" + minute + " " + meridiem;

                        // Use DatabaseHelper to insert the task
                        DatabaseHelper db = new DatabaseHelper(getActivity());
                        if (updating) {
                            db.updateTask(taskId, task, taskDate, taskTime);
                        } else {
                            db.addTask(task, taskDate, taskTime, wiscId);
                        }

                        Bundle notificationResult = new Bundle();
                        notificationResult.putString("taskTitle", task);
                        notificationResult.putString("taskDateTime", taskDate + " " + taskTime);
                        notificationResult.putInt("taskId", taskId);
                        getParentFragmentManager().setFragmentResult("task_notification_key", notificationResult);

                        Bundle listUpdateResult = new Bundle();
                        listUpdateResult.putBoolean("added", true);
                        getParentFragmentManager().setFragmentResult("task_add_key", listUpdateResult);

                        getFragmentManager().popBackStack();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //  Start of Dialog Code
                    new AlertDialog.Builder(getActivity())
                            .setMessage("There was an error processing your task. Make sure each box is filled and a valid date is entered.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Dismiss the dialog when "Ok" is clicked
                                    dialog.dismiss();

                                }
                            }).show();
                    //  End of Dialog Code
                }
            }
        });

        return view;
    }

    private void populateEntries() {
        ((TextView)view.findViewById(R.id.tasktext)).setText("Edit Task/Assignment:");

        DatabaseHelper db = new DatabaseHelper(getActivity());
        String[] taskDetails = db.getTaskById(taskId);

        String old_task = taskDetails[0];
        String old_month = taskDetails[1].split("-")[1];
        String old_day = taskDetails[1].split("-")[2];
        String old_year = taskDetails[1].split("-")[0];
        String old_hour = taskDetails[2].split(" ")[0].split(":")[0];
        String old_minute = taskDetails[2].split(" ")[0].split(":")[1];
        String old_meridiem = taskDetails[2].split(" ")[1];

        ((EditText)view.findViewById(R.id.the_task)).setText(old_task);
        ((EditText)view.findViewById(R.id.the_month)).setText(old_month);
        ((EditText)view.findViewById(R.id.the_day)).setText(old_day);
        ((EditText)view.findViewById(R.id.the_year)).setText(old_year);
        ((EditText)view.findViewById(R.id.the_hour)).setText(old_hour);
        ((EditText)view.findViewById(R.id.the_minute)).setText(old_minute);

        if (old_meridiem.equals("AM")) {
            ((RadioButton)view.findViewById(R.id.am_dot)).setChecked(true);
        } else if (old_meridiem.equals("PM")){
            ((RadioButton)view.findViewById(R.id.pm_dot)).setChecked(true);
        }
    }

}
