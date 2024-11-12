package com.example.btl_nhom16;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {
    private EditText editTextTaskName, editTextTaskDescription;
    private TextView textViewStartDate, textViewDueDate;
    private Button buttonSaveTask;
    private DatabaseHelper databaseHelper;

    private Calendar startCalendar = Calendar.getInstance();
    private Calendar dueCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Khởi tạo các view
        editTextTaskName = findViewById(R.id.editTextTaskName);
        editTextTaskDescription = findViewById(R.id.editTextTaskDescription);
        textViewStartDate = findViewById(R.id.textViewStartDate);
        textViewDueDate = findViewById(R.id.textViewDueDate);
        buttonSaveTask = findViewById(R.id.buttonSaveTask);
        databaseHelper = new DatabaseHelper(this);

        // Thiết lập sự kiện cho chọn ngày bắt đầu
        textViewStartDate.setOnClickListener(v -> showDatePickerDialog(startCalendar, textViewStartDate));

        // Thiết lập sự kiện cho chọn ngày hạn hoàn thành
        textViewDueDate.setOnClickListener(v -> showDatePickerDialog(dueCalendar, textViewDueDate));

        // Thiết lập sự kiện cho nút lưu công việc
        buttonSaveTask.setOnClickListener(v -> saveTask());
    }

    // Hiển thị DatePicker
    private void showDatePickerDialog(Calendar calendar, TextView textView) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            calendar.set(Calendar.YEAR, selectedYear);
            calendar.set(Calendar.MONTH, selectedMonth);
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
            textView.setText(getFormattedDate(calendar));
            showTimePickerDialog(calendar, textView);
        }, year, month, day);

        datePickerDialog.show();
    }

    // Hiển thị TimePicker
    private void showTimePickerDialog(Calendar calendar, TextView textView) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
            calendar.set(Calendar.MINUTE, selectedMinute);
            textView.append(" " + String.format("%02d:%02d", selectedHour, selectedMinute));
        }, hour, minute, true);

        timePickerDialog.show();
    }

    // Định dạng ngày tháng
    private String getFormattedDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    private void saveTask() {
        String taskName = editTextTaskName.getText().toString().trim();
        String taskDescription = editTextTaskDescription.getText().toString().trim();

        if (taskName.isEmpty()) {
            editTextTaskName.setError("Task name is required");
            return;
        }

        // Lưu công việc vào database

        boolean isInserted = databaseHelper.insertTask(taskName, taskDescription, startCalendar.getTimeInMillis(), dueCalendar.getTimeInMillis());
        if (isInserted) {
            // Truy vấn để xác nhận công việc có trong cơ sở dữ liệu
            List<Task> tasks = databaseHelper.getAllTasks();
            Log.d("HomeActivity", "Tasks after insert: " + tasks.size());
            Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AddTaskActivity.this, HomeActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Error adding task", Toast.LENGTH_SHORT).show();
        }


    }

}


