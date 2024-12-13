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
    private ImageView favoritesButton, homeButton, completedTasksButton, notificationSettingsButton, logoutButton, logoIcon;
    private LinearLayout notificationSettingsLayout;
    private Button selectTimeButton;
    private TextView selectedTimeText, headerTitle;
    private int selectedHour = 21;
    private int selectedMinute = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getwide();
        loadSavedTime();

        logoIcon.setOnClickListener(view -> {
            switchToTasksView();
            hideAllNoTasksMessages();
            loadTasks();
            headerTitle.setText("Tất cả công việc");
        });

        homeButton.setOnClickListener(view -> {
            switchToTasksView();
            hideAllNoTasksMessages();
            loadUpcomingTasks();
            headerTitle.setText("Công việc sắp đến hạn");
        });

        favoritesButton.setOnClickListener(view -> {
            switchToTasksView();
            hideAllNoTasksMessages();
            loadFavoriteTasks();
            headerTitle.setText("Công việc yêu thích");
        });

        completedTasksButton.setOnClickListener(view -> {
            switchToTasksView();
            hideAllNoTasksMessages();
            loadCompletedTasks();
            headerTitle.setText("Công việc đã hoàn thành");
        });

        notificationSettingsButton.setOnClickListener(view -> {
            switchToNotificationSettingsView();
            headerTitle.setText("Cài đặt thông báo");
        });

        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        databaseHelper = new DatabaseHelper(this);
        taskAdapter = new TaskAdapter(HomeActivity.this, taskList, databaseHelper);
        recyclerViewTasks.setAdapter(taskAdapter);

        addTask();
        logout();
        loadTasks();
        selectTimeButton.setOnClickListener(v -> showTimePickerDialog());
    }

    private void hideAllNoTasksMessages() {
        findViewById(R.id.noTasksText).setVisibility(View.GONE);
        findViewById(R.id.noCompletedTasksText).setVisibility(View.GONE);
        findViewById(R.id.noFavoriteTasksText).setVisibility(View.GONE);
        findViewById(R.id.noUpcomingTasksText).setVisibility(View.GONE);
    }

    public void getwide() {
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        notificationSettingsLayout = findViewById(R.id.notificationSettingsLayout);
        selectTimeButton = findViewById(R.id.selectTimeButton);
        selectedTimeText = findViewById(R.id.selectedTimeText);
        favoritesButton = findViewById(R.id.favoritesButton);
        homeButton = findViewById(R.id.homesButton);
        completedTasksButton = findViewById(R.id.completedTasksButton);
        notificationSettingsButton = findViewById(R.id.notificationSettingsButton);
        headerTitle = findViewById(R.id.headerTitle);
        logoIcon = findViewById(R.id.logoIcon);
    }

    public void logout() {
        logoutButton = findViewById(R.id.logoutIcon);
        logoutButton.setOnClickListener(view -> {
            Dialog dialog = new Dialog(HomeActivity.this);
            dialog.setContentView(R.layout.popup_logout);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            Button btnCancelLogout = dialog.findViewById(R.id.btnCancel);
            Button btnConfirmLogout = dialog.findViewById(R.id.btnConfirm);

            btnCancelLogout.setOnClickListener(v -> dialog.dismiss());
            btnConfirmLogout.setOnClickListener(v -> {
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

    public void addTask() {
        findViewById(R.id.addTaskButton).setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });
    }

    public void loadTasks() {
        hideAllNoTasksMessages();
        taskList.clear();
        taskAdapter.notifyDataSetChanged();
        List<Task> allTasks = databaseHelper.getAllTasks();
        if (allTasks.isEmpty()) {
            findViewById(R.id.noTasksText).setVisibility(View.VISIBLE);
        } else {
            taskList.addAll(allTasks);
            taskAdapter.notifyDataSetChanged();
            findViewById(R.id.noTasksText).setVisibility(View.GONE);
        }
    }

    private void loadUpcomingTasks() {
        hideAllNoTasksMessages();
        taskList.clear();
        taskAdapter.notifyDataSetChanged();
        List<Task> upcomingTasks = databaseHelper.getUpcomingTasks();
        if (upcomingTasks.isEmpty()) {
            findViewById(R.id.noUpcomingTasksText).setVisibility(View.VISIBLE);
        } else {
            taskList.addAll(upcomingTasks);
            taskAdapter.notifyDataSetChanged();
            findViewById(R.id.noUpcomingTasksText).setVisibility(View.GONE);
        }
    }

    private void loadFavoriteTasks() {
        hideAllNoTasksMessages();
        taskList.clear();
        taskAdapter.notifyDataSetChanged();
        List<Task> favoriteTasks = databaseHelper.getFavoriteTasks();
        if (favoriteTasks.isEmpty()) {
            findViewById(R.id.noFavoriteTasksText).setVisibility(View.VISIBLE);
        } else {
            taskList.addAll(favoriteTasks);
            taskAdapter.notifyDataSetChanged();
            findViewById(R.id.noFavoriteTasksText).setVisibility(View.GONE);
        }
    }

    private void loadCompletedTasks() {
        hideAllNoTasksMessages();
        taskList.clear();
        taskAdapter.notifyDataSetChanged();
        List<Task> completedTasks = databaseHelper.getCompletedTasks();
        if (completedTasks.isEmpty()) {
            findViewById(R.id.noCompletedTasksText).setVisibility(View.VISIBLE);
        } else {
            taskList.addAll(completedTasks);
            taskAdapter.notifyDataSetChanged();
            findViewById(R.id.noCompletedTasksText).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTaskList();
    }

    private void refreshTaskList() {
        loadTasks();
    }

    private void switchToNotificationSettingsView() {
        recyclerViewTasks.setVisibility(View.GONE);
        hideAllNoTasksMessages();
        notificationSettingsLayout.setVisibility(View.VISIBLE);
    }

    private void switchToTasksView() {
        recyclerViewTasks.setVisibility(View.VISIBLE);
        notificationSettingsLayout.setVisibility(View.GONE);
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    selectedTimeText.setText(String.format(Locale.getDefault(),
                            "%02d:%02d", selectedHour, selectedMinute));

                    setDailyNotification(HomeActivity.this, selectedHour, selectedMinute);
                    Toast.makeText(HomeActivity.this,
                            "Đã lưu thời gian thông báo hàng ngày!", Toast.LENGTH_SHORT).show();
                }, selectedHour, selectedMinute, true
        );
        timePickerDialog.show();
    }

    private void setDailyNotification(Context context, int hour, int minute) {
        saveTimeToPreferences(hour, minute);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                                                AlarmManager.INTERVAL_DAY, pendingIntent);
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
        selectedHour = sharedPreferences.getInt("selected_hour", 21);
        selectedMinute = sharedPreferences.getInt("selected_minute", 0);
        selectedTimeText.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
    }

}
