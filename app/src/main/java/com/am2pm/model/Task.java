package com.am2pm.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "task_table")
public class Task {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String description;     // New: For details
    public long dueDate;           // Timestamp
    public boolean isAllDay;       // New: Time specific?
    public String recurrenceRule;  // New: "DAILY", "WEEKLY", "NONE"
    
    public int priority;           // 1=Low, 2=Med, 3=High
    public String category;        // "Startup", "Personal", etc.
    public String attachmentPath;  // New: Path to photo/file
    public boolean isEncrypted;    // New: Security flag
    
    public boolean isCompleted;

    // Constructor
    public Task(String title, String description, long dueDate, int priority, String category) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.category = category;
        this.isCompleted = false;
        this.recurrenceRule = "NONE";
        this.isEncrypted = false;
    }
}