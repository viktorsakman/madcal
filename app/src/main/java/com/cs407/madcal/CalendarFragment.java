package com.cs407.madcal;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.applandeo.materialcalendarview.EventDay;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.fragment.app.Fragment;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class CalendarFragment extends Fragment {

    private String wiscId;
    private DatabaseHelper db;
    CalendarView calendarView;
    private ListView listView;
    ArrayList<Integer> taskIdList;
    ArrayList<String> taskDescriptions;
    ArrayAdapter<String> adapter;
    ArrayList<Calendar> highlightedDates;

    final String defaultGreeting = "Select one of the highlighted dates in order to see the tasks that are due on that day." +
            "\n\nIf there are none, add some tasks.";

    private String parseDate(String[] task) {
        String[] dateParts = task[0].split("Due on: ")[1].split(",")[0].trim().split(" ")[0].split("/");
        int month = Integer.parseInt(dateParts[0]);
        int day = Integer.parseInt(dateParts[1]);
        int year = Integer.parseInt(dateParts[2]);

        String taskDate = "";
        if (month < 10) {
            taskDate += month;
        } else {
            taskDate += String.format("%02d", month);
        }
        taskDate += "/";

        if (day < 10) {
            taskDate += day;
        } else {
            taskDate += String.format("%02d", day);
        }
        taskDate += "/" + year;

        return taskDate;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Retrieve the WISC ID from the fragment's arguments
        if (getArguments() != null) {
            wiscId = getArguments().getString("WISC_ID");
        }

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);

        db = new DatabaseHelper(requireContext());
        createTaskList(view);
        highlightedDates = new ArrayList<>();
        refreshCalendar();

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Calendar clickedDayCalendar = eventDay.getCalendar();

                // Format the date as you want
                String selectedDate = (clickedDayCalendar.get(Calendar.MONTH) + 1) + "/" +
                        clickedDayCalendar.get(Calendar.DAY_OF_MONTH) + "/" +
                        clickedDayCalendar.get(Calendar.YEAR);

                // Filter tasks for the selected date
                List<String[]> allTasks = db.getTasksByWiscId(wiscId);
                taskDescriptions.clear();
                taskIdList.clear();

                for (String[] task : allTasks) {
                    String taskDate = parseDate(task);
                    if (taskDate.equals(selectedDate)) {
                        taskDescriptions.add(task[0]);
                        taskIdList.add(Integer.parseInt(task[1]));
                    }
                }
                if (taskDescriptions.isEmpty()) {
                    taskDescriptions.add(defaultGreeting);
                }
                adapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCalendar();
        createTaskList(getView());
    }

    private void refreshCalendar() {
        List<String[]> tasks = db.getTasksByWiscId(wiscId);
        List<EventDay> events = new ArrayList<>();
        for (String[] task : tasks) {
            String[] dueDate = task[0].split("Due on: ")[1].split(",")[0].trim().split(" ")[0].split("/");

            int year = Integer.parseInt(dueDate[2]);
            int month = Integer.parseInt(dueDate[0]) - 1;
            int day = Integer.parseInt(dueDate[1]);

            Calendar dateToHighlight = Calendar.getInstance();
            dateToHighlight.set(year, month, day);
            EventDay dayToHighlight = new EventDay(dateToHighlight, R.drawable.red_arrow);
            events.add(dayToHighlight);
            highlightedDates.add(dateToHighlight);
        }
        calendarView.setEvents(events);
    }
    private void createTaskList(View view) {
        listView = view.findViewById(R.id.taskListView);
        List<String[]> tasks = db.getTasksByWiscId(wiscId);

        taskDescriptions = new ArrayList<>();
        taskIdList = new ArrayList<>();

        taskDescriptions.add(defaultGreeting);

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, taskDescriptions);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
