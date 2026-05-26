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
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public void setExercises(List<ExerciseEntity> exercises) {
        this.exercises.clear();
        if (exercises != null) {
            this.exercises.addAll(exercises);
        }
        notifyDataSetChanged();
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvExerciseName;
        private final TextView tvMuscleGroup;
        private final TextView tvDefaultSets;
        private final TextView tvDefaultReps;

        ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvMuscleGroup = itemView.findViewById(R.id.tvMuscleGroup);
            tvDefaultSets = itemView.findViewById(R.id.tvDefaultSets);
            tvDefaultReps = itemView.findViewById(R.id.tvDefaultReps);
        }

        void bind(ExerciseEntity exercise) {
            tvExerciseName.setText(exercise.getName());
            tvMuscleGroup.setText(exercise.getTargetMuscleGroup());
            tvDefaultSets.setText(String.valueOf(exercise.getDefaultSets()));
            tvDefaultReps.setText(String.valueOf(exercise.getDefaultReps()));
        }
    }
}
