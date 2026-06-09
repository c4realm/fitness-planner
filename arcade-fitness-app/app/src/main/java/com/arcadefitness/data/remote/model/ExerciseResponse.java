package com.arcadefitness.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class ExerciseResponse {
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("target_muscle_group")
    public String targetMuscleGroup;

    @SerializedName("default_sets")
    public int defaultSets;

    @SerializedName("default_reps")
    public int defaultReps;

    @SerializedName("thumbnail_url")
    public String thumbnailUrl;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("remote_id")
    public String remoteId;
}