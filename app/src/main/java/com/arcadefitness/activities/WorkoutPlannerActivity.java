package com.arcadefitness.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arcadefitness.R;
import com.arcadefitness.adapter.WorkoutSessionAdapter;
import com.arcadefitness.data.local.entity.ExerciseEntity;
import com.arcadefitness.data.local.entity.WorkoutEntity;
import com.arcadefitness.viewmodel.WorkoutPlannerViewModel;

import java.util.List;

public class WorkoutPlannerActivity extends AppCompatActivity {

    private WorkoutPlannerViewModel viewModel;
    private WorkoutSessionAdapter adapter;
    private View layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_planner);

        initViews();
        setupRecyclerView();
        setupViewModel();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCreateWorkout).setOnClickListener(v -> {
            Intent intent = new Intent(this, WorkoutTrackingActivity.class);
            startActivity(intent);
        });
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rvWorkoutSessions);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);
        adapter = new WorkoutSessionAdapter();
        adapter.setOnWorkoutClickListener(workout -> {
            Intent intent = new Intent(this, WorkoutTrackingActivity.class);
            intent.putExtra("workout_id", workout.getId());
            startActivity(intent);
        });
        rv.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(WorkoutPlannerViewModel.class);

        viewModel.getAllWorkouts().observe(this, new Observer<List<WorkoutEntity>>() {
            @Override
            public void onChanged(List<WorkoutEntity> workouts) {
                adapter.setWorkouts(workouts);
                layoutEmpty.setVisibility(workouts == null || workouts.isEmpty() ? View.VISIBLE : View.GONE);
                findViewById(R.id.rvWorkoutSessions)
                        .setVisibility(workouts == null || workouts.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
    }
}
