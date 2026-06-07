package com.arcadefitness.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.arcadefitness.data.local.entity.GoalEntity;

import java.util.List;

@Dao
public interface GoalDao {

    @Query("SELECT * FROM goals ORDER BY created_at DESC")
    List<GoalEntity> getAll();

    @Query("SELECT * FROM goals ORDER BY created_at DESC")
    LiveData<List<GoalEntity>> getAllLiveData();

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    GoalEntity getById(int id);

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    LiveData<GoalEntity> getByIdLiveData(int id);

    @Query("SELECT * FROM goals WHERE status = :status ORDER BY created_at DESC")
    List<GoalEntity> getByStatus(String status);

    @Query("SELECT * FROM goals WHERE status = :status ORDER BY created_at DESC")
    LiveData<List<GoalEntity>> getByStatusLiveData(String status);

    @Query("SELECT * FROM goals WHERE type = :type ORDER BY created_at DESC")
    List<GoalEntity> getByType(String type);

    @Query("SELECT * FROM goals WHERE type = :type ORDER BY created_at DESC")
    LiveData<List<GoalEntity>> getByTypeLiveData(String type);

    @Query("SELECT * FROM goals WHERE status = 'ACTIVE' ORDER BY target_date ASC")
    LiveData<List<GoalEntity>> getActiveGoalsLiveData();

    @Query("SELECT * FROM goals WHERE status = 'ACTIVE' ORDER BY target_date ASC")
    List<GoalEntity> getActiveGoals();

    @Query("SELECT * FROM goals WHERE is_synced = 0 ORDER BY updated_at ASC")
    List<GoalEntity> getUnsynced();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(GoalEntity goal);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<GoalEntity> goals);

    @Update
    void update(GoalEntity goal);

    @Delete
    void delete(GoalEntity goal);

    @Query("DELETE FROM goals WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM goals")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM goals")
    int getCount();

    @Query("SELECT COUNT(*) FROM goals WHERE status = 'ACTIVE'")
    int getActiveCount();

    @Query("UPDATE goals SET current_value = :value, updated_at = :updatedAt WHERE id = :id")
    void updateProgress(int id, double value, long updatedAt);

    @Query("UPDATE goals SET status = :status, updated_at = :updatedAt WHERE id = :id")
    void updateStatus(int id, String status, long updatedAt);

    @Query("UPDATE goals SET is_synced = 1, remote_id = :remoteId WHERE id = :id")
    void markSynced(int id, String remoteId);

    @Query("UPDATE goals SET is_synced = 0 WHERE id = :id")
    void markUnsynced(int id);
}
