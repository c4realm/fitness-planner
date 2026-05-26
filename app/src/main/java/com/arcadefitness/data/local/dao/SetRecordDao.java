package com.arcadefitness.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.arcadefitness.data.local.entity.SetRecordEntity;

import java.util.List;

@Dao
public interface SetRecordDao {

    @Query("SELECT * FROM set_records WHERE workout_id = :workoutId ORDER BY set_number ASC")
    List<SetRecordEntity> getByWorkoutId(int workoutId);

    @Query("SELECT * FROM set_records WHERE workout_id = :workoutId AND exercise_id = :exerciseId ORDER BY set_number ASC")
    List<SetRecordEntity> getByWorkoutAndExercise(int workoutId, int exerciseId);

    @Query("SELECT * FROM set_records WHERE id = :id LIMIT 1")
    SetRecordEntity getById(int id);

    @Query("SELECT * FROM set_records WHERE is_completed = 1 AND workout_id = :workoutId ORDER BY set_number ASC")
    List<SetRecordEntity> getCompletedByWorkout(int workoutId);

    @Query("SELECT * FROM set_records WHERE is_synced = 0 ORDER BY timestamp ASC")
    List<SetRecordEntity> getUnsynced();

    @Query("SELECT * FROM set_records WHERE workout_id = :workoutId AND is_completed = 1")
    List<SetRecordEntity> getCompletedSetsForWorkout(int workoutId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SetRecordEntity setRecord);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<SetRecordEntity> setRecords);

    @Update
    void update(SetRecordEntity setRecord);

    @Delete
    void delete(SetRecordEntity setRecord);

    @Query("DELETE FROM set_records WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM set_records WHERE workout_id = :workoutId")
    void deleteByWorkoutId(int workoutId);

    @Query("DELETE FROM set_records")
    void deleteAll();

    @Query("UPDATE set_records SET is_completed = 1, weight = :weight, reps = :reps, timestamp = :timestamp WHERE id = :id")
    void markCompleted(int id, double weight, int reps, long timestamp);

    @Query("SELECT COUNT(*) FROM set_records WHERE workout_id = :workoutId")
    int getCountByWorkout(int workoutId);

    @Query("SELECT COUNT(*) FROM set_records WHERE workout_id = :workoutId AND is_completed = 1")
    int getCompletedCountByWorkout(int workoutId);

    @Query("SELECT SUM(weight * reps) FROM set_records WHERE workout_id = :workoutId AND is_completed = 1")
    Double getTotalVolumeByWorkout(int workoutId);

    @Query("UPDATE set_records SET is_synced = 1, remote_id = :remoteId WHERE id = :id")
    void markSynced(int id, String remoteId);

    @Query("UPDATE set_records SET is_synced = 0 WHERE id = :id")
    void markUnsynced(int id);
}
