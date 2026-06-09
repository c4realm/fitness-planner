package com.arcadefitness.data.remote;

import com.arcadefitness.data.remote.model.AuthRequest;
import com.arcadefitness.data.remote.model.AuthResponse;
import com.arcadefitness.data.remote.model.ExerciseResponse;
import com.arcadefitness.data.remote.model.GoalResponse;
import com.arcadefitness.data.remote.model.SetRecordResponse;
import com.arcadefitness.data.remote.model.SyncBatchRequest;
import com.arcadefitness.data.remote.model.SyncBatchResponse;
import com.arcadefitness.data.remote.model.UserProfileResponse;
import com.arcadefitness.data.remote.model.WorkoutResponse;
import com.arcadefitness.data.remote.model.WorkoutSessionResponse;
import com.arcadefitness.utils.AppConstants;

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
    Call<AuthResponse> register(@Body AuthRequest request);

    @POST(AppConstants.ENDPOINT_LOGIN)
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST(AppConstants.ENDPOINT_LOGOUT)
    Call<ResponseBody> logout();

    // ── User profile ──────────────────────────────────────────────────────────
    @GET(AppConstants.ENDPOINT_PROFILE)
    Call<UserProfileResponse> getProfile();

    @PUT(AppConstants.ENDPOINT_PROFILE)
    Call<UserProfileResponse> updateProfile(@Body UserProfileResponse profile);

    // ── Workouts ──────────────────────────────────────────────────────────────
    @GET(AppConstants.ENDPOINT_WORKOUTS)
    Call<List<WorkoutResponse>> getWorkouts();

    @GET(AppConstants.ENDPOINT_WORKOUT_BY_ID)
    Call<WorkoutResponse> getWorkoutById(@Path("id") String id);

    @POST(AppConstants.ENDPOINT_WORKOUTS)
    Call<WorkoutResponse> createWorkout(@Body WorkoutResponse workout);

    @PUT(AppConstants.ENDPOINT_WORKOUT_BY_ID)
    Call<WorkoutResponse> updateWorkout(@Path("id") String id, @Body WorkoutResponse workout);

    @DELETE(AppConstants.ENDPOINT_WORKOUT_BY_ID)
    Call<ResponseBody> deleteWorkout(@Path("id") String id);

    // ── Exercises ─────────────────────────────────────────────────────────────
    @GET(AppConstants.ENDPOINT_EXERCISES)
    Call<List<ExerciseResponse>> getExercises();

    @POST(AppConstants.ENDPOINT_EXERCISES)
    Call<ExerciseResponse> createExercise(@Body ExerciseResponse exercise);

    // ── Workout sessions ──────────────────────────────────────────────────────
    @GET(AppConstants.ENDPOINT_SESSIONS)
    Call<List<WorkoutSessionResponse>> getSessions();

    @POST(AppConstants.ENDPOINT_SESSIONS)
    Call<WorkoutSessionResponse> createSession(@Body WorkoutSessionResponse session);

    @PUT("sessions/{id}")
    Call<WorkoutSessionResponse> updateSession(@Path("id") String id, @Body WorkoutSessionResponse session);

    // ── Set records ───────────────────────────────────────────────────────────
    @GET(AppConstants.ENDPOINT_SET_RECORDS)
    Call<List<SetRecordResponse>> getSetRecords();

    @POST(AppConstants.ENDPOINT_SET_RECORDS)
    Call<SetRecordResponse> createSetRecord(@Body SetRecordResponse setRecord);

    // ── Goals ─────────────────────────────────────────────────────────────────
    @GET(AppConstants.ENDPOINT_GOALS)
    Call<List<GoalResponse>> getGoals();

    @POST(AppConstants.ENDPOINT_GOALS)
    Call<GoalResponse> createGoal(@Body GoalResponse goal);

    @PUT("goals/{id}")
    Call<GoalResponse> updateGoal(@Path("id") String id, @Body GoalResponse goal);

    @DELETE("goals/{id}")
    Call<ResponseBody> deleteGoal(@Path("id") String id);

    // ── Sync batch ────────────────────────────────────────────────────────────
    // Sends all pending local changes in one POST.
    // The server processes each entry and returns per-entry results.
    @POST(AppConstants.ENDPOINT_SYNC_BATCH)
    Call<SyncBatchResponse> syncBatch(@Body SyncBatchRequest request);
}