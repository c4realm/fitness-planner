package com.arcadefitness.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.arcadefitness.data.local.entity.ExerciseEntity;

import java.util.List;

@Dao
public interface ExerciseDao {

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    List<ExerciseEntity> getAll();

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    LiveData<List<ExerciseEntity>> getAllLiveData();

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    ExerciseEntity getById(int id);

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    LiveData<ExerciseEntity> getByIdLiveData(int id);

    @Query("SELECT * FROM exercises WHERE target_muscle_group = :muscleGroup ORDER BY name ASC")
    List<ExerciseEntity> getByMuscleGroup(String muscleGroup);

    @Query("SELECT * FROM exercises WHERE target_muscle_group = :muscleGroup ORDER BY name ASC")
    LiveData<List<ExerciseEntity>> getByMuscleGroupLiveData(String muscleGroup);

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' OR target_muscle_group LIKE '%' || :query || '%' ORDER BY name ASC")
    List<ExerciseEntity> search(String query);

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' OR target_muscle_group LIKE '%' || :query || '%' ORDER BY name ASC")
    LiveData<List<ExerciseEntity>> searchLiveData(String query);

    @Query("SELECT * FROM exercises WHERE is_synced = 0 ORDER BY created_at ASC")
    List<ExerciseEntity> getUnsynced();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ExerciseEntity exercise);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<ExerciseEntity> exercises);

    @Update
    void update(ExerciseEntity exercise);

    @Delete
    void delete(ExerciseEntity exercise);

    @Query("DELETE FROM exercises WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM exercises")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM exercises")
    int getCount();

    @Query("UPDATE exercises SET is_synced = 1, remote_id = :remoteId WHERE id = :id")
    void markSynced(int id, String remoteId);

    @Query("UPDATE exercises SET is_synced = 0 WHERE id = :id")
    void markUnsynced(int id);
}
