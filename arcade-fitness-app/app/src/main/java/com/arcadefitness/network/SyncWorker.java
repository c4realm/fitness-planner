package com.arcadefitness.network;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.arcadefitness.data.local.AppDatabase;
import com.arcadefitness.data.local.entity.SyncQueueEntryEntity;
import com.arcadefitness.data.remote.ApiService;
import com.arcadefitness.data.remote.RetrofitClient;
import com.arcadefitness.data.remote.model.SyncBatchRequest;
import com.arcadefitness.data.remote.model.SyncBatchResponse;
import com.arcadefitness.utils.AppConstants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork() started");

        if (!NetworkChangeReceiver.isConnected(getApplicationContext())) {
            Log.d(TAG, "No connection — retrying later");
            return Result.retry();
        }

        try {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            ApiService api = RetrofitClient
                    .getInstance(getApplicationContext())
                    .getApiService();

            List<SyncQueueEntryEntity> pending = db.syncQueueDao()
                    .getPendingEntries(AppConstants.SYNC_MAX_RETRIES);

            if (pending == null || pending.isEmpty()) {
                Log.d(TAG, "Nothing to sync — queue empty");
                return Result.success();
            }

            Log.d(TAG, "Syncing " + pending.size() + " entries");

            int total = pending.size();
            int offset = 0;

            while (offset < total) {
                int end = Math.min(offset + AppConstants.SYNC_BATCH_SIZE, total);
                List<SyncQueueEntryEntity> batch = pending.subList(offset, end);
                offset = end;

                List<SyncBatchRequest.SyncEntry> entries = new ArrayList<>();
                for (SyncQueueEntryEntity e : batch) {
                    entries.add(new SyncBatchRequest.SyncEntry(
                            e.getId(),
                            e.getTableName(),
                            e.getRecordId(),
                            e.getOperationType(),   // was e.operation — now uses getter
                            e.getPayload(),
                            e.getCreatedAt()));
                }

                Response<SyncBatchResponse> response =
                        api.syncBatch(new SyncBatchRequest(entries)).execute();

                if (!response.isSuccessful()) {
                    Log.e(TAG, "Batch failed HTTP " + response.code());
                    markRetry(db, batch);
                    continue;
                }

                SyncBatchResponse result = response.body();
                if (result == null || result.results == null) {
                    Log.e(TAG, "Empty response body");
                    markRetry(db, batch);
                    continue;
                }

                for (SyncBatchResponse.SyncResult r : result.results) {
                    if (r.success) {
                        db.syncQueueDao().markSynced(r.localId);
                        Log.d(TAG, "Synced localId=" + r.localId + " remoteId=" + r.remoteId);
                    } else {
                        db.syncQueueDao().incrementAttempt(r.localId);
                        Log.w(TAG, "Failed localId=" + r.localId + " error=" + r.error);
                    }
                }
            }

            Log.d(TAG, "Sync complete");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "SyncWorker exception: " + e.getMessage(), e);
            return Result.retry();
        }
    }

    private void markRetry(AppDatabase db, List<SyncQueueEntryEntity> batch) {
        for (SyncQueueEntryEntity e : batch) {
            db.syncQueueDao().incrementAttempt(e.getId());
        }
    }
}