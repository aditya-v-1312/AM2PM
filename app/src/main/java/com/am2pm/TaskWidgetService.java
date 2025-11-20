package com.am2pm;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.am2pm.database.AppDatabase;
import com.am2pm.model.Task;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TaskWidgetItemFactory(this.getApplicationContext());
    }
}

class TaskWidgetItemFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private List<Task> taskList = new ArrayList<>();

    public TaskWidgetItemFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        // Initialize
    }

    @Override
    public void onDataSetChanged() {
        // This runs in a background thread, so it's safe to query Room directly
        AppDatabase db = AppDatabase.getDatabase(context);
        taskList = db.taskDao().getAllTasks();
    }

    @Override
    public void onDestroy() {
        taskList.clear();
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position >= taskList.size()) return null;

        Task task = taskList.get(position);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_item);

        views.setTextViewText(R.id.widgetTaskTitle, task.title);
        
        if (task.dueDate > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            views.setTextViewText(R.id.widgetTaskTime, sdf.format(new Date(task.dueDate)));
        } else {
            views.setTextViewText(R.id.widgetTaskTime, "No Date");
        }

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}