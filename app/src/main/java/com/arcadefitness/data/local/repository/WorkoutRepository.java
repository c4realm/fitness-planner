package com.arcadefitness.data.local.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.arcadefitness.data.local.AppDatabase;
import com.arcadefitness.data.local.dao.WorkoutDao;
import com.arcadefitness.data.local.entity.WorkoutEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class WorkoutRepository {

    private final WorkoutDao workoutDao;
    private final ExecutorService executor;

    public WorkoutRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.workoutDao = db.workoutDao();
        this.executor = AppDatabase.getDatabaseWriteExecutor();
    }

    public LiveData<List<WorkoutEntity>> getAllWorkouts() {
        return workoutDao.getAllLiveData();
    }

    public LiveData<WorkoutEntity> getWorkoutById(int id) {
        return workoutDao.getByIdLiveData(id);
    }

    public void insert(WorkoutEntity workout, final Runnable onSuccess) {
        executor.execute(() -> {
            workoutDao.insert(workout);
            if (onSuccess != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onSuccess);
            }
        });
    }

    public void update(WorkoutEntity workout, final Runnable onSuccess) {
        executor.execute(() -> {
            workoutDao.update(workout);
            if (onSuccess != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onSuccess);
            }
        });
    }

    public void delete(WorkoutEntity workout, final Runnable onSuccess) {
        executor.execute(() -> {
            workoutDao.delete(workout);
            if (onSuccess != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onSuccess);
            }
        });
    }

    public void deleteAll(final Runnable onSuccess) {
        executor.execute(() -> {
            workoutDao.deleteAll();
            if (onSuccess != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onSuccess);
            }
        });
    }
}
