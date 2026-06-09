package com.arcadefitness.data.remote.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SyncBatchRequest {

    @SerializedName("entries")
    public List<SyncEntry> entries;

    public SyncBatchRequest(List<SyncEntry> entries) {
        this.entries = entries;
    }

    public static class SyncEntry {
        @SerializedName("local_id")
        public long localId;

        @SerializedName("table_name")
        public String tableName;

        @SerializedName("record_id")
        public long recordId;

        @SerializedName("operation_type")
        public String operationType;

        @SerializedName("payload")
        public String payload;

        @SerializedName("created_at")
        public long createdAt;

        public SyncEntry(long localId, String tableName, long recordId,
                         String operationType, String payload, long createdAt) {
            this.localId = localId;
            this.tableName = tableName;
            this.recordId = recordId;
            this.operationType = operationType;
            this.payload = payload;
            this.createdAt = createdAt;
        }
    }
}