package com.cs407.madcal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import java.util.List;

public class CustomAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> taskDescriptions;
    private List<Boolean> checkboxStates; // List to track checkbox states

    public CustomAdapter(Context context, List<String> taskDescriptions, List<Boolean> checkboxStates) {
        super(context, R.layout.list_item_todo, taskDescriptions);
        this.context = context;
        this.taskDescriptions = taskDescriptions;
        this.checkboxStates = checkboxStates;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_todo, null);
        }

        CheckBox checkbox = view.findViewById(R.id.todo_checkbox);
        TextView descriptionTextView = view.findViewById(R.id.todo_description);

        // Set the task description
        String taskDescription = taskDescriptions.get(position);
        descriptionTextView.setText(taskDescription);

        // Set the checkbox state based on checkboxStates list
        checkbox.setChecked(checkboxStates.get(position));
        checkbox.setVisibility(View.VISIBLE);


        return view;
    }
}
