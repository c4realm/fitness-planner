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

    // ── Listeners — held on the adapter (non-static), passed into bind() ──
    private OnWorkoutClickListener clickListener;
    private OnWorkoutLongClickListener longClickListener;

    // ── Click listener interface ──────────────────────────────────────
    public interface OnWorkoutClickListener {
        void onWorkoutClick(WorkoutEntity workout);
    }

    public void setOnWorkoutClickListener(OnWorkoutClickListener listener) {
        this.clickListener = listener;
    }

    // ── Long-click listener interface (used by WorkoutPlannerActivity
    //    to show Edit / Delete options on long-press) ──────────────────
    public interface OnWorkoutLongClickListener {
        void onWorkoutLongClick(WorkoutEntity workout);
    }

    public void setOnWorkoutLongClickListener(OnWorkoutLongClickListener listener) {
        this.longClickListener = listener;
    }

    // ── RecyclerView boilerplate ──────────────────────────────────────

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
        // Both listeners are passed explicitly so the static ViewHolder
        // never needs to reference the outer adapter instance.
        holder.bind(workout, clickListener, longClickListener);
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public void setWorkouts(List<WorkoutEntity> list) {
        this.workouts.clear();
        if (list != null) this.workouts.addAll(list);
        notifyDataSetChanged();
    }

    /** Used by WorkoutPlannerActivity swipe-to-delete / long-press delete. */
    public WorkoutEntity getWorkoutAt(int position) {
        return workouts.get(position);
    }

    // ── ViewHolder ────────────────────────────────────────────────────

    static class WorkoutSessionViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvSessionWorkoutName;
        private final TextView tvSessionDate;
        private final TextView tvSessionDuration;
        private final TextView tvSessionStatus;

        WorkoutSessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSessionWorkoutName = itemView.findViewById(R.id.tvSessionWorkoutName);
            tvSessionDate        = itemView.findViewById(R.id.tvSessionDate);
            tvSessionDuration    = itemView.findViewById(R.id.tvSessionDuration);
            tvSessionStatus      = itemView.findViewById(R.id.tvSessionStatus);
        }

        void bind(WorkoutEntity workout,
                  OnWorkoutClickListener clickListener,
                  OnWorkoutLongClickListener longClickListener) {

            tvSessionWorkoutName.setText(workout.getName());

            String dateStr = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    .format(new Date(workout.getCreatedAt()));
            tvSessionDate.setText(dateStr);
            tvSessionDuration.setText(workout.getEstimatedDurationMinutes() + " min");
            tvSessionStatus.setText(workout.getTargetMuscleGroup());

            // Single tap — navigate to tracking
            itemView.setOnClickListener(clickListener != null
                    ? v -> clickListener.onWorkoutClick(workout)
                    : null);

            // Long press — show Edit / Delete dialog in WorkoutPlannerActivity
            itemView.setOnLongClickListener(longClickListener != null
                    ? v -> { longClickListener.onWorkoutLongClick(workout); return true; }
                    : null);
        }
    }
}