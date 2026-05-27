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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;
import java.util.concurrent.ExecutorService;

import retrofit2.Call;
import retrofit2.Callback;
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

    private final MutableLiveData<Boolean> syncInProgress = new MutableLiveData<>(false);
    private final MutableLiveData<String> syncStatusMessage = new MutableLiveData<>("");

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    private FitnessRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.workoutDao = db.workoutDao();
        this.exerciseDao = db.exerciseDao();
        this.setRecordDao = db.setRecordDao();
        this.syncQueueDao = db.syncQueueDao();
        this.userProfileDao = db.userProfileDao();
        this.goalDao = db.goalDao();
        this.workoutSessionDao = db.workoutSessionDao();
        this.executor = AppDatabase.DATABASE_WRITE_EXECUTOR;
        this.apiService = RetrofitClient.getInstance().getApiService();
        this.gson = new Gson();
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

    public LiveData<Boolean> getSyncInProgress() {
        return syncInProgress;
    }

    public LiveData<String> getSyncStatusMessage() {
        return syncStatusMessage;
    }

    // ═════════════════════════════════════════════════════════════════
    //  WORKOUTS
    // ═════════════════════════════════════════════════════════════════

    public void getAllWorkouts(RepositoryCallback<List<WorkoutEntity>> callback) {
        executor.execute(() -> {
            try {
                List<WorkoutEntity> workouts = workoutDao.getAll();
                postSuccess(callback, workouts);
            } catch (Exception e) {
                postError(callback, "Failed to load workouts: " + e.getMessage());
            }
        });
    }

    public void getWorkoutById(int workoutId, RepositoryCallback<WorkoutEntity> callback) {
        executor.execute(() -> {
            try {
                WorkoutEntity workout = workoutDao.getById(workoutId);
                if (workout != null) {
                    postSuccess(callback, workout);
                } else {
                    postError(callback, "Workout not found");
                }
            } catch (Exception e) {
                postError(callback, "Failed to load workout: " + e.getMessage());
            }
        });
    }

    public void insertWorkout(WorkoutEntity workout, RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                long id = workoutDao.insert(workout);
                int localId = (int) id;
                workout.setId(localId);

                String payload = gson.toJson(workout);
                SyncQueueEntryEntity syncEntry = new SyncQueueEntryEntity(
                    "workouts", localId, "INSERT", payload
                );
                syncQueueDao.insert(syncEntry);

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

                String payload = gson.toJson(workout);
                SyncQueueEntryEntity syncEntry = new SyncQueueEntryEntity(
                    "workouts", workout.getId(), "UPDATE", payload
                );
                syncQueueDao.insert(syncEntry);

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

                String payload = gson.toJson(workout);
                SyncQueueEntryEntity syncEntry = new SyncQueueEntryEntity(
                    "workouts", workout.getId(), "DELETE", payload
                );
                syncQueueDao.insert(syncEntry);

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
                List<ExerciseEntity> exercises = exerciseDao.getAll();
                postSuccess(callback, exercises);
            } catch (Exception e) {
                postError(callback, "Failed to load exercises: " + e.getMessage());
            }
        });
    }

    public void getExerciseById(int exerciseId, RepositoryCallback<ExerciseEntity> callback) {
        executor.execute(() -> {
            try {
                ExerciseEntity exercise = exerciseDao.getById(exerciseId);
                if (exercise != null) {
                    postSuccess(callback, exercise);
                } else {
                    postError(callback, "Exercise not found");
                }
            } catch (Exception e) {
                postError(callback, "Failed to load exercise: " + e.getMessage());
            }
        });
    }

    public void searchExercises(String query, RepositoryCallback<List<ExerciseEntity>> callback) {
        executor.execute(() -> {
            try {
                List<ExerciseEntity> results = exerciseDao.search(query);
                postSuccess(callback, results);
            } catch (Exception e) {
                postError(callback, "Search failed: " + e.getMessage());
            }
        });
    }

    public void getExercisesByMuscleGroup(String muscleGroup,
                                          RepositoryCallback<List<ExerciseEntity>> callback) {
        executor.execute(() -> {
            try {
                List<ExerciseEntity> results = exerciseDao.getByMuscleGroup(muscleGroup);
                postSuccess(callback, results);
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

                String payload = gson.toJson(exercise);
                SyncQueueEntryEntity syncEntry = new SyncQueueEntryEntity(
                    "exercises", localId, "INSERT", payload
                );
                syncQueueDao.insert(syncEntry);

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

                String payload = gson.toJson(exercise);
                SyncQueueEntryEntity syncEntry = new SyncQueueEntryEntity(
                    "exercises", exercise.getId(), "UPDATE", payload
                );
                syncQueueDao.insert(syncEntry);

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
                List<SetRecordEntity> records = setRecordDao.getByWorkoutId(workoutId);
                postSuccess(callback, records);
            } catch (Exception e) {
                postError(callback, "Failed to load set records: " + e.getMessage());
            }
        });
    }

    public void getSetRecordsByWorkoutAndExercise(int workoutId, int exerciseId,
                                                  RepositoryCallback<List<SetRecordEntity>> callback) {
        executor.execute(() -> {
            try {
                List<SetRecordEntity> records =
                    setRecordDao.getByWorkoutAndExercise(workoutId, exerciseId);
                postSuccess(callback, records);
            } catch (Exception e) {
                postError(callback, "Failed to load set records: " + e.getMessage());
            }
        });
    }

    public void insertSetRecord(SetRecordEntity setRecord, RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                long id = setRecordDao.insert(setRecord);
                int localId = (int) id;
                setRecord.setId(localId);

                String payload = gson.toJson(setRecord);
                SyncQueueEntryEntity syncEntry = new SyncQueueEntryEntity(
                    "set_records", localId, "INSERT", payload
                );
                syncQueueDao.insert(syncEntry);

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

                String payload = gson.toJson(setRecord);
                SyncQueueEntryEntity syncEntry = new SyncQueueEntryEntity(
                    "set_records", setRecord.getId(), "UPDATE", payload
                );
                syncQueueDao.insert(syncEntry);

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
                long timestamp = System.currentTimeMillis();
                setRecordDao.markCompleted(setId, weight, reps, timestamp);

                SetRecordEntity record = setRecordDao.getById(setId);
                if (record != null) {
                    record.setIsCompleted(1);
                    record.setWeight(weight);
                    record.setReps(reps);
                    record.setTimestamp(timestamp);

                    String payload = gson.toJson(record);
                    SyncQueueEntryEntity syncEntry = new SyncQueueEntryEntity(
                        "set_records", setId, "UPDATE", payload
                    );
                    syncQueueDao.insert(syncEntry);
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

                String payload = gson.toJson(setRecord);
                SyncQueueEntryEntity syncEntry = new SyncQueueEntryEntity(
                    "set_records", setRecord.getId(), "DELETE", payload
                );
                syncQueueDao.insert(syncEntry);

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
                int count = setRecordDao.getCompletedCountByWorkout(workoutId);
                postSuccess(callback, count);
            } catch (Exception e) {
                postError(callback, "Failed to get completed count: " + e.getMessage());
            }
        });
    }

    public void getTotalVolumeByWorkout(int workoutId,
                                        RepositoryCallback<Double> callback) {
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
                List<UserProfileEntity> profiles = userProfileDao.getAll();
                postSuccess(callback, profiles);
            } catch (Exception e) {
                postError(callback, "Failed to load profiles: " + e.getMessage());
            }
        });
    }

    public void getUserProfileById(int profileId, RepositoryCallback<UserProfileEntity> callback) {
        executor.execute(() -> {
            try {
                UserProfileEntity profile = userProfileDao.getById(profileId);
                if (profile != null) {
                    postSuccess(callback, profile);
                } else {
                    postError(callback, "Profile not found");
                }
            } catch (Exception e) {
                postError(callback, "Failed to load profile: " + e.getMessage());
            }
        });
    }

    public void getUserProfileByEmail(String email, RepositoryCallback<UserProfileEntity> callback) {
        executor.execute(() -> {
            try {
                UserProfileEntity profile = userProfileDao.getByEmail(email);
                postSuccess(callback, profile);
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

                String payload = gson.toJson(profile);
                SyncQueueEntryEntity syncEntry = new SyncQueueEntryEntity(
                    "user_profiles", localId, "INSERT", payload
                );
                syncQueueDao.insert(syncEntry);

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

                String payload = gson.toJson(profile);
                SyncQueueEntryEntity syncEntry = new SyncQueueEntryEntity(
                    "user_profiles", profile.getId(), "UPDATE", payload
                );
                syncQueueDao.insert(syncEntry);

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

                String payload = gson.toJson(profile);
                SyncQueueEntryEntity syncEntry = new SyncQueueEntryEntity(
                    "user_profiles", profile.getId(), "DELETE", payload
                );
                syncQueueDao.insert(syncEntry);

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
                List<GoalEntity> goals = goalDao.getAll();
                postSuccess(callback, goals);
            } catch (Exception e) {
                postError(callback, "Failed to load goals: " + e.getMessage());
            }
        });
    }

    public void getActiveGoals(RepositoryCallback<List<GoalEntity>> callback) {
        executor.execute(() -> {
            try {
                List<GoalEntity> goals = goalDao.getActiveGoals();
                postSuccess(callback, goals);
            } catch (Exception e) {
                postError(callback, "Failed to load active goals: " + e.getMessage());
            }
        });
    }

    public void getGoalById(int goalId, RepositoryCallback<GoalEntity> callback) {
        executor.execute(() -> {
            try {
                GoalEntity goal = goalDao.getById(goalId);
                if (goal != null) {
                    postSuccess(callback, goal);
                } else {
                    postError(callback, "Goal not found");
                }
            } catch (Exception e) {
                postError(callback, "Failed to load goal: " + e.getMessage());
            }
        });
    }

    public void insertGoal(GoalEntity goal, RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                long id = goalDao.insert(goal);
                int localId = (int) id;
                goal.setId(localId);

                String payload = gson.toJson(goal);
                SyncQueueEntryEntity syncEntry = new SyncQueueEntryEntity(
                    "goals", localId, "INSERT", payload
                );
                syncQueueDao.insert(syncEntry);

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

                String payload = gson.toJson(goal);
                SyncQueueEntryEntity syncEntry = new SyncQueueEntryEntity(
                    "goals", goal.getId(), "UPDATE", payload
                );
                syncQueueDao.insert(syncEntry);

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

                String payload = gson.toJson(goal);
                SyncQueueEntryEntity syncEntry = new SyncQueueEntryEntity(
                    "goals", goal.getId(), "DELETE", payload
                );
                syncQueueDao.insert(syncEntry);

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
                List<WorkoutSessionEntity> sessions = workoutSessionDao.getAll();
                postSuccess(callback, sessions);
            } catch (Exception e) {
                postError(callback, "Failed to load sessions: " + e.getMessage());
            }
        });
    }

    public void getCompletedSessions(RepositoryCallback<List<WorkoutSessionEntity>> callback) {
        executor.execute(() -> {
            try {
                List<WorkoutSessionEntity> sessions = workoutSessionDao.getCompletedSessions();
                postSuccess(callback, sessions);
            } catch (Exception e) {
                postError(callback, "Failed to load completed sessions: " + e.getMessage());
            }
        });
    }

    public void getWorkoutSessionById(int sessionId, RepositoryCallback<WorkoutSessionEntity> callback) {
        executor.execute(() -> {
            try {
                WorkoutSessionEntity session = workoutSessionDao.getById(sessionId);
                if (session != null) {
                    postSuccess(callback, session);
                } else {
                    postError(callback, "Session not found");
                }
            } catch (Exception e) {
                postError(callback, "Failed to load session: " + e.getMessage());
            }
        });
    }

    public void getCurrentSession(RepositoryCallback<WorkoutSessionEntity> callback) {
        executor.execute(() -> {
            try {
                WorkoutSessionEntity session = workoutSessionDao.getCurrentSession();
                postSuccess(callback, session);
            } catch (Exception e) {
                postError(callback, "Failed to load current session: " + e.getMessage());
            }
        });
    }

    public void insertWorkoutSession(WorkoutSessionEntity session, RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                long id = workoutSessionDao.insert(session);
                int localId = (int) id;
                session.setId(localId);

                String payload = gson.toJson(session);
                SyncQueueEntryEntity syncEntry = new SyncQueueEntryEntity(
                    "workout_sessions", localId, "INSERT", payload
                );
                syncQueueDao.insert(syncEntry);

                postSuccess(callback, localId);
            } catch (Exception e) {
                postError(callback, "Failed to save session: " + e.getMessage());
            }
        });
    }

    public void updateWorkoutSession(WorkoutSessionEntity session, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                workoutSessionDao.update(session);

                String payload = gson.toJson(session);
                SyncQueueEntryEntity syncEntry = new SyncQueueEntryEntity(
                    "workout_sessions", session.getId(), "UPDATE", payload
                );
                syncQueueDao.insert(syncEntry);

                postSuccess(callback, null);
            } catch (Exception e) {
                postError(callback, "Failed to update session: " + e.getMessage());
            }
        });
    }

    public void completeWorkoutSession(int sessionId, int durationMinutes, int caloriesBurned,
                                       double totalVolume, int rating, String notes,
                                       RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                WorkoutSessionEntity session = workoutSessionDao.getById(sessionId);
                if (session == null) {
                    postError(callback, "Session not found");
                    return;
                }
                session.setStatus("COMPLETED");
                session.setEndTimestamp(System.currentTimeMillis());
                session.setDurationMinutes(durationMinutes);
                session.setCaloriesBurned(caloriesBurned);
                session.setTotalVolume(totalVolume);
                session.setRating(rating);
                session.setNotes(notes);
                workoutSessionDao.update(session);

                String payload = gson.toJson(session);
                SyncQueueEntryEntity syncEntry = new SyncQueueEntryEntity(
                    "workout_sessions", sessionId, "UPDATE", payload
                );
                syncQueueDao.insert(syncEntry);

                postSuccess(callback, null);
            } catch (Exception e) {
                postError(callback, "Failed to complete session: " + e.getMessage());
            }
        });
    }

    public void deleteWorkoutSession(WorkoutSessionEntity session, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                workoutSessionDao.delete(session);

                String payload = gson.toJson(session);
                SyncQueueEntryEntity syncEntry = new SyncQueueEntryEntity(
                    "workout_sessions", session.getId(), "DELETE", payload
                );
                syncQueueDao.insert(syncEntry);

                postSuccess(callback, null);
            } catch (Exception e) {
                postError(callback, "Failed to delete session: " + e.getMessage());
            }
        });
    }

    public void getSessionsSince(long fromTimestamp, RepositoryCallback<List<WorkoutSessionEntity>> callback) {
        executor.execute(() -> {
            try {
                List<WorkoutSessionEntity> sessions = workoutSessionDao.getCompletedSessions();
                postSuccess(callback, sessions);
            } catch (Exception e) {
                postError(callback, "Failed to load sessions: " + e.getMessage());
            }
        });
    }

    public void getWeeklyStats(RepositoryCallback<int[]> callback) {
        executor.execute(() -> {
            try {
                long weekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000;
                int sessionsCount = workoutSessionDao.getCompletedCountSince(weekAgo);
                Integer totalDuration = workoutSessionDao.getTotalDurationSince(weekAgo);
                Integer totalCalories = workoutSessionDao.getTotalCaloriesSince(weekAgo);
                Double totalVolume = workoutSessionDao.getTotalVolumeSince(weekAgo);
                postSuccess(callback, new int[]{
                    sessionsCount,
                    totalDuration != null ? totalDuration : 0,
                    totalCalories != null ? totalCalories : 0,
                    totalVolume != null ? totalVolume.intValue() : 0
                });
            } catch (Exception e) {
                postError(callback, "Failed to load weekly stats: " + e.getMessage());
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════
    //  SYNC ENGINE
    // ═════════════════════════════════════════════════════════════════

    public void syncOfflineQueue() {
        processSyncQueue();
    }

    public void processSyncQueue() {
        if (Boolean.TRUE.equals(syncInProgress.getValue())) {
            return;
        }

        syncInProgress.postValue(true);
        syncStatusMessage.postValue("Syncing...");

        executor.execute(() -> {
            try {
                List<SyncQueueEntryEntity> pendingEntries =
                    syncQueueDao.getPendingAndFailed();

                if (pendingEntries.isEmpty()) {
                    syncInProgress.postValue(false);
                    syncStatusMessage.postValue("All synced");
                    return;
                }

                for (SyncQueueEntryEntity entry : pendingEntries) {
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
            String tableName = entry.getTableName();
            String operation = entry.getOperationType();
            Call<JsonObject> call = null;

            if ("workouts".equals(tableName)) {
                call = buildWorkoutSyncCall(operation, payload);
            } else if ("exercises".equals(tableName)) {
                call = buildExerciseSyncCall(operation, payload);
            } else if ("set_records".equals(tableName)) {
                call = buildSetRecordSyncCall(operation, payload);
            } else if ("user_profiles".equals(tableName)) {
                call = buildUserProfileSyncCall(operation, payload);
            } else if ("goals".equals(tableName)) {
                call = buildGoalSyncCall(operation, payload);
            } else if ("workout_sessions".equals(tableName)) {
                call = buildWorkoutSessionSyncCall(operation, payload);
            }

            if (call != null) {
                Response<JsonObject> response = call.execute();
                if (response.isSuccessful()) {
                    syncQueueDao.markCompleted(entry.getId());
                    markLocalEntitySynced(tableName, entry.getRecordId(), response.body());
                } else {
                    syncQueueDao.markFailed(entry.getId(),
                        "HTTP " + response.code() + ": " + response.message());
                }
            } else {
                syncQueueDao.markCompleted(entry.getId());
            }
        } catch (Exception e) {
            syncQueueDao.markFailed(entry.getId(), e.getMessage());
        }
    }

    private Call<JsonObject> buildWorkoutSyncCall(String operation, JsonObject payload) {
        switch (operation) {
            case "INSERT":
                return apiService.createWorkout(payload);
            case "UPDATE":
                String workoutRemoteId = getRemoteId(payload, "remoteId");
                if (workoutRemoteId != null) {
                    return apiService.updateWorkout(workoutRemoteId, payload);
                }
                return apiService.createWorkout(payload);
            case "DELETE":
                String deleteWorkoutRemoteId = getRemoteId(payload, "remoteId");
                if (deleteWorkoutRemoteId != null) {
                    return apiService.deleteWorkout(deleteWorkoutRemoteId);
                }
                return null;
            default:
                return null;
        }
    }

    private Call<JsonObject> buildExerciseSyncCall(String operation, JsonObject payload) {
        switch (operation) {
            case "INSERT":
                return apiService.createExercise(payload);
            case "UPDATE":
                String exerciseRemoteId = getRemoteId(payload, "remoteId");
                if (exerciseRemoteId != null) {
                    return apiService.updateExercise(exerciseRemoteId, payload);
                }
                return apiService.createExercise(payload);
            case "DELETE":
                String deleteExerciseRemoteId = getRemoteId(payload, "remoteId");
                if (deleteExerciseRemoteId != null) {
                    return apiService.deleteExercise(deleteExerciseRemoteId);
                }
                return null;
            default:
                return null;
        }
    }

    private Call<JsonObject> buildSetRecordSyncCall(String operation, JsonObject payload) {
        switch (operation) {
            case "INSERT":
                return apiService.createSetRecord(payload);
            case "UPDATE":
                String setRemoteId = getRemoteId(payload, "remoteId");
                if (setRemoteId != null) {
                    return apiService.updateSetRecord(setRemoteId, payload);
                }
                return apiService.createSetRecord(payload);
            case "DELETE":
                String deleteSetRemoteId = getRemoteId(payload, "remoteId");
                if (deleteSetRemoteId != null) {
                    return null;
                }
                return null;
            default:
                return null;
        }
    }

    private Call<JsonObject> buildUserProfileSyncCall(String operation, JsonObject payload) {
        switch (operation) {
            case "INSERT":
                return apiService.createUserProfile(payload);
            case "UPDATE":
                String profileRemoteId = getRemoteId(payload, "remoteId");
                if (profileRemoteId != null) {
                    return apiService.updateUserProfile(profileRemoteId, payload);
                }
                return apiService.createUserProfile(payload);
            case "DELETE":
                String deleteProfileRemoteId = getRemoteId(payload, "remoteId");
                if (deleteProfileRemoteId != null) {
                    return apiService.deleteUserProfile(deleteProfileRemoteId);
                }
                return null;
            default:
                return null;
        }
    }

    private Call<JsonObject> buildGoalSyncCall(String operation, JsonObject payload) {
        switch (operation) {
            case "INSERT":
                return apiService.createGoal(payload);
            case "UPDATE":
                String goalRemoteId = getRemoteId(payload, "remoteId");
                if (goalRemoteId != null) {
                    return apiService.updateGoal(goalRemoteId, payload);
                }
                return apiService.createGoal(payload);
            case "DELETE":
                String deleteGoalRemoteId = getRemoteId(payload, "remoteId");
                if (deleteGoalRemoteId != null) {
                    return apiService.deleteGoal(deleteGoalRemoteId);
                }
                return null;
            default:
                return null;
        }
    }

    private Call<JsonObject> buildWorkoutSessionSyncCall(String operation, JsonObject payload) {
        switch (operation) {
            case "INSERT":
                return apiService.createWorkoutSession(payload);
            case "UPDATE":
                String sessionRemoteId = getRemoteId(payload, "remoteId");
                if (sessionRemoteId != null) {
                    return apiService.updateWorkoutSession(sessionRemoteId, payload);
                }
                return apiService.createWorkoutSession(payload);
            case "DELETE":
                String deleteSessionRemoteId = getRemoteId(payload, "remoteId");
                if (deleteSessionRemoteId != null) {
                    return apiService.deleteWorkoutSession(deleteSessionRemoteId);
                }
                return null;
            default:
                return null;
        }
    }

    private String getRemoteId(JsonObject payload, String key) {
        if (payload.has(key) && !payload.get(key).isJsonNull()) {
            String value = payload.get(key).getAsString();
            if (value != null && !value.isEmpty() && !value.equals("null")) {
                return value;
            }
        }
        return null;
    }

    private void markLocalEntitySynced(String tableName, int localId, JsonObject response) {
        if (response == null) return;

        String remoteId = null;
        if (response.has("id") && !response.get("id").isJsonNull()) {
            remoteId = response.get("id").getAsString();
        } else if (response.has("_id") && !response.get("_id").isJsonNull()) {
            remoteId = response.get("_id").getAsString();
        }

        String finalRemoteId = (remoteId != null && !remoteId.isEmpty()) ? remoteId : null;
        switch (tableName) {
            case "workouts":
                workoutDao.markSynced(localId, finalRemoteId);
                break;
            case "exercises":
                exerciseDao.markSynced(localId, finalRemoteId);
                break;
            case "set_records":
                setRecordDao.markSynced(localId, finalRemoteId);
                break;
            case "user_profiles":
                userProfileDao.markSynced(localId, finalRemoteId);
                break;
            case "goals":
                goalDao.markSynced(localId, finalRemoteId);
                break;
            case "workout_sessions":
                workoutSessionDao.markSynced(localId, finalRemoteId);
                break;
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

    // ═════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═════════════════════════════════════════════════════════════════

    private <T> void postSuccess(final RepositoryCallback<T> callback, final T result) {
        if (callback != null) {
            new android.os.Handler(android.os.Looper.getMainLooper()).post(
                () -> callback.onSuccess(result)
            );
        }
    }

    private <T> void postError(final RepositoryCallback<T> callback, final String errorMessage) {
        if (callback != null) {
            new android.os.Handler(android.os.Looper.getMainLooper()).post(
                () -> callback.onError(errorMessage)
            );
        }
    }
}
