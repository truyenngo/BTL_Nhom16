package com.example.btl_nhom16;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private DatabaseHelper databaseHelper;// Cần DatabaseHelper để cập nhật dữ liệu
    private Context parentContext;

    public TaskAdapter(Context parentContext, List<Task> taskList, DatabaseHelper databaseHelper) {
        this.taskList = taskList;
        this.databaseHelper = databaseHelper;
        this.parentContext = parentContext;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        // Cập nhật dữ liệu hiển thị
        holder.taskName.setText(task.getName());
        holder.taskDescription.setText(task.getDescription());
        String statusText = task.isCompleted() ? "Completed" : "Pending";
        holder.taskDueDate.setText("Due Date: " + task.getDueDate());

        if (task.isFavorite()) {
            holder.favoriteIcon.setImageResource(R.drawable.ic_star_filled);
        } else {
            holder.favoriteIcon.setImageResource(R.drawable.ic_star_empty);
        }

        // Xử lý sự kiện click vào icon sao
        holder.favoriteIcon.setOnClickListener(v -> {
            boolean newFavoriteStatus = !task.isFavorite();
            task.setFavorite(newFavoriteStatus);

            if (newFavoriteStatus) {
                holder.favoriteIcon.setImageResource(R.drawable.ic_star_filled);
                databaseHelper.addToFavorites(task);
            } else {
                holder.favoriteIcon.setImageResource(R.drawable.ic_star_empty);
                databaseHelper.removeFromFavorites(task);
            }
        });

        if (task.isCompleted()) {
            holder.doneIcon.setImageResource(R.drawable.ic_done_filled);
        } else {
            holder.doneIcon.setImageResource(R.drawable.ic_done_empty);
        }
        holder.doneIcon.setOnClickListener(view -> {
            boolean newDoneStatus = !task.isCompleted();
            task.setCompleted(newDoneStatus);

            if (newDoneStatus) {
                holder.doneIcon.setImageResource(R.drawable.ic_done_filled);
                databaseHelper.addTaskToCompleted(task);
            } else {
                holder.doneIcon.setImageResource(R.drawable.ic_done_empty);
                databaseHelper.removeTaskFromCompleted(task);
            }
        });

        // event longclick
        holder.itemView.setOnLongClickListener(v -> {
            // Hiển thị hộp thoại xác nhận
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Xóa task khỏi cơ sở dữ liệu
                        databaseHelper.deleteTask(task);

                        // Xóa task khỏi danh sách trong bộ nhớ
                        taskList.remove(position);

                        // Cập nhật RecyclerView
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, taskList.size());

                        // Hiển thị thông báo
                        Toast.makeText(holder.itemView.getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        });

        // event click
        holder.itemView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(parentContext, DetailTaskActivity.class);
                intent.putExtra("selectedTaskId", task.getId());
                parentContext.startActivity(intent);
            } catch (Exception e) {
                new AlertDialog.Builder(parentContext)
                        .setTitle("Lmao")
                        .setMessage(e.getMessage())
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });
    }



    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateList(List<Task> newTaskList) {
        taskList = newTaskList;
        notifyDataSetChanged();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskName;
        TextView taskDescription;
        TextView taskDueDate;
        ImageView favoriteIcon;  // Icon sao
        ImageView doneIcon; // Icon done

        public TaskViewHolder(View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.taskName);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskDueDate = itemView.findViewById(R.id.taskDueDate);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
            doneIcon = itemView.findViewById(R.id.doneIcon);
        }
    }
}
