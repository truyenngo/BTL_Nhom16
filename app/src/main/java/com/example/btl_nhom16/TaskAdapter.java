package com.example.btl_nhom16;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        Log.d("TaskAdapter", "Binding task: " + task.getName());  // Log tên công việc
        holder.taskName.setText(task.getName());
        holder.taskDescription.setText(task.getDescription());

        // Trạng thái công việc (hoàn thành hoặc chưa hoàn thành)
        String statusText = task.isCompleted() ? "Completed" : "Pending";
        holder.taskStatus.setText("Status: " + statusText);
        holder.taskStatus.setTextColor(task.isCompleted() ?
                holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark) :
                holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));

        // Ngày đến hạn
        holder.taskDueDate.setText("Due Date: " + task.getDueDate());
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

        public TaskViewHolder(View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.taskName);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskStatus = itemView.findViewById(R.id.taskStatus);
            taskDueDate = itemView.findViewById(R.id.taskDueDate);
        }
    }
}
