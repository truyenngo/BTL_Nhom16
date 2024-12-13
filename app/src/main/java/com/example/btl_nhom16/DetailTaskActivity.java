package com.example.btl_nhom16;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DetailTaskActivity extends AddTaskActivity {
    private RecyclerView recyclerViewSubtasks;
    private SubtaskAdapter subtaskAdapter;
    Task curTask;
    private ImageView
        m_SaveBtn,
        m_DeleteBtn,
        m_ToggleFavoriteBtn,
        m_ToggleDoneBtn;
    private Button m_AddSubtaskBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_task);
        InitAllCommonViewElements();
        Init_Task();

        recyclerViewSubtasks = findViewById(R.id.recyclerViewSubtasks);
        recyclerViewSubtasks.setLayoutManager(new LinearLayoutManager(this));
        subtaskAdapter = new SubtaskAdapter(curTask, DetailTaskActivity.this, databaseHelper.getAllSubtasksOf(curTask), databaseHelper);
        recyclerViewSubtasks.setAdapter(subtaskAdapter);

        Init_SaveBtn();
        Init_DeleteBtn();
        Init_ToggleFavoriteBtn();
        Init_ToggleDoneBtn();
        Init_AddSubtaskBtn();
    }

    private void Init_Task() {
        curTask = databaseHelper
                .getAllTasks()
                .stream()
                .filter(task -> task.getId() == getIntent().getIntExtra("selectedTaskId", -1))
                .findFirst().orElse(null);
        if (curTask == null) return;

        editTextTaskName.setText(curTask.getName());
        editTextTaskDescription.setText(curTask.getDescription());
        textViewStartDate.setText(curTask.getCreationDate());
        textViewDueDate.setText(curTask.getDueDate());
    }
    private void Init_SaveBtn() {
        m_SaveBtn = findViewById(R.id.saveIcon);
        m_SaveBtn.setOnClickListener(view -> {
            String taskName = editTextTaskName.getText().toString().trim();
            String taskDescription = editTextTaskDescription.getText().toString().trim();

            if (taskName.isEmpty()) {
                editTextTaskName.setError("Task name is required");
                return;
            }

            if (startCalendar.after(dueCalendar)) {
                Toast.makeText(this, "Start date must be before due date", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isUpdated = databaseHelper.updateTask(curTask.getId(), taskName, taskDescription, startCalendar.getTimeInMillis(), dueCalendar.getTimeInMillis());
            if (isUpdated) {
                List<Task> tasks = databaseHelper.getAllTasks();
                Log.d("DetailTaskActivity", "Tasks after Update: " + tasks.size());
                Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error adding task", Toast.LENGTH_SHORT).show();
            }

            for (int i = 0; i < subtaskAdapter.getItemCount(); ++i) {
                SubtaskAdapter.SubtaskViewHolder holder = ((SubtaskAdapter.SubtaskViewHolder)recyclerViewSubtasks.findViewHolderForLayoutPosition(i));
                Subtask subtask = subtaskAdapter.getSubtaskList().get(i);
                databaseHelper.updateSubtask(subtask.getId(), subtask.getTask_id(), holder.taskDescription.getText().toString());
            }
        });
    }
    private void Init_DeleteBtn() {
        m_DeleteBtn = findViewById(R.id.trashIcon);
        m_DeleteBtn.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    databaseHelper.deleteTask(curTask);
                    Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DetailTaskActivity.this, HomeActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
        });
    }
    private void Init_ToggleFavoriteBtn() {
        m_ToggleFavoriteBtn = findViewById(R.id.favoriteIcon);

        if (curTask.isFavorite()) {
            m_ToggleFavoriteBtn.setImageResource(R.drawable.ic_star_filled);
        } else {
            m_ToggleFavoriteBtn.setImageResource(R.drawable.ic_star_empty);
        }

        m_ToggleFavoriteBtn.setOnClickListener(view -> {
            boolean newFavoriteStatus = !curTask.isFavorite();
            curTask.setFavorite(newFavoriteStatus);

            if (newFavoriteStatus) {
                m_ToggleFavoriteBtn.setImageResource(R.drawable.ic_star_filled);
                databaseHelper.addToFavorites(curTask);
            } else {
                m_ToggleFavoriteBtn.setImageResource(R.drawable.ic_star_empty);
                databaseHelper.removeFromFavorites(curTask);
            }
        });
    }
    private void Init_ToggleDoneBtn() {
        m_ToggleDoneBtn = findViewById(R.id.doneIcon);
        if (curTask.isCompleted()) {
            m_ToggleDoneBtn.setImageResource(R.drawable.ic_done_filled);
        } else {
            m_ToggleDoneBtn.setImageResource(R.drawable.ic_done_empty);
        }
        m_ToggleDoneBtn.setOnClickListener(view -> {
            boolean newDoneStatus = !curTask.isCompleted();
            curTask.setCompleted(newDoneStatus);

            if (newDoneStatus) {
                m_ToggleDoneBtn.setImageResource(R.drawable.ic_done_filled);
                databaseHelper.addTaskToCompleted(curTask);
                subtaskAdapter.updateList(databaseHelper.getAllSubtasksOf(curTask));
            } else {
                m_ToggleDoneBtn.setImageResource(R.drawable.ic_done_empty);
                databaseHelper.removeTaskFromCompleted(curTask);
            }
        });
    }
    private void Init_AddSubtaskBtn() {
        m_AddSubtaskBtn = findViewById(R.id.btnAddSubtask);
        m_AddSubtaskBtn.setOnClickListener(view -> {
            subtaskAdapter.addEmptySubtask();
            recyclerViewSubtasks.post(() -> {
                recyclerViewSubtasks.smoothScrollToPosition(subtaskAdapter.getItemCount() - 1);
                recyclerViewSubtasks.post(() -> {
                    ((SubtaskAdapter.SubtaskViewHolder)recyclerViewSubtasks.findViewHolderForLayoutPosition(subtaskAdapter.getItemCount() - 1))
                            .taskDescription
                            .requestFocus();
                });
            });
        });
    }
}
