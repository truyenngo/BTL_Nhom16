package com.example.btl_nhom16;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private DatabaseHelper databaseHelper;
    private ImageView favoritesButton, homeButton, completedTasksButton, notificationSettingsButton, logoutButton;
    private LinearLayout notificationSettingsLayout;
    private EditText timeBeforeDeadlineEditText;
    private Button saveTimeBeforeDeadlineButton;
    private Button selectTimeButton;
    private TextView selectedTimeText, headerTitle;
    private int selectedHour = 21; // Mặc định là 21 giờ
    private int selectedMinute = 0; // Mặc định là 0 phút

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getwide();

        // Lấy thời gian đã lưu và hiển thị
        loadSavedTime();

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

        selectTimeButton.setOnClickListener(v -> showTimePickerDialog());
    }

    public void getwide() {
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
            Dialog dialog = new Dialog(HomeActivity.this);
            dialog.setContentView(R.layout.popup_logout);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            Button btnCancel = dialog.findViewById(R.id.btnCancel);
            Button btnConfirm = dialog.findViewById(R.id.btnConfirm);

            btnCancel.setOnClickListener(v -> dialog.dismiss());
            btnConfirm.setOnClickListener(v -> {
                SharedPreferences sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });

            dialog.show();
        });
    }

    // Nút thêm công việc
    public void addTask() {
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
            Toast.makeText(this, "No tasks found", Toast.LENGTH_SHORT).show();
        }
    }

    // Lấy danh sách các công việc chưa đến hạn
    private void loadUpcomingTasks() {
        hideAllNoTasksMessages();  // Ẩn tất cả các thông báo "No tasks"

        List<Task> upcomingTasks = databaseHelper.getUpcomingTasks();

        if (upcomingTasks.isEmpty()) {
            findViewById(R.id.noUpcomingTasksText).setVisibility(View.VISIBLE);  // Hiển thị "No upcoming tasks"
        } else {
            recyclerViewTasks.setVisibility(View.VISIBLE);
            findViewById(R.id.noUpcomingTasksText).setVisibility(View.GONE);  // Ẩn thông báo
            taskAdapter.updateList(upcomingTasks);
        }
    }


    // Lấy danh sách các công việc yêu thích
    private void loadFavoriteTasks() {
        hideAllNoTasksMessages();  // Ẩn tất cả các thông báo "No tasks"

        List<Task> favoriteTasks = databaseHelper.getFavoriteTasks();

        if (favoriteTasks.isEmpty()) {
            findViewById(R.id.noFavoriteTasksText).setVisibility(View.VISIBLE);  // Hiển thị "No favorite tasks"
        } else {
            recyclerViewTasks.setVisibility(View.VISIBLE);
            findViewById(R.id.noFavoriteTasksText).setVisibility(View.GONE);  // Ẩn thông báo
            taskAdapter.updateList(favoriteTasks);
        }
    }

    // Lấy danh sách các công việc hoàn thành
    private void loadCompletedTasks() {
        hideAllNoTasksMessages();  // Ẩn tất cả các thông báo "No tasks"

        List<Task> completedTasks = databaseHelper.getCompletedTasks();

        if (completedTasks.isEmpty()) {
            findViewById(R.id.noCompletedTasksText).setVisibility(View.VISIBLE);  // Hiển thị "No completed tasks"
        } else {
            recyclerViewTasks.setVisibility(View.VISIBLE);
            findViewById(R.id.noCompletedTasksText).setVisibility(View.GONE);  // Ẩn thông báo
            taskAdapter.updateList(completedTasks);
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

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    selectedTimeText.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));

                    // Lưu thời gian đã chọn và đặt thông báo
                    setDailyNotification(HomeActivity.this, selectedHour, selectedMinute);
                    Toast.makeText(HomeActivity.this, "Đã lưu thời gian thông báo hàng ngày!", Toast.LENGTH_SHORT).show();
                },
                selectedHour, selectedMinute, true // Giờ và phút mặc định
        );
        timePickerDialog.show();
    }

    private void setDailyNotification(Context context, int hour, int minute) {
        // Lưu thời gian vào SharedPreferences
        saveTimeToPreferences(hour, minute);

        // Cài đặt thông báo
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Nếu thời gian đã chọn trong ngày đã qua, hãy đặt thông báo cho ngày hôm sau
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Sử dụng cờ IMMUTABLE nếu gặp vấn đề trên Android 12+
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        } else {
            Log.e("AlarmManager", "AlarmManager not available");
        }
    }


    private void saveTimeToPreferences(int hour, int minute) {
        SharedPreferences sharedPreferences = getSharedPreferences("notification_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("selected_hour", hour);
        editor.putInt("selected_minute", minute);
        editor.apply();
    }


    private void loadSavedTime() {
        SharedPreferences sharedPreferences = getSharedPreferences("notification_preferences", MODE_PRIVATE);
        selectedHour = sharedPreferences.getInt("selected_hour", 21); // Mặc định 21 giờ
        selectedMinute = sharedPreferences.getInt("selected_minute", 0); // Mặc định 0 phút
        selectedTimeText.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
    }

    private void hideAllNoTasksMessages() {
        findViewById(R.id.noCompletedTasksText).setVisibility(View.GONE);
        findViewById(R.id.noFavoriteTasksText).setVisibility(View.GONE);
        findViewById(R.id.noUpcomingTasksText).setVisibility(View.GONE);
        recyclerViewTasks.setVisibility(View.GONE);
    }

}
