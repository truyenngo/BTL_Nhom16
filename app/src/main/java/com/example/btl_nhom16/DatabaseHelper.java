package com.example.btl_nhom16;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "TaskManagement.db";
    public static final int DATABASE_VERSION = 5;

    public static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "username";
    private static final String COLUMN_USER_PASSWORD = "password";

    public static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_TASK_ID = "task_id";
    private static final String COLUMN_TASK_NAME = "task_name";
    private static final String COLUMN_TASK_DESCRIPTION = "task_description";
    private static final String COLUMN_TASK_IS_COMPLETED = "is_completed";
    private static final String COLUMN_TASK_CREATION_DATE = "creation_date";
    private static final String COLUMN_TASK_DUE_DATE = "due_date";
    private static final String COLUMN_TASK_IS_FAVORITE = "isFavorite";

    public static final String TABLE_SUBTASKS = "subtasks";
    private static final String COLUMN_SUBTASK_ID = "subtask_id";
    private static final String COLUMN_SUBTASK_TASK_ID_FK = "task_id";
    private static final String COLUMN_SUBTASK_DESCRIPTION = "subtask_description";
    private static final String COLUMN_SUBTASK_IS_COMPLETED = "subtask_is_completed";

    private static final String TABLE_SETTINGS = "settings";
    private static final String COLUMN_TIME_BEFORE_DEADLINE = "time_before_deadline";
    private static final String COLUMN_DAILY_NOTIFICATION_TIME = "daily_notification_time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_NAME + " TEXT, "
                + COLUMN_USER_PASSWORD + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + " ("
                + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TASK_NAME + " TEXT, "
                + COLUMN_TASK_DESCRIPTION + " TEXT, "
                + COLUMN_TASK_IS_COMPLETED + " INTEGER DEFAULT 0, "
                + COLUMN_TASK_CREATION_DATE + " TEXT, "
                + COLUMN_TASK_DUE_DATE + " TEXT, "
                + COLUMN_TASK_IS_FAVORITE + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_TASKS_TABLE);

        String CREATE_SUBTASKS_TABLE = "CREATE TABLE " + TABLE_SUBTASKS + " ("
                + COLUMN_SUBTASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SUBTASK_TASK_ID_FK + " INTEGER, "
                + COLUMN_SUBTASK_DESCRIPTION + " TEXT, "
                + COLUMN_SUBTASK_IS_COMPLETED + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_SUBTASKS_TABLE);

        String CREATE_SETTINGS_TABLE = "CREATE TABLE " + TABLE_SETTINGS + " ("
                + COLUMN_TIME_BEFORE_DEADLINE + " INTEGER DEFAULT 10, "
                + COLUMN_DAILY_NOTIFICATION_TIME + " TEXT DEFAULT '21:00')";
        db.execSQL(CREATE_SETTINGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COLUMN_TASK_IS_FAVORITE + " INTEGER DEFAULT 0");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error upgrading database", e);
            }
        }

        if (oldVersion < 5) {
            try {
                String CREATE_SUBTASKS_TABLE = "CREATE TABLE " + TABLE_SUBTASKS + " ("
                        + COLUMN_SUBTASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_SUBTASK_TASK_ID_FK + " INTEGER, "
                        + COLUMN_SUBTASK_DESCRIPTION + " TEXT, "
                        + COLUMN_SUBTASK_IS_COMPLETED + " INTEGER DEFAULT 0)";
                db.execSQL(CREATE_SUBTASKS_TABLE);
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error upgrading database", e);
            }
        }
    }

    public boolean insertTask(String name, String description, long startDate, long dueDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_NAME, name);
        values.put(COLUMN_TASK_DESCRIPTION, description);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String startDateString = sdf.format(new Date(startDate));
        String dueDateString = sdf.format(new Date(dueDate));

        values.put(COLUMN_TASK_CREATION_DATE, startDateString);
        values.put(COLUMN_TASK_DUE_DATE, dueDateString);

        long result = db.insert(TABLE_TASKS, null, values);
        db.close();

        Log.d("DatabaseHelper", "Insert result: " + result);

        return result != -1;
    }

    public boolean updateTask(int taskId, String name, String description, long startDate, long dueDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_NAME, name);
        values.put(COLUMN_TASK_DESCRIPTION, description);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String startDateString = sdf.format(new Date(startDate));
        String dueDateString = sdf.format(new Date(dueDate));

        values.put(COLUMN_TASK_CREATION_DATE, startDateString);
        values.put(COLUMN_TASK_DUE_DATE, dueDateString);

        long result = db.update(TABLE_TASKS, values, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();

        Log.d("DatabaseHelper", "Update result: " + result);

        return result != -1;
    }

    public void deleteTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
        Log.d("DatabaseHelper", "Deleted task with ID: " + task.getId());

        deleteAllSubtasksOf(task);
    }

    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_NAME, username);
            values.put(COLUMN_USER_PASSWORD, password);
            long result = db.insert(TABLE_USERS, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding user", e);
            return false;
        } finally {
            db.close();
        }
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USER_NAME
                + " = ? AND " + COLUMN_USER_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS, null);
        Log.d("DatabaseHelper", "Query result count: " + cursor.getCount());

        int columnTaskId = cursor.getColumnIndex(COLUMN_TASK_ID);
        int columnTaskName = cursor.getColumnIndex(COLUMN_TASK_NAME);
        int columnTaskDescription = cursor.getColumnIndex(COLUMN_TASK_DESCRIPTION);
        int columnTaskCreationDate = cursor.getColumnIndex(COLUMN_TASK_CREATION_DATE);
        int columnTaskDueDate = cursor.getColumnIndex(COLUMN_TASK_DUE_DATE);
        int columnTaskIsCompleted = cursor.getColumnIndex(COLUMN_TASK_IS_COMPLETED);
        int columnTaskIsFavorite = cursor.getColumnIndex(COLUMN_TASK_IS_FAVORITE);

        if (columnTaskId == -1 || columnTaskName == -1 || columnTaskDescription == -1 ||
                columnTaskCreationDate == -1 || columnTaskDueDate == -1 ||
                columnTaskIsCompleted == -1 || columnTaskIsFavorite == -1) {

            cursor.close();
            db.close();
            return taskList;
        }

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(columnTaskId);
                String name = cursor.getString(columnTaskName);
                String description = cursor.getString(columnTaskDescription);
                String creationDate = cursor.getString(columnTaskCreationDate);
                String dueDate = cursor.getString(columnTaskDueDate);
                boolean isCompleted = cursor.getInt(columnTaskIsCompleted) == 1;
                boolean isFavorite = cursor.getInt(columnTaskIsFavorite) == 1;

                Task task = new Task(id, name, description, isCompleted, creationDate
                        , dueDate, isFavorite);
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }

    public void addToFavorites(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_IS_FAVORITE, 1);
        db.update(TABLE_TASKS, values, COLUMN_TASK_ID + " = ?",
                new String[]{String.valueOf(task.getId())});
        db.close();
    }

    public void removeFromFavorites(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_IS_FAVORITE, 0);
        db.update(TABLE_TASKS, values, COLUMN_TASK_ID + " = ?",
                new String[]{String.valueOf(task.getId())});
        db.close();
    }

    public List<Task> getFavoriteTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS + " WHERE " +
                COLUMN_TASK_IS_FAVORITE + " = 1", null);
        Log.d("DatabaseHelper", "Query result count: " + cursor.getCount());

        int columnTaskId = cursor.getColumnIndex(COLUMN_TASK_ID);
        int columnTaskName = cursor.getColumnIndex(COLUMN_TASK_NAME);
        int columnTaskDescription = cursor.getColumnIndex(COLUMN_TASK_DESCRIPTION);
        int columnTaskCreationDate = cursor.getColumnIndex(COLUMN_TASK_CREATION_DATE);
        int columnTaskDueDate = cursor.getColumnIndex(COLUMN_TASK_DUE_DATE);
        int columnTaskIsCompleted = cursor.getColumnIndex(COLUMN_TASK_IS_COMPLETED);
        int columnTaskIsFavorite = cursor.getColumnIndex(COLUMN_TASK_IS_FAVORITE);

        if (columnTaskId == -1 || columnTaskName == -1 || columnTaskDescription == -1 ||
                columnTaskCreationDate == -1 || columnTaskDueDate == -1 ||
                columnTaskIsCompleted == -1 || columnTaskIsFavorite == -1) {
            cursor.close();
            db.close();
            return taskList;
        }

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(columnTaskId);
                String name = cursor.getString(columnTaskName);
                String description = cursor.getString(columnTaskDescription);
                String creationDate = cursor.getString(columnTaskCreationDate);
                String dueDate = cursor.getString(columnTaskDueDate);
                boolean isCompleted = cursor.getInt(columnTaskIsCompleted) == 1;
                boolean isFavorite = cursor.getInt(columnTaskIsFavorite) == 1;

                Task task = new Task(id, name, description, isCompleted, creationDate,
                        dueDate, isFavorite);
                taskList.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return taskList;
    }

    public List<Task> getUpcomingTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS, null);
        Log.d("DatabaseHelper", "Query result count: " + cursor.getCount());

        int columnTaskId = cursor.getColumnIndex(COLUMN_TASK_ID);
        int columnTaskName = cursor.getColumnIndex(COLUMN_TASK_NAME);
        int columnTaskDescription = cursor.getColumnIndex(COLUMN_TASK_DESCRIPTION);
        int columnTaskCreationDate = cursor.getColumnIndex(COLUMN_TASK_CREATION_DATE);
        int columnTaskDueDate = cursor.getColumnIndex(COLUMN_TASK_DUE_DATE);
        int columnTaskIsCompleted = cursor.getColumnIndex(COLUMN_TASK_IS_COMPLETED);
        int columnTaskIsFavorite = cursor.getColumnIndex(COLUMN_TASK_IS_FAVORITE);

        if (columnTaskId == -1 || columnTaskName == -1 || columnTaskDescription == -1 ||
                columnTaskCreationDate == -1 || columnTaskDueDate == -1 ||
                columnTaskIsCompleted == -1 || columnTaskIsFavorite == -1) {
            cursor.close();
            db.close();
            return taskList;
        }

        long currentTime = System.currentTimeMillis();
        Set<Integer> addedTaskIds = new HashSet<>();

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(columnTaskId);
                String name = cursor.getString(columnTaskName);
                String description = cursor.getString(columnTaskDescription);
                String creationDate = cursor.getString(columnTaskCreationDate);
                String dueDate = cursor.getString(columnTaskDueDate);
                boolean isCompleted = cursor.getInt(columnTaskIsCompleted) == 1;
                boolean isFavorite = cursor.getInt(columnTaskIsFavorite) == 1;

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                                                Locale.getDefault());
                Date dueDateObject = null;
                try {
                    dueDateObject = sdf.parse(dueDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (dueDateObject != null && !isCompleted &&
                        dueDateObject.getTime() > currentTime) {
                    if (!addedTaskIds.contains(id)) {
                        Task task = new Task(id, name, description, isCompleted, creationDate,
                                                                        dueDate, isFavorite);
                        taskList.add(task);
                        addedTaskIds.add(id);
                    }
                }

                if (isFavorite && (dueDateObject == null ||
                        dueDateObject.getTime() > currentTime)) {
                    if (!addedTaskIds.contains(id)) {
                        Task task = new Task(id, name, description, isCompleted, creationDate,
                                dueDate, isFavorite);
                        taskList.add(task);
                        addedTaskIds.add(id);
                    }
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        if (taskList.isEmpty()) {
            Log.d("DatabaseHelper", "No upcoming tasks found.");
        }
        return taskList;
    }

    public List<Task> getCompletedTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS + " WHERE " +
                COLUMN_TASK_IS_COMPLETED + " = 1", null);
        Log.d("DatabaseHelper", "Query result count: " + cursor.getCount());

        int columnTaskId = cursor.getColumnIndex(COLUMN_TASK_ID);
        int columnTaskName = cursor.getColumnIndex(COLUMN_TASK_NAME);
        int columnTaskDescription = cursor.getColumnIndex(COLUMN_TASK_DESCRIPTION);
        int columnTaskCreationDate = cursor.getColumnIndex(COLUMN_TASK_CREATION_DATE);
        int columnTaskDueDate = cursor.getColumnIndex(COLUMN_TASK_DUE_DATE);
        int columnTaskIsCompleted = cursor.getColumnIndex(COLUMN_TASK_IS_COMPLETED);
        int columnTaskIsFavorite = cursor.getColumnIndex(COLUMN_TASK_IS_FAVORITE);

        if (columnTaskId == -1 || columnTaskName == -1 || columnTaskDescription == -1 ||
                columnTaskCreationDate == -1 || columnTaskDueDate == -1 ||
                columnTaskIsCompleted == -1 || columnTaskIsFavorite == -1) {
            cursor.close();
            db.close();
            return taskList;
        }

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(columnTaskId);
                String name = cursor.getString(columnTaskName);
                String description = cursor.getString(columnTaskDescription);
                String creationDate = cursor.getString(columnTaskCreationDate);
                String dueDate = cursor.getString(columnTaskDueDate);
                boolean isCompleted = cursor.getInt(columnTaskIsCompleted) == 1;
                boolean isFavorite = cursor.getInt(columnTaskIsFavorite) == 1;

                if (isCompleted) {
                    Task task = new Task(id, name, description, isCompleted, creationDate,
                            dueDate, isFavorite);
                    taskList.add(task);
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        if (taskList.isEmpty()) {
            Log.d("DatabaseHelper", "No completed tasks found.");
        }
        return taskList;
    }

    public boolean insertSubtask(int taskId, String subtaskDescription) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SUBTASK_TASK_ID_FK, taskId);
        values.put(COLUMN_SUBTASK_DESCRIPTION, subtaskDescription);

        long result = db.insert(TABLE_SUBTASKS, null, values);
        db.close();

        Log.d("DatabaseHelper", "Insert result: " + result);

        return result != -1;
    }

    public boolean updateSubtask(int subtaskId, int taskId, String subtaskDescription) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SUBTASK_TASK_ID_FK, taskId);
        values.put(COLUMN_SUBTASK_DESCRIPTION, subtaskDescription);

        long result = db.update(TABLE_SUBTASKS, values, COLUMN_SUBTASK_ID + " = ?", new String[]{String.valueOf(subtaskId)});
        db.close();

        Log.d("DatabaseHelper", "Update result: " + result);

        return result != -1;
    }

    public void deleteSubtask(Subtask subtask) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SUBTASKS, COLUMN_SUBTASK_ID + " = ?", new String[]{String.valueOf(subtask.getId())});
        db.close();
        Log.d("DatabaseHelper", "Deleted subtask with ID: " + subtask.getId());
    }

    public void deleteAllSubtasksOf(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SUBTASKS, COLUMN_SUBTASK_TASK_ID_FK + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
        Log.d("DatabaseHelper", "Deleted all subtask of task with ID: " + task.getId());
    }

    public List<Subtask> getAllSubtasksOf(Task task) {
        List<Subtask> subtaskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SUBTASKS + " WHERE " + COLUMN_SUBTASK_TASK_ID_FK + " = ?", new String[]{String.valueOf(task.getId())});
        Log.d("DatabaseHelper", "Query result count: " + cursor.getCount());

        int columnSubtaskId = cursor.getColumnIndex(COLUMN_SUBTASK_ID);
        int columnTaskId = cursor.getColumnIndex(COLUMN_SUBTASK_TASK_ID_FK);
        int columnSubtaskDescription = cursor.getColumnIndex(COLUMN_SUBTASK_DESCRIPTION);
        int columnSubtaskIsCompleted = cursor.getColumnIndex(COLUMN_SUBTASK_IS_COMPLETED);

        if (columnSubtaskId == -1 ||
                columnTaskId == -1 ||
                columnSubtaskDescription == -1 ||
                columnSubtaskIsCompleted == -1) {
            cursor.close();
            db.close();
            return subtaskList;
        }

        if (cursor.moveToFirst()) {
            do {
                int subtaskId = cursor.getInt(columnSubtaskId);
                int taskId = cursor.getInt(columnTaskId);
                String description = cursor.getString(columnSubtaskDescription);
                boolean isCompleted = cursor.getInt(columnSubtaskIsCompleted) == 1;
                Subtask subtask = new Subtask(subtaskId, taskId, description, isCompleted);
                subtaskList.add(subtask);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return subtaskList;
    }

    public void addTaskToCompleted(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_IS_COMPLETED, 1);
        db.update(TABLE_TASKS, values, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
        addAllSubtasksOfToCompleted(task);
    }

    public void removeTaskFromCompleted(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_IS_COMPLETED, 0);
        db.update(TABLE_TASKS, values, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
    }

    public void addSubtaskToCompleted(Subtask subtask) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SUBTASK_IS_COMPLETED, 1);
        db.update(TABLE_SUBTASKS, values, COLUMN_SUBTASK_ID + " = ?", new String[]{String.valueOf(subtask.getId())});
        db.close();
    }

    public void addAllSubtasksOfToCompleted(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SUBTASK_IS_COMPLETED, 1);
        db.update(TABLE_SUBTASKS, values, COLUMN_SUBTASK_TASK_ID_FK + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
    }

    public void removeSubtaskFromCompleted(Subtask subtask) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SUBTASK_IS_COMPLETED, 0);
        db.update(TABLE_SUBTASKS, values, COLUMN_SUBTASK_ID + " = ?", new String[]{String.valueOf(subtask.getId())});
        db.close();
    }
}

