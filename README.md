# Arcade Fitness Planner

Phase 2 technical documentation for St. Mary's University.

Instructor: Dawit Yetemgeta

## Overview

Arcade Fitness Planner is a native Android application written in Java for the St. Mary's University Mobile Application Development submission.
Phase 2 adds the core fitness domain: workout planning, exercise browsing, workout tracking, progress monitoring, local Room storage, and offline-first sync support.

The app uses MVVM, Room, LiveData, RecyclerView, and a repository split that keeps local reads responsive while preserving transactional writes and sync queue handling.

## Phase 2 Architecture Tree

```text
app/src/main/java/com/arcadefitness/
├── activities/
│   ├── SplashActivity.java
│   ├── LoginActivity.java
│   ├── RegisterActivity.java
│   ├── DashboardActivity.java
│   ├── WorkoutPlannerActivity.java
│   ├── WorkoutTrackingActivity.java
│   ├── ExerciseLibraryActivity.java
│   └── ProgressTrackingActivity.java
├── viewmodel/
│   ├── DashboardViewModel.java
│   ├── WorkoutPlannerViewModel.java
│   ├── WorkoutTrackingViewModel.java
│   └── ProgressViewModel.java
├── data/
│   ├── local/
│   │   ├── AppDatabase.java
│   │   ├── dao/
│   │   │   ├── WorkoutDao.java
│   │   │   ├── ExerciseDao.java
│   │   │   ├── SetRecordDao.java
│   │   │   ├── SyncQueueDao.java
│   │   │   ├── UserProfileDao.java
│   │   │   ├── GoalDao.java
│   │   │   └── WorkoutSessionDao.java
│   │   ├── entity/
│   │   │   ├── WorkoutEntity.java
│   │   │   ├── ExerciseEntity.java
│   │   │   ├── SetRecordEntity.java
│   │   │   ├── SyncQueueEntryEntity.java
│   │   │   ├── UserProfileEntity.java
│   │   │   ├── GoalEntity.java
│   │   │   └── WorkoutSessionEntity.java
│   │   └── repository/
│   │       ├── WorkoutRepository.java
│   │       └── ExerciseRepository.java
│   ├── repository/
│   │   └── FitnessRepository.java
│   └── remote/
│       ├── ApiService.java
│       └── RetrofitClient.java
├── adapter/
│   ├── ExerciseAdapter.java
│   ├── WorkoutSessionAdapter.java
│   └── ProgressAdapter.java
└── network/
    └── NetworkChangeReceiver.java
```

## 7-Table Database Schema

### 1. `workouts`

Key columns: `id`, `name`, `description`, `target_muscle_group`, `estimated_duration_minutes`, `exercise_count`, `created_at`, `updated_at`, `is_synced`, `remote_id`

Purpose: stores workout plans shown in the planner and used as the parent record for workout sessions and set records.

Relationships: parent table for `workout_sessions.workout_id` and `set_records.workout_id`.

### 2. `exercises`

Key columns: `id`, `name`, `description`, `target_muscle_group`, `default_sets`, `default_reps`, `thumbnail_url`, `created_at`, `is_synced`, `remote_id`

Purpose: stores the exercise library used by the exercise browser and as the child table for set logging.

Relationships: parent table for `set_records.exercise_id`.

### 3. `set_records`

Key columns: `id`, `workout_id`, `exercise_id`, `set_number`, `weight`, `reps`, `is_completed`, `timestamp`, `is_synced`, `remote_id`

Purpose: stores each logged set during workout tracking.

Relationships: `set_records.workout_id -> workouts.id CASCADE`, `set_records.exercise_id -> exercises.id CASCADE`.

### 4. `sync_queue`

Key columns: `id`, `table_name`, `record_id`, `operation`, `payload`, `status`, `created_at`, `last_attempt_at`, `attempt_count`

Purpose: queues local writes for deferred upload when connectivity is available.

Relationships: references local records by table name and record id; processed by the sync pipeline.

### 5. `user_profiles`

Key columns: `id`, `full_name`, `email`, `age`, `gender`, `goal`, `profile_image_url`, `created_at`, `updated_at`, `is_synced`, `remote_id`

Purpose: stores user profile metadata for the authenticated user.

Relationships: standalone table, used by profile and sync features.

### 6. `goals`

Key columns: `id`, `title`, `description`, `type`, `target_value`, `current_value`, `unit`, `start_date`, `target_date`, `status`, `created_at`, `updated_at`, `is_synced`, `remote_id`

Purpose: stores active and completed progress goals shown on the progress screen.

Relationships: standalone table, tracked through the repository and sync queue.

### 7. `workout_sessions`

Key columns: `id`, `workout_id`, `start_timestamp`, `end_timestamp`, `duration_minutes`, `calories_burned`, `total_volume`, `status`, `notes`, `rating`, `created_at`, `is_synced`, `remote_id`

Purpose: stores each workout execution instance and drives progress statistics.

Relationships: `workout_sessions.workout_id -> workouts.id CASCADE`.

## MVVM Data Flow

```text
UI Screen (Activity)
   |
   v
ViewModel
   |
   +------------------------------+
   |                              |
   v                              v
LiveData read path            FitnessRepository write path
   |                              |
   v                              v
Repository                     DAO / Transaction
   |                              |
   v                              v
DAO ---------------------------> Room
   |                              |
   v                              v
SQLite                      SyncQueueEntryEntity
                                   |
                                   v
                           NetworkChangeReceiver
                                   |
                                   v
                          Retrofit / ApiService
                                   |
                                   v
                              Remote backend
```

## Local Build Instructions

### Prerequisites

* Android Studio Hedgehog or newer
* JDK 17
* Android SDK 34
* Device or emulator running Android 7.0+ (minSdk 24)

### Build and Run

1. Clone the repository.
2. Open the project in Android Studio.
3. Let Gradle sync finish.
4. Run the `app` configuration on a connected device or emulator.

### Offline-first note

`google-services.json` has been intentionally removed for Phase 2 offline-first development.
Google Sign-In is disabled in this phase, and the app now builds without Firebase or Play Services auth configuration.

## Key Architectural Decisions

### Two repository layers

* `WorkoutRepository` and `ExerciseRepository` are LiveData-first repositories used by the UI for fast local reads and automatic RecyclerView refresh.
* `FitnessRepository` handles transactional writes, query callbacks, and sync queue creation for offline-first persistence.

### Offline-first sync queue

* Every write inserted through `FitnessRepository` also creates a `SyncQueueEntryEntity` record.
* `NetworkChangeReceiver` watches connectivity and allows deferred sync work to resume when the device is online.
* This keeps the UI responsive and preserves local data even when the network is unavailable.

### Cascading workout sessions

* `WorkoutSessionEntity` uses `@ForeignKey(... onDelete = CASCADE)` to `WorkoutEntity`.
* When a workout is removed, its related sessions stay consistent with the parent lifecycle.
