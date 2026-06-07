package com.arcadefitness.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.arcadefitness.data.local.entity.SyncQueueEntryEntity;

import java.util.List;

@Dao
public interface SyncQueueDao {

    @Query("SELECT * FROM sync_queue ORDER BY created_at ASC")
    List<SyncQueueEntryEntity> getAll();

    @Query("SELECT * FROM sync_queue WHERE status = :status ORDER BY created_at ASC")
    List<SyncQueueEntryEntity> getByStatus(String status);

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY created_at ASC LIMIT :limit")
    List<SyncQueueEntryEntity> getPending(int limit);

    @Query("SELECT * FROM sync_queue WHERE id = :id LIMIT 1")
    SyncQueueEntryEntity getById(int id);

    @Query("SELECT * FROM sync_queue WHERE status IN ('PENDING', 'FAILED') ORDER BY created_at ASC")
    List<SyncQueueEntryEntity> getPendingAndFailed();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SyncQueueEntryEntity entry);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<SyncQueueEntryEntity> entries);

    @Update
    void update(SyncQueueEntryEntity entry);

    @Delete
    void delete(SyncQueueEntryEntity entry);

    @Query("DELETE FROM sync_queue WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM sync_queue WHERE status = 'COMPLETED'")
    void deleteCompleted();

    @Query("DELETE FROM sync_queue WHERE status = 'FAILED' AND retry_count >= :maxRetries")
    void deleteFailedAboveMaxRetries(int maxRetries);

    @Query("DELETE FROM sync_queue")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING'")
    int getPendingCount();

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'FAILED'")
    int getFailedCount();

    @Query("UPDATE sync_queue SET status = 'IN_PROGRESS' WHERE id = :id")
    void markInProgress(int id);

    @Query("UPDATE sync_queue SET status = 'COMPLETED' WHERE id = :id")
    void markCompleted(int id);

    @Query("UPDATE sync_queue SET status = 'FAILED', retry_count = retry_count + 1, error_message = :errorMessage WHERE id = :id")
    void markFailed(int id, String errorMessage);

    @Query("UPDATE sync_queue SET status = 'PENDING', retry_count = 0, error_message = NULL WHERE id = :id")
    void resetForRetry(int id);
}
