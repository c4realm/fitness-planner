package com.arcadefitness.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.arcadefitness.data.local.entity.WorkoutEntity;

import java.util.List;

@Dao
public interface WorkoutDao {

    @Query("SELECT * FROM workouts ORDER BY updated_at DESC")
    List<WorkoutEntity> getAll();

    @Query("SELECT * FROM workouts ORDER BY updated_at DESC")
    LiveData<List<WorkoutEntity>> getAllLiveData();

    @Query("SELECT * FROM workouts WHERE id = :id LIMIT 1")
    WorkoutEntity getById(int id);

    @Query("SELECT * FROM workouts WHERE id = :id LIMIT 1")
    LiveData<WorkoutEntity> getByIdLiveData(int id);

    @Query("SELECT * FROM workouts WHERE name LIKE :name ORDER BY updated_at DESC")
    List<WorkoutEntity> getByName(String name);

    @Query("SELECT * FROM workouts WHERE name LIKE :name ORDER BY updated_at DESC")
    LiveData<List<WorkoutEntity>> getByNameLiveData(String name);

    @Query("SELECT * FROM workouts WHERE target_muscle_group = :muscleGroup ORDER BY updated_at DESC")
    List<WorkoutEntity> getByTargetMuscleGroup(String muscleGroup);

    @Query("SELECT * FROM workouts WHERE target_muscle_group = :muscleGroup ORDER BY updated_at DESC")
    LiveData<List<WorkoutEntity>> getByTargetMuscleGroupLiveData(String muscleGroup);

    @Query("SELECT * FROM workouts WHERE is_synced = 0 ORDER BY updated_at ASC")
    List<WorkoutEntity> getUnsynced();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(WorkoutEntity workout);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<WorkoutEntity> workouts);

    @Update
    void update(WorkoutEntity workout);

    @Delete
    void delete(WorkoutEntity workout);

    @Query("DELETE FROM workouts WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM workouts")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM workouts")
    int getCount();

    @Query("UPDATE workouts SET is_synced = 1, remote_id = :remoteId WHERE id = :id")
    void markSynced(int id, String remoteId);

    @Query("UPDATE workouts SET is_synced = 0 WHERE id = :id")
    void markUnsynced(int id);
}
