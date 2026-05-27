package com.arcadefitness.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.arcadefitness.R;
import com.arcadefitness.data.local.entity.WorkoutEntity;
import com.arcadefitness.data.local.entity.WorkoutSessionEntity;
import com.arcadefitness.viewmodel.WorkoutTrackingViewModel;

public class WorkoutTrackingActivity extends AppCompatActivity {

    private WorkoutTrackingViewModel viewModel;

    private TextView tvElapsedTime, tvSetsCompleted;
    private TextView tvCurrentExerciseName, tvCurrentSetsInfo;
    private LinearLayout layoutActiveControls, layoutNoSession, layoutSessionSummary;
    private TextView tvSummaryDuration, tvSummaryVolume, tvSummaryCalories;
    private Button btnPause, btnComplete, btnBackToDashboard;

    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_tracking);

        initViews();
        setupViewModel();

        int workoutId = getIntent().getIntExtra("workout_id", -1);
        if (workoutId > 0) {
            viewModel.startSession(workoutId);
        } else {
            viewModel.loadCurrentSession();
        }
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvElapsedTime = findViewById(R.id.tvElapsedTime);
        tvSetsCompleted = findViewById(R.id.tvSetsCompleted);
        tvCurrentExerciseName = findViewById(R.id.tvCurrentExerciseName);
        tvCurrentSetsInfo = findViewById(R.id.tvCurrentSetsInfo);
        layoutActiveControls = findViewById(R.id.layoutActiveControls);
        layoutNoSession = findViewById(R.id.layoutNoSession);
        layoutSessionSummary = findViewById(R.id.layoutSessionSummary);
        tvSummaryDuration = findViewById(R.id.tvSummaryDuration);
        tvSummaryVolume = findViewById(R.id.tvSummaryVolume);
        tvSummaryCalories = findViewById(R.id.tvSummaryCalories);
        btnPause = findViewById(R.id.btnPause);
        btnComplete = findViewById(R.id.btnComplete);
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard);

        btnPause.setOnClickListener(v -> {
            if (Boolean.TRUE.equals(viewModel.getIsRunning().getValue())) {
                viewModel.pauseSession();
                btnPause.setText(R.string.btn_resume);
            } else {
                viewModel.resumeSession();
                btnPause.setText(R.string.btn_pause);
            }
        });

        btnComplete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.confirm_end_workout)
                    .setPositiveButton(R.string.yes_end, (dialog, which) -> {
                        viewModel.completeSession(5, "");
                        showSummary();
                    })
                    .setNegativeButton(R.string.no_continue, null)
                    .show();
        });

        btnBackToDashboard.setOnClickListener(v -> {
            finish();
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(WorkoutTrackingViewModel.class);

        viewModel.getCurrentSession().observe(this, new Observer<WorkoutSessionEntity>() {
            @Override
            public void onChanged(WorkoutSessionEntity session) {
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
                }
            }
        });

        viewModel.getCurrentWorkout().observe(this, new Observer<WorkoutEntity>() {
            @Override
            public void onChanged(WorkoutEntity workout) {
                if (workout != null) {
                    tvCurrentExerciseName.setText(workout.getName());
                    tvCurrentSetsInfo.setText(workout.getExerciseCount() + " exercises · "
                            + workout.getTargetMuscleGroup());
                }
            }
        });

        viewModel.getElapsedSeconds().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer seconds) {
                if (seconds != null) {
                    int mins = seconds / 60;
                    int secs = seconds % 60;
                    tvElapsedTime.setText(String.format("%02d:%02d", mins, secs));
                }
            }
        });

        viewModel.getIsRunning().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean running) {
                if (Boolean.TRUE.equals(running)) {
                    startTimer();
                } else {
                    stopTimer();
                }
            }
        });
    }

    private void startTimer() {
        stopTimer();
        timer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                viewModel.tickSecond();
            }

            @Override
            public void onFinish() {
            }
        }.start();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
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
