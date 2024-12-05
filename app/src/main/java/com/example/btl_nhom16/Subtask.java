package com.example.btl_nhom16;

public class Subtask {
    private int id;
    private int task_id;
    private String description;
    private boolean isCompleted;

    public Subtask(int id, int task_id, String description, boolean isCompleted) {
        this.id = id;
        this.task_id = task_id;
        this.description = description;
        this.isCompleted = isCompleted;
    }

    // Getters and Setters
    public int getId() { return id; }
    public int getTask_id() { return task_id; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}

