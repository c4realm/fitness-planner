package com.arcadefitness.data.remote;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ApiHelper.java
 *
 * Wraps Retrofit calls so every Activity/Repository gets clean callbacks
 * without duplicating try-catch and response-parsing boilerplate.
 *
 * The backend always returns:
 *   { "status": "success"|"error", "data": { ... }, "message": "..." }
 *
 * Usage:
 *   ApiHelper.call(RetrofitClient.getApi().login(body), new ApiHelper.ApiCallback<JsonObject>() {
 *       @Override public void onSuccess(JsonObject data) { ... }
 *       @Override public void onError(int code, String message) { ... }
 *   });
 */
public final class ApiHelper {

    private static final String TAG = "ApiHelper";

    private ApiHelper() {}

    public interface ApiCallback<T> {
        void onSuccess(T data);
        void onError(int httpCode, String message);
    }

    /**
     * Executes a Retrofit Call and delivers the "data" object from the
     * backend's standard response envelope to the callback.
     */
    public static void call(Call<JsonObject> call, ApiCallback<JsonObject> callback) {
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> c, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    // Extract the "data" field from the envelope
                    JsonElement dataEl = body.get("data");
                    JsonObject data = (dataEl != null && dataEl.isJsonObject())
                            ? dataEl.getAsJsonObject()
                            : body;
                    callback.onSuccess(data);
                } else {
                    String msg = "HTTP " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String raw = response.errorBody().string();
                            JsonObject err = new com.google.gson.JsonParser()
                                    .parse(raw).getAsJsonObject();
                            if (err.has("message")) {
                                msg = err.get("message").getAsString();
                            }
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Could not parse error body", e);
                    }
                    callback.onError(response.code(), msg);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> c, Throwable t) {
                Log.e(TAG, "Network failure", t);
                String msg = t.getMessage() != null ? t.getMessage() : "Network error";
                // Distinguish timeout from no-connection
                if (t instanceof java.net.SocketTimeoutException) {
                    msg = "Request timed out. Check your connection.";
                } else if (t instanceof java.net.UnknownHostException) {
                    msg = "Cannot reach server. Check your connection.";
                }
                callback.onError(0, msg);
            }
        });
    }
}