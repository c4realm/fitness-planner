package com.arcadefitness.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.arcadefitness.data.local.entity.GoalEntity;
import com.arcadefitness.data.local.entity.WorkoutSessionEntity;
import com.arcadefitness.data.local.repository.WorkoutRepository;
import com.arcadefitness.data.repository.FitnessRepository;

import java.util.List;

public class ProgressViewModel extends AndroidViewModel {

    private final FitnessRepository fitnessRepository;
    private final WorkoutRepository workoutRepository;

    private final MutableLiveData<List<GoalEntity>> goals = new MutableLiveData<>();
    private final MutableLiveData<List<WorkoutSessionEntity>> completedSessions = new MutableLiveData<>();
    private final MutableLiveData<int[]> weeklyStats = new MutableLiveData<>(new int[]{0, 0, 0, 0});

    public ProgressViewModel(@NonNull Application application) {
        super(application);
        fitnessRepository = FitnessRepository.getInstance(application);
        workoutRepository = new WorkoutRepository(application);
        loadData();
    }

    public LiveData<List<GoalEntity>> getGoals() {
        return goals;
    }

    public LiveData<List<WorkoutSessionEntity>> getCompletedSessions() {
        return completedSessions;
    }

    public LiveData<int[]> getWeeklyStats() {
        return weeklyStats;
    }

    public void loadData() {
        fitnessRepository.getActiveGoals(new FitnessRepository.RepositoryCallback<List<GoalEntity>>() {
            @Override
            public void onSuccess(List<GoalEntity> result) {
                goals.postValue(result);
            }

            @Override
            public void onError(String errorMessage) {
            }
        });

        fitnessRepository.getCompletedSessions(new FitnessRepository.RepositoryCallback<List<WorkoutSessionEntity>>() {
            @Override
            public void onSuccess(List<WorkoutSessionEntity> result) {
                completedSessions.postValue(result);
            }

            @Override
            public void onError(String errorMessage) {
            }
        });

        fitnessRepository.getWeeklyStats(new FitnessRepository.RepositoryCallback<int[]>() {
            @Override
            public void onSuccess(int[] result) {
                weeklyStats.postValue(result);
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    public void refresh() {
        loadData();
    }
}
