package com.arcadefitness.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.arcadefitness.data.local.entity.UserProfileEntity;

import java.util.List;

@Dao
public interface UserProfileDao {

    @Query("SELECT * FROM user_profiles ORDER BY display_name ASC")
    List<UserProfileEntity> getAll();

    @Query("SELECT * FROM user_profiles ORDER BY display_name ASC")
    LiveData<List<UserProfileEntity>> getAllLiveData();

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    UserProfileEntity getById(int id);

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    LiveData<UserProfileEntity> getByIdLiveData(int id);

    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    UserProfileEntity getByEmail(String email);

    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    LiveData<UserProfileEntity> getByEmailLiveData(String email);

    @Query("SELECT * FROM user_profiles WHERE is_synced = 0 ORDER BY updated_at ASC")
    List<UserProfileEntity> getUnsynced();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UserProfileEntity profile);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<UserProfileEntity> profiles);

    @Update
    void update(UserProfileEntity profile);

    @Delete
    void delete(UserProfileEntity profile);

    @Query("DELETE FROM user_profiles WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM user_profiles")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM user_profiles")
    int getCount();

    @Query("UPDATE user_profiles SET is_synced = 1, remote_id = :remoteId WHERE id = :id")
    void markSynced(int id, String remoteId);

    @Query("UPDATE user_profiles SET is_synced = 0 WHERE id = :id")
    void markUnsynced(int id);
}
