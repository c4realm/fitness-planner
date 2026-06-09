package com.arcadefitness.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class SetRecordResponse {
    @SerializedName("id")
    public String id;

    @SerializedName("workout_id")
    public String workoutId;

    @SerializedName("exercise_id")
    public String exerciseId;

    @SerializedName("set_number")
    public int setNumber;

    @SerializedName("weight")
    public float weight;

    @SerializedName("reps")
    public int reps;

    @SerializedName("is_completed")
    public boolean isCompleted;

    @SerializedName("timestamp")
    public long timestamp;

    @SerializedName("remote_id")
    public String remoteId;
}