package com.arcadefitness.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.arcadefitness.data.local.entity.ExerciseEntity;
import com.arcadefitness.data.local.entity.WorkoutEntity;
import com.arcadefitness.data.local.entity.WorkoutSessionEntity;
import com.arcadefitness.data.local.repository.ExerciseRepository;
import com.arcadefitness.data.local.repository.WorkoutRepository;

import java.util.List;

public class WorkoutPlannerViewModel extends AndroidViewModel {

    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;

    private final LiveData<List<WorkoutEntity>> allWorkouts;
    private final LiveData<List<ExerciseEntity>> allExercises;

    public WorkoutPlannerViewModel(@NonNull Application application) {
        super(application);
        workoutRepository = new WorkoutRepository(application);
        exerciseRepository = new ExerciseRepository(application);
        allWorkouts = workoutRepository.getAllWorkouts();
        allExercises = exerciseRepository.getAllExercises();
    }

    public LiveData<List<WorkoutEntity>> getAllWorkouts() {
        return allWorkouts;
    }

    public LiveData<List<ExerciseEntity>> getAllExercises() {
        return allExercises;
    }

    public WorkoutRepository getWorkoutRepository() {
        return workoutRepository;
    }

    public ExerciseRepository getExerciseRepository() {
        return exerciseRepository;
    }
}
