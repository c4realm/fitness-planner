package com.arcadefitness.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.arcadefitness.R;
import com.arcadefitness.data.local.AppDatabase;
import com.arcadefitness.data.local.entity.ExerciseEntity;
import com.arcadefitness.data.local.entity.SetRecordEntity;
import com.arcadefitness.data.local.entity.WorkoutEntity;
import com.arcadefitness.data.local.entity.WorkoutSessionEntity;
import com.arcadefitness.data.repository.FitnessRepository;
import com.arcadefitness.viewmodel.WorkoutTrackingViewModel;

import java.util.List;

public class WorkoutTrackingActivity extends AppCompatActivity {

    private WorkoutTrackingViewModel viewModel;

    private TextView tvElapsedTime, tvSetsCompleted;
    private TextView tvCurrentExerciseName, tvCurrentSetsInfo;
    private LinearLayout layoutActiveControls, layoutNoSession, layoutSessionSummary;
    private TextView tvSummaryDuration, tvSummaryVolume, tvSummaryCalories;
    private Button btnPause, btnComplete, btnBackToDashboard;
    private EditText etSetWeight, etSetReps;
    private Button btnMarkSetDone;

    private CountDownTimer timer;
    private int completedSetsCount  = 0;
    private int resolvedExerciseId  = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_tracking);

        initViews();
        setupViewModel();
        resolveFirstExerciseId();

        int workoutId = getIntent().getIntExtra("workout_id", -1);
        if (workoutId > 0) {
            // Came from Planner — start immediately
            viewModel.startSession(workoutId);
        } else {
            // Came from Dashboard / FAB — show picker first
            showWorkoutPickerOrResume();
        }
    }

    // ── WORKOUT PICKER ───────────────────────────────────────────────

    /**
     * Loads all workouts from Room on a background thread.
     * If an active session already exists, resumes it.
     * If workouts exist, shows a picker dialog.
     * If no workouts exist yet, prompts the user to create one.
     */
    private void showWorkoutPickerOrResume() {
        AppDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> {
            WorkoutSessionEntity active = AppDatabase.getInstance(this)
                    .workoutSessionDao().getCurrentSession();

            if (active != null) {
                // Resume existing session — no picker needed
                runOnUiThread(() -> viewModel.loadCurrentSession());
                return;
            }

            List<WorkoutEntity> workouts = AppDatabase.getInstance(this)
                    .workoutDao().getAll();

            runOnUiThread(() -> {
                if (workouts == null || workouts.isEmpty()) {
                    showNoWorkoutsDialog();
                } else {
                    showPickerDialog(workouts);
                }
            });
        });
    }

    private void showPickerDialog(List<WorkoutEntity> workouts) {
        String[] names = new String[workouts.size()];
        for (int i = 0; i < workouts.size(); i++) {
            WorkoutEntity w = workouts.get(i);
            names[i] = w.getName() + "  ·  " + w.getTargetMuscleGroup();
        }

        new AlertDialog.Builder(this)
                .setTitle("Choose a Workout")
                .setItems(names, (dialog, which) -> {
                    int selectedId = workouts.get(which).getId();
                    viewModel.startSession(selectedId);
                })
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showNoWorkoutsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Workouts Yet")
                .setMessage("Create a workout in the Planner first, then come back to track it.")
                .setPositiveButton("Go to Planner", (dialog, which) -> {
                    startActivity(new Intent(this, WorkoutPlannerActivity.class));
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    // ── RESOLVE EXERCISE ID ──────────────────────────────────────────

    private void resolveFirstExerciseId() {
        AppDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> {
            List<ExerciseEntity> all = AppDatabase.getInstance(this)
                    .exerciseDao().getAll();
            if (all != null && !all.isEmpty()) {
                resolvedExerciseId = all.get(0).getId();
            }
        });
    }

    // ── INIT ────────────────────────────────────────────────────────

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvElapsedTime         = findViewById(R.id.tvElapsedTime);
        tvSetsCompleted       = findViewById(R.id.tvSetsCompleted);
        tvCurrentExerciseName = findViewById(R.id.tvCurrentExerciseName);
        tvCurrentSetsInfo     = findViewById(R.id.tvCurrentSetsInfo);
        layoutActiveControls  = findViewById(R.id.layoutActiveControls);
        layoutNoSession       = findViewById(R.id.layoutNoSession);
        layoutSessionSummary  = findViewById(R.id.layoutSessionSummary);
        tvSummaryDuration     = findViewById(R.id.tvSummaryDuration);
        tvSummaryVolume       = findViewById(R.id.tvSummaryVolume);
        tvSummaryCalories     = findViewById(R.id.tvSummaryCalories);
        btnPause              = findViewById(R.id.btnPause);
        btnComplete           = findViewById(R.id.btnComplete);
        btnBackToDashboard    = findViewById(R.id.btnBackToDashboard);
        etSetWeight           = findViewById(R.id.etSetWeight);
        etSetReps             = findViewById(R.id.etSetReps);
        btnMarkSetDone        = findViewById(R.id.btnMarkSetDone);

        updateSetsCompletedText();
        btnMarkSetDone.setOnClickListener(v -> markSetDone());

        btnPause.setOnClickListener(v -> {
            if (Boolean.TRUE.equals(viewModel.getIsRunning().getValue())) {
                viewModel.pauseSession();
                btnPause.setText(R.string.btn_resume);
            } else {
                viewModel.resumeSession();
                btnPause.setText(R.string.btn_pause);
            }
        });

        btnComplete.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle(R.string.confirm_end_workout)
                        .setPositiveButton(R.string.yes_end, (dialog, which) -> {
                            viewModel.completeSession(5, "");
                            showSummary();
                        })
                        .setNegativeButton(R.string.no_continue, null)
                        .show()
        );

        btnBackToDashboard.setOnClickListener(v -> finish());
    }

    // ── VIEWMODEL ────────────────────────────────────────────────────

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(WorkoutTrackingViewModel.class);

        viewModel.getCurrentSession().observe(this, session -> {
            if (session == null) {
                layoutNoSession.setVisibility(View.VISIBLE);
                layoutActiveControls.setVisibility(View.GONE);
                layoutSessionSummary.setVisibility(View.GONE);
            } else if ("COMPLETED".equals(session.getStatus())) {
                showSummary();
            } else {
                layoutNoSession.setVisibility(View.GONE);
                layoutActiveControls.setVisibility(View.VISIBLE);
                layoutSessionSummary.setVisibility(View.GONE);
                completedSetsCount = 0;
                updateSetsCompletedText();
            }
        });

        viewModel.getCurrentWorkout().observe(this, workout -> {
            if (workout != null) {
                tvCurrentExerciseName.setText(workout.getName());
                tvCurrentSetsInfo.setText(workout.getExerciseCount()
                        + " exercises · " + workout.getTargetMuscleGroup());
            }
        });

        viewModel.getElapsedSeconds().observe(this, seconds -> {
            if (seconds != null) {
                int mins = seconds / 60;
                int secs = seconds % 60;
                tvElapsedTime.setText(String.format("%02d:%02d", mins, secs));
            }
        });

        viewModel.getIsRunning().observe(this, running -> {
            if (Boolean.TRUE.equals(running)) startTimer();
            else stopTimer();
        });
    }

    // ── SET LOGGING ──────────────────────────────────────────────────

    private void markSetDone() {
        WorkoutSessionEntity session = viewModel.getCurrentSession().getValue();
        if (session == null) {
            Toast.makeText(this, "No active session", Toast.LENGTH_SHORT).show();
            return;
        }

        String weightText = etSetWeight.getText().toString().trim();
        String repsText   = etSetReps.getText().toString().trim();

        if (weightText.isEmpty()) {
            etSetWeight.setError("Weight is required");
            etSetWeight.requestFocus();
            return;
        }
        if (repsText.isEmpty()) {
            etSetReps.setError("Reps are required");
            etSetReps.requestFocus();
            return;
        }

        double weight;
        int reps;
        try {
            weight = Double.parseDouble(weightText);
        } catch (NumberFormatException e) {
            etSetWeight.setError("Enter a valid number");
            etSetWeight.requestFocus();
            return;
        }
        try {
            reps = Integer.parseInt(repsText);
        } catch (NumberFormatException e) {
            etSetReps.setError("Enter a valid number");
            etSetReps.requestFocus();
            return;
        }

        int exerciseId = resolvedExerciseId > 0 ? resolvedExerciseId : 1;

        SetRecordEntity setRecord = new SetRecordEntity();
        setRecord.setWorkoutId(session.getWorkoutId());
        setRecord.setExerciseId(exerciseId);
        setRecord.setWeight(weight);
        setRecord.setReps(reps);
        setRecord.setSetNumber(completedSetsCount + 1);
        setRecord.setIsCompleted(1);
        setRecord.setTimestamp(System.currentTimeMillis());

        FitnessRepository.getInstance(this).insertSetRecord(setRecord,
                new FitnessRepository.RepositoryCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer result) {
                        completedSetsCount++;
                        updateSetsCompletedText();
                        etSetWeight.setText("");
                        etSetReps.setText("");
                        Toast.makeText(WorkoutTrackingActivity.this,
                                "Set logged ✓", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(WorkoutTrackingActivity.this,
                                "Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateSetsCompletedText() {
        if (tvSetsCompleted != null)
            tvSetsCompleted.setText(completedSetsCount + " sets completed");
    }

    // ── TIMER ────────────────────────────────────────────────────────

    private void startTimer() {
        stopTimer();
        timer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override public void onTick(long ms) { viewModel.tickSecond(); }
            @Override public void onFinish() {}
        }.start();
    }

    private void stopTimer() {
        if (timer != null) { timer.cancel(); timer = null; }
    }

    private void showSummary() {
        stopTimer();
        layoutActiveControls.setVisibility(View.GONE);
        layoutNoSession.setVisibility(View.GONE);
        layoutSessionSummary.setVisibility(View.VISIBLE);

        WorkoutSessionEntity session = viewModel.getCurrentSession().getValue();
        if (session != null) {
            tvSummaryDuration.setText(session.getDurationMinutes() + " min");
            tvSummaryVolume.setText(String.format("%.0f kg", session.getTotalVolume()));
            tvSummaryCalories.setText(session.getCaloriesBurned() + "");
        }
    }

    @Override
    protected void onDestroy() {
        stopTimer();
        super.onDestroy();
    }
}