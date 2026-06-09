package com.arcadefitness.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class WorkoutSessionResponse {
    @SerializedName("id")
    public String id;

    @SerializedName("workout_id")
    public String workoutId;

    @SerializedName("start_timestamp")
    public long startTimestamp;

    @SerializedName("end_timestamp")
    public long endTimestamp;

    @SerializedName("duration_minutes")
    public int durationMinutes;

    @SerializedName("calories_burned")
    public float caloriesBurned;

    @SerializedName("total_volume")
    public float totalVolume;

    @SerializedName("status")
    public String status;

    @SerializedName("notes")
    public String notes;

    @SerializedName("rating")
    public int rating;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("remote_id")
    public String remoteId;
}