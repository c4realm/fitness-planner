package com.arcadefitness.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.arcadefitness.data.local.dao.ExerciseDao;
import com.arcadefitness.data.local.dao.SetRecordDao;
import com.arcadefitness.data.local.dao.SyncQueueDao;
import com.arcadefitness.data.local.dao.WorkoutDao;
import com.arcadefitness.data.local.entity.ExerciseEntity;
import com.arcadefitness.data.local.entity.SetRecordEntity;
import com.arcadefitness.data.local.entity.SyncQueueEntryEntity;
import com.arcadefitness.data.local.entity.WorkoutEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {
        WorkoutEntity.class,
        ExerciseEntity.class,
        SetRecordEntity.class,
        SyncQueueEntryEntity.class
    },
    version = 1,
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
                                createDefaultExercise("Bench Press", "Chest", 4, 10, now),
                                createDefaultExercise("Incline Dumbbell Press", "Chest", 3, 12, now),
                                createDefaultExercise("Push-Ups", "Chest", 3, 15, now),
                                createDefaultExercise("Dumbbell Row", "Back", 4, 10, now),
                                createDefaultExercise("Pull-Ups", "Back", 3, 8, now),
                                createDefaultExercise("Lat Pulldown", "Back", 3, 12, now),
                                createDefaultExercise("Overhead Press", "Shoulders", 4, 10, now),
                                createDefaultExercise("Lateral Raise", "Shoulders", 3, 15, now),
                                createDefaultExercise("Front Raise", "Shoulders", 3, 12, now),
                                createDefaultExercise("Squat", "Legs", 4, 10, now),
                                createDefaultExercise("Romanian Deadlift", "Legs", 4, 10, now),
                                createDefaultExercise("Leg Press", "Legs", 3, 12, now),
                                createDefaultExercise("Leg Extension", "Legs", 3, 12, now),
                                createDefaultExercise("Leg Curl", "Legs", 3, 12, now),
                                createDefaultExercise("Barbell Curl", "Arms", 3, 12, now),
                                createDefaultExercise("Tricep Pushdown", "Arms", 3, 12, now),
                                createDefaultExercise("Hammer Curl", "Arms", 3, 12, now),
                                createDefaultExercise("Overhead Tricep Extension", "Arms", 3, 12, now),
                                createDefaultExercise("Plank", "Core", 3, 30, now),
                                createDefaultExercise("Crunches", "Core", 3, 20, now),
                                createDefaultExercise("Hanging Leg Raise", "Core", 3, 15, now),
                                createDefaultExercise("Russian Twist", "Core", 3, 20, now),
                                createDefaultExercise("Deadlift", "Full Body", 4, 8, now),
                                createDefaultExercise("Clean and Press", "Full Body", 3, 8, now),
                                createDefaultExercise("Burpees", "Full Body", 3, 15, now)
                            };
                            for (ExerciseEntity exercise : defaults) {
                                exerciseDao.insert(exercise);
                            }
                        }
                    }
                });
            }

            private ExerciseEntity createDefaultExercise(String name, String muscleGroup,
                                                         int sets, int reps, long now) {
                ExerciseEntity entity = new ExerciseEntity();
                entity.setName(name);
                entity.setDescription("Standard " + name.toLowerCase() + " exercise");
                entity.setTargetMuscleGroup(muscleGroup);
                entity.setDefaultSets(sets);
                entity.setDefaultReps(reps);
                entity.setCreatedAt(now);
                entity.setIsSynced(1);
                return entity;
            }
        };
}
