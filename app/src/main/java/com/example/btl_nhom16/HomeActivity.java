package com.example.btl_nhom16;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private DatabaseHelper databaseHelper;
    private ImageView favoritesButton;  // Nút yêu thích

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        favoritesButton = findViewById(R.id.favoritesButton);  // Khởi tạo nút yêu thích

        // Cấu hình RecyclerView
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();

        // Khởi tạo DatabaseHelper và TaskAdapter
        databaseHelper = new DatabaseHelper(this);
        taskAdapter = new TaskAdapter(taskList, databaseHelper);
        recyclerViewTasks.setAdapter(taskAdapter);

        // Lấy dữ liệu từ SQLite Database
        loadTasks();

        // Nút thêm công việc
        findViewById(R.id.addTaskButton).setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        // Sự kiện cho nút yêu thích
        favoritesButton.setOnClickListener(view -> {
            loadFavoriteTasks();  // Tải lại các công việc yêu thích
            boolean isSelected = favoritesButton.isSelected(); // Kiểm tra trạng thái hiện tại
            favoritesButton.setSelected(!isSelected); // Đảo trạng thái (true -> false hoặc false -> true)
        });
    }

    // Lấy tất cả các công việc
    public void loadTasks() {
        List<Task> tasks = databaseHelper.getAllTasks();
        Log.d("HomeActivity", "Tasks loaded: " + tasks.size());
        if (tasks != null && !tasks.isEmpty()) {
            taskAdapter.updateList(tasks);
        } else {
            Toast.makeText(this, "No tasks found", Toast.LENGTH_SHORT).show();
        }
    }

    // Lấy các công việc yêu thích
    public void loadFavoriteTasks() {
        List<Task> favoriteTasks = databaseHelper.getFavoriteTasks();  // Lấy các công việc yêu thích
        Log.d("HomeActivity", "Favorite tasks loaded: " + favoriteTasks.size());
        if (favoriteTasks != null && !favoriteTasks.isEmpty()) {
            taskAdapter.updateList(favoriteTasks);  // Cập nhật danh sách trong adapter
        } else {
            Toast.makeText(this, "No favorite tasks found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks(); // Tải lại công việc khi quay lại HomeActivity
    }
}




