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

/**
 * ApiService.java
 *
 * All endpoints match the actual backend routes in arcade-fitness-backend/routes/.
 * Base URL is set in AppConstants.BASE_URL (must end with /api/).
 *
 * Backend route map:
 *   POST   /api/auth/register
 *   POST   /api/auth/login
 *   GET    /api/workouts
 *   GET    /api/workouts/:id
 *   POST   /api/workouts
 *   PUT    /api/workouts/:id
 *   DELETE /api/workouts/:id
 *   GET    /api/exercises                  (?muscle_group= optional)
 *   GET    /api/set-records/workout/:id
 *   GET    /api/set-records/session/:id
 *   POST   /api/set-records
 *   PUT    /api/set-records/:id
 *   DELETE /api/set-records/:id
 *   GET    /api/profiles
 *   PUT    /api/profiles
 *   GET    /api/goals
 *   POST   /api/goals
 *   PUT    /api/goals/:id
 *   DELETE /api/goals/:id
 *   GET    /api/sessions
 *   GET    /api/sessions/:id
 *   POST   /api/sessions
 *   PUT    /api/sessions/:id/complete
 *   DELETE /api/sessions/:id
 *   POST   /api/sync                       (batch sync)
 *   GET    /api/health
 */
public interface ApiService {

    // ── AUTH ─────────────────────────────────────────────────────────
    // Response: { status, data: { user: { user_id, email }, token } }

    @POST("auth/register")
    Call<JsonObject> register(@Body JsonObject credentials);

    @POST("auth/login")
    Call<JsonObject> login(@Body JsonObject credentials);

    // ── WORKOUTS ─────────────────────────────────────────────────────
    // All require Authorization: Bearer <token>
    // Response body: { status, data: { workout } } or { status, data: { workouts: [] } }

    @GET("workouts")
    Call<JsonObject> getWorkouts();

    @GET("workouts/{id}")
    Call<JsonObject> getWorkout(@Path("id") int remoteId);

    @POST("workouts")
    Call<JsonObject> createWorkout(@Body JsonObject workout);

    @PUT("workouts/{id}")
    Call<JsonObject> updateWorkout(@Path("id") int remoteId, @Body JsonObject workout);

    @DELETE("workouts/{id}")
    Call<JsonObject> deleteWorkout(@Path("id") int remoteId);

    // ── EXERCISES ────────────────────────────────────────────────────
    // GET /api/exercises?muscle_group=Chest  (optional filter)
    // Backend is read-only — no POST/PUT/DELETE for exercises

    @GET("exercises")
    Call<JsonObject> getExercises();

    @GET("exercises")
    Call<JsonObject> getExercisesByMuscleGroup(@Query("muscle_group") String muscleGroup);

    // ── SET RECORDS ──────────────────────────────────────────────────

    @GET("set-records/workout/{workoutId}")
    Call<JsonObject> getSetRecordsByWorkout(@Path("workoutId") int remoteWorkoutId);

    @GET("set-records/session/{sessionId}")
    Call<JsonObject> getSetRecordsBySession(@Path("sessionId") int remoteSessionId);

    @POST("set-records")
    Call<JsonObject> createSetRecord(@Body JsonObject setRecord);

    @PUT("set-records/{id}")
    Call<JsonObject> updateSetRecord(@Path("id") int remoteId, @Body JsonObject setRecord);

    @DELETE("set-records/{id}")
    Call<JsonObject> deleteSetRecord(@Path("id") int remoteId);

    // ── PROFILES ─────────────────────────────────────────────────────
    // Backend scopes to the authenticated user — no ID in path

    @GET("profiles")
    Call<JsonObject> getProfile();

    @PUT("profiles")
    Call<JsonObject> updateProfile(@Body JsonObject profile);

    // ── GOALS ────────────────────────────────────────────────────────

    @GET("goals")
    Call<JsonObject> getGoals();

    @POST("goals")
    Call<JsonObject> createGoal(@Body JsonObject goal);

    @PUT("goals/{id}")
    Call<JsonObject> updateGoal(@Path("id") int remoteId, @Body JsonObject goal);

    @DELETE("goals/{id}")
    Call<JsonObject> deleteGoal(@Path("id") int remoteId);

    // ── SESSIONS ─────────────────────────────────────────────────────
    // Note: complete uses PUT /:id/complete (not a standard update)

    @GET("sessions")
    Call<JsonObject> getSessions();

    @GET("sessions/{id}")
    Call<JsonObject> getSession(@Path("id") int remoteId);

    @POST("sessions")
    Call<JsonObject> createSession(@Body JsonObject session);

    @PUT("sessions/{id}/complete")
    Call<JsonObject> completeSession(@Path("id") int remoteId, @Body JsonObject completionData);

    @DELETE("sessions/{id}")
    Call<JsonObject> deleteSession(@Path("id") int remoteId);

    // ── BATCH SYNC ───────────────────────────────────────────────────
    // POST /api/sync
    // Body: { operations: [ { table, action, data }, ... ] }

    @POST("sync")
    Call<JsonObject> batchSync(@Body JsonObject batchPayload);

    // ── HEALTH CHECK ─────────────────────────────────────────────────

    @GET("health")
    Call<JsonObject> health();
}