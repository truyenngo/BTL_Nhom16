package com.example.btl_nhom16;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private DatabaseHelper databaseHelper;  // Cần DatabaseHelper để cập nhật dữ liệu

    public TaskAdapter(List<Task> taskList, DatabaseHelper databaseHelper) {
        this.taskList = taskList;
        this.databaseHelper = databaseHelper;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        // Thiết lập tên, mô tả, trạng thái và ngày đến hạn
        holder.taskName.setText(task.getName());
        holder.taskDescription.setText(task.getDescription());
        String statusText = task.isCompleted() ? "Completed" : "Pending";
        holder.taskStatus.setText("Status: " + statusText);
        holder.taskStatus.setTextColor(task.isCompleted() ?
                holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark) :
                holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        holder.taskDueDate.setText("Due Date: " + task.getDueDate());

        // Cập nhật icon yêu thích
        if (task.isFavorite()) {
            holder.favoriteIcon.setImageResource(R.drawable.ic_star_filled);  // Sao đầy
        } else {
            holder.favoriteIcon.setImageResource(R.drawable.ic_star_empty);  // Sao trống
        }

        // Xử lý sự kiện click vào icon sao
        holder.favoriteIcon.setOnClickListener(v -> {
            boolean newFavoriteStatus = !task.isFavorite();  // Đảo trạng thái yêu thích
            task.setFavorite(newFavoriteStatus);  // Cập nhật trạng thái yêu thích của công việc

            if (newFavoriteStatus) {
                holder.favoriteIcon.setImageResource(R.drawable.ic_star_filled);  // Thay đổi icon sao thành đầy
                databaseHelper.addToFavorites(task);  // Cập nhật công việc là yêu thích trong cơ sở dữ liệu
            } else {
                holder.favoriteIcon.setImageResource(R.drawable.ic_star_empty);  // Thay đổi icon sao thành trống
                databaseHelper.removeFromFavorites(task);  // Loại bỏ công việc khỏi danh sách yêu thích trong cơ sở dữ liệu
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
        TextView taskStatus;
        TextView taskDueDate;
        ImageView favoriteIcon;  // Icon sao

        public TaskViewHolder(View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.taskName);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskStatus = itemView.findViewById(R.id.taskStatus);
            taskDueDate = itemView.findViewById(R.id.taskDueDate);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
        }
    }
}
