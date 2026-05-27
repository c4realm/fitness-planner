package com.arcadefitness.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
    tableName = "workout_sessions",
    foreignKeys = {
        @ForeignKey(
            entity = WorkoutEntity.class,
            parentColumns = "id",
            childColumns = "workout_id",
            onDelete = CASCADE
        )
    },
    indices = {
        @Index(value = {"workout_id"}),
        @Index(value = {"status"}),
        @Index(value = {"remote_id"}, unique = true),
        @Index(value = {"is_synced"})
    }
)
public class WorkoutSessionEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "workout_id")
    private int workoutId;

    @ColumnInfo(name = "start_timestamp")
    private long startTimestamp;

    @ColumnInfo(name = "end_timestamp")
    private long endTimestamp;

    @ColumnInfo(name = "duration_minutes")
    private int durationMinutes;

    @ColumnInfo(name = "calories_burned")
    private int caloriesBurned;

    @ColumnInfo(name = "total_volume")
    private double totalVolume;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "notes")
    private String notes;

    @ColumnInfo(name = "rating")
    private int rating;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    private int isSynced;

    @ColumnInfo(name = "remote_id")
    private String remoteId;

    public WorkoutSessionEntity() {
        this.createdAt = System.currentTimeMillis();
        this.isSynced = 0;
        this.status = "IN_PROGRESS";
        this.startTimestamp = System.currentTimeMillis();
    }

    @Ignore
    public WorkoutSessionEntity(int workoutId, String status) {
        this();
        this.workoutId = workoutId;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(int workoutId) {
        this.workoutId = workoutId;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(int caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public double getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(double totalVolume) {
        this.totalVolume = totalVolume;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getIsSynced() {
        return isSynced;
    }

    public void setIsSynced(int isSynced) {
        this.isSynced = isSynced;
    }

    public String getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }
}
