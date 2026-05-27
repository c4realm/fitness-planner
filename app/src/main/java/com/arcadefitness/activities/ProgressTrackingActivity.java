package com.arcadefitness.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arcadefitness.R;
import com.arcadefitness.adapter.ProgressAdapter;
import com.arcadefitness.data.local.entity.GoalEntity;
import com.arcadefitness.viewmodel.ProgressViewModel;

import java.util.List;

public class ProgressTrackingActivity extends AppCompatActivity {

    private ProgressViewModel viewModel;
    private ProgressAdapter progressAdapter;
    private View layoutNoGoals;

    private TextView tvWeekWorkouts, tvWeekDuration, tvWeekCalories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_tracking);

        initViews();
        setupRecyclerView();
        setupViewModel();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddGoal).setOnClickListener(v -> { });
    }

    private void initViews() {
        tvWeekWorkouts = findViewById(R.id.tvWeekWorkouts);
        tvWeekDuration = findViewById(R.id.tvWeekDuration);
        tvWeekCalories = findViewById(R.id.tvWeekCalories);
        layoutNoGoals = findViewById(R.id.layoutNoGoals);
    }

    private void setupRecyclerView() {
        RecyclerView rvGoals = findViewById(R.id.rvGoals);
        rvGoals.setLayoutManager(new LinearLayoutManager(this));
        rvGoals.setHasFixedSize(true);
        progressAdapter = new ProgressAdapter();
        rvGoals.setAdapter(progressAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);

        viewModel.getGoals().observe(this, new Observer<List<GoalEntity>>() {
            @Override
            public void onChanged(List<GoalEntity> goals) {
                progressAdapter.setGoals(goals);
                boolean empty = goals == null || goals.isEmpty();
                layoutNoGoals.setVisibility(empty ? View.VISIBLE : View.GONE);
                findViewById(R.id.rvGoals).setVisibility(empty ? View.GONE : View.VISIBLE);
            }
        });

        viewModel.getWeeklyStats().observe(this, new Observer<int[]>() {
            @Override
            public void onChanged(int[] stats) {
                if (stats != null && stats.length >= 4) {
                    tvWeekWorkouts.setText(String.valueOf(stats[0]));
                    tvWeekDuration.setText(stats[1] + "m");
                    tvWeekCalories.setText(String.valueOf(stats[2]));
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh();
        }
    }
}
