package com.example.btl_nhom16;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "TaskManagement.db";
    public static final int DATABASE_VERSION = 4;

    // Bảng người dùng
    public static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "username";
    private static final String COLUMN_USER_PASSWORD = "password";

    // Bảng tasks
    public static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_TASK_ID = "task_id";
    private static final String COLUMN_TASK_NAME = "task_name";
    private static final String COLUMN_TASK_DESCRIPTION = "task_description";
    private static final String COLUMN_TASK_IS_COMPLETED = "is_completed";
    private static final String COLUMN_TASK_CREATION_DATE = "creation_date";
    private static final String COLUMN_TASK_DUE_DATE = "due_date";
    private static final String COLUMN_TASK_IS_FAVORITE = "isFavorite"; // Cột trạng thái yêu thích
    //Bảng lưu time
    private static final String TABLE_SETTINGS = "settings";
    private static final String COLUMN_TIME_BEFORE_DEADLINE = "time_before_deadline"; // Thời gian trước hạn
    private static final String COLUMN_DAILY_NOTIFICATION_TIME = "daily_notification_time"; // Thời gian thông báo hàng ngày

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng người dùng
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_NAME + " TEXT, "
                + COLUMN_USER_PASSWORD + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);

        // Tạo bảng công việc
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + " ("
                + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TASK_NAME + " TEXT, "
                + COLUMN_TASK_DESCRIPTION + " TEXT, "
                + COLUMN_TASK_IS_COMPLETED + " INTEGER DEFAULT 0, "  // Trạng thái hoàn thành (0 = chưa hoàn thành, 1 = hoàn thành)
                + COLUMN_TASK_CREATION_DATE + " TEXT, "
                + COLUMN_TASK_DUE_DATE + " TEXT, "
                + COLUMN_TASK_IS_FAVORITE + " INTEGER DEFAULT 0)";  // Cột trạng thái yêu thích
        db.execSQL(CREATE_TASKS_TABLE);

        // Tạo bảng cài đặt
        String CREATE_SETTINGS_TABLE = "CREATE TABLE " + TABLE_SETTINGS + " ("
                + COLUMN_TIME_BEFORE_DEADLINE + " INTEGER DEFAULT 10, "
                + COLUMN_DAILY_NOTIFICATION_TIME + " TEXT DEFAULT '21:00')";
        db.execSQL(CREATE_SETTINGS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            // Kiểm tra và thêm cột isFavorite vào bảng tasks nếu nó chưa tồn tại
            try {
                db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COLUMN_TASK_IS_FAVORITE + " INTEGER DEFAULT 0");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error upgrading database", e);
            }
        }

    }

    // Thêm công việc mới vào cơ sở dữ liệu
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

    // Lấy các công việc đã hoàn thành theo ngày
    public List<Task> getExpiredTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Truy vấn lấy tất cả công việc
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS, null);
        Log.d("DatabaseHelper", "Query result count: " + cursor.getCount());

        // Kiểm tra các cột trong cơ sở dữ liệu
        int columnTaskId = cursor.getColumnIndex(COLUMN_TASK_ID);
        int columnTaskName = cursor.getColumnIndex(COLUMN_TASK_NAME);
        int columnTaskDescription = cursor.getColumnIndex(COLUMN_TASK_DESCRIPTION);
        int columnTaskCreationDate = cursor.getColumnIndex(COLUMN_TASK_CREATION_DATE);
        int columnTaskDueDate = cursor.getColumnIndex(COLUMN_TASK_DUE_DATE);
        int columnTaskIsCompleted = cursor.getColumnIndex(COLUMN_TASK_IS_COMPLETED);

        if (columnTaskId == -1 || columnTaskName == -1 || columnTaskDescription == -1 ||
                columnTaskCreationDate == -1 || columnTaskDueDate == -1 || columnTaskIsCompleted == -1) {
            cursor.close();
            db.close();
            return taskList;
        }

        long currentTime = System.currentTimeMillis();  // Thời gian thực

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(columnTaskId);
                String name = cursor.getString(columnTaskName);
                String description = cursor.getString(columnTaskDescription);
                String creationDate = cursor.getString(columnTaskCreationDate);
                String dueDate = cursor.getString(columnTaskDueDate);
                boolean isCompleted = cursor.getInt(columnTaskIsCompleted) == 1;

                // Chuyển đổi ngày hết hạn thành long để so sánh
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date dueDateObject = null;
                try {
                    dueDateObject = sdf.parse(dueDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (dueDateObject != null && dueDateObject.getTime() < currentTime) {
                    // Nếu công việc đã quá hạn, thêm vào danh sách
                    Task task = new Task(id, name, description, isCompleted, creationDate, dueDate, false);
                    taskList.add(task);
                }

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return taskList;
    }

    // Xóa công việc khỏi cơ sở dữ liệu
    public void deleteTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
        Log.d("DatabaseHelper", "Deleted task with ID: " + task.getId());
    }

    // Lấy danh sách công việc từ cơ sở dữ liệu
    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Truy vấn dữ liệu
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS, null);
        Log.d("DatabaseHelper", "Query result count: " + cursor.getCount());

        // Kiểm tra xem các cột có tồn tại không
        int columnTaskId = cursor.getColumnIndex(COLUMN_TASK_ID);
        int columnTaskName = cursor.getColumnIndex(COLUMN_TASK_NAME);
        int columnTaskDescription = cursor.getColumnIndex(COLUMN_TASK_DESCRIPTION);
        int columnTaskCreationDate = cursor.getColumnIndex(COLUMN_TASK_CREATION_DATE);
        int columnTaskDueDate = cursor.getColumnIndex(COLUMN_TASK_DUE_DATE);
        int columnTaskIsCompleted = cursor.getColumnIndex(COLUMN_TASK_IS_COMPLETED);
        int columnTaskIsFavorite = cursor.getColumnIndex(COLUMN_TASK_IS_FAVORITE); // Đảm bảo là bạn lấy được cột này

        if (columnTaskId == -1 || columnTaskName == -1 || columnTaskDescription == -1 ||
                columnTaskCreationDate == -1 || columnTaskDueDate == -1 || columnTaskIsCompleted == -1 || columnTaskIsFavorite == -1) {
            // Nếu có cột nào không tìm thấy, trả về danh sách rỗng hoặc log lỗi
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

                // Tạo đối tượng Task và thêm vào danh sách
                Task task = new Task(id, name, description, isCompleted, creationDate, dueDate, isFavorite);
                taskList.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return taskList;
    }

    // Kiểm tra đăng nhập
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USER_NAME + " = ? AND " + COLUMN_USER_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Thêm người dùng mới vào cơ sở dữ liệu
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

    // Thêm công việc vào mục yêu thích
    public void addToFavorites(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_IS_FAVORITE, 1);  // Đánh dấu là yêu thích
        db.update(TABLE_TASKS, values, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
    }

    // Gỡ công việc khỏi mục yêu thích
    public void removeFromFavorites(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_IS_FAVORITE, 0);  // Đánh dấu là không yêu thích
        db.update(TABLE_TASKS, values, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
    }

    // Lấy danh sách các công việc yêu thích
    public List<Task> getFavoriteTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Truy vấn dữ liệu chỉ lấy các công việc yêu thích
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS + " WHERE " + COLUMN_TASK_IS_FAVORITE + " = 1", null);
        Log.d("DatabaseHelper", "Query result count: " + cursor.getCount());

        // Kiểm tra xem các cột có tồn tại không
        int columnTaskId = cursor.getColumnIndex(COLUMN_TASK_ID);
        int columnTaskName = cursor.getColumnIndex(COLUMN_TASK_NAME);
        int columnTaskDescription = cursor.getColumnIndex(COLUMN_TASK_DESCRIPTION);
        int columnTaskCreationDate = cursor.getColumnIndex(COLUMN_TASK_CREATION_DATE);
        int columnTaskDueDate = cursor.getColumnIndex(COLUMN_TASK_DUE_DATE);
        int columnTaskIsCompleted = cursor.getColumnIndex(COLUMN_TASK_IS_COMPLETED);
        int columnTaskIsFavorite = cursor.getColumnIndex(COLUMN_TASK_IS_FAVORITE);

        if (columnTaskId == -1 || columnTaskName == -1 || columnTaskDescription == -1 ||
                columnTaskCreationDate == -1 || columnTaskDueDate == -1 || columnTaskIsCompleted == -1 || columnTaskIsFavorite == -1) {
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

                // Tạo đối tượng Task và thêm vào danh sách
                Task task = new Task(id, name, description, isCompleted, creationDate, dueDate, isFavorite);
                taskList.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return taskList;
    }




}

