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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SubtaskAdapter extends RecyclerView.Adapter<SubtaskAdapter.SubtaskViewHolder> {
    private List<Subtask> subtaskList;
    private DatabaseHelper databaseHelper;// Cần DatabaseHelper để cập nhật dữ liệu
    private Context parentContext;
    private Task curTask;
    public List<Subtask> getSubtaskList() { return subtaskList; }

    public SubtaskAdapter(Task curTask, Context parentContext, List<Subtask> subtaskList, DatabaseHelper databaseHelper) {
        this.curTask = curTask;
        this.subtaskList = subtaskList;
        this.databaseHelper = databaseHelper;
        this.parentContext = parentContext;
    }

    @Override
    public SubtaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subtask, parent, false);
        return new SubtaskAdapter.SubtaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SubtaskViewHolder holder, int position) {
        Subtask subtask = subtaskList.get(position);

        // Cập nhật dữ liệu hiển thị
        holder.taskDescription.setText(subtask.getDescription());
        if (subtask.isCompleted()) {
            holder.doneIcon.setImageResource(R.drawable.ic_done_filled);
        } else {
            holder.doneIcon.setImageResource(R.drawable.ic_done_empty);
        }
        holder.doneIcon.setOnClickListener(view -> {
            boolean newDoneStatus = !subtask.isCompleted();
            subtask.setCompleted(newDoneStatus);

            if (newDoneStatus) {
                holder.doneIcon.setImageResource(R.drawable.ic_done_filled);
                databaseHelper.addSubtaskToCompleted(subtask);
            } else {
                holder.doneIcon.setImageResource(R.drawable.ic_done_empty);
                databaseHelper.removeSubtaskFromCompleted(subtask);
            }
        });

        holder.trashIcon.setOnClickListener(view -> {
            // Hiển thị hộp thoại xác nhận
            new AlertDialog.Builder(parentContext)
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete this subtask?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Xóa task khỏi cơ sở dữ liệu
                        databaseHelper.deleteSubtask(subtask);

                        updateList(databaseHelper.getAllSubtasksOf(curTask));

                        // Hiển thị thông báo
                        Toast.makeText(parentContext, "Subtask deleted", Toast.LENGTH_SHORT).show();

                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return subtaskList.size();
    }

    public void updateList(List<Subtask> newTaskList) {
        subtaskList = newTaskList;
        notifyDataSetChanged();
    }

    public void addEmptySubtask() {
        databaseHelper.insertSubtask(curTask.getId(), "");
        subtaskList = databaseHelper.getAllSubtasksOf(curTask);
        notifyItemInserted(getItemCount() - 1);
    }

    public static class SubtaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskDescription;
        ImageView doneIcon; // Icon done
        ImageView trashIcon; // Icon thùng rác

        public SubtaskViewHolder(View itemView) {
            super(itemView);
            taskDescription = itemView.findViewById(R.id.editTextSubtaskDescription);
            doneIcon = itemView.findViewById(R.id.subtaskDoneIcon);
            trashIcon = itemView.findViewById(R.id.subtaskTrashIcon);
        }
    }
}
