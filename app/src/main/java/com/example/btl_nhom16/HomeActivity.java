package com.example.btl_nhom16;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private DatabaseHelper databaseHelper;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        searchEditText = findViewById(R.id.searchEditText);
        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);

        // Cấu hình RecyclerView
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);
        recyclerViewTasks.setAdapter(taskAdapter);

        // Lấy dữ liệu từ SQLite Database
        databaseHelper = new DatabaseHelper(this);
        loadTasks();

        // Xử lý tìm kiếm
//        searchEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                filterTasks(s.toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {}
//        });

        // Nút thêm công việc
        fabAddTask.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });
    }

    private void loadTasks() {
        taskList.clear();
        taskList.addAll(databaseHelper.getAllTasks());
        taskAdapter.notifyDataSetChanged();
    }

//    private void filterTasks(String query) {
//        List<Task> filteredList = new ArrayList<>();
//        for (Task task : taskList) {
//            if (task.getName().toLowerCase().contains(query.toLowerCase())) {
//                filteredList.add(task);
//            }
//        }
//        taskAdapter.setFilteredList(filteredList);
//    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks(); // Tải lại công việc khi quay lại HomeActivity
    }
}

