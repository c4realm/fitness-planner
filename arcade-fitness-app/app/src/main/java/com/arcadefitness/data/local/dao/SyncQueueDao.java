package com.arcadefitness.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.arcadefitness.data.local.entity.SyncQueueEntryEntity;

import java.util.List;

@Dao
public interface SyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SyncQueueEntryEntity entry);

    @Query("SELECT * FROM sync_queue " +
            "WHERE status != 'SYNCED' " +
            "AND retry_count < :maxRetries " +
            "ORDER BY created_at ASC")
    List<SyncQueueEntryEntity> getPendingEntries(int maxRetries);

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING'")
    LiveData<Integer> getPendingCount();

    @Query("UPDATE sync_queue SET status = 'SYNCED' WHERE id = :id")
    void markSynced(long id);

    @Query("UPDATE sync_queue " +
            "SET retry_count = retry_count + 1, " +
            "    status = CASE WHEN retry_count + 1 >= 3 THEN 'FAILED' ELSE status END " +
            "WHERE id = :id")
    void incrementAttempt(long id);

    @Query("DELETE FROM sync_queue WHERE status = 'SYNCED'")
    void deleteSynced();

    @Query("DELETE FROM sync_queue WHERE status = 'FAILED'")
    void deleteFailed();

    @Query("DELETE FROM sync_queue")
    void deleteAll();
}