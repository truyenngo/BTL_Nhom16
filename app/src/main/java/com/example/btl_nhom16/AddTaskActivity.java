package com.example.btl_nhom16;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {
    protected EditText editTextTaskName, editTextTaskDescription;
    protected TextView textViewStartDate, textViewDueDate;
    protected Button buttonSaveTask;
    protected DatabaseHelper databaseHelper;

    protected Calendar startCalendar = Calendar.getInstance();
    protected Calendar dueCalendar = Calendar.getInstance();

    protected void InitAllCommonViewElements() {

        editTextTaskName = findViewById(R.id.editTextTaskName);
        editTextTaskDescription = findViewById(R.id.editTextTaskDescription);
        textViewStartDate = findViewById(R.id.textViewStartDate);
        textViewDueDate = findViewById(R.id.textViewDueDate);
        buttonSaveTask = findViewById(R.id.buttonSaveTask);
        databaseHelper = new DatabaseHelper(this);

        textViewStartDate.setOnClickListener(v -> showDatePickerDialog(startCalendar, textViewStartDate));
        textViewDueDate.setOnClickListener(v -> showDatePickerDialog(dueCalendar, textViewDueDate));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        InitAllCommonViewElements();
        buttonSaveTask.setOnClickListener(v -> saveTask());
    }

    protected void showDatePickerDialog(Calendar calendar, TextView textView) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
            calendar.set(Calendar.YEAR, selectedYear);
            calendar.set(Calendar.MONTH, selectedMonth);
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
            textView.setText(getFormattedDate(calendar));
            showTimePickerDialog(calendar, textView);
        }, year, month, day);
        datePickerDialog.show();
    }

    protected void showTimePickerDialog(Calendar calendar, TextView textView) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
            calendar.set(Calendar.MINUTE, selectedMinute);
            textView.setText(getFormattedDate_InMillis(calendar));
        }, hour, minute, true);
        timePickerDialog.show();
    }

    protected String getFormattedDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    protected String getFormattedDate_InMillis(Calendar calendar) {
        return getFormattedDate(calendar) + " " + String.format("%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    protected void saveTask() {
        String taskName = editTextTaskName.getText().toString().trim();
        String taskDescription = editTextTaskDescription.getText().toString().trim();
        if (taskName.isEmpty()) {
            editTextTaskName.setError("Task name is required");
            return;
        }

        if (startCalendar.after(dueCalendar)) {
            Toast.makeText(this, "Start date must be before due date",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isInserted = databaseHelper.insertTask(taskName, taskDescription,
                startCalendar.getTimeInMillis(), dueCalendar.getTimeInMillis());

        if (isInserted) {
            List<Task> tasks = databaseHelper.getAllTasks();
            Log.d("AddTaskActivity", "Tasks after insert: " + tasks.size());
            Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AddTaskActivity.this, HomeActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Error adding task", Toast.LENGTH_SHORT).show();
        }
    }
}



