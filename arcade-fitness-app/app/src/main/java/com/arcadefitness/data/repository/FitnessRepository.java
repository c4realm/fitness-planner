package com.arcadefitness.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.arcadefitness.data.local.AppDatabase;
import com.arcadefitness.data.local.dao.ExerciseDao;
import com.arcadefitness.data.local.dao.GoalDao;
import com.arcadefitness.data.local.dao.SetRecordDao;
import com.arcadefitness.data.local.dao.SyncQueueDao;
import com.arcadefitness.data.local.dao.UserProfileDao;
import com.arcadefitness.data.local.dao.WorkoutDao;
import com.arcadefitness.data.local.dao.WorkoutSessionDao;
import com.arcadefitness.data.local.entity.ExerciseEntity;
import com.arcadefitness.data.local.entity.GoalEntity;
import com.arcadefitness.data.local.entity.SetRecordEntity;
import com.arcadefitness.data.local.entity.SyncQueueEntryEntity;
import com.arcadefitness.data.local.entity.UserProfileEntity;
import com.arcadefitness.data.local.entity.WorkoutEntity;
import com.arcadefitness.data.local.entity.WorkoutSessionEntity;
import com.arcadefitness.data.remote.ApiService;
import com.arcadefitness.data.remote.RetrofitClient;
import com.arcadefitness.utils.AppConstants;
import com.arcadefitness.utils.SessionManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;
import java.util.concurrent.ExecutorService;

import retrofit2.Call;
import retrofit2.Response;

public class FitnessRepository {

    private static volatile FitnessRepository INSTANCE;

    private final WorkoutDao workoutDao;
    private final ExerciseDao exerciseDao;
    private final SetRecordDao setRecordDao;
    private final SyncQueueDao syncQueueDao;
    private final UserProfileDao userProfileDao;
    private final GoalDao goalDao;
    private final WorkoutSessionDao workoutSessionDao;
    private final ExecutorService executor;
    private final ApiService apiService;
    private final Gson gson;
    private final SessionManager sessionManager;

