package com.example.btl_nhom16;

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
    public static final int DATABASE_VERSION = 3;

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
                + COLUMN_TASK_DUE_DATE + " TEXT)";
        db.execSQL(CREATE_TASKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nếu phiên bản cũ thấp hơn 1, xóa tất cả dữ liệu và bảng
        if (oldVersion < 3) {
            // Xóa tất cả các bảng
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

            // Tạo lại bảng
            onCreate(db); // Gọi lại phương thức onCreate để tạo lại tất cả các bảng từ đầu
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

        Log.d("DatabaseHelper", "Insert result: " + result);  // Kiểm tra kết quả của insert

        return result != -1;
    }



    // Lấy danh sách công việc từ cơ sở dữ liệu
    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Truy vấn dữ liệu
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS, null);
        Log.d("DatabaseHelper", "Query result count: " + cursor.getCount());  // Log số lượng bản ghi

        // Kiểm tra xem các cột có tồn tại không
        int columnTaskId = cursor.getColumnIndex(COLUMN_TASK_ID);
        int columnTaskName = cursor.getColumnIndex(COLUMN_TASK_NAME);
        int columnTaskDescription = cursor.getColumnIndex(COLUMN_TASK_DESCRIPTION);
        int columnTaskCreationDate = cursor.getColumnIndex(COLUMN_TASK_CREATION_DATE);
        int columnTaskDueDate = cursor.getColumnIndex(COLUMN_TASK_DUE_DATE);
        int columnTaskIsCompleted = cursor.getColumnIndex(COLUMN_TASK_IS_COMPLETED);

        if (columnTaskId == -1 || columnTaskName == -1 || columnTaskDescription == -1 ||
                columnTaskCreationDate == -1 || columnTaskDueDate == -1 || columnTaskIsCompleted == -1) {
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

                // Tạo đối tượng Task và thêm vào danh sách
                Task task = new Task(id, name, description, isCompleted, creationDate, dueDate);
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
            return result != -1; // Trả về true nếu thành công
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding user", e);
            return false;
        } finally {
            db.close();
        }
    }



}
