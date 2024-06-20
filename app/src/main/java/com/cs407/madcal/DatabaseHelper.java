package com.cs407.madcal;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.content.SharedPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 8;

    // Database Name
    private static final String DATABASE_NAME = "UserManager.db";

    // User table name
    private static final String TABLE_USER = "user";

    // User Table Columns names
    private static final String COLUMN_USER_ID = "wisc_id";

    private static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_TASK_ID = "task_id";
    private static final String COLUMN_TASK_TITLE = "title";
    private static final String COLUMN_TASK_DATE = "date";
    private static final String COLUMN_TASK_TIME = "time";
    private static final String COLUMN_TASK_WISC_ID = "wisc_id";

    // New table for classes
    private static final String TABLE_CLASSES = "classes";
    private static final String COLUMN_CLASS_NAME = "class_name";
    private static final String COLUMN_CLASS_DAYS = "class_days";
    private static final String COLUMN_CLASS_RANGE = "class_range";
    private static final String COLUMN_CLASS_WISC_ID = "wisc_id";
    private static final String COLUMN_CLASS_ID = "class_id";

    // New table for map pins
    private static final String TABLE_PINS = "map_pins";
    private static final String COLUMN_PIN_ID = "pin_id";
    private static final String COLUMN_PIN_LAT = "latitude";
    private static final String COLUMN_PIN_LNG = "longitude";
    private static final String COLUMN_PIN_CLASS_NAME = "class_name";
    private static final String COLUMN_PIN_CLASS_DAYS = "class_days";
    private static final String COLUMN_PIN_CLASS_ROOM = "class_room";


    // create table sql query
    private String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
            + COLUMN_USER_ID + " TEXT PRIMARY KEY" + ")";

    // drop table sql query
    private String DROP_USER_TABLE = "DROP TABLE IF EXISTS " + TABLE_USER;

    // SQL query to create the tasks table
    private static final String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
            + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TASK_TITLE + " TEXT,"
            + COLUMN_TASK_DATE + " TEXT,"
            + COLUMN_TASK_TIME + " TEXT,"
            + COLUMN_TASK_WISC_ID + " TEXT" + ")";

    // SQL query to drop the tasks table
    private static final String DROP_TASKS_TABLE = "DROP TABLE IF EXISTS " + TABLE_TASKS;

    // SQL query to create the classes table
    private static final String CREATE_CLASSES_TABLE = "CREATE TABLE " + TABLE_CLASSES + "("
            + COLUMN_CLASS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_CLASS_NAME + " TEXT,"
            + COLUMN_CLASS_DAYS + " TEXT,"
            + COLUMN_CLASS_RANGE + " TEXT,"
            + COLUMN_CLASS_WISC_ID + " TEXT" + ")";

    // SQL query to drop the classes table
    private static final String DROP_CLASSES_TABLE = "DROP TABLE IF EXISTS " + TABLE_CLASSES;

    // SQL query to create the pins table
    private static final String CREATE_PINS_TABLE = "CREATE TABLE " + TABLE_PINS + "("
            + COLUMN_PIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_PIN_LAT + " REAL,"
            + COLUMN_PIN_LNG + " REAL,"
            + COLUMN_PIN_CLASS_NAME + " TEXT,"
            + COLUMN_PIN_CLASS_DAYS + " TEXT,"
            + COLUMN_PIN_CLASS_ROOM + " TEXT,"
            + COLUMN_USER_ID + " TEXT" + ")";

    // SQL query to drop the pins table
    private static final String DROP_PINS_TABLE = "DROP TABLE IF EXISTS " + TABLE_PINS;

    /**
     * Constructor
     *
     * @param context
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_TASKS_TABLE);
        db.execSQL(CREATE_CLASSES_TABLE);
        db.execSQL(CREATE_PINS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop User Table if exist
        db.execSQL(DROP_USER_TABLE);
        db.execSQL(DROP_TASKS_TABLE);
        db.execSQL(DROP_CLASSES_TABLE);
        db.execSQL(DROP_PINS_TABLE);
        onCreate(db);
    }

    /**
     * This method is to create user record
     *
     * @param wiscId
     */
    public void addUser(String wiscId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, wiscId);

        // Inserting Row
        db.insert(TABLE_USER, null, values);
        db.close();
    }

    public void addTask(String title, String date, String time, String wiscId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TITLE, title);
        values.put(COLUMN_TASK_DATE, date);
        values.put(COLUMN_TASK_TIME, time);
        values.put(COLUMN_TASK_WISC_ID, wiscId);

        db.insert(TABLE_TASKS, null, values);
        db.close();
    }

    public void addClass(String className, String classDays, String classRange, String wiscId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CLASS_NAME, className);
        values.put(COLUMN_CLASS_DAYS, classDays);
        values.put(COLUMN_CLASS_RANGE, classRange);
        values.put(COLUMN_CLASS_WISC_ID, wiscId);

        db.insert(TABLE_CLASSES, null, values);
        db.close();
    }

    public int addPin(double lat, double lng, String className, String classDays, String classRoom, String wiscId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PIN_LAT, lat);
        values.put(COLUMN_PIN_LNG, lng);
        values.put(COLUMN_PIN_CLASS_NAME, className);
        values.put(COLUMN_PIN_CLASS_DAYS, classDays);
        values.put(COLUMN_PIN_CLASS_ROOM, classRoom);
        values.put(COLUMN_USER_ID, wiscId);

        long id = db.insert(TABLE_PINS, null, values);
        db.close();
        return (int) id;
    }
    public List<String[]> getTasksByWiscId(String wiscId) {
        List<TaskDetail> taskDetails = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_TASKS + " WHERE " + COLUMN_TASK_WISC_ID + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[] { wiscId });

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());

        if (cursor.moveToFirst()) {
            do {
                String taskId = cursor.getString(cursor.getColumnIndex(COLUMN_TASK_ID));
                String taskTitle = cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TITLE));
                String taskDate = cursor.getString(cursor.getColumnIndex(COLUMN_TASK_DATE));
                String taskTime = cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TIME));
                Date dateTime = null;
                try {
                    dateTime = dateTimeFormat.parse(taskDate + " " + taskTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                taskDetails.add(new TaskDetail(taskTitle, dateTime, taskId));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        Collections.sort(taskDetails, new Comparator<TaskDetail>() {
            @Override
            public int compare(TaskDetail t1, TaskDetail t2) {
                return t1.dateTime.compareTo(t2.dateTime);
            }
        });

        List<String[]> sortedTasks = new ArrayList<>();
        SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault());

        for (TaskDetail task : taskDetails) {
            String formattedDate = outputFormat.format(task.dateTime);
            sortedTasks.add(new String[]{task.title + "\nDue on: " + formattedDate, task.id});
        }

        return sortedTasks;
    }

    private static class TaskDetail {
        String title;
        Date dateTime;
        String id;

        public TaskDetail(String title, Date dateTime, String id) {
            this.title = title;
            this.dateTime = dateTime;
            this.id = id;
        }
    }

    public List<String> getClassesByDay(String day, String wiscId) {
        List<ClassDetail> classDetails = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

        Cursor cursor = db.query(TABLE_CLASSES, new String[]{COLUMN_CLASS_NAME, COLUMN_CLASS_DAYS, COLUMN_CLASS_RANGE},
                COLUMN_CLASS_DAYS + " LIKE ? AND " + COLUMN_CLASS_WISC_ID + " = ?", new String[]{"%" + day + "%", wiscId}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String className = cursor.getString(cursor.getColumnIndex(COLUMN_CLASS_NAME));
                String classDays = cursor.getString(cursor.getColumnIndex(COLUMN_CLASS_DAYS));
                String classRange = cursor.getString(cursor.getColumnIndex(COLUMN_CLASS_RANGE));

                String[] daysArray = classDays.split(",");
                String[] rangesArray = classRange.split(",");
                for (int i = 0; i < daysArray.length; i++) {
                    if (day.equals(daysArray[i])) {
                        String dayRange = rangesArray[i];
                        String startTimeStr = dayRange.split(" to ")[0];
                        try {
                            Date startTime = timeFormat.parse(startTimeStr);
                            classDetails.add(new ClassDetail(className, dayRange, startTime));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        Collections.sort(classDetails, new Comparator<ClassDetail>() {
            @Override
            public int compare(ClassDetail c1, ClassDetail c2) {
                return c1.startTime.compareTo(c2.startTime);
            }
        });

        List<String> sortedClasses = new ArrayList<>();
        for (ClassDetail classDetail : classDetails) {
            sortedClasses.add(classDetail.className + ", From " + classDetail.dayRange);
        }

        return sortedClasses;
    }

    private static class ClassDetail {
        String className;
        String dayRange;
        Date startTime;

        public ClassDetail(String className, String dayRange, Date startTime) {
            this.className = className;
            this.dayRange = dayRange;
            this.startTime = startTime;
        }
    }
    public List<String[]> getClassesByWiscId(String wiscId) {
        List<String[]> classData = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COLUMN_CLASS_ID + ", " + COLUMN_CLASS_NAME +
                " FROM " + TABLE_CLASSES +
                " WHERE " + COLUMN_CLASS_WISC_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{wiscId});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_CLASS_ID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_CLASS_NAME));
                classData.add(new String[]{String.valueOf(id), name});
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return classData;
    }

    public List<PinData> getPinsByWiscId(String wiscId) {
        List<PinData> pins = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PINS, new String[]{COLUMN_PIN_ID, COLUMN_PIN_LAT, COLUMN_PIN_LNG, COLUMN_PIN_CLASS_NAME, COLUMN_PIN_CLASS_DAYS, COLUMN_PIN_CLASS_ROOM},
                COLUMN_USER_ID + "=?", new String[]{wiscId}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_PIN_ID));
                double lat = cursor.getDouble(cursor.getColumnIndex(COLUMN_PIN_LAT));
                double lng = cursor.getDouble(cursor.getColumnIndex(COLUMN_PIN_LNG));
                String className = cursor.getString(cursor.getColumnIndex(COLUMN_PIN_CLASS_NAME));
                String classDays = cursor.getString(cursor.getColumnIndex(COLUMN_PIN_CLASS_DAYS));
                String classRoom = cursor.getString(cursor.getColumnIndex(COLUMN_PIN_CLASS_ROOM));

                pins.add(new PinData(lat, lng, className, classDays, classRoom, id));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return pins;
    }

    public PinData getPinById(int pinId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PINS, new String[]{COLUMN_PIN_ID, COLUMN_PIN_LAT, COLUMN_PIN_LNG, COLUMN_PIN_CLASS_NAME, COLUMN_PIN_CLASS_DAYS, COLUMN_PIN_CLASS_ROOM},
                COLUMN_PIN_ID + "=?", new String[]{String.valueOf(pinId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            double lat = cursor.getDouble(cursor.getColumnIndex(COLUMN_PIN_LAT));
            double lng = cursor.getDouble(cursor.getColumnIndex(COLUMN_PIN_LNG));
            String className = cursor.getString(cursor.getColumnIndex(COLUMN_PIN_CLASS_NAME));
            String classDays = cursor.getString(cursor.getColumnIndex(COLUMN_PIN_CLASS_DAYS));
            String classRoom = cursor.getString(cursor.getColumnIndex(COLUMN_PIN_CLASS_ROOM));

            cursor.close();
            db.close();
            return new PinData(lat, lng, className, classDays, classRoom, pinId);
        } else {
            db.close();
            return null;
        }
    }

    public String[] getTaskById(int taskId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] taskDetails = new String[4];

        Cursor cursor = db.query(TABLE_TASKS, new String[] {COLUMN_TASK_TITLE, COLUMN_TASK_DATE, COLUMN_TASK_TIME},
                COLUMN_TASK_ID + "=?", new String[]{String.valueOf(taskId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TITLE));
            String date = cursor.getString(cursor.getColumnIndex(COLUMN_TASK_DATE));
            String time = cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TIME));

            taskDetails[0] = title;
            taskDetails[1] = date;
            taskDetails[2] = time;
            // You can add more details if needed

            cursor.close();
        }

        db.close();
        return taskDetails;
    }

    public String[] getClassById(int classId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] classDetails = new String[3]; // [class_name, class_days, class_range]

        Cursor cursor = db.query(TABLE_CLASSES, new String[] {COLUMN_CLASS_NAME, COLUMN_CLASS_DAYS, COLUMN_CLASS_RANGE},
                COLUMN_CLASS_ID + "=?", new String[]{String.valueOf(classId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String className = cursor.getString(cursor.getColumnIndex(COLUMN_CLASS_NAME));
            String classDays = cursor.getString(cursor.getColumnIndex(COLUMN_CLASS_DAYS));
            String classRange = cursor.getString(cursor.getColumnIndex(COLUMN_CLASS_RANGE));

            classDetails[0] = className;
            classDetails[1] = classDays;
            classDetails[2] = classRange;

            cursor.close();
        }

        db.close();
        return classDetails;
    }
    public void updateTask(int taskId, String title, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TITLE, title);
        values.put(COLUMN_TASK_DATE, date);
        values.put(COLUMN_TASK_TIME, time);

        db.update(TABLE_TASKS, values, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
    }

    public void updateClass(int classId, String className, String classDays, String classRange, String wiscId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CLASS_NAME, className);
        values.put(COLUMN_CLASS_DAYS, classDays);
        values.put(COLUMN_CLASS_RANGE, classRange);
        values.put(COLUMN_CLASS_WISC_ID, wiscId);

        db.update(TABLE_CLASSES, values, COLUMN_CLASS_ID + " = ?", new String[]{String.valueOf(classId)});
        db.close();
    }

    public void updatePin(int pinId, double lat, double lng, String className, String classDays, String classRoom) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PIN_LAT, lat);
        values.put(COLUMN_PIN_LNG, lng);
        values.put(COLUMN_PIN_CLASS_NAME, className);
        values.put(COLUMN_PIN_CLASS_DAYS, classDays);
        values.put(COLUMN_PIN_CLASS_ROOM, classRoom);

        db.update(TABLE_PINS, values, COLUMN_PIN_ID + " = ?", new String[]{String.valueOf(pinId)});
        db.close();
    }

    /**
     * This method is to check user exist or not
     *
     * @param wiscId
     * @return true/false
     */
    public boolean checkUser(String wiscId) {
        // array of columns to fetch
        String[] columns = {
                COLUMN_USER_ID
        };
        SQLiteDatabase db = this.getReadableDatabase();

        // selection criteria
        String selection = COLUMN_USER_ID + " = ?";

        // selection argument
        String[] selectionArgs = {wiscId};

        // query user table with condition
        Cursor cursor = db.query(TABLE_USER, columns, selection, selectionArgs, null, null, null);

        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();

        if (cursorCount > 0) {
            return true;
        }

        return false;
    }

    /**
     * This method is to delete user record
     *
     * @param wiscId
     */
    public void deleteUser(String wiscId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // delete user record by id
        db.delete(TABLE_USER, COLUMN_USER_ID + " = ?", new String[]{wiscId});
        db.close();
    }

    public void deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
    }

    public void deleteClass(int classId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CLASSES, COLUMN_CLASS_ID + " = ?", new String[]{String.valueOf(classId)});
        db.close();
    }

    public void deletePin(int pinId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PINS, COLUMN_PIN_ID + " = ?", new String[]{String.valueOf(pinId)});
        db.close();
    }

    public static class PinData {
        public double latitude;
        public double longitude;
        public String className;
        public String classDays;
        public String classRoom;
        public int id;

        public PinData(double latitude, double longitude, String className, String classDays, String classRoom, int id) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.className = className;
            this.classDays = classDays;
            this.classRoom = classRoom;
            this.id = id;
        }
    }
}
