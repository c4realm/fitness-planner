package com.arcadefitness.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("token")
    public String token;

    @SerializedName("user_id")
    public String userId;

    @SerializedName("email")
    public String email;

    @SerializedName("message")
    public String message;
}