package com.arcadefitness.data.remote;

import com.arcadefitness.data.remote.model.ExerciseResponse;
import com.arcadefitness.data.remote.model.SyncBatchRequest;
import com.arcadefitness.data.remote.model.SyncBatchResponse;
import com.arcadefitness.data.remote.model.WorkoutResponse;
import com.arcadefitness.data.remote.model.WorkoutSessionResponse;
import com.arcadefitness.utils.AppConstants;

import com.google.gson.JsonObject;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // ── Health check ─────────────────────────────────────────────────────────
    // Use this to verify the server is reachable before any real call.
    // Expected response: { "status": "ok" }
    @GET(AppConstants.ENDPOINT_HEALTH_CHECK)
    Call<ResponseBody> healthCheck();

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST(AppConstants.ENDPOINT_REGISTER)
    Call<JsonObject> register(@Body JsonObject body);

    @POST(AppConstants.ENDPOINT_LOGIN)
    Call<JsonObject> login(@Body JsonObject body);

    @POST(AppConstants.ENDPOINT_LOGOUT)
    Call<ResponseBody> logout();

    // ── User profile ──────────────────────────────────────────────────────────
    @GET(AppConstants.ENDPOINT_PROFILE)
    Call<JsonObject> getProfile();

    @PUT(AppConstants.ENDPOINT_PROFILE)
    Call<JsonObject> updateProfile(@Body JsonObject body);

    // ── Workouts ──────────────────────────────────────────────────────────────
    @GET(AppConstants.ENDPOINT_WORKOUTS)
    Call<List<WorkoutResponse>> getWorkouts();

    @GET(AppConstants.ENDPOINT_WORKOUT_BY_ID)
    Call<WorkoutResponse> getWorkoutById(@Path("id") String id);

    @POST(AppConstants.ENDPOINT_WORKOUTS)
    Call<JsonObject> createWorkout(@Body JsonObject body);

    @PUT(AppConstants.ENDPOINT_WORKOUT_BY_ID)
    Call<JsonObject> updateWorkout(@Path("id") int id, @Body JsonObject body);

    @DELETE(AppConstants.ENDPOINT_WORKOUT_BY_ID)
    Call<JsonObject> deleteWorkout(@Path("id") int id);

    // ── Exercises ─────────────────────────────────────────────────────────────
    @GET(AppConstants.ENDPOINT_EXERCISES)
    Call<List<ExerciseResponse>> getExercises();

    @POST(AppConstants.ENDPOINT_EXERCISES)
    Call<ExerciseResponse> createExercise(@Body ExerciseResponse exercise);

    // ── Workout sessions ──────────────────────────────────────────────────────
    @GET(AppConstants.ENDPOINT_SESSIONS)
    Call<List<WorkoutSessionResponse>> getSessions();

    @POST(AppConstants.ENDPOINT_SESSIONS)
    Call<JsonObject> createSession(@Body JsonObject body);

    @PUT("sessions/{id}")
    Call<WorkoutSessionResponse> updateSession(@Path("id") String id, @Body WorkoutSessionResponse session);

    @PUT("sessions/{id}/complete")
    Call<JsonObject> completeSession(@Path("id") int id, @Body JsonObject body);

    @DELETE("sessions/{id}")
    Call<JsonObject> deleteSession(@Path("id") int id);

    // ── Set records ───────────────────────────────────────────────────────────
    @GET(AppConstants.ENDPOINT_SET_RECORDS)
    Call<JsonObject> getSetRecords();

    @POST(AppConstants.ENDPOINT_SET_RECORDS)
    Call<JsonObject> createSetRecord(@Body JsonObject body);

    @PUT("set-records/{id}")
    Call<JsonObject> updateSetRecord(@Path("id") int id, @Body JsonObject body);

    @DELETE("set-records/{id}")
    Call<JsonObject> deleteSetRecord(@Path("id") int id);

    // ── Goals ─────────────────────────────────────────────────────────────────
    @GET(AppConstants.ENDPOINT_GOALS)
    Call<JsonObject> getGoals();

    @POST(AppConstants.ENDPOINT_GOALS)
    Call<JsonObject> createGoal(@Body JsonObject body);

    @PUT("goals/{id}")
    Call<JsonObject> updateGoal(@Path("id") int id, @Body JsonObject body);

    @DELETE("goals/{id}")
    Call<JsonObject> deleteGoal(@Path("id") int id);

    // ── Sync batch ────────────────────────────────────────────────────────────
    // Sends all pending local changes in one POST.
    // The server processes each entry and returns per-entry results.
    @POST(AppConstants.ENDPOINT_SYNC_BATCH)
    Call<SyncBatchResponse> syncBatch(@Body SyncBatchRequest request);
}
