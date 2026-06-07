package com.arcadefitness.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.arcadefitness.data.local.entity.SetRecordEntity;
import com.arcadefitness.data.local.entity.WorkoutEntity;
import com.arcadefitness.data.local.entity.WorkoutSessionEntity;
import com.arcadefitness.data.local.repository.ExerciseRepository;
import com.arcadefitness.data.local.repository.WorkoutRepository;
import com.arcadefitness.data.repository.FitnessRepository;

import java.util.List;

public class WorkoutTrackingViewModel extends AndroidViewModel {

    private final FitnessRepository fitnessRepository;
    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;

    private final MutableLiveData<WorkoutSessionEntity> currentSession = new MutableLiveData<>(null);
    private final MutableLiveData<WorkoutEntity> currentWorkout = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> elapsedSeconds = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isRunning = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> completedSets = new MutableLiveData<>(0);

    public WorkoutTrackingViewModel(@NonNull Application application) {
        super(application);
        fitnessRepository = FitnessRepository.getInstance(application);
        workoutRepository = new WorkoutRepository(application);
        exerciseRepository = new ExerciseRepository(application);
    }

    public LiveData<WorkoutSessionEntity> getCurrentSession() {
        return currentSession;
    }

    public LiveData<WorkoutEntity> getCurrentWorkout() {
        return currentWorkout;
    }

    public LiveData<Integer> getElapsedSeconds() {
        return elapsedSeconds;
    }

    public LiveData<Boolean> getIsRunning() {
        return isRunning;
    }

    public LiveData<Integer> getCompletedSets() {
        return completedSets;
    }

    public void startSession(int workoutId) {
        fitnessRepository.getWorkoutById(workoutId, new FitnessRepository.RepositoryCallback<WorkoutEntity>() {
            @Override
            public void onSuccess(WorkoutEntity workout) {
                currentWorkout.postValue(workout);
                WorkoutSessionEntity session = new WorkoutSessionEntity(workoutId, "IN_PROGRESS");
                fitnessRepository.insertWorkoutSession(session, new FitnessRepository.RepositoryCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer sessionId) {
                        session.setId(sessionId);
                        currentSession.postValue(session);
                        isRunning.postValue(true);
                        elapsedSeconds.postValue(0);
                    }

                    @Override
                    public void onError(String errorMessage) {
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    public void pauseSession() {
        isRunning.postValue(false);
    }

    public void resumeSession() {
        isRunning.postValue(true);
    }

    public void tickSecond() {
        if (Boolean.TRUE.equals(isRunning.getValue())) {
            Integer current = elapsedSeconds.getValue();
            elapsedSeconds.postValue(current != null ? current + 1 : 1);
        }
    }

    public void completeSession(int rating, String notes) {
        WorkoutSessionEntity session = currentSession.getValue();
        if (session == null) return;

        int seconds = elapsedSeconds.getValue() != null ? elapsedSeconds.getValue() : 0;
        int minutes = seconds / 60;
        int calories = (int) (minutes * 7.5);

        fitnessRepository.completeWorkoutSession(session.getId(), minutes, calories, 0, rating, notes,
                new FitnessRepository.RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        session.setStatus("COMPLETED");
                        session.setDurationMinutes(minutes);
                        session.setCaloriesBurned(calories);
                        currentSession.postValue(session);
                        isRunning.postValue(false);
                    }

                    @Override
                    public void onError(String errorMessage) {
                    }
                });
    }

    public void loadCurrentSession() {
        fitnessRepository.getCurrentSession(new FitnessRepository.RepositoryCallback<WorkoutSessionEntity>() {
            @Override
            public void onSuccess(WorkoutSessionEntity session) {
                if (session != null) {
                    currentSession.postValue(session);
                    int elapsedMin = session.getDurationMinutes();
                    elapsedSeconds.postValue(elapsedMin * 60);
                    fitnessRepository.getWorkoutById(session.getWorkoutId(), new FitnessRepository.RepositoryCallback<WorkoutEntity>() {
                        @Override
                        public void onSuccess(WorkoutEntity workout) {
                            currentWorkout.postValue(workout);
                        }

                        @Override
                        public void onError(String errorMessage) {
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }
}
