package com.arcadefitness.data.remote.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SyncBatchResponse {

    @SerializedName("processed")
    public int processed;

    @SerializedName("failed")
    public int failed;

    @SerializedName("results")
    public List<SyncResult> results;

    public static class SyncResult {
        @SerializedName("local_id")
        public long localId;

        @SerializedName("remote_id")
        public String remoteId;

        @SerializedName("success")
        public boolean success;

        @SerializedName("error")
        public String error;
    }
}