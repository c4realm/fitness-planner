package com.arcadefitness.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.arcadefitness.data.local.dao.ExerciseDao;
import com.arcadefitness.data.local.dao.GoalDao;
import com.arcadefitness.data.local.dao.SetRecordDao;
import com.arcadefitness.data.local.dao.SyncQueueDao;
import com.arcadefitness.data.local.dao.UserProfileDao;
import com.arcadefitness.data.local.dao.WorkoutDao;
import com.arcadefitness.data.local.dao.WorkoutExerciseDao;
import com.arcadefitness.data.local.dao.WorkoutSessionDao;
import com.arcadefitness.data.local.entity.ExerciseEntity;
import com.arcadefitness.data.local.entity.GoalEntity;
import com.arcadefitness.data.local.entity.SetRecordEntity;
import com.arcadefitness.data.local.entity.SyncQueueEntryEntity;
import com.arcadefitness.data.local.entity.UserProfileEntity;
import com.arcadefitness.data.local.entity.WorkoutEntity;
import com.arcadefitness.data.local.entity.WorkoutExerciseEntity;
import com.arcadefitness.data.local.entity.WorkoutSessionEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                WorkoutEntity.class,
                ExerciseEntity.class,
                SetRecordEntity.class,
                SyncQueueEntryEntity.class,
                UserProfileEntity.class,
                GoalEntity.class,
                WorkoutSessionEntity.class,
                WorkoutExerciseEntity.class
        },
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService DATABASE_WRITE_EXECUTOR =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract WorkoutDao workoutDao();
    public abstract ExerciseDao exerciseDao();
    public abstract SetRecordDao setRecordDao();
    public abstract SyncQueueDao syncQueueDao();
    public abstract UserProfileDao userProfileDao();
    public abstract GoalDao goalDao();
    public abstract WorkoutSessionDao workoutSessionDao();
    public abstract WorkoutExerciseDao workoutExerciseDao();

    AppDatabase() {}

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "arcade_fitness_db"
                            )
                            .addCallback(sRoomDatabaseCallback)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static ExecutorService getDatabaseWriteExecutor() {
        return DATABASE_WRITE_EXECUTOR;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);
                    DATABASE_WRITE_EXECUTOR.execute(() -> {
                        // Pre-populate default exercises on first creation
                        AppDatabase database = INSTANCE;
                        if (database != null) {
                            ExerciseDao exerciseDao = database.exerciseDao();
                            if (exerciseDao.getCount() == 0) {
                                long now = System.currentTimeMillis();
                                ExerciseEntity[] defaults = {
                                        createDefaultExercise("Bench Press", "Chest", "Barbell press lying flat on bench", 4, 10, now),
                                        createDefaultExercise("Incline Dumbbell Press", "Chest", "Press dumbbells on inclined bench", 3, 12, now),
                                        createDefaultExercise("Push-Ups", "Chest", "Chest press from floor position", 3, 15, now),
                                        createDefaultExercise("Dumbbell Row", "Back", "Row dumbbell to hip while braced", 4, 10, now),
                                        createDefaultExercise("Pull-Ups", "Back", "Bodyweight pull to bar from hang", 3, 8, now),
                                        createDefaultExercise("Lat Pulldown", "Back", "Pull cable bar down to chest", 3, 12, now),
                                        createDefaultExercise("Overhead Press", "Shoulders", "Press barbell overhead from shoulders", 4, 10, now),
                                        createDefaultExercise("Lateral Raise", "Shoulders", "Raise dumbbells out to shoulder height", 3, 15, now),
                                        createDefaultExercise("Front Raise", "Shoulders", "Raise dumbbells forward to shoulder", 3, 12, now),
                                        createDefaultExercise("Squat", "Legs", "Barbell squat to parallel depth", 4, 10, now),
                                        createDefaultExercise("Romanian Deadlift", "Legs", "Hip hinge with barbell, soft knees", 4, 10, now),
                                        createDefaultExercise("Leg Press", "Legs", "Push platform away with both legs", 3, 12, now),
                                        createDefaultExercise("Leg Extension", "Legs", "Extend legs on seated machine", 3, 12, now),
                                        createDefaultExercise("Leg Curl", "Legs", "Curl legs on lying machine", 3, 12, now),
                                        createDefaultExercise("Barbell Curl", "Arms", "Curl barbell up to shoulders", 3, 12, now),
                                        createDefaultExercise("Tricep Pushdown", "Arms", "Push cable bar down to hips", 3, 12, now),
                                        createDefaultExercise("Hammer Curl", "Arms", "Curl dumbbells with neutral grip", 3, 12, now),
                                        createDefaultExercise("Overhead Tricep Extension", "Arms", "Extend dumbbell overhead both hands", 3, 12, now),
                                        createDefaultExercise("Plank", "Core", "Hold straight body above ground", 3, 30, now),
                                        createDefaultExercise("Crunches", "Core", "Curl upper body off floor", 3, 20, now),
                                        createDefaultExercise("Hanging Leg Raise", "Core", "Raise legs straight while hanging", 3, 15, now),
                                        createDefaultExercise("Russian Twist", "Core", "Rotate torso side to side seated", 3, 20, now),
                                        createDefaultExercise("Deadlift", "Full Body", "Lift barbell from floor to hip", 4, 8, now),
                                        createDefaultExercise("Clean and Press", "Full Body", "Clean barbell then press overhead", 3, 8, now),
                                        createDefaultExercise("Burpees", "Full Body", "Full-body jump squat to floor", 3, 15, now)
                                };
                                for (ExerciseEntity exercise : defaults) {
                                    exerciseDao.insert(exercise);
                                }
                            }
                        }
                    });
                }

                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                    DATABASE_WRITE_EXECUTOR.execute(() -> {
                        // Pre-populate default exercises on every open if empty
                        AppDatabase database = INSTANCE;
                        if (database != null) {
                            ExerciseDao exerciseDao = database.exerciseDao();
                            if (exerciseDao.getCount() == 0) {
                                long now = System.currentTimeMillis();
                                ExerciseEntity[] defaults = {
                                        createDefaultExercise("Bench Press", "Chest", "Barbell press lying flat on bench", 4, 10, now),
                                        createDefaultExercise("Incline Dumbbell Press", "Chest", "Press dumbbells on inclined bench", 3, 12, now),
                                        createDefaultExercise("Push-Ups", "Chest", "Chest press from floor position", 3, 15, now),
                                        createDefaultExercise("Dumbbell Row", "Back", "Row dumbbell to hip while braced", 4, 10, now),
                                        createDefaultExercise("Pull-Ups", "Back", "Bodyweight pull to bar from hang", 3, 8, now),
                                        createDefaultExercise("Lat Pulldown", "Back", "Pull cable bar down to chest", 3, 12, now),
                                        createDefaultExercise("Overhead Press", "Shoulders", "Press barbell overhead from shoulders", 4, 10, now),
                                        createDefaultExercise("Lateral Raise", "Shoulders", "Raise dumbbells out to shoulder height", 3, 15, now),
                                        createDefaultExercise("Front Raise", "Shoulders", "Raise dumbbells forward to shoulder", 3, 12, now),
                                        createDefaultExercise("Squat", "Legs", "Barbell squat to parallel depth", 4, 10, now),
                                        createDefaultExercise("Romanian Deadlift", "Legs", "Hip hinge with barbell, soft knees", 4, 10, now),
                                        createDefaultExercise("Leg Press", "Legs", "Push platform away with both legs", 3, 12, now),
                                        createDefaultExercise("Leg Extension", "Legs", "Extend legs on seated machine", 3, 12, now),
                                        createDefaultExercise("Leg Curl", "Legs", "Curl legs on lying machine", 3, 12, now),
                                        createDefaultExercise("Barbell Curl", "Arms", "Curl barbell up to shoulders", 3, 12, now),
                                        createDefaultExercise("Tricep Pushdown", "Arms", "Push cable bar down to hips", 3, 12, now),
                                        createDefaultExercise("Hammer Curl", "Arms", "Curl dumbbells with neutral grip", 3, 12, now),
                                        createDefaultExercise("Overhead Tricep Extension", "Arms", "Extend dumbbell overhead both hands", 3, 12, now),
                                        createDefaultExercise("Plank", "Core", "Hold straight body above ground", 3, 30, now),
                                        createDefaultExercise("Crunches", "Core", "Curl upper body off floor", 3, 20, now),
                                        createDefaultExercise("Hanging Leg Raise", "Core", "Raise legs straight while hanging", 3, 15, now),
                                        createDefaultExercise("Russian Twist", "Core", "Rotate torso side to side seated", 3, 20, now),
                                        createDefaultExercise("Deadlift", "Full Body", "Lift barbell from floor to hip", 4, 8, now),
                                        createDefaultExercise("Clean and Press", "Full Body", "Clean barbell then press overhead", 3, 8, now),
                                        createDefaultExercise("Burpees", "Full Body", "Full-body jump squat to floor", 3, 15, now)
                                };
                                for (ExerciseEntity exercise : defaults) {
                                    exerciseDao.insert(exercise);
                                }
                            }
                        }
                    });
                }

                private ExerciseEntity createDefaultExercise(String name, String muscleGroup,
                                                              String description, int sets, int reps, long now) {
                    ExerciseEntity entity = new ExerciseEntity();
                    entity.setName(name);
                    entity.setDescription(description);
                    entity.setTargetMuscleGroup(muscleGroup);
                    entity.setDefaultSets(sets);
                    entity.setDefaultReps(reps);
                    entity.setCreatedAt(now);
                    entity.setIsSynced(1);
                    return entity;
                }
            };
}
