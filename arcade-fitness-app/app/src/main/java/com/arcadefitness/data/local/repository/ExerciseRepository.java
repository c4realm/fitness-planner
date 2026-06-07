package com.arcadefitness.data.local.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.arcadefitness.data.local.AppDatabase;
import com.arcadefitness.data.local.dao.ExerciseDao;
import com.arcadefitness.data.local.entity.ExerciseEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class ExerciseRepository {

    private final ExerciseDao exerciseDao;
    private final ExecutorService executor;

    public ExerciseRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.exerciseDao = db.exerciseDao();
        this.executor = AppDatabase.getDatabaseWriteExecutor();
    }

    public LiveData<List<ExerciseEntity>> getAllExercises() {
        return exerciseDao.getAllLiveData();
    }

    public LiveData<ExerciseEntity> getExerciseById(int id) {
        return exerciseDao.getByIdLiveData(id);
    }

    public LiveData<List<ExerciseEntity>> getExercisesByMuscleGroup(String muscleGroup) {
        return exerciseDao.getByMuscleGroupLiveData(muscleGroup);
    }

    public LiveData<List<ExerciseEntity>> searchExercises(String query) {
        return exerciseDao.searchLiveData(query);
    }

    public void insert(ExerciseEntity exercise, final Runnable onSuccess) {
        executor.execute(() -> {
            exerciseDao.insert(exercise);
            if (onSuccess != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onSuccess);
            }
        });
    }

    public void update(ExerciseEntity exercise, final Runnable onSuccess) {
        executor.execute(() -> {
            exerciseDao.update(exercise);
            if (onSuccess != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onSuccess);
            }
        });
    }

    public void delete(ExerciseEntity exercise, final Runnable onSuccess) {
        executor.execute(() -> {
            exerciseDao.delete(exercise);
            if (onSuccess != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onSuccess);
            }
        });
    }

    public void deleteAll(final Runnable onSuccess) {
        executor.execute(() -> {
            exerciseDao.deleteAll();
            if (onSuccess != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onSuccess);
            }
        });
    }
}
