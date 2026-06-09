package com.arcadefitness.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class WorkoutResponse {
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("target_muscle_group")
    public String targetMuscleGroup;

    @SerializedName("estimated_duration_minutes")
    public int estimatedDurationMinutes;

    @SerializedName("exercise_count")
    public int exerciseCount;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;

    @SerializedName("remote_id")
    public String remoteId;
}