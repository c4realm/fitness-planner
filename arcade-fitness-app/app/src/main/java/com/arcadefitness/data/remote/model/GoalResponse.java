package com.arcadefitness.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class GoalResponse {
    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("description")
    public String description;

    @SerializedName("type")
    public String type;

    @SerializedName("target_value")
    public float targetValue;

    @SerializedName("current_value")
    public float currentValue;

    @SerializedName("unit")
    public String unit;

    @SerializedName("start_date")
    public String startDate;

    @SerializedName("target_date")
    public String targetDate;

    @SerializedName("status")
    public String status;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;

    @SerializedName("remote_id")
    public String remoteId;
}