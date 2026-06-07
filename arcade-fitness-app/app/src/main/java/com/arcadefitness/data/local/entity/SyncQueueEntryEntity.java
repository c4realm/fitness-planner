package com.arcadefitness.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "sync_queue",
    indices = {
        @Index(value = {"status"}),
        @Index(value = {"table_name", "record_id"})
    }
)
public class SyncQueueEntryEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "table_name")
    private String tableName;

    @ColumnInfo(name = "record_id")
    private int recordId;

    @ColumnInfo(name = "operation_type")
    private String operationType;

    @ColumnInfo(name = "payload")
    private String payload;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "retry_count", defaultValue = "0")
    private int retryCount;

    @ColumnInfo(name = "error_message")
    private String errorMessage;

    public SyncQueueEntryEntity() {
        this.createdAt = System.currentTimeMillis();
        this.status = "PENDING";
        this.retryCount = 0;
    }

    public SyncQueueEntryEntity(String tableName, int recordId,
                                String operationType, String payload) {
        this();
        this.tableName = tableName;
        this.recordId = recordId;
        this.operationType = operationType;
        this.payload = payload;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
