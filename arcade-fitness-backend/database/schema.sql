-- ============================================================================
-- Arcade Fitness Planner — 3NF PostgreSQL Schema with Audit Trails
-- St. Mary's University Academic Submission
-- ============================================================================

-- 1. USERS
CREATE TABLE IF NOT EXISTS users (
    user_id       SERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- 2. USER PROFILES
CREATE TABLE IF NOT EXISTS user_profiles (
    profile_id    SERIAL PRIMARY KEY,
    user_id       INTEGER     NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    height_cm     NUMERIC(5,1),
    initial_weight_kg NUMERIC(5,1),
    target_weight_kg  NUMERIC(5,1),
    date_of_birth DATE,
    gender        CHAR(1)     CHECK (gender IN ('M', 'F', 'O')),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. WORKOUTS (routine architecture templates)
CREATE TABLE IF NOT EXISTS workouts (
    workout_id    SERIAL PRIMARY KEY,
    user_id       INTEGER     NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    day_of_week   SMALLINT    CHECK (day_of_week BETWEEN 0 AND 6),
    is_template   BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_workouts_user_id ON workouts(user_id);

-- 4. EXERCISES (seeded with 25 default movements)
CREATE TABLE IF NOT EXISTS exercises (
    exercise_id   SERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    muscle_group  VARCHAR(100) NOT NULL,
    description   TEXT,
    requires_equipment BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_exercises_muscle_group ON exercises(muscle_group);

-- 5. SET RECORDS
CREATE TABLE IF NOT EXISTS set_records (
    set_id        SERIAL PRIMARY KEY,
    user_id       INTEGER     NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    workout_id    INTEGER     NOT NULL REFERENCES workouts(workout_id) ON DELETE CASCADE,
    exercise_id   INTEGER     NOT NULL REFERENCES exercises(exercise_id) ON DELETE CASCADE,
    session_id    INTEGER,                                          -- set after session is created
    set_number    SMALLINT    NOT NULL CHECK (set_number > 0),
    weight_kg     NUMERIC(6,2),
    reps          SMALLINT    CHECK (reps > 0),
    rpe           NUMERIC(2,1) CHECK (rpe BETWEEN 1 AND 10),
    completed     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_set_records_workout_id   ON set_records(workout_id);
CREATE INDEX idx_set_records_session_id   ON set_records(session_id);
CREATE INDEX idx_set_records_user_id      ON set_records(user_id);

-- 6. GOALS
CREATE TABLE IF NOT EXISTS goals (
    goal_id       SERIAL PRIMARY KEY,
    user_id       INTEGER     NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    goal_type     VARCHAR(50) NOT NULL CHECK (goal_type IN ('daily_calories', 'weekly_workouts', 'weight_target', 'custom')),
    target_value  NUMERIC(10,2) NOT NULL,
    current_value NUMERIC(10,2) NOT NULL DEFAULT 0,
    start_date    DATE        NOT NULL DEFAULT CURRENT_DATE,
    end_date      DATE,
    achieved      BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_goals_user_id ON goals(user_id);

-- 7. WORKOUT SESSIONS (historical execution logs)
CREATE TABLE IF NOT EXISTS workout_sessions (
    session_id    SERIAL PRIMARY KEY,
    user_id       INTEGER     NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    workout_id    INTEGER     NOT NULL REFERENCES workouts(workout_id) ON DELETE CASCADE,
    started_at    TIMESTAMPTZ,
    ended_at      TIMESTAMPTZ,
    duration_min  SMALLINT    CHECK (duration_min > 0),
    notes         TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_workout_sessions_user_id    ON workout_sessions(user_id);
CREATE INDEX idx_workout_sessions_workout_id ON workout_sessions(workout_id);

-- 8. SYNC QUEUE (immutable server-side audit log)
CREATE TABLE IF NOT EXISTS sync_queue (
    sync_id       SERIAL PRIMARY KEY,
    user_id       INTEGER     NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    table_name    VARCHAR(100) NOT NULL,
    operation     VARCHAR(20) NOT NULL CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE')),
    payload       JSONB       NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'processing', 'completed', 'failed')),
    attempt_count SMALLINT    NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at  TIMESTAMPTZ
);

CREATE INDEX idx_sync_queue_status    ON sync_queue(status);
CREATE INDEX idx_sync_queue_user_id   ON sync_queue(user_id);
CREATE INDEX idx_sync_queue_created   ON sync_queue(created_at);

-- ============================================================================
-- SEED: 25 Default Exercises
-- ============================================================================
INSERT INTO exercises (name, muscle_group, description, requires_equipment) VALUES
  ('Bench Press',              'Chest',     'Barbell bench press — flat',            TRUE),
  ('Incline Dumbbell Press',   'Chest',     'Dumbbell press on incline bench',       TRUE),
  ('Push-Up',                  'Chest',     'Bodyweight push-up',                    FALSE),
  ('Cable Fly',                'Chest',     'Cable crossover fly',                   TRUE),
  ('Deadlift',                 'Back',      'Conventional barbell deadlift',         TRUE),
  ('Pull-Up',                  'Back',      'Overhand grip pull-up',                 FALSE),
  ('Barbell Row',              'Back',      'Bent-over barbell row',                 TRUE),
  ('Lat Pulldown',             'Back',      'Wide-grip lat pulldown',                TRUE),
  ('Overhead Press',           'Shoulders', 'Standing barbell overhead press',       TRUE),
  ('Lateral Raise',            'Shoulders', 'Dumbbell lateral raise',                TRUE),
  ('Face Pull',                'Shoulders', 'Rope face pull to cable machine',       TRUE),
  ('Squat',                    'Legs',      'Barbell back squat',                    TRUE),
  ('Leg Press',                'Legs',      'Machine leg press',                     TRUE),
  ('Romanian Deadlift',        'Legs',      'Dumbbell Romanian deadlift',            TRUE),
  ('Leg Curl',                 'Legs',      'Lying leg curl machine',                TRUE),
  ('Bicep Curl',               'Arms',      'Standing dumbbell bicep curl',          TRUE),
  ('Tricep Pushdown',          'Arms',      'Cable tricep pushdown',                 TRUE),
  ('Hammer Curl',              'Arms',      'Neutral-grip dumbbell curl',            TRUE),
  ('Skull Crusher',            'Arms',      'Lying barbell tricep extension',        TRUE),
  ('Plank',                    'Core',      'Front hold plank',                      FALSE),
  ('Crunches',                 'Core',      'Floor crunches',                        FALSE),
  ('Hanging Leg Raise',        'Core',      'Hanging leg raise on pull-up bar',      FALSE),
  ('Russian Twist',            'Core',      'Seated rotational twist',               FALSE),
  ('Running',                  'Cardio',    'Treadmill or outdoor running',          FALSE),
  ('Cycling',                  'Cardio',    'Stationary or outdoor cycling',         TRUE);
