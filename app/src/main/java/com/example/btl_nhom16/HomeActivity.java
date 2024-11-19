package com.example.btl_nhom16;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
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
    private TextView selectedTimeText;

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
            loadTasks(); 
        });

        favoritesButton.setOnClickListener(view -> {
            switchToTasksView();
            loadFavoriteTasks();
        });

        completedTasksButton.setOnClickListener(view -> {
            switchToTasksView();
            loadCompletedTasks();
        });
        notificationSettingsButton.setOnClickListener(view -> switchToNotificationSettingsView());
        // Cấu hình RecyclerView và Adapter
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        databaseHelper = new DatabaseHelper(this);
        taskAdapter = new TaskAdapter(taskList, databaseHelper);
        recyclerViewTasks.setAdapter(taskAdapter);
        addTask();
        // Lấy dữ liệu từ Database
        loadTasks();
        // Đăng xuất
        logout();

    }
    public void getwide(){
        // Khởi tạo các view
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
    }
    // Đăng xuất
    public void logout() {
        logoutButton = findViewById(R.id.logoutIcon);
        logoutButton.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(HomeActivity.this, view);
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu_logout, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.action_logout) {
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });
    }
    // Nút thêm công việc
    public  void addTask(){
        findViewById(R.id.addTaskButton).setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });
    }
    // Lấy tất cả các công việc
    public void loadTasks() {
        List<Task> tasks = databaseHelper.getAllTasks();
        if (tasks != null && !tasks.isEmpty()) {
            taskAdapter.updateList(tasks);
        } else {
            Toast.makeText(this, "No tasks found", Toast.LENGTH_SHORT).show();
        }
    }

    // Lấy các công việc đã hoàn thành và quá hạn
    public void loadCompletedTasks() {
        List<Task> expiredTasks = databaseHelper.getExpiredTasks();
        if (expiredTasks != null && !expiredTasks.isEmpty()) {
            taskAdapter.updateList(expiredTasks);
        } else {
            Toast.makeText(this, "No expired tasks found", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks(); // Tải lại công việc khi quay lại HomeActivity
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




