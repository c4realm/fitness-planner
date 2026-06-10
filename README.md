<div align="center">

<img src="/arcade-fitness-app/docs/readme_banner.png" width="100%" alt="Arcade Fitness Planner Logo" />

# Arcade Fitness Planner

### **Plan it. Track it. Do it.**

<br/>

![Platform](https://img.shields.io/badge/Platform-Android%20API%2024+-brightgreen?style=for-the-badge&logo=android)
![Language](https://img.shields.io/badge/Language-Java-orange?style=for-the-badge&logo=openjdk)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue?style=for-the-badge)
![Database](https://img.shields.io/badge/Database-Room%20SQLite-ff6b00?style=for-the-badge)
![Phase](https://img.shields.io/badge/Phase%202-Complete%20✓-brightgreen?style=for-the-badge)
![University](https://img.shields.io/badge/St.%20Mary's%20University-Academic%20Project-red?style=for-the-badge)

</div>

---

## Overview

**Arcade Fitness Planner** is a full-stack Android fitness application developed as a project for the **Mobile Application Development** course at **St. Mary's University**.

The system allows users to plan workouts, track live sessions with set-by-set logging, browse an exercise library, monitor progress, set goals, calculate BMI, and sync all data to a PostgreSQL backend — working fully offline when no connection is available.


---

## Team

| Name | Role |
| ------------------- | ----------------------- |
| **Amar Abdulmejid** | Lead Developer |
| **Kaleab Dejene** | UI/UX & Frontend |
| **Kidus Kibrom** | Backend & API |
| **Yonas Ajanew** | Database & Testing |

---

## Academic Information

| Field | Details |
| --------------- | ------------------------------ |
| **Instructor** | Dawit Yetemgeta |
| **Course** | Mobile Application Development |
| **Department** | Computer Science |
| **Institution** | St. Mary's University |

---

## ✅ Development Phases

### Phase 1 — Onboarding
| Screen | Status |
| ------------------- | ------------ |
| Splash Screen — animated rotating rings | ✅ Complete |
| Login Screen — validation, offline fallback | ✅ Complete |
| Registration Screen — full field validation | ✅ Complete |
| Dashboard Screen | ✅ Complete |

### Phase 2 — Core MVVM Engine
| Feature | Status |
| --------------------------------------- | ------------ |
| 8-Table Room Database Schema | ✅ Complete |
| MVVM ViewModels — 4 screens | ✅ Complete |
| Repository Layer — 3 repositories | ✅ Complete |
| Workout Planner — create and browse | ✅ Complete |
| Exercise Library — 25 exercises, muscle group filter | ✅ Complete |
| Live Workout Tracking — timer, set logging, complete flow | ✅ Complete |
| Workout History Screen | ✅ Complete |
| Progress & Goals Tracking | ✅ Complete |
| Offline-First Sync Queue + NetworkChangeReceiver | ✅ Complete |
| Guest Access Flow | ✅ Complete |
| Dark / Light Theme | ✅ Complete |

### Phase 3 — Backend, Profile & Health
| Feature | Status |
| --------------------------------------- | ------------ |
| Node.js / Express REST API — 8 route groups | ✅ Complete |
| PostgreSQL Schema — 8 tables, 3NF, 25 exercises seeded | ✅ Complete |
| JWT authentication + bcrypt password hashing | ✅ Complete |
| Retrofit2 live API integration — login, register, sync | ✅ Complete |
| EncryptedSharedPreferences + SHA-256 credential hashing | ✅ Complete |
| User Profile Screen — view, edit, body metrics | ✅ Complete |
| BMI Calculator — metric / imperial, health category, saves to profile | ✅ Complete |
| Professional vector icon system | ✅ Complete |
| Glide image loading for exercise library | ✅ Complete |
| Offline fallback — local auth when server unreachable | ✅ Complete |
| Google Sign-In | 🔜 Future |
| Push Notifications | 🔜 Future |

---

##  Project Architecture

```
app/src/main/java/com/arcadefitness/
│
├── activities/                         # All 8 screen controllers
│   ├── SplashActivity.java             # Animated entry point
│   ├── LoginActivity.java              # Email/password + guest access
│   ├── RegisterActivity.java           # Registration + guest access
│   ├── DashboardActivity.java          # Home screen, quick actions, stats
│   ├── WorkoutPlannerActivity.java     # Create & browse workout plans
│   ├── WorkoutTrackingActivity.java    # Live session timer + set logging
│   ├── ExerciseLibraryActivity.java    # Muscle group filter + browse
│   └── ProgressTrackingActivity.java   # Goals, weekly stats, sessions
│
├── viewmodel/                          # MVVM — UI state management
│   ├── DashboardViewModel.java
│   ├── WorkoutPlannerViewModel.java
│   ├── WorkoutTrackingViewModel.java
│   └── ProgressViewModel.java
│
├── data/
│   ├── local/
│   │   ├── AppDatabase.java            # Room DB — version 2, 7 entities
│   │   ├── dao/                        # 7 DAO interfaces
│   │   │   ├── WorkoutDao.java
│   │   │   ├── ExerciseDao.java
│   │   │   ├── SetRecordDao.java
│   │   │   ├── SyncQueueDao.java
│   │   │   ├── UserProfileDao.java
│   │   │   ├── GoalDao.java
│   │   │   └── WorkoutSessionDao.java
│   │   ├── entity/                     # 7 Room entity classes
│   │   │   ├── WorkoutEntity.java
│   │   │   ├── ExerciseEntity.java
│   │   │   ├── SetRecordEntity.java
│   │   │   ├── SyncQueueEntryEntity.java
│   │   │   ├── UserProfileEntity.java
│   │   │   ├── GoalEntity.java
│   │   │   └── WorkoutSessionEntity.java
│   │   └── repository/                 # LiveData-first read repositories
│   │       ├── WorkoutRepository.java
│   │       └── ExerciseRepository.java
│   ├── repository/
│   │   └── FitnessRepository.java      # Transactional writes + sync queue
│   └── remote/                         # Phase 3 — Retrofit stubs
│       ├── ApiService.java
│       └── RetrofitClient.java
│
├── adapter/                            # RecyclerView — ViewHolder pattern
│   ├── ExerciseAdapter.java
│   ├── WorkoutSessionAdapter.java
│   └── ProgressAdapter.java
│
├── network/
│   └── NetworkChangeReceiver.java      # Connectivity listener → sync flush
│
└── utils/
    ├── AppConstants.java
    ├── SessionManager.java
    └── ValidationUtils.java
```

---

##  7-Table Database Schema

```
┌─────────────────────────────────────────────────────────────┐
│                        workouts                             │
│  id · name · target_muscle_group · estimated_duration       │
│  created_at · is_synced · remote_id                         │
└──────────────┬──────────────────────────┬───────────────────┘
               │ 1:N                      │ 1:N
               ▼                          ▼
┌──────────────────────────┐  ┌──────────────────────────────┐
│     workout_sessions     │  │         set_records          │
│  id · workout_id         │  │  id · workout_id             │
│  start_timestamp         │  │  exercise_id · set_number    │
│  end_timestamp           │  │  weight · reps               │
│  duration_minutes        │  │  is_completed · timestamp    │
│  calories_burned         │  │  is_synced · remote_id       │
│  status · is_synced      │  └──────────┬───────────────────┘
└──────────────────────────┘             │ N:1
                                         ▼
                             ┌──────────────────────────────┐
                             │          exercises           │
                             │  id · name                   │
                             │  target_muscle_group         │
                             │  default_sets · default_reps │
                             │  is_synced · remote_id       │
                             └──────────────────────────────┘

┌──────────────────────┐  ┌─────────────────────┐  ┌──────────────────────┐
│     user_profiles    │  │       goals         │  │      sync_queue      │
│  id · full_name      │  │  id · title · type  │  │  id · table_name     │
│  email · age         │  │  target_value        │  │  record_id           │
│  gender · goal       │  │  current_value       │  │  operation · payload │
│  is_synced           │  │  unit · status       │  │  status              │
└──────────────────────┘  │  is_synced           │  │  attempt_count       │
                          └─────────────────────┘  └──────────────────────┘
```

**Relationships:**
- `workout_sessions.workout_id → workouts.id` (CASCADE DELETE)
- `set_records.workout_id → workouts.id` (CASCADE DELETE)
- `set_records.exercise_id → exercises.id` (CASCADE DELETE)
- `sync_queue` — flat queue, references any table by name + record_id

---

##  MVVM Data Flow

```
  ┌─────────────────────────────────┐
  │        Activity / Fragment      │  ← UI layer (no business logic)
  └────────────────┬────────────────┘
                   │ observes LiveData / calls methods
                   ▼
  ┌─────────────────────────────────┐
  │            ViewModel            │  ← State holder, survives rotation
  └────────┬────────────────┬───────┘
           │                │
    reads  │                │ writes
           ▼                ▼
  ┌──────────────┐  ┌──────────────────────┐
  │  Workout /   │  │   FitnessRepository  │  ← Transactional writes
  │  Exercise    │  │   (singleton)        │      + sync queue enqueue
  │  Repository  │  └──────────┬───────────┘
  └──────┬───────┘             │
         │                     │
         ▼                     ▼
  ┌─────────────────────────────────┐
  │          Room (SQLite)          │  ← 7-table local database
  └────────────────┬────────────────┘
                   │ on connectivity change
                   ▼
  ┌─────────────────────────────────┐
  │      NetworkChangeReceiver      │  ← BroadcastReceiver
  └────────────────┬────────────────┘
                   │ flushes pending sync_queue entries
                   ▼
  ┌─────────────────────────────────┐
  │      Retrofit / ApiService      │  ← Phase 3 remote backend
  └─────────────────────────────────┘
```

---

##  Tech Stack

| Category | Technology |
| ------------------ | ---------------------------------------- |
| **Platform** | Android API 24+ (Android 7.0 and above) |
| **Language** | Java |
| **Architecture** | MVVM + Repository Pattern |
| **UI** | XML Layouts + Material Design Components |
| **Local Database** | Room (SQLite) — 7 tables |
| **Reactive UI** | LiveData + Observer pattern |
| **Async** | ExecutorService (background threads) |
| **Networking** | Retrofit2 + OkHttp3 (Phase 3) |
| **Auth** | Email/Password local · Google (Phase 3) |
| **Build** | Gradle 9.0 · AGP 8.10 · JDK 17 |

---

##  Design System

| Token | Value | Usage |
| -------------------- | ----------- | ------------------------------ |
| **Background** | `#121212` | Screen backgrounds |
| **Card Surface** | `#1A1A1A` | Cards, list items |
| **Input Surface** | `#1C1C1C` | Text fields |
| **Primary Accent** | `#FF6B00` | Buttons, highlights, icons |
| **Text Primary** | `#FFFFFF` | Headings, labels |
| **Text Secondary** | `#888888` | Subtitles, hints |
| **Typography** | Inter | 400 · 500 · 700 · 900 weights |

---

## Local Setup & Build

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- Physical device or emulator — Android 7.0+ (API 24+)

### Android App

```bash
# 1. Clone
git clone https://github.com/c4realm/fitness-planner.git

# 2. Open arcade-fitness-app/ in Android Studio

# 3. Set your machine's local IP in AppConstants.java
#    private static final String SERVER_IP = "YOUR_LOCAL_IP";

# 4. Sync Gradle → Run
```

### Backend

```bash
cd arcade-fitness-backend

# 1. Install dependencies
npm install

# 2. Create .env file
cp .env.example .env
# Fill in DATABASE_URL and JWT_SECRET

# 3. Create database and run schema
psql -U postgres -c "CREATE DATABASE arcade_fitness;"
psql -U postgres -d arcade_fitness -f database/schema.sql

# 4. Start server
node server.js
# Server runs on http://localhost:3000
```

> **Network note:** When testing on a real device, set `SERVER_IP` in `AppConstants.java` to your machine's local IP address (run `ip addr` on Linux or `ipconfig` on Windows). When using the emulator, use `10.0.2.2`.

---

## Screens

| # | Screen | Activity |
| -- | ---------------------- | -------------------------- |
| 1 | Splash | `SplashActivity` |
| 2 | Login | `LoginActivity` |
| 3 | Register | `RegisterActivity` |
| 4 | Dashboard | `DashboardActivity` |
| 5 | Workout Planner | `WorkoutPlannerActivity` |
| 6 | Exercise Library | `ExerciseLibraryActivity` |
| 7 | Workout Tracking | `WorkoutTrackingActivity` |
| 8 | Workout History | `WorkoutHistoryActivity` |
| 9 | Progress Tracking | `ProgressTrackingActivity` |
| 10 | User Profile | `UserProfileActivity` |
| 11 | BMI Calculator | `BmiCalculatorActivity` |
| 12 | Network Test | `NetworkTestActivity` |

---

## Security

- Passwords hashed with **SHA-256** before local storage — never stored in plain text
- Session tokens stored in **EncryptedSharedPreferences** backed by Android Keystore (AES-256-GCM)
- Backend passwords hashed with **bcryptjs** at cost factor 10
- All API routes protected with **JWT Bearer token** middleware
- HTTP security headers via **Helmet.js**
- All database queries use **parameterised statements** — no SQL injection exposure

---

##  Future Enhancements

- Google Sign-In with Firebase Authentication
- Push notifications for workout reminders
- Wearable fitness device integration
- Social features — achievements and challenges
- Advanced analytics with performance charts
- Personalised AI workout recommendations

---

## License

Academic Project — **St. Mary's University © 2026**

<div align="center">

*Built with Java, Node.js, and PostgreSQL by the Arcade Fitness Planner Team*

</div>
