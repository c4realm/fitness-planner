package com.arcadefitness.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "exercises",
    indices = {
        @Index(value = {"remote_id"}, unique = true),
        @Index(value = {"target_muscle_group"}),
        @Index(value = {"name"})
    }
)
public class ExerciseEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "target_muscle_group")
    private String targetMuscleGroup;

    @ColumnInfo(name = "default_sets", defaultValue = "3")
    private int defaultSets;

    @ColumnInfo(name = "default_reps", defaultValue = "10")
    private int defaultReps;

    @ColumnInfo(name = "thumbnail_url")
    private String thumbnailUrl;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    private int isSynced;

    @ColumnInfo(name = "remote_id")
    private String remoteId;

    public ExerciseEntity() {
        this.createdAt = System.currentTimeMillis();
        this.isSynced = 0;
        this.defaultSets = 3;
        this.defaultReps = 10;
    }

    @Ignore
    public ExerciseEntity(String name, String description, String targetMuscleGroup) {
        this();
        this.name = name;
        this.description = description;
        this.targetMuscleGroup = targetMuscleGroup;
    }

    @Ignore
    public ExerciseEntity(String name, String description, String targetMuscleGroup,
                          int defaultSets, int defaultReps) {
        this(name, description, targetMuscleGroup);
        this.defaultSets = defaultSets;
        this.defaultReps = defaultReps;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTargetMuscleGroup() {
        return targetMuscleGroup;
    }

    public void setTargetMuscleGroup(String targetMuscleGroup) {
        this.targetMuscleGroup = targetMuscleGroup;
    }

    public int getDefaultSets() {
        return defaultSets;
    }

    public void setDefaultSets(int defaultSets) {
        this.defaultSets = defaultSets;
    }

    public int getDefaultReps() {
        return defaultReps;
    }

    public void setDefaultReps(int defaultReps) {
        this.defaultReps = defaultReps;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
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
