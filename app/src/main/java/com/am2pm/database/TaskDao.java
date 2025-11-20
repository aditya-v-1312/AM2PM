package com.am2pm.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.am2pm.model.Task;
import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    // Get all tasks, sorted by Not Completed first, then by Priority (High to Low)
    @Query("SELECT * FROM task_table ORDER BY isCompleted ASC, priority DESC")
    List<Task> getAllTasks();
    
    // Delete everything (useful for testing)
    @Query("DELETE FROM task_table")
    void deleteAll();
}