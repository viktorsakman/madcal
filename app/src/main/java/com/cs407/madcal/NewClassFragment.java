package com.cs407.madcal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;


public class NewClassFragment extends Fragment {

    private ArrayList<String> taskList;
    private ArrayAdapter<String> taskAdapter;
    View view;
    private String wiscId; // Variable to store WISC ID
    private int classId;
    private boolean updating;

    private DatabaseHelper db;

    private int[] weekdayIds = {R.id.monday_dot, R.id.tuesday_dot, R.id.wednesday_dot,
            R.id.thursday_dot, R.id.friday_dot, R.id.saturday_dot, R.id.sunday_dot};

    private ArrayList<Integer> checkedDaysIds;
    private ArrayList<String> checkedDays;

    private ArrayList<String> checkedDaysRange;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_newclass, container, false);
        checkedDaysIds = new ArrayList<Integer>();
        checkedDays = new ArrayList<String>();
        checkedDaysRange = new ArrayList<String>();
        db = new DatabaseHelper(getActivity());

        if (getArguments() != null) {
            wiscId = getArguments().getString("WISC_ID");
            classId = getArguments().getInt("CLASS_ID");
        }
        if (classId != 0) {
            updating = true;
            populateEntries();
        }


        Button saveClassButton = view.findViewById(R.id.save_class_button);
        saveClassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Called onClick()!");

                try {
                    String class_name = ((EditText) view.findViewById(R.id.class_name)).getText().toString().trim();
                    if (class_name.isEmpty() || class_name.length() <= 0) {
                        new AlertDialog.Builder(getActivity())
                                .setMessage("Your class has to have a name.")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                        return;
                    }

                    for (int id : weekdayIds) {
                        CheckBox checkBox = view.findViewById(id);
                        if (checkBox.isChecked()) {
                            checkedDaysIds.add(id);
                        }
                    }
                    if (checkedDaysIds.size() < 1) {
                        new AlertDialog.Builder(getActivity())
                                .setMessage("Your class must run on at least one day.")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                        return;
                    }

                    boolean ret = false;
                    for (int id : checkedDaysIds) {
                        checkedDays.add(((CheckBox)view.findViewById(id)).getText().toString().trim());
                        String weekday = ((CheckBox)view.findViewById(id)).getText().toString().trim().toLowerCase();


                        String from_hour = ((EditText)view.findViewById(getResources()
                                .getIdentifier(weekday + "_from_hour", "id", getActivity().getPackageName())))
                                .getText().toString().trim();

                        String from_minute = ((EditText)view.findViewById(getResources()
                                .getIdentifier(weekday + "_from_minute", "id", getActivity().getPackageName())))
                                .getText().toString().trim();

                        String to_hour = ((EditText)view.findViewById(getResources()
                                .getIdentifier(weekday + "_to_hour", "id", getActivity().getPackageName())))
                                .getText().toString().trim();

                        String to_minute = ((EditText)view.findViewById(getResources()
                                .getIdentifier(weekday + "_to_minute", "id", getActivity().getPackageName())))
                                .getText().toString().trim();

                        RadioGroup radioGroup = view.findViewById(getResources()
                                .getIdentifier(weekday + "_from_group", "id", getActivity().getPackageName()));

                        int selectedId = radioGroup.getCheckedRadioButtonId();
                        String from_meridiem = "";
                        if (selectedId == getResources().getIdentifier(weekday + "_from_am_dot", "id",
                                getActivity().getPackageName())) {
                            from_meridiem = "AM";
                        } else if (selectedId == getResources().getIdentifier(weekday + "_from_pm_dot", "id",
                                getActivity().getPackageName())) {
                            from_meridiem = "PM";
                        } else {
                            new AlertDialog.Builder(getActivity())
                                    .setMessage("You must select either AM or PM.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                            ret = true;
                            break;
                        }

                        radioGroup = view.findViewById(getResources()
                                .getIdentifier(weekday + "_to_group", "id", getActivity().getPackageName()));
                        selectedId = radioGroup.getCheckedRadioButtonId();
                        String to_meridiem = "";
                        if (selectedId == getResources().getIdentifier(weekday + "_to_am_dot", "id",
                                getActivity().getPackageName())) {
                            to_meridiem = "AM";
                        } else if (selectedId == getResources().getIdentifier(weekday + "_to_pm_dot", "id",
                                getActivity().getPackageName())) {
                            to_meridiem = "PM";
                        } else {
                            new AlertDialog.Builder(getActivity())
                                    .setMessage("You must select either AM or PM.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                            ret = true;
                            break;
                        }

                        System.out.println(class_name + " runs on " + weekday + " from " + from_hour + ":" + from_minute + " " + from_meridiem + " to " + to_hour + ":" + to_minute + " " + to_meridiem);

                        if ((Integer.valueOf(from_hour) < 1 || Integer.valueOf(from_hour) > 12
                                || Integer.valueOf(to_hour) < 1 || Integer.valueOf(to_hour) > 12)
                                || (Integer.valueOf(from_minute) < 0 || Integer.valueOf(from_minute) > 59
                                || Integer.valueOf(to_minute) < 0 || Integer.valueOf(to_minute) > 59)) {
                            new AlertDialog.Builder(getActivity())
                                    .setMessage("You've entered an invalid (AM/PM) time. Try again.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                            ret = true;
                            break;
                        }

                        if (Integer.valueOf(from_hour) < 10 && from_hour.length() == 1) {
                            from_hour = "0" + from_hour;
                        }

                        if (Integer.valueOf(from_minute) < 10 && from_minute.length() == 1) {
                            from_minute = "0" + from_minute;
                        }

                        if (Integer.valueOf(to_hour) < 10 && to_hour.length() == 1) {
                            to_hour = "0" + to_hour;
                        }

                        if (Integer.valueOf(to_minute) < 10 && to_minute.length() == 1) {
                            to_minute = "0" + to_minute;
                        }

                        String fromToRange = from_hour + ":" + from_minute + " " + from_meridiem +
                                " to " + to_hour + ":" + to_minute + " " + to_meridiem;
                        checkedDaysRange.add(fromToRange);
                    }

                    if (ret) {
                        checkedDays.clear();
                        checkedDaysIds.clear();
                        checkedDaysRange.clear();
                        return;
                    }

                    String class_days = "";
                    String class_range = "";
                    for (int i = 0; i < checkedDaysIds.size(); i++) {
                        if (i != 0) {
                            class_days += ",";
                            class_range += ",";
                        }
                        class_days += checkedDays.get(i);
                        class_range += checkedDaysRange.get(i);
                    }

                    if (updating) {
                        db.updateClass(classId, class_name, class_days, class_range, wiscId);
                    } else {
                        db.addClass(class_name, class_days, class_range, wiscId);
                    }

                    // After successful add
                    Bundle result = new Bundle();
                    result.putBoolean("new_class", true);
                    getParentFragmentManager().setFragmentResult("class_add_key", result);
                    getFragmentManager().popBackStack();


                } catch (Exception e) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage("There was an error processing your class. Make sure that for each weekday selected," +
                                    "each box is filled and a valid time is entered.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Dismiss the dialog when "Ok" is clicked
                                    dialog.dismiss();
                                }
                            }).show();
                    e.printStackTrace();
                }
            }
        });
        return view;
    }

    private void populateEntries() {
        String[] classDetails = db.getClassById(classId);

        String old_className = classDetails[0];
        String old_classDays = classDetails[1];
        String old_classRange = classDetails[2];

        ((EditText) view.findViewById(R.id.class_name)).setText(old_className);

        String[] old_daysArray = old_classDays.split(",");
        String[] old_rangesArray = old_classRange.split(",");

        for (int i = 0; i < old_daysArray.length; i++) {

            String day = old_daysArray[i].trim();
            String range = old_rangesArray[i].trim();
            String[] times = range.split(" to ");

            String[] startTime = times[0].split(":");
            String[] endTime = times[1].split(":");

            int checkBoxId = getResources().getIdentifier(day.toLowerCase() + "_dot", "id", getActivity().getPackageName());
            CheckBox checkBox = view.findViewById(checkBoxId);
            if (checkBox != null) {
                checkBox.setChecked(true);
            }

            int old_fromHourId = getResources().getIdentifier(day.toLowerCase() + "_from_hour", "id", getActivity().getPackageName());
            int old_fromMinuteId = getResources().getIdentifier(day.toLowerCase() + "_from_minute", "id", getActivity().getPackageName());
            int old_toHourId = getResources().getIdentifier(day.toLowerCase() + "_to_hour", "id", getActivity().getPackageName());
            int old_toMinuteId = getResources().getIdentifier(day.toLowerCase() + "_to_minute", "id", getActivity().getPackageName());

            EditText fromHour = view.findViewById(old_fromHourId);
            EditText fromMinute = view.findViewById(old_fromMinuteId);
            EditText toHour = view.findViewById(old_toHourId);
            EditText toMinute = view.findViewById(old_toMinuteId);

            if (fromHour != null && fromMinute != null && toHour != null && toMinute != null) {
                fromHour.setText(startTime[0]);
                fromMinute.setText(startTime[1].substring(0, 2));
                toHour.setText(endTime[0]);
                toMinute.setText(endTime[1].substring(0, 2));

                day = day.toLowerCase();
                String startAmPm = startTime[1].substring(3);
                String endAmPm = endTime[1].substring(3);

                int fromAmPmId = getResources().getIdentifier(day + "_from_" + startAmPm.toLowerCase() + "_dot", "id", getActivity().getPackageName());
                int toAmPmId = getResources().getIdentifier(day + "_to_" + endAmPm.toLowerCase() + "_dot", "id", getActivity().getPackageName());

                RadioButton fromAmPm = view.findViewById(fromAmPmId);
                RadioButton toAmPm = view.findViewById(toAmPmId);

                if (fromAmPm != null) fromAmPm.setChecked(true);
                if (toAmPm != null) toAmPm.setChecked(true);
            }
        }
    }
}
