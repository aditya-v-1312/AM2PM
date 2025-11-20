package com.am2pm.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.am2pm.R;
import com.am2pm.model.Task;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Task task);
        void onCheckBoxClick(Task task, boolean isChecked);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task currentTask = tasks.get(position);
        
        holder.textViewTitle.setText(currentTask.title);
        holder.checkBox.setChecked(currentTask.isCompleted);
        
        // Handle Description
        if (currentTask.description != null && !currentTask.description.isEmpty()) {
            holder.textViewDesc.setText(currentTask.description);
            holder.textViewDesc.setVisibility(View.VISIBLE);
        } else {
            holder.textViewDesc.setVisibility(View.GONE);
        }

        // Handle Category Chip
        holder.chipCategory.setText(currentTask.category);
        
        // Handle Date Format
        if (currentTask.dueDate > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            holder.textViewDate.setText(sdf.format(new Date(currentTask.dueDate)));
        } else {
            holder.textViewDate.setText("No Date");
        }

        // Dim text if completed (Strikethrough logic can be added here)
        if (currentTask.isCompleted) {
            holder.textViewTitle.setAlpha(0.5f);
        } else {
            holder.textViewTitle.setAlpha(1.0f);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(currentTask);
        });
        
        holder.checkBox.setOnClickListener(v -> {
            if (listener != null) listener.onCheckBoxClick(currentTask, holder.checkBox.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewDesc, textViewDate, chipCategory;
        CheckBox checkBox;

        public TaskViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.taskTitle);
            textViewDesc = itemView.findViewById(R.id.taskDesc);
            textViewDate = itemView.findViewById(R.id.taskDate);
            chipCategory = itemView.findViewById(R.id.chipCategory);
            checkBox = itemView.findViewById(R.id.taskCheckBox);
        }
    }
}