    private final MutableLiveData<Boolean> syncInProgress = new MutableLiveData<>(false);
    private final MutableLiveData<String> syncStatusMessage = new MutableLiveData<>("");

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    private FitnessRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.workoutDao        = db.workoutDao();
        this.exerciseDao       = db.exerciseDao();
        this.setRecordDao      = db.setRecordDao();
        this.syncQueueDao      = db.syncQueueDao();
        this.userProfileDao    = db.userProfileDao();
        this.goalDao           = db.goalDao();
        this.workoutSessionDao = db.workoutSessionDao();
        this.executor          = AppDatabase.DATABASE_WRITE_EXECUTOR;
        this.apiService        = RetrofitClient.getInstance().getApiService();
        this.gson              = new Gson();
        this.sessionManager    = new SessionManager(context);
    }

    public static FitnessRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (FitnessRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FitnessRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    // ── Current user ─────────────────────────────────────────────────

    private String currentUserId() {
        String id = sessionManager.getUserId();
        return (id == null || id.isEmpty()) ? AppConstants.GUEST_USER_ID : id;
    }

    public LiveData<Boolean> getSyncInProgress()     { return syncInProgress; }
    public LiveData<String>  getSyncStatusMessage()  { return syncStatusMessage; }

    // ═════════════════════════════════════════════════════════════════
    //  WORKOUTS
    // ═════════════════════════════════════════════════════════════════

    public void getAllWorkouts(RepositoryCallback<List<WorkoutEntity>> callback) {
        executor.execute(() -> {
            try {
                postSuccess(callback, workoutDao.getAll(currentUserId()));
            } catch (Exception e) {
                postError(callback, "Failed to load workouts: " + e.getMessage());
            }
        });
    }

    public void getAllWorkoutsLiveData(RepositoryCallback<LiveData<List<WorkoutEntity>>> callback) {
        executor.execute(() -> {
            try {
                postSuccess(callback, workoutDao.getAllLiveData(currentUserId()));
            } catch (Exception e) {
                postError(callback, "Failed to load workouts: " + e.getMessage());
            }
        });
    }

    public void getWorkoutById(int workoutId, RepositoryCallback<WorkoutEntity> callback) {
        executor.execute(() -> {
            try {
                WorkoutEntity workout = workoutDao.getById(workoutId);
                if (workout != null) postSuccess(callback, workout);
                else postError(callback, "Workout not found");
            } catch (Exception e) {
                postError(callback, "Failed to load workout: " + e.getMessage());
            }
        });
    }

    public void insertWorkout(WorkoutEntity workout, RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                workout.setOwnerId(currentUserId());
                long id = workoutDao.insert(workout);
                int localId = (int) id;
                workout.setId(localId);
                queueSync("workouts", localId, "INSERT", gson.toJson(workout));
                postSuccess(callback, localId);
            } catch (Exception e) {
                postError(callback, "Failed to save workout: " + e.getMessage());
            }
        });
    }

    public void updateWorkout(WorkoutEntity workout, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                workout.setUpdatedAt(System.currentTimeMillis());
                workoutDao.update(workout);
                queueSync("workouts", workout.getId(), "UPDATE", gson.toJson(workout));
                postSuccess(callback, null);
            } catch (Exception e) {
                postError(callback, "Failed to update workout: " + e.getMessage());
            }
        });
    }

    public void deleteWorkout(WorkoutEntity workout, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                workoutDao.delete(workout);
                queueSync("workouts", workout.getId(), "DELETE", gson.toJson(workout));
                setRecordDao.deleteByWorkoutId(workout.getId());
                postSuccess(callback, null);
            } catch (Exception e) {
                postError(callback, "Failed to delete workout: " + e.getMessage());
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════
    //  EXERCISES
    // ═════════════════════════════════════════════════════════════════

    public void getAllExercises(RepositoryCallback<List<ExerciseEntity>> callback) {
        executor.execute(() -> {
            try {
                postSuccess(callback, exerciseDao.getAll());
            } catch (Exception e) {
                postError(callback, "Failed to load exercises: " + e.getMessage());
            }
        });
    }

    public void getExerciseById(int exerciseId, RepositoryCallback<ExerciseEntity> callback) {
        executor.execute(() -> {
            try {
                ExerciseEntity ex = exerciseDao.getById(exerciseId);
                if (ex != null) postSuccess(callback, ex);
                else postError(callback, "Exercise not found");
            } catch (Exception e) {
                postError(callback, "Failed to load exercise: " + e.getMessage());
            }
        });
    }

    public void searchExercises(String query, RepositoryCallback<List<ExerciseEntity>> callback) {
        executor.execute(() -> {
            try {
                postSuccess(callback, exerciseDao.search(query));
            } catch (Exception e) {
                postError(callback, "Search failed: " + e.getMessage());
            }
        });
    }

    public void getExercisesByMuscleGroup(String muscleGroup,
                                          RepositoryCallback<List<ExerciseEntity>> callback) {
        executor.execute(() -> {
            try {
                postSuccess(callback, exerciseDao.getByMuscleGroup(muscleGroup));
            } catch (Exception e) {
                postError(callback, "Failed to load exercises: " + e.getMessage());
            }
        });
    }

    public void insertExercise(ExerciseEntity exercise, RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                long id = exerciseDao.insert(exercise);
                int localId = (int) id;
                exercise.setId(localId);
                // Exercises are read-only on the backend — no sync needed
                postSuccess(callback, localId);
            } catch (Exception e) {
                postError(callback, "Failed to save exercise: " + e.getMessage());
            }
        });
    }

    public void updateExercise(ExerciseEntity exercise, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                exerciseDao.update(exercise);
                // Exercises are read-only on the backend — no sync needed
                postSuccess(callback, null);
            } catch (Exception e) {
                postError(callback, "Failed to update exercise: " + e.getMessage());
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════
    //  SET RECORDS
    // ═════════════════════════════════════════════════════════════════

    public void getSetRecordsByWorkout(int workoutId,
                                       RepositoryCallback<List<SetRecordEntity>> callback) {
        executor.execute(() -> {
            try {
                postSuccess(callback, setRecordDao.getByWorkoutId(workoutId));
            } catch (Exception e) {
                postError(callback, "Failed to load set records: " + e.getMessage());
            }
        });
    }

    public void getSetRecordsByWorkoutAndExercise(int workoutId, int exerciseId,
                                                  RepositoryCallback<List<SetRecordEntity>> callback) {
        executor.execute(() -> {
            try {
                postSuccess(callback, setRecordDao.getByWorkoutAndExercise(workoutId, exerciseId));
            } catch (Exception e) {
                postError(callback, "Failed to load set records: " + e.getMessage());
            }
        });
    }

    public void insertSetRecord(SetRecordEntity setRecord, RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                setRecord.setOwnerId(currentUserId());
                long id = setRecordDao.insert(setRecord);
                int localId = (int) id;
                setRecord.setId(localId);
                queueSync("set_records", localId, "INSERT", gson.toJson(setRecord));
                postSuccess(callback, localId);
            } catch (Exception e) {
                postError(callback, "Failed to save set record: " + e.getMessage());
            }
        });
    }

    public void updateSetRecord(SetRecordEntity setRecord, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                setRecordDao.update(setRecord);
                queueSync("set_records", setRecord.getId(), "UPDATE", gson.toJson(setRecord));
                postSuccess(callback, null);
            } catch (Exception e) {
                postError(callback, "Failed to update set record: " + e.getMessage());
            }
        });
    }

    public void markSetCompleted(int setId, double weight, int reps,
                                 RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                long ts = System.currentTimeMillis();
                setRecordDao.markCompleted(setId, weight, reps, ts);
                SetRecordEntity record = setRecordDao.getById(setId);
                if (record != null) {
                    record.setIsCompleted(1);
                    record.setWeight(weight);
                    record.setReps(reps);
                    record.setTimestamp(ts);
                    queueSync("set_records", setId, "UPDATE", gson.toJson(record));
                }
                postSuccess(callback, null);
            } catch (Exception e) {
                postError(callback, "Failed to mark set completed: " + e.getMessage());
            }
        });
    }

    public void deleteSetRecord(SetRecordEntity setRecord, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                setRecordDao.delete(setRecord);
                queueSync("set_records", setRecord.getId(), "DELETE", gson.toJson(setRecord));
                postSuccess(callback, null);
            } catch (Exception e) {
                postError(callback, "Failed to delete set record: " + e.getMessage());
            }
        });
    }

    public void getCompletedCountByWorkout(int workoutId,
                                           RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                postSuccess(callback, setRecordDao.getCompletedCountByWorkout(workoutId));
            } catch (Exception e) {
                postError(callback, "Failed to get completed count: " + e.getMessage());
            }
        });
    }

    public void getTotalVolumeByWorkout(int workoutId, RepositoryCallback<Double> callback) {
        executor.execute(() -> {
            try {
                Double volume = setRecordDao.getTotalVolumeByWorkout(workoutId);
                postSuccess(callback, volume != null ? volume : 0.0);
            } catch (Exception e) {
                postError(callback, "Failed to get total volume: " + e.getMessage());
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════
    //  USER PROFILES
    // ═════════════════════════════════════════════════════════════════

    public void getAllUserProfiles(RepositoryCallback<List<UserProfileEntity>> callback) {
        executor.execute(() -> {
            try {
                postSuccess(callback, userProfileDao.getAll());
            } catch (Exception e) {
                postError(callback, "Failed to load profiles: " + e.getMessage());
            }
        });
    }

    public void getUserProfileById(int profileId, RepositoryCallback<UserProfileEntity> callback) {
        executor.execute(() -> {
            try {
                UserProfileEntity p = userProfileDao.getById(profileId);
                if (p != null) postSuccess(callback, p);
                else postError(callback, "Profile not found");
            } catch (Exception e) {
                postError(callback, "Failed to load profile: " + e.getMessage());
            }
        });
    }

    public void getUserProfileByEmail(String email, RepositoryCallback<UserProfileEntity> callback) {
        executor.execute(() -> {
            try {
                postSuccess(callback, userProfileDao.getByEmail(email));
            } catch (Exception e) {
                postError(callback, "Failed to load profile: " + e.getMessage());
            }
        });
    }

    public void insertUserProfile(UserProfileEntity profile, RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                long id = userProfileDao.insert(profile);
                int localId = (int) id;
                profile.setId(localId);
                queueSync("user_profiles", localId, "INSERT", gson.toJson(profile));
                postSuccess(callback, localId);
            } catch (Exception e) {
                postError(callback, "Failed to save profile: " + e.getMessage());
            }
        });
    }

    public void updateUserProfile(UserProfileEntity profile, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                profile.setUpdatedAt(System.currentTimeMillis());
                userProfileDao.update(profile);
                queueSync("user_profiles", profile.getId(), "UPDATE", gson.toJson(profile));
                postSuccess(callback, null);
            } catch (Exception e) {
                postError(callback, "Failed to update profile: " + e.getMessage());
            }
        });
    }

    public void deleteUserProfile(UserProfileEntity profile, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                userProfileDao.delete(profile);
                queueSync("user_profiles", profile.getId(), "DELETE", gson.toJson(profile));
                postSuccess(callback, null);
            } catch (Exception e) {
                postError(callback, "Failed to delete profile: " + e.getMessage());
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════
    //  GOALS
    // ═════════════════════════════════════════════════════════════════

    public void getAllGoals(RepositoryCallback<List<GoalEntity>> callback) {
        executor.execute(() -> {
            try {
                postSuccess(callback, goalDao.getAll(currentUserId()));
            } catch (Exception e) {
                postError(callback, "Failed to load goals: " + e.getMessage());
            }
        });
    }

    public void getActiveGoals(RepositoryCallback<List<GoalEntity>> callback) {
        executor.execute(() -> {
            try {
                postSuccess(callback, goalDao.getActiveGoals(currentUserId()));
            } catch (Exception e) {
                postError(callback, "Failed to load active goals: " + e.getMessage());
            }
        });
    }

    public void getGoalById(int goalId, RepositoryCallback<GoalEntity> callback) {
        executor.execute(() -> {
            try {
                GoalEntity goal = goalDao.getById(goalId);
                if (goal != null) postSuccess(callback, goal);
                else postError(callback, "Goal not found");
            } catch (Exception e) {
                postError(callback, "Failed to load goal: " + e.getMessage());
            }
        });
    }

    public void insertGoal(GoalEntity goal, RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                goal.setOwnerId(currentUserId());
                long id = goalDao.insert(goal);
                int localId = (int) id;
                goal.setId(localId);
                queueSync("goals", localId, "INSERT", gson.toJson(goal));
                postSuccess(callback, localId);
            } catch (Exception e) {
                postError(callback, "Failed to save goal: " + e.getMessage());
            }
        });
    }

    public void updateGoal(GoalEntity goal, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                goal.setUpdatedAt(System.currentTimeMillis());
                goalDao.update(goal);
                queueSync("goals", goal.getId(), "UPDATE", gson.toJson(goal));
                postSuccess(callback, null);
            } catch (Exception e) {
                postError(callback, "Failed to update goal: " + e.getMessage());
            }
        });
    }

    public void updateGoalProgress(int goalId, double value, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                goalDao.updateProgress(goalId, value, System.currentTimeMillis());
                postSuccess(callback, null);
            } catch (Exception e) {
                postError(callback, "Failed to update goal progress: " + e.getMessage());
            }
        });
    }

    public void deleteGoal(GoalEntity goal, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                goalDao.delete(goal);
                queueSync("goals", goal.getId(), "DELETE", gson.toJson(goal));
                postSuccess(callback, null);
            } catch (Exception e) {
                postError(callback, "Failed to delete goal: " + e.getMessage());
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════
    //  WORKOUT SESSIONS
    // ═════════════════════════════════════════════════════════════════

    public void getAllWorkoutSessions(RepositoryCallback<List<WorkoutSessionEntity>> callback) {
        executor.execute(() -> {
            try {
                postSuccess(callback, workoutSessionDao.getAll());
            } catch (Exception e) {
                postError(callback, "Failed to load sessions: " + e.getMessage());
            }
        });
    }

    public void getCompletedSessions(RepositoryCallback<List<WorkoutSessionEntity>> callback) {
        executor.execute(() -> {
            try {
                postSuccess(callback, workoutSessionDao.getCompletedSessions(currentUserId()));
            } catch (Exception e) {
                postError(callback, "Failed to load completed sessions: " + e.getMessage());
            }
        });
    }

    public void getWorkoutSessionById(int sessionId,
                                      RepositoryCallback<WorkoutSessionEntity> callback) {
        executor.execute(() -> {
            try {
                WorkoutSessionEntity s = workoutSessionDao.getById(sessionId);
                if (s != null) postSuccess(callback, s);
                else postError(callback, "Session not found");
            } catch (Exception e) {
                postError(callback, "Failed to load session: " + e.getMessage());
            }
        });
    }

    public void getCurrentSession(RepositoryCallback<WorkoutSessionEntity> callback) {
        executor.execute(() -> {
            try {
                postSuccess(callback, workoutSessionDao.getCurrentSession(currentUserId()));
            } catch (Exception e) {
                postError(callback, "Failed to load current session: " + e.getMessage());
            }
        });
    }

    public void getCurrentSessionLiveData(
            RepositoryCallback<LiveData<WorkoutSessionEntity>> callback) {
        executor.execute(() -> {
            try {
                postSuccess(callback,
                        workoutSessionDao.getCurrentSessionLiveData(currentUserId()));
            } catch (Exception e) {
                postError(callback, "Failed to load current session: " + e.getMessage());
            }
        });
    }

    public void insertWorkoutSession(WorkoutSessionEntity session,
                                     RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                session.setOwnerId(currentUserId());
                long id = workoutSessionDao.insert(session);
                int localId = (int) id;
                session.setId(localId);
                queueSync("workout_sessions", localId, "INSERT", gson.toJson(session));
                postSuccess(callback, localId);
            } catch (Exception e) {
                postError(callback, "Failed to save session: " + e.getMessage());
            }
        });
    }

    public void updateWorkoutSession(WorkoutSessionEntity session,
                                     RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                workoutSessionDao.update(session);
                queueSync("workout_sessions", session.getId(), "UPDATE", gson.toJson(session));
                postSuccess(callback, null);
            } catch (Exception e) {
                postError(callback, "Failed to update session: " + e.getMessage());
            }
        });
    }

    public void completeWorkoutSession(int sessionId, int durationMinutes,
                                       int caloriesBurned, double totalVolume,
                                       int rating, String notes,
                                       RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                WorkoutSessionEntity session = workoutSessionDao.getById(sessionId);
                if (session == null) { postError(callback, "Session not found"); return; }
                session.setStatus("COMPLETED");
                session.setEndTimestamp(System.currentTimeMillis());
                session.setDurationMinutes(durationMinutes);
                session.setCaloriesBurned(caloriesBurned);
                session.setTotalVolume(totalVolume);
                session.setRating(rating);
                session.setNotes(notes);
                workoutSessionDao.update(session);
                queueSync("workout_sessions", sessionId, "UPDATE", gson.toJson(session));
                postSuccess(callback, null);
            } catch (Exception e) {
                postError(callback, "Failed to complete session: " + e.getMessage());
            }
        });
    }

    public void deleteWorkoutSession(WorkoutSessionEntity session,
                                     RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                workoutSessionDao.delete(session);
                queueSync("workout_sessions", session.getId(), "DELETE", gson.toJson(session));
                postSuccess(callback, null);
            } catch (Exception e) {
                postError(callback, "Failed to delete session: " + e.getMessage());
            }
        });
    }

    public void getSessionsSince(long fromTimestamp,
                                 RepositoryCallback<List<WorkoutSessionEntity>> callback) {
        executor.execute(() -> {
            try {
                postSuccess(callback,
                        workoutSessionDao.getCompletedSince(currentUserId(), fromTimestamp));
            } catch (Exception e) {
                postError(callback, "Failed to load sessions: " + e.getMessage());
            }
        });
    }

    public void getWeeklyStats(RepositoryCallback<int[]> callback) {
        executor.execute(() -> {
            try {
                String uid    = currentUserId();
                long weekAgo  = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000;
                int  sessions = workoutSessionDao.getCompletedCountSince(uid, weekAgo);
                Integer dur   = workoutSessionDao.getTotalDurationSince(uid, weekAgo);
                Integer cal   = workoutSessionDao.getTotalCaloriesSince(uid, weekAgo);
                Double  vol   = workoutSessionDao.getTotalVolumeSince(uid, weekAgo);
                postSuccess(callback, new int[]{
                        sessions,
                        dur != null ? dur : 0,
                        cal != null ? cal : 0,
                        vol != null ? vol.intValue() : 0
                });
            } catch (Exception e) {
                postError(callback, "Failed to load weekly stats: " + e.getMessage());
            }
        });
    }

    public void getCompletedSessionCount(RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                postSuccess(callback, workoutSessionDao.getCompletedCount(currentUserId()));
            } catch (Exception e) {
                postError(callback, "Failed to get session count: " + e.getMessage());
            }
        });
    }

    public void getStreakDays(RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                List<String> dates = workoutSessionDao.getCompletedSessionDates(currentUserId());
                int streak = 0;
                java.util.Calendar cal = java.util.Calendar.getInstance();
                for (String date : dates) {
                    String expected = String.format("%04d-%02d-%02d",
                            cal.get(java.util.Calendar.YEAR),
                            cal.get(java.util.Calendar.MONTH) + 1,
                            cal.get(java.util.Calendar.DAY_OF_MONTH));
                    if (date.equals(expected)) {
                        streak++;
                        cal.add(java.util.Calendar.DAY_OF_MONTH, -1);
                    } else {
                        break;
                    }
                }
                postSuccess(callback, streak);
            } catch (Exception e) {
                postError(callback, "Failed to calculate streak: " + e.getMessage());
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════
    //  SYNC ENGINE
    // ═════════════════════════════════════════════════════════════════

    public void syncOfflineQueue() { processSyncQueue(); }

    public void processSyncQueue() {
        if (Boolean.TRUE.equals(syncInProgress.getValue())) return;

        syncInProgress.postValue(true);
        syncStatusMessage.postValue("Syncing...");

        executor.execute(() -> {
            try {
                List<SyncQueueEntryEntity> pending = syncQueueDao.getPendingAndFailed();
                if (pending.isEmpty()) {
                    syncInProgress.postValue(false);
                    syncStatusMessage.postValue("All synced");
                    return;
                }
                for (SyncQueueEntryEntity entry : pending) {
                    syncQueueDao.markInProgress(entry.getId());
                    processSingleSyncEntry(entry);
                }
                syncQueueDao.deleteCompleted();
                syncQueueDao.deleteFailedAboveMaxRetries(5);
                syncInProgress.postValue(false);
                syncStatusMessage.postValue("Sync complete");
            } catch (Exception e) {
                syncInProgress.postValue(false);
                syncStatusMessage.postValue("Sync failed: " + e.getMessage());
            }
        });
    }

    private void processSingleSyncEntry(SyncQueueEntryEntity entry) {
        try {
            JsonObject payload = JsonParser.parseString(entry.getPayload()).getAsJsonObject();
            String table       = entry.getTableName();
            String op          = entry.getOperationType();

            Call<JsonObject> call = buildSyncCall(table, op, payload);
            if (call == null) {
                // Nothing to sync for this table/op — mark done
                syncQueueDao.markCompleted(entry.getId());
                return;
            }

            Response<JsonObject> response = call.execute();
            if (response.isSuccessful()) {
                syncQueueDao.markCompleted(entry.getId());
                if (response.body() != null) {
                    markLocalEntitySynced(table, entry.getRecordId(), response.body());
                }
            } else {
                syncQueueDao.markFailed(entry.getId(),
                        "HTTP " + response.code() + ": " + response.message());
            }
        } catch (Exception e) {
            syncQueueDao.markFailed(entry.getId(),
                    e.getMessage() != null ? e.getMessage() : "Unknown error");
        }
    }

    /**
     * Maps a (tableName, operation, payload) triple to the correct ApiService call.
     *
     * Remote IDs are stored as Strings in the local DB (remote_id column) because
     * they come back from the server as JSON integers but we store them as text.
     * ApiService methods accept int, so we parse with Integer.parseInt() here.
     * If the String is null / not yet synced, we fall back to a CREATE call.
     */
    private Call<JsonObject> buildSyncCall(String table, String op, JsonObject payload) {
        switch (table) {
            case "workouts":        return buildWorkoutSyncCall(op, payload);
            case "set_records":     return buildSetRecordSyncCall(op, payload);
            case "user_profiles":   return buildUserProfileSyncCall(op, payload);
            case "goals":           return buildGoalSyncCall(op, payload);
            case "workout_sessions":return buildWorkoutSessionSyncCall(op, payload);
            // exercises are read-only on the backend — skip
            default:                return null;
        }
    }

    private Call<JsonObject> buildWorkoutSyncCall(String op, JsonObject p) {
        switch (op) {
            case "INSERT": return apiService.createWorkout(p);
            case "UPDATE": {
                int rid = parseRemoteId(p);
                return rid > 0 ? apiService.updateWorkout(rid, p)
                        : apiService.createWorkout(p);
            }
            case "DELETE": {
                int rid = parseRemoteId(p);
                return rid > 0 ? apiService.deleteWorkout(rid) : null;
            }
            default: return null;
        }
    }

    private Call<JsonObject> buildSetRecordSyncCall(String op, JsonObject p) {
        switch (op) {
            case "INSERT": return apiService.createSetRecord(p);
            case "UPDATE": {
                int rid = parseRemoteId(p);
                return rid > 0 ? apiService.updateSetRecord(rid, p)
                        : apiService.createSetRecord(p);
            }
            case "DELETE": {
                int rid = parseRemoteId(p);
                return rid > 0 ? apiService.deleteSetRecord(rid) : null;
            }
            default: return null;
        }
    }

    private Call<JsonObject> buildUserProfileSyncCall(String op, JsonObject p) {
        switch (op) {
            // Backend scopes profiles to the auth user — no ID in path
            case "INSERT": return apiService.updateProfile(p); // upsert via PUT
            case "UPDATE": return apiService.updateProfile(p);
            case "DELETE": return null; // no DELETE endpoint on backend
            default: return null;
        }
    }

    private Call<JsonObject> buildGoalSyncCall(String op, JsonObject p) {
        switch (op) {
            case "INSERT": return apiService.createGoal(p);
            case "UPDATE": {
                int rid = parseRemoteId(p);
                return rid > 0 ? apiService.updateGoal(rid, p)
                        : apiService.createGoal(p);
            }
            case "DELETE": {
                int rid = parseRemoteId(p);
                return rid > 0 ? apiService.deleteGoal(rid) : null;
            }
            default: return null;
        }
    }

    private Call<JsonObject> buildWorkoutSessionSyncCall(String op, JsonObject p) {
        switch (op) {
            case "INSERT": return apiService.createSession(p);
            case "UPDATE": {
                int rid = parseRemoteId(p);
                if (rid <= 0) return apiService.createSession(p);
                // If status is COMPLETED, use the /complete endpoint
                if (p.has("status") && "COMPLETED".equals(p.get("status").getAsString())) {
                    return apiService.completeSession(rid, p);
                }
                return null; // no generic update for sessions in this backend
            }
            case "DELETE": {
                int rid = parseRemoteId(p);
                return rid > 0 ? apiService.deleteSession(rid) : null;
            }
            default: return null;
        }
    }

    /**
     * Reads the "remote_id" (or "remoteId") field from a sync payload and
     * parses it to int. Returns 0 if absent, null, or unparseable.
     */
    private int parseRemoteId(JsonObject payload) {
        for (String key : new String[]{"remote_id", "remoteId"}) {
            if (payload.has(key) && !payload.get(key).isJsonNull()) {
                try {
                    String raw = payload.get(key).getAsString();
                    if (raw != null && !raw.isEmpty() && !raw.equals("null")) {
                        return Integer.parseInt(raw);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return 0;
    }

    private void markLocalEntitySynced(String table, int localId, JsonObject response) {
        if (response == null) return;
        // Backend wraps the actual object in a "data" field
        JsonObject data = response;
        if (response.has("data") && response.get("data").isJsonObject()) {
            data = response.getAsJsonObject("data");
        }
        // Look for the server-assigned ID
        String remoteId = null;
        for (String key : new String[]{"id", "workout_id", "session_id", "goal_id",
                "set_id", "profile_id"}) {
            if (data.has(key) && !data.get(key).isJsonNull()) {
                remoteId = data.get(key).getAsString();
                break;
            }
        }
        if (remoteId == null || remoteId.isEmpty()) return;
        final String finalRemoteId = remoteId;
        switch (table) {
            case "workouts":         workoutDao.markSynced(localId, finalRemoteId);        break;
            case "set_records":      setRecordDao.markSynced(localId, finalRemoteId);      break;
            case "user_profiles":    userProfileDao.markSynced(localId, finalRemoteId);    break;
            case "goals":            goalDao.markSynced(localId, finalRemoteId);           break;
            case "workout_sessions": workoutSessionDao.markSynced(localId, finalRemoteId); break;
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  UTILITY
    // ═════════════════════════════════════════════════════════════════

    /** Inserts an entry into the local sync queue. */
    private void queueSync(String table, int localId, String operation, String payload) {
        try {
            syncQueueDao.insert(new SyncQueueEntryEntity(table, localId, operation, payload));
        } catch (Exception e) {
            android.util.Log.e("FitnessRepository",
                    "Failed to queue sync entry: " + e.getMessage());
        }
    }

    public void clearAllData(RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                workoutDao.deleteAll();
                exerciseDao.deleteAll();
                setRecordDao.deleteAll();
                syncQueueDao.deleteAll();
                userProfileDao.deleteAll();
                goalDao.deleteAll();
                workoutSessionDao.deleteAll();
                postSuccess(callback, null);
            } catch (Exception e) {
                postError(callback, "Failed to clear data: " + e.getMessage());
            }
        });
    }

    private <T> void postSuccess(RepositoryCallback<T> callback, T result) {
        if (callback != null) {
            new android.os.Handler(android.os.Looper.getMainLooper())
                    .post(() -> callback.onSuccess(result));
        }
    }

    private <T> void postError(RepositoryCallback<T> callback, String message) {
        if (callback != null) {
            new android.os.Handler(android.os.Looper.getMainLooper())
                    .post(() -> callback.onError(message));
        }
    }
}