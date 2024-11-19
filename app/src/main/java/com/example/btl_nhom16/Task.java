package com.example.btl_nhom16;

public class Task {
    private int id;
    private String name;
    private String description;
    private boolean isCompleted;
    private String creationDate;
    private String dueDate;
    private boolean isFavorite;

    public Task(int id, String name, String description, boolean isCompleted, String creationDate, String dueDate, boolean isFavorite) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isCompleted = isCompleted;
        this.creationDate = creationDate;
        this.dueDate = dueDate;
        this.isFavorite = isFavorite;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}

