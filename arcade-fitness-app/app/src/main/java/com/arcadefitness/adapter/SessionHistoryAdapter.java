package com.arcadefitness.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arcadefitness.R;
import com.arcadefitness.data.local.entity.WorkoutSessionEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SessionHistoryAdapter extends RecyclerView.Adapter<SessionHistoryAdapter.SessionViewHolder> {

    private final List<WorkoutSessionEntity> sessions = new ArrayList<>();
    private Map<Integer, String> workoutNames = new HashMap<>();

    public void setWorkoutNames(Map<Integer, String> names) {
        this.workoutNames = names != null ? names : new HashMap<>();
    }

    public void setData(List<WorkoutSessionEntity> data) {
        sessions.clear();
        if (data != null) {
            sessions.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session_history, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        WorkoutSessionEntity session = sessions.get(position);
        String name = workoutNames.getOrDefault(session.getWorkoutId(), "Workout");
        String date = new SimpleDateFormat("MMM d yyyy", Locale.getDefault())
                .format(new Date(session.getStartTimestamp()));
        String duration = session.getDurationMinutes() + " min";
        String calories = session.getCaloriesBurned() + " kcal";
        String volume = String.format("%.0f kg", session.getTotalVolume());

        holder.tvName.setText(name);
        holder.tvDate.setText(date);
        holder.tvDuration.setText(duration);
        holder.tvCalories.setText(calories);
        holder.tvVolume.setText(volume);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvDuration, tvCalories, tvMuscleGroup, tvVolume;

        SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvHistoryWorkoutName);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvDuration = itemView.findViewById(R.id.tvHistoryDuration);
            tvCalories = itemView.findViewById(R.id.tvHistoryCalories);
            tvMuscleGroup = itemView.findViewById(R.id.tvHistoryMuscleGroup);
            tvVolume = itemView.findViewById(R.id.tvHistoryVolume);
        }
    }
}
