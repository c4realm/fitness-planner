package com.arcadefitness.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arcadefitness.R;
import com.arcadefitness.data.local.entity.ExerciseEntity;

import java.util.ArrayList;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private final List<ExerciseEntity> exercises = new ArrayList<>();

    public interface OnExerciseClickListener {
        void onExerciseClick(ExerciseEntity exercise);
    }

    private OnExerciseClickListener clickListener;

    public void setOnExerciseClickListener(OnExerciseClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        ExerciseEntity exercise = exercises.get(position);
        holder.bind(exercise);
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onExerciseClick(exercise);
        });
    }

    @Override
    public int getItemCount() { return exercises.size(); }

    public void setExercises(List<ExerciseEntity> list) {
        this.exercises.clear();
        if (list != null) this.exercises.addAll(list);
        notifyDataSetChanged();
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvMuscleIcon;
        private final TextView tvExerciseName;
        private final TextView tvMuscleGroup;
        private final TextView tvDescription;
        private final TextView tvDefaultSets;
        private final TextView tvDefaultReps;

        ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMuscleIcon   = itemView.findViewById(R.id.tvMuscleIcon);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvMuscleGroup  = itemView.findViewById(R.id.tvMuscleGroup);
            tvDescription  = itemView.findViewById(R.id.tvDescription);
            tvDefaultSets  = itemView.findViewById(R.id.tvDefaultSets);
            tvDefaultReps  = itemView.findViewById(R.id.tvDefaultReps);
        }

        void bind(ExerciseEntity exercise) {
            tvExerciseName.setText(exercise.getName());
            tvMuscleGroup.setText(exercise.getTargetMuscleGroup());
            tvDescription.setText(exercise.getDescription());
            tvDefaultSets.setText(String.valueOf(exercise.getDefaultSets()));
            tvDefaultReps.setText(exercise.getDefaultReps() + " reps");
            tvMuscleIcon.setText(getMuscleEmoji(exercise.getTargetMuscleGroup()));
        }

        private String getMuscleEmoji(String muscleGroup) {
            if (muscleGroup == null) return "💪";
            switch (muscleGroup) {
                case "Chest":     return "🫁";
                case "Back":      return "🔙";
                case "Shoulders": return "🏋️";
                case "Legs":      return "🦵";
                case "Arms":      return "💪";
                case "Core":      return "⚡";
                case "Full Body": return "🔥";
                default:          return "💪";
            }
        }
    }
}
