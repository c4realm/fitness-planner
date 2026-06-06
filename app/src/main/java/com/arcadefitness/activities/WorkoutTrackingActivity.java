package com.arcadefitness.activities;

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
import androidx.lifecycle.Observer;
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
    private int completedSetsCount = 0;

    // First valid exercise ID from the database — resolved on start
    private int resolvedExerciseId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_tracking);

        initViews();
        setupViewModel();
        resolveFirstExerciseId();

        int workoutId = getIntent().getIntExtra("workout_id", -1);
        if (workoutId > 0) {
            viewModel.startSession(workoutId);
        } else {
            viewModel.loadCurrentSession();
        }
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

    // ── RESOLVE EXERCISE ID ──────────────────────────────────────────
    /**
     * Fetches the first exercise ID from the database on a background thread.
     * This prevents the FOREIGN KEY constraint failure when logging a set,
     * because set_records.exercise_id must reference a real row in exercises.
     */
    private void resolveFirstExerciseId() {
        AppDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> {
            List<ExerciseEntity> all = AppDatabase.getInstance(this).exerciseDao().getAll();
            if (all != null && !all.isEmpty()) {
                resolvedExerciseId = all.get(0).getId();
            }
        });
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

        // Use resolved exercise ID — must be a real row in exercises table
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
