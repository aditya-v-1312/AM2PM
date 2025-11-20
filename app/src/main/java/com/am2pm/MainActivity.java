package com.am2pm;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.am2pm.adapter.TaskAdapter;
import com.am2pm.database.AppDatabase;
import com.am2pm.model.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    private RecyclerView tasksRecyclerView;
    private FloatingActionButton addTaskFab;
    private TaskAdapter adapter;
    private AppDatabase database;
    private List<Task> taskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkNotificationPermission();

        // 1. Initialize Database
        database = AppDatabase.getDatabase(this);

        // 2. Setup List (RecyclerView)
        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter();
        tasksRecyclerView.setAdapter(adapter);

        // Handle Clicks on List Items (Delete/Complete)
        adapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Task task) {
                // TODO: Edit task later
            }

            @Override
            public void onCheckBoxClick(Task task, boolean isChecked) {
                updateTaskStatus(task, isChecked);
            }
        });

        // 3. Setup Add Button
        addTaskFab = findViewById(R.id.addTaskFab);
        addTaskFab.setOnClickListener(v -> showAddTaskDialog());

        // 4. Load Data
        loadTasks();
    }

    private void scheduleNotification(String title, long timeInMillis) {
        // If due date is in the past, don't schedule
        if (timeInMillis <= System.currentTimeMillis()) return; 

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("title", title);
        int uniqueId = (int) (System.currentTimeMillis() & 0xfffffff);
        intent.putExtra("id", uniqueId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, uniqueId, intent, PendingIntent.FLAG_IMMUTABLE);

        // Use setAlarmClock for high precision on Samsung devices
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(timeInMillis, pendingIntent);
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // We need to update the layout file next to support these new fields
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        builder.setView(view);

        final EditText editTitle = view.findViewById(R.id.editTitle);
        final EditText editDesc = view.findViewById(R.id.editDesc); // Needed in XML
        final Button btnDate = view.findViewById(R.id.btnDate);     // Needed in XML
        final Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory); // Needed in XML
        
        // Setup Categories
        String[] categories = {"Personal", "Startup", "IITM", "NUV", "Work"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Date Picker Logic
        final Calendar calendar = Calendar.getInstance();
        btnDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view1, year, month, day) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                
                // Once date is picked, show Time Picker
                new TimePickerDialog(this, (view2, hour, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                    btnDate.setText(sdf.format(calendar.getTime()));
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
                
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        Button btnSave = view.findViewById(R.id.btnSaveTask);
        final AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String description = editDesc.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();
            
            if (!title.isEmpty()) {
                // Save with new fields
                saveTask(title, description, calendar.getTimeInMillis(), category);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // Update the save function signature
    private void saveTask(String title, String description, long dueDate, String category) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Task newTask = new Task(title, description, dueDate, 2, category);
            database.taskDao().insert(newTask);
            
            // Update UI + Schedule Alarm + UPDATE WIDGET
            runOnUiThread(() -> {
                scheduleNotification(title, dueDate);
                updateWidget(); // <--- ADD THIS LINE
            });
            
            loadTasks();
        });
    }

    private void saveTask(String title, int priority) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Task newTask = new Task(title, "", System.currentTimeMillis(), priority, "Personal");
            database.taskDao().insert(newTask);
            loadTasks(); // Refresh list
        });
    }

    private void updateTaskStatus(Task task, boolean isChecked) {
        if (isChecked) {
            task.isCompleted = true;
            adapter.notifyDataSetChanged();

            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    database.taskDao().delete(task);
                    updateWidget(); // <--- ADD THIS LINE
                    loadTasks();
                });
            }, 500);

        } else {
            // If they uncheck it (rare case), just update status
            AppDatabase.databaseWriteExecutor.execute(() -> {
                task.isCompleted = false;
                database.taskDao().update(task);
                loadTasks();
            });
        }
    }

    private void loadTasks() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            taskList = database.taskDao().getAllTasks();
            
            // Update UI on the Main Thread
            runOnUiThread(() -> {
                adapter.setTasks(taskList);
            });
        });
    }

    private void updateWidget() {
        android.appwidget.AppWidgetManager man = android.appwidget.AppWidgetManager.getInstance(this);
        int[] ids = man.getAppWidgetIds(new android.content.ComponentName(this, TaskWidgetProvider.class));
        man.notifyAppWidgetViewDataChanged(ids, R.id.widgetListView);
    }

    // Check for Android 13+ Notification Permission
    private void checkNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
}