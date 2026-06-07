package com.arcadefitness.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.arcadefitness.data.local.entity.WorkoutExerciseEntity;

import java.util.List;

/**
 * WorkoutExerciseDao.java
 * DAO for the workout_exercises junction table.
 * Queries exercises belonging to a workout and workouts containing an exercise.
 */
@Dao
public interface WorkoutExerciseDao {

    // ── INSERT / UPDATE / DELETE ──────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WorkoutExerciseEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WorkoutExerciseEntity> entities);

    @Update
    void update(WorkoutExerciseEntity entity);

    @Delete
    void delete(WorkoutExerciseEntity entity);

    // ── QUERIES ───────────────────────────────────────────────────────

    /** All junction rows for a specific workout, ordered by display position. */
    @Query("SELECT * FROM workout_exercises WHERE workout_id = :workoutId ORDER BY order_index ASC")
    List<WorkoutExerciseEntity> getByWorkoutId(int workoutId);

    @Query("SELECT * FROM workout_exercises WHERE workout_id = :workoutId ORDER BY order_index ASC")
    LiveData<List<WorkoutExerciseEntity>> getByWorkoutIdLiveData(int workoutId);

    /** All junction rows for a specific exercise (which workouts include it). */
    @Query("SELECT * FROM workout_exercises WHERE exercise_id = :exerciseId")
    List<WorkoutExerciseEntity> getByExerciseId(int exerciseId);

    /** Count of exercises in a workout. */
    @Query("SELECT COUNT(*) FROM workout_exercises WHERE workout_id = :workoutId")
    int getExerciseCountForWorkout(int workoutId);

    /** Remove all exercises from a workout (e.g. when deleting). */
    @Query("DELETE FROM workout_exercises WHERE workout_id = :workoutId")
    void deleteByWorkoutId(int workoutId);

    /** Remove a specific exercise from a specific workout. */
    @Query("DELETE FROM workout_exercises WHERE workout_id = :workoutId AND exercise_id = :exerciseId")
    void deleteByWorkoutAndExercise(int workoutId, int exerciseId);

    @Query("DELETE FROM workout_exercises")
    void deleteAll();
}
