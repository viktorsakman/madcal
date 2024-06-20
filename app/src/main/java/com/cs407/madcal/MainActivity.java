package com.cs407.madcal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    String wiscId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Notification permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }


        // Check if user is logged in
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (!isLoggedIn) {
            navigateToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        wiscId = sharedPreferences.getString("WISC_ID", ""); // Retrieve the WISC ID from SharedPreferences
        if (wiscId == null) {
            logout();
        }

        NotificationHelper notificationHelper = new NotificationHelper();
        notificationHelper.createNotificationChannel(this);
        scheduleNotifications();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, wiscId); // Pass the WISC ID
        viewPager.setAdapter(sectionsPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Calendar");
                    break;
                case 1:
                    tab.setText("To Do");
                    break;
                case 2:
                    tab.setText("UW Map");
                    break;
                case 3:
                    tab.setText("Schedule");
                    break;
            }
        }).attach();

        // Disable swipe for the map tab
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position != 1) {
                    hideToDoTabFragment();
                }
                if (position == 2) { // Assuming the map is at position 2
                    viewPager.setUserInputEnabled(false);
                } else {
                    viewPager.setUserInputEnabled(true);
                }
            }
        });

        getSupportFragmentManager().setFragmentResultListener("task_notification_key", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String taskTitle = result.getString("taskTitle");
                String taskDateTime = result.getString("taskDateTime");
                int taskId = result.getInt("taskId");
                handleTaskUpdate(taskTitle, taskDateTime, taskId);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if user is logged in
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (!isLoggedIn) {
            navigateToLogin();
            return;
        }
    }

    private void handleTaskUpdate(String taskTitle, String taskDateTime, int taskId) {
        Log.e("HANDLE", "We are about to schedule " + taskTitle + " of ID: " + taskId + " which is due " + taskDateTime);
        NotificationHelper notificationHelper = new NotificationHelper();
        notificationHelper.cancelNotification(this, taskId);

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
        try {
            Date dueDate = dateTimeFormat.parse(taskDateTime);
            if (dueDate != null) {
                long dueTime = dueDate.getTime();
                Log.e("HANDLE", "START!");
                scheduleNotificationForTask(taskTitle, dueTime, taskId);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void hideToDoTabFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment emptyFragment = new Fragment();
        fragmentTransaction.replace(R.id.fragment_container, emptyFragment);
        fragmentTransaction.commit();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.help_menu) {
            String help_message = "For the Calendar, select a day that is highlighted with a red arrow in order to see the tasks that are due that day.\n\n";
            help_message += "For the To Do list, add, edit, or delete tasks/assignments as you see fit.\n\n";
            help_message += "For the UW Map, search for a building. When you select a building, you can create a pin that assigns the building to a specific class. " +
                    "When you click on the pin, you get the information on which class is running within the building.\n\n";
            help_message += "For the Schedule, add, edit, or delete classes as you see fit.";

            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(help_message)
                    .setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            return true;
        } else if (itemId == R.id.action_logout) {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("Are you sure you want to log out of id: " + wiscId + "?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            logout();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        SharedPreferences.Editor editor = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    private void scheduleNotifications() {
        DatabaseHelper db = new DatabaseHelper(this);
        List<String[]> tasks = db.getTasksByWiscId(wiscId);

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault());
        long currentTime = System.currentTimeMillis();

        for (String[] task : tasks) {
            String fullTaskString = task[0];
            int taskId = Integer.parseInt(task[1]);

            // Extract the date-time string
            String dateTimeString = fullTaskString.substring(fullTaskString.indexOf("Due on: ") + 8).trim();

            try {
                Date dueDate = dateTimeFormat.parse(dateTimeString);
                if (dueDate != null) {
                    long dueTime = dueDate.getTime();
                    if (dueTime > currentTime) {
                        scheduleNotificationForTask(fullTaskString.split("\n")[0], dueTime, taskId);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void scheduleNotificationForTask(String taskTitle, long dueTime, int taskId) {
        long[] intervals = {
                7 * 24 * 60 * 60 * 1000L, // 7 days
                3 * 24 * 60 * 60 * 1000L, // 3 days
                24 * 60 * 60 * 1000L,     // 1 day
                12 * 60 * 60 * 1000L,     // 12 hours
                3 * 60 * 60 * 1000L,      // 3 hours
                60 * 60 * 1000L,          // 1 hour
                60 * 1000L                // 1 minute
        };

        NotificationHelper notificationHelper = new NotificationHelper();

        for (long interval : intervals) {
            long triggerTime = dueTime - interval;

            // Ensure triggerTime is in the future
            if (triggerTime > System.currentTimeMillis())  {
                String duration = getDurationString(interval);
                Log.e("TASK", "Scheduled \"" + taskTitle + "\", for when it is due in " + duration);
                notificationHelper.scheduleNotification(this, taskTitle, "This task is due in " + duration, triggerTime, taskId); // Convert back to milliseconds
            }
        }
    }


    private String getDurationString(long interval) {
        if (interval >= 24 * 60 * 60 * 1000) {
            return interval / (24 * 60 * 60 * 1000L) + " day(s)!";
        } else if (interval >= 60 * 60 * 1000L) {
            return interval / (60 * 60 * 1000L ) + " hour(s)!";
        } else if (interval == 60 * 1000L) {
            return "1 minute! You should get to submitting!";
        } else {
            return interval / 60000 + " minutes";
        }
    }
}
