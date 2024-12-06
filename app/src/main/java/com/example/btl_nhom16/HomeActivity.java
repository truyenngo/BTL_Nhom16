package com.example.btl_nhom16;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private DatabaseHelper databaseHelper;
    private ImageView favoritesButton, homeButton, completedTasksButton, notificationSettingsButton, logoutButton;  // Nút yêu thích

    private LinearLayout notificationSettingsLayout;
    private EditText timeBeforeDeadlineEditText;
    private Button saveTimeBeforeDeadlineButton;
    private Button selectTimeButton;
    private TextView selectedTimeText, headerTitle;

    private int selectedTimeBeforeDeadline = 0;
    private int selectedTimeForDailyNotification = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getwide();

        // Xử lý các nút điều hướng
        homeButton.setOnClickListener(view -> {
            switchToTasksView();
            loadUpcomingTasks();
            headerTitle.setText("Công việc sắp đến hạn");
        });

        favoritesButton.setOnClickListener(view -> {
            switchToTasksView();
            loadFavoriteTasks();
            headerTitle.setText("Công việc yêu thích");
        });

        completedTasksButton.setOnClickListener(view -> {
            switchToTasksView();
            loadCompletedTasks();
            headerTitle.setText("Công việc đã hoàn thành");
        });

        notificationSettingsButton.setOnClickListener(view -> {
            switchToNotificationSettingsView();
            headerTitle.setText("Cài đặt thông báo");
        });

        // Cấu hình RecyclerView và Adapter
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        databaseHelper = new DatabaseHelper(this);
        taskAdapter = new TaskAdapter(HomeActivity.this, taskList, databaseHelper);
        recyclerViewTasks.setAdapter(taskAdapter);

        addTask();

        loadTasks();

        logout();
    }

    public void getwide(){
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        notificationSettingsLayout = findViewById(R.id.notificationSettingsLayout);
        timeBeforeDeadlineEditText = findViewById(R.id.timeBeforeDeadlineEditText);
        saveTimeBeforeDeadlineButton = findViewById(R.id.saveTimeBeforeDeadlineButton);
        selectTimeButton = findViewById(R.id.selectTimeButton);
        selectedTimeText = findViewById(R.id.selectedTimeText);

        favoritesButton = findViewById(R.id.favoritesButton);  // Nút yêu thích
        homeButton = findViewById(R.id.homesButton);  // Nút Home
        completedTasksButton = findViewById(R.id.completedTasksButton);  // Nút Hoàn thành
        notificationSettingsButton = findViewById(R.id.notificationSettingsButton);  // Nút Cài đặt thông báo
        headerTitle = findViewById(R.id.headerTitle);
    }

    public void logout() {
        logoutButton = findViewById(R.id.logoutIcon);
        logoutButton.setOnClickListener(view -> {
            // Tạo Dialog
            Dialog dialog = new Dialog(HomeActivity.this);
            dialog.setContentView(R.layout.popup_logout);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT)); // Nền trong suốt

            // Ánh xạ các nút trong layout
            Button btnCancel = dialog.findViewById(R.id.btnCancel);
            Button btnConfirm = dialog.findViewById(R.id.btnConfirm);

            // Xử lý nút "No"
            btnCancel.setOnClickListener(v -> dialog.dismiss());

            // Xử lý nút "Yes"
            btnConfirm.setOnClickListener(v -> {
                // Xóa thông tin đăng nhập từ SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                // Chuyển hướng người dùng về màn hình đăng nhập
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });

            // Hiển thị Dialog
            dialog.show();
        });
    }



    // Nút thêm công việc
    public  void addTask(){
        findViewById(R.id.addTaskButton).setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });
    }

    // Lấy các công việc chưa đến hạn
    public void loadTasks() {
        List<Task> tasks = databaseHelper.getAllTasks();
        if (tasks != null && !tasks.isEmpty()) {
            taskAdapter.updateList(tasks);
        } else {
            Toast.makeText(this, "No upcoming tasks found", Toast.LENGTH_SHORT).show();
        }
    }

    // Lấy các công việc sắp đến hạn
    public void loadUpcomingTasks() {
        List<Task> tasks = databaseHelper.getUpcomingTasks();
        if (tasks != null && !tasks.isEmpty()) {
            taskAdapter.updateList(tasks);
        } else {
            Toast.makeText(this, "No upcoming tasks found", Toast.LENGTH_SHORT).show();
        }
    }

    // Lấy các công việc yêu thích
    public void loadFavoriteTasks() {
        List<Task> favoriteTasks = databaseHelper.getFavoriteTasks();
        if (favoriteTasks != null && !favoriteTasks.isEmpty()) {
            taskAdapter.updateList(favoriteTasks);
        } else {
            Toast.makeText(this, "No favorite tasks found", Toast.LENGTH_SHORT).show();
        }
    }

    // Lấy các công việc đã hoàn thành
    public void loadCompletedTasks() {
        List<Task> doneTasks = databaseHelper.getCompletedTasks();
        if (doneTasks != null && !doneTasks.isEmpty()) {
            taskAdapter.updateList(doneTasks);
        } else {
            Toast.makeText(this, "No done tasks found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    // Chuyển sang giao diện cài đặt thông báo
    private void switchToNotificationSettingsView() {
        recyclerViewTasks.setVisibility(View.GONE); // Ẩn task list
        notificationSettingsLayout.setVisibility(View.VISIBLE); // Hiển thị layout cài đặt thông báo
    }

    // Quay lại hiển thị danh sách task
    private void switchToTasksView() {
        recyclerViewTasks.setVisibility(View.VISIBLE); // Hiển thị lại danh sách task
        notificationSettingsLayout.setVisibility(View.GONE); // Ẩn layout cài đặt thông báo
    }

}




