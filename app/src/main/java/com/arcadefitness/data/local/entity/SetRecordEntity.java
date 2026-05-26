package com.arcadefitness.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
    tableName = "set_records",
    foreignKeys = {
        @ForeignKey(
            entity = WorkoutEntity.class,
            parentColumns = "id",
            childColumns = "workout_id",
            onDelete = CASCADE
        ),
        @ForeignKey(
            entity = ExerciseEntity.class,
            parentColumns = "id",
            childColumns = "exercise_id",
            onDelete = CASCADE
        )
    },
    indices = {
        @Index(value = {"workout_id"}),
        @Index(value = {"exercise_id"}),
        @Index(value = {"remote_id"}, unique = true),
        @Index(value = {"is_synced"})
    }
)
public class SetRecordEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "workout_id")
    private int workoutId;

    @ColumnInfo(name = "exercise_id")
    private int exerciseId;

    @ColumnInfo(name = "set_number")
    private int setNumber;

    @ColumnInfo(name = "weight")
    private double weight;

    @ColumnInfo(name = "reps")
    private int reps;

    @ColumnInfo(name = "is_completed", defaultValue = "0")
    private int isCompleted;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    private int isSynced;

    @ColumnInfo(name = "remote_id")
    private String remoteId;

    public SetRecordEntity() {
        this.timestamp = System.currentTimeMillis();
        this.isCompleted = 0;
        this.isSynced = 0;
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

    public int getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public int getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(int setNumber) {
        this.setNumber = setNumber;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(int isCompleted) {
        this.isCompleted = isCompleted;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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
