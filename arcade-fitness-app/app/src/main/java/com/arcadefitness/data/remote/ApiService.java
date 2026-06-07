package com.arcadefitness.data.remote;

import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ── AUTH ─────────────────────────────────────────────────────────

    @POST("auth/login")
    Call<JsonObject> login(@Body JsonObject credentials);

    @POST("auth/register")
    Call<JsonObject> register(@Body JsonObject userData);

    @POST("auth/google")
    Call<JsonObject> googleAuth(@Body JsonObject idToken);

    // ── WORKOUTS ─────────────────────────────────────────────────────

    @GET("workouts")
    Call<List<JsonObject>> getWorkouts();

    @GET("workouts/{id}")
    Call<JsonObject> getWorkout(@Path("id") String remoteId);

    @POST("workouts")
    Call<JsonObject> createWorkout(@Body JsonObject workout);

    @PUT("workouts/{id}")
    Call<JsonObject> updateWorkout(@Path("id") String remoteId, @Body JsonObject workout);

    @DELETE("workouts/{id}")
    Call<JsonObject> deleteWorkout(@Path("id") String remoteId);

    @POST("workouts/sync")
    Call<List<JsonObject>> syncWorkouts(@Body List<JsonObject> payloads);

    // ── EXERCISES ────────────────────────────────────────────────────

    @GET("exercises")
    Call<List<JsonObject>> getExercises();

    @GET("exercises/{id}")
    Call<JsonObject> getExercise(@Path("id") String remoteId);

    @GET("exercises/search")
    Call<List<JsonObject>> searchExercises(@Query("q") String query);

    @GET("exercises/muscle-group/{group}")
    Call<List<JsonObject>> getExercisesByMuscleGroup(@Path("group") String muscleGroup);

    @POST("exercises")
    Call<JsonObject> createExercise(@Body JsonObject exercise);

    @PUT("exercises/{id}")
    Call<JsonObject> updateExercise(@Path("id") String remoteId, @Body JsonObject exercise);

    @DELETE("exercises/{id}")
    Call<JsonObject> deleteExercise(@Path("id") String remoteId);

    @POST("exercises/sync")
    Call<List<JsonObject>> syncExercises(@Body List<JsonObject> payloads);

    // ── SET RECORDS ──────────────────────────────────────────────────

    @GET("set-records/workout/{workoutId}")
    Call<List<JsonObject>> getSetRecordsByWorkout(@Path("workoutId") String remoteWorkoutId);

    @POST("set-records")
    Call<JsonObject> createSetRecord(@Body JsonObject setRecord);

    @PUT("set-records/{id}")
    Call<JsonObject> updateSetRecord(@Path("id") String remoteId, @Body JsonObject setRecord);

    @POST("set-records/sync")
    Call<List<JsonObject>> syncSetRecords(@Body List<JsonObject> payloads);

    // ── USER PROFILES ────────────────────────────────────────────────

    @GET("user-profiles")
    Call<List<JsonObject>> getUserProfiles();

    @GET("user-profiles/{id}")
    Call<JsonObject> getUserProfile(@Path("id") String remoteId);

    @POST("user-profiles")
    Call<JsonObject> createUserProfile(@Body JsonObject profile);

    @PUT("user-profiles/{id}")
    Call<JsonObject> updateUserProfile(@Path("id") String remoteId, @Body JsonObject profile);

    @DELETE("user-profiles/{id}")
    Call<JsonObject> deleteUserProfile(@Path("id") String remoteId);

    // ── GOALS ────────────────────────────────────────────────────────

    @GET("goals")
    Call<List<JsonObject>> getGoals();

    @GET("goals/{id}")
    Call<JsonObject> getGoal(@Path("id") String remoteId);

    @POST("goals")
    Call<JsonObject> createGoal(@Body JsonObject goal);

    @PUT("goals/{id}")
    Call<JsonObject> updateGoal(@Path("id") String remoteId, @Body JsonObject goal);

    @DELETE("goals/{id}")
    Call<JsonObject> deleteGoal(@Path("id") String remoteId);

    // ── WORKOUT SESSIONS ────────────────────────────────────────────

    @GET("workout-sessions")
    Call<List<JsonObject>> getWorkoutSessions();

    @GET("workout-sessions/{id}")
    Call<JsonObject> getWorkoutSession(@Path("id") String remoteId);

    @POST("workout-sessions")
    Call<JsonObject> createWorkoutSession(@Body JsonObject session);

    @PUT("workout-sessions/{id}")
    Call<JsonObject> updateWorkoutSession(@Path("id") String remoteId, @Body JsonObject session);

    @DELETE("workout-sessions/{id}")
    Call<JsonObject> deleteWorkoutSession(@Path("id") String remoteId);

    // ── SYNC ─────────────────────────────────────────────────────────

    @POST("sync/batch")
    Call<JsonObject> batchSync(@Body JsonObject batchPayload);
}
