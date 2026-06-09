package com.arcadefitness.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class UserProfileResponse {
    @SerializedName("id")
    public String id;

    @SerializedName("full_name")
    public String fullName;

    @SerializedName("email")
    public String email;

    @SerializedName("age")
    public int age;

    @SerializedName("gender")
    public String gender;

    @SerializedName("goal")
    public String goal;

    @SerializedName("profile_image_url")
    public String profileImageUrl;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;

    @SerializedName("remote_id")
    public String remoteId;
}