package com.arcadefitness.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import static androidx.room.ForeignKey.CASCADE;

/**
 * WorkoutExerciseEntity.java
 * Junction table — resolves the many-to-many relationship between
 * WorkoutEntity and ExerciseEntity.
 *
 * One workout can contain many exercises.
 * One exercise can appear in many workouts.
 *
 * Additional fields: order_index (display order inside the workout),
 * planned_sets and planned_reps (override the exercise defaults).
 */
@Entity(
        tableName = "workout_exercises",
        primaryKeys = {"workout_id", "exercise_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = WorkoutEntity.class,
                        parentColumns = "id",
                        childColumns  = "workout_id",
                        onDelete      = CASCADE
                ),
                @ForeignKey(
                        entity = ExerciseEntity.class,
                        parentColumns = "id",
                        childColumns  = "exercise_id",
                        onDelete      = CASCADE
                )
        },
        indices = {
                @Index(value = {"workout_id"}),
                @Index(value = {"exercise_id"})
        }
)
public class WorkoutExerciseEntity {

    @ColumnInfo(name = "workout_id")
    private int workoutId;

    @ColumnInfo(name = "exercise_id")
    private int exerciseId;

    /** Display order of this exercise within the workout (0-based). */
    @ColumnInfo(name = "order_index", defaultValue = "0")
    private int orderIndex;

    /** Override the exercise default sets for this workout. */
    @ColumnInfo(name = "planned_sets", defaultValue = "3")
    private int plannedSets;

    /** Override the exercise default reps for this workout. */
    @ColumnInfo(name = "planned_reps", defaultValue = "10")
    private int plannedReps;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    // ── CONSTRUCTORS ─────────────────────────────────────────────────

    public WorkoutExerciseEntity() {}

    public WorkoutExerciseEntity(int workoutId, int exerciseId, int orderIndex,
                                 int plannedSets, int plannedReps) {
        this.workoutId   = workoutId;
        this.exerciseId  = exerciseId;
        this.orderIndex  = orderIndex;
        this.plannedSets = plannedSets;
        this.plannedReps = plannedReps;
        this.createdAt   = System.currentTimeMillis();
    }

    // ── GETTERS & SETTERS ─────────────────────────────────────────────

    public int  getWorkoutId()   { return workoutId; }
    public void setWorkoutId(int workoutId)   { this.workoutId = workoutId; }

    public int  getExerciseId()  { return exerciseId; }
    public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }

    public int  getOrderIndex()  { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public int  getPlannedSets() { return plannedSets; }
    public void setPlannedSets(int plannedSets) { this.plannedSets = plannedSets; }

    public int  getPlannedReps() { return plannedReps; }
    public void setPlannedReps(int plannedReps) { this.plannedReps = plannedReps; }

    public long getCreatedAt()   { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
