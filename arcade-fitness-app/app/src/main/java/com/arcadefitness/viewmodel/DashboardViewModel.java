package com.arcadefitness.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.arcadefitness.data.local.entity.ExerciseEntity;
import com.arcadefitness.data.local.entity.WorkoutEntity;
import com.arcadefitness.data.local.repository.ExerciseRepository;
import com.arcadefitness.data.local.repository.WorkoutRepository;
import com.arcadefitness.data.repository.FitnessRepository;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final ExerciseRepository exerciseRepository;
    private final WorkoutRepository  workoutRepository;
    private final FitnessRepository  fitnessRepository;

    private final LiveData<List<ExerciseEntity>> allExercises;
    private final LiveData<List<WorkoutEntity>>  allWorkouts;

    // Stats — loaded once on init, refreshed on onResume via loadWeeklyStats()
    private final androidx.lifecycle.MutableLiveData<Integer> weeklySessionCount  = new androidx.lifecycle.MutableLiveData<>(0);
    private final androidx.lifecycle.MutableLiveData<Integer> weeklyCalories      = new androidx.lifecycle.MutableLiveData<>(0);
    private final androidx.lifecycle.MutableLiveData<Integer> weeklyDuration      = new androidx.lifecycle.MutableLiveData<>(0);

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        exerciseRepository = new ExerciseRepository(application);
        workoutRepository  = new WorkoutRepository(application);
        fitnessRepository  = FitnessRepository.getInstance(application);
        allExercises = exerciseRepository.getAllExercises();
        allWorkouts  = workoutRepository.getAllWorkouts();
        loadWeeklyStats();
    }

    public LiveData<List<ExerciseEntity>> getAllExercises() { return allExercises; }
    public LiveData<List<WorkoutEntity>>  getAllWorkouts()  { return allWorkouts; }
    public LiveData<Integer> getWeeklySessionCount()       { return weeklySessionCount; }
    public LiveData<Integer> getWeeklyCalories()           { return weeklyCalories; }
    public LiveData<Integer> getWeeklyDuration()           { return weeklyDuration; }
    public ExerciseRepository getExerciseRepository()      { return exerciseRepository; }
    public WorkoutRepository  getWorkoutRepository()       { return workoutRepository; }

    /** Call from Activity.onResume() to keep stats fresh. */
    public void loadWeeklyStats() {
        fitnessRepository.getWeeklyStats(new FitnessRepository.RepositoryCallback<int[]>() {
            @Override
            public void onSuccess(int[] stats) {
                // stats[0]=sessions, stats[1]=duration, stats[2]=calories
                weeklySessionCount.postValue(stats[0]);
                weeklyDuration.postValue(stats[1]);
                weeklyCalories.postValue(stats[2]);
            }
            @Override
            public void onError(String errorMessage) {
                weeklySessionCount.postValue(0);
                weeklyDuration.postValue(0);
                weeklyCalories.postValue(0);
            }
        });
    }
}
