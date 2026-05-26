package com.arcadefitness.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.arcadefitness.data.local.entity.ExerciseEntity;
import com.arcadefitness.data.local.entity.WorkoutEntity;
import com.arcadefitness.data.local.repository.ExerciseRepository;
import com.arcadefitness.data.local.repository.WorkoutRepository;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final ExerciseRepository exerciseRepository;
    private final WorkoutRepository workoutRepository;

    private final LiveData<List<ExerciseEntity>> allExercises;
    private final LiveData<List<WorkoutEntity>> allWorkouts;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        exerciseRepository = new ExerciseRepository(application);
        workoutRepository = new WorkoutRepository(application);
        allExercises = exerciseRepository.getAllExercises();
        allWorkouts = workoutRepository.getAllWorkouts();
    }

    public LiveData<List<ExerciseEntity>> getAllExercises() {
        return allExercises;
    }

    public LiveData<List<WorkoutEntity>> getAllWorkouts() {
        return allWorkouts;
    }

    public ExerciseRepository getExerciseRepository() {
        return exerciseRepository;
    }

    public WorkoutRepository getWorkoutRepository() {
        return workoutRepository;
    }
}
