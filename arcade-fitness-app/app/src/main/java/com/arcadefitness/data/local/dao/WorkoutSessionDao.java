package com.arcadefitness.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.arcadefitness.data.local.entity.WorkoutSessionEntity;

import java.util.List;

@Dao
public interface WorkoutSessionDao {

    @Query("SELECT * FROM workout_sessions ORDER BY start_timestamp DESC")
    List<WorkoutSessionEntity> getAll();

    @Query("SELECT * FROM workout_sessions WHERE owner_id = :ownerId ORDER BY start_timestamp DESC")
    LiveData<List<WorkoutSessionEntity>> getAllLiveData(String ownerId);

    @Query("SELECT * FROM workout_sessions WHERE id = :id LIMIT 1")
    WorkoutSessionEntity getById(int id);

    @Query("SELECT * FROM workout_sessions WHERE id = :id LIMIT 1")
    LiveData<WorkoutSessionEntity> getByIdLiveData(int id);

    @Query("SELECT * FROM workout_sessions WHERE workout_id = :workoutId ORDER BY start_timestamp DESC")
    List<WorkoutSessionEntity> getByWorkoutId(int workoutId);

    @Query("SELECT * FROM workout_sessions WHERE workout_id = :workoutId ORDER BY start_timestamp DESC")
    LiveData<List<WorkoutSessionEntity>> getByWorkoutIdLiveData(int workoutId);

    @Query("SELECT * FROM workout_sessions WHERE status = :status ORDER BY start_timestamp DESC")
    List<WorkoutSessionEntity> getByStatus(String status);

    @Query("SELECT * FROM workout_sessions WHERE status = :status ORDER BY start_timestamp DESC")
    LiveData<List<WorkoutSessionEntity>> getByStatusLiveData(String status);

    @Query("SELECT * FROM workout_sessions WHERE status = 'IN_PROGRESS' AND owner_id = :ownerId LIMIT 1")
    WorkoutSessionEntity getCurrentSession(String ownerId);

    @Query("SELECT * FROM workout_sessions WHERE status = 'IN_PROGRESS' AND owner_id = :ownerId LIMIT 1")
    LiveData<WorkoutSessionEntity> getCurrentSessionLiveData(String ownerId);

    @Query("SELECT * FROM workout_sessions WHERE status = 'COMPLETED' ORDER BY end_timestamp DESC")
    LiveData<List<WorkoutSessionEntity>> getCompletedSessionsLiveData();

    @Query("SELECT * FROM workout_sessions WHERE status = 'COMPLETED' AND owner_id = :ownerId ORDER BY end_timestamp DESC")
    List<WorkoutSessionEntity> getCompletedSessions(String ownerId);

    @Query("SELECT * FROM workout_sessions WHERE status = 'COMPLETED' AND owner_id = :ownerId AND start_timestamp >= :fromTimestamp ORDER BY start_timestamp DESC")
    List<WorkoutSessionEntity> getCompletedSince(String ownerId, long fromTimestamp);

    @Query("SELECT * FROM workout_sessions WHERE start_timestamp >= :fromTimestamp ORDER BY start_timestamp DESC")
    LiveData<List<WorkoutSessionEntity>> getSessionsSinceLiveData(long fromTimestamp);

    @Query("SELECT * FROM workout_sessions WHERE is_synced = 0 ORDER BY created_at ASC")
    List<WorkoutSessionEntity> getUnsynced();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(WorkoutSessionEntity session);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<WorkoutSessionEntity> sessions);

    @Update
    void update(WorkoutSessionEntity session);

    @Delete
    void delete(WorkoutSessionEntity session);

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM workout_sessions WHERE workout_id = :workoutId")
    void deleteByWorkoutId(int workoutId);

    @Query("DELETE FROM workout_sessions")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM workout_sessions")
    int getCount();

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE status = 'COMPLETED' AND owner_id = :ownerId")
    int getCompletedCount(String ownerId);

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE status = 'COMPLETED' AND owner_id = :ownerId AND end_timestamp >= :fromTimestamp")
    int getCompletedCountSince(String ownerId, long fromTimestamp);

    @Query("SELECT SUM(duration_minutes) FROM workout_sessions WHERE status = 'COMPLETED' AND owner_id = :ownerId AND end_timestamp >= :fromTimestamp")
    Integer getTotalDurationSince(String ownerId, long fromTimestamp);

    @Query("SELECT SUM(calories_burned) FROM workout_sessions WHERE status = 'COMPLETED' AND owner_id = :ownerId AND end_timestamp >= :fromTimestamp")
    Integer getTotalCaloriesSince(String ownerId, long fromTimestamp);

    @Query("SELECT SUM(total_volume) FROM workout_sessions WHERE status = 'COMPLETED' AND owner_id = :ownerId AND end_timestamp >= :fromTimestamp")
    Double getTotalVolumeSince(String ownerId, long fromTimestamp);

    @Query("UPDATE workout_sessions SET is_synced = 1, remote_id = :remoteId WHERE id = :id")
    void markSynced(int id, String remoteId);

    @Query("UPDATE workout_sessions SET is_synced = 0 WHERE id = :id")
    void markUnsynced(int id);

    @Query("SELECT DISTINCT date(start_timestamp / 1000, 'unixepoch') as session_date FROM workout_sessions WHERE status = 'COMPLETED' AND owner_id = :ownerId ORDER BY session_date DESC")
    List<String> getCompletedSessionDates(String ownerId);
}
