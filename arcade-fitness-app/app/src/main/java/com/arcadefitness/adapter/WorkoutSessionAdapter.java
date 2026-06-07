package com.arcadefitness.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arcadefitness.R;
import com.arcadefitness.data.local.entity.WorkoutEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkoutSessionAdapter extends RecyclerView.Adapter<WorkoutSessionAdapter.WorkoutSessionViewHolder> {

    private final List<WorkoutEntity> workouts = new ArrayList<>();
    private OnWorkoutClickListener listener;

    public interface OnWorkoutClickListener {
        void onWorkoutClick(WorkoutEntity workout);
    }

    public void setOnWorkoutClickListener(OnWorkoutClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkoutSessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_session, parent, false);
        return new WorkoutSessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutSessionViewHolder holder, int position) {
        WorkoutEntity workout = workouts.get(position);
        holder.bind(workout, listener);
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public void setWorkouts(List<WorkoutEntity> workouts) {
        this.workouts.clear();
        if (workouts != null) {
            this.workouts.addAll(workouts);
        }
        notifyDataSetChanged();
    }

    static class WorkoutSessionViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvSessionWorkoutName;
        private final TextView tvSessionDate;
        private final TextView tvSessionDuration;
        private final TextView tvSessionStatus;

        WorkoutSessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSessionWorkoutName = itemView.findViewById(R.id.tvSessionWorkoutName);
            tvSessionDate = itemView.findViewById(R.id.tvSessionDate);
            tvSessionDuration = itemView.findViewById(R.id.tvSessionDuration);
            tvSessionStatus = itemView.findViewById(R.id.tvSessionStatus);
        }

        void bind(WorkoutEntity workout, OnWorkoutClickListener listener) {
            tvSessionWorkoutName.setText(workout.getName());
            String dateStr = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    .format(new Date(workout.getCreatedAt()));
            tvSessionDate.setText(dateStr);
            tvSessionDuration.setText(workout.getEstimatedDurationMinutes() + " min");
            tvSessionStatus.setText(workout.getTargetMuscleGroup());

            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onWorkoutClick(workout));
            }
        }
    }
}
