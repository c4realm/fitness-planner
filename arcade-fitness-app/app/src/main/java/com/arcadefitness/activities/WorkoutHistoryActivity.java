package com.arcadefitness.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arcadefitness.R;
import com.arcadefitness.adapter.SessionHistoryAdapter;
import com.arcadefitness.data.local.entity.WorkoutEntity;
import com.arcadefitness.data.local.entity.WorkoutSessionEntity;
import com.arcadefitness.data.repository.FitnessRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutHistoryActivity extends AppCompatActivity {

    private SessionHistoryAdapter adapter;
    private View layoutEmpty;
    private TextView tvTotalWorkouts, tvTotalDuration, tvTotalCalories;
    private RecyclerView rvSessionHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_history);

        initViews();
        loadData();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tvTotalWorkouts = findViewById(R.id.tvTotalWorkouts);
        tvTotalDuration = findViewById(R.id.tvTotalDuration);
        tvTotalCalories = findViewById(R.id.tvTotalCalories);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        rvSessionHistory = findViewById(R.id.rvSessionHistory);
        rvSessionHistory.setLayoutManager(new LinearLayoutManager(this));
        rvSessionHistory.setHasFixedSize(true);
        adapter = new SessionHistoryAdapter();
        rvSessionHistory.setAdapter(adapter);
    }

    private void loadData() {
        FitnessRepository repo = FitnessRepository.getInstance(this);

        repo.getAllWorkouts(new FitnessRepository.RepositoryCallback<List<WorkoutEntity>>() {
            @Override
            public void onSuccess(List<WorkoutEntity> workouts) {
                Map<Integer, String> nameMap = new HashMap<>();
                if (workouts != null) {
                    for (WorkoutEntity w : workouts) {
                        nameMap.put(w.getId(), w.getName());
                    }
                }
                Map<Integer, String> finalNameMap = nameMap;
                repo.getCompletedSessions(new FitnessRepository.RepositoryCallback<List<WorkoutSessionEntity>>() {
                    @Override
                    public void onSuccess(List<WorkoutSessionEntity> sessions) {
                        runOnUiThread(() -> displayData(finalNameMap, sessions));
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> layoutEmpty.setVisibility(View.VISIBLE));
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                repo.getCompletedSessions(new FitnessRepository.RepositoryCallback<List<WorkoutSessionEntity>>() {
                    @Override
                    public void onSuccess(List<WorkoutSessionEntity> sessions) {
                        runOnUiThread(() -> displayData(new HashMap<>(), sessions));
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> layoutEmpty.setVisibility(View.VISIBLE));
                    }
                });
            }
        });
    }

    private void displayData(Map<Integer, String> nameMap, List<WorkoutSessionEntity> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvSessionHistory.setVisibility(View.GONE);
            return;
        }
        layoutEmpty.setVisibility(View.GONE);
        rvSessionHistory.setVisibility(View.VISIBLE);

        adapter.setWorkoutNames(nameMap);
        adapter.setData(sessions);

        int totalSessions = sessions.size();
        int totalDuration = 0;
        int totalCalories = 0;
        for (WorkoutSessionEntity s : sessions) {
            totalDuration += s.getDurationMinutes();
            totalCalories += s.getCaloriesBurned();
        }
        tvTotalWorkouts.setText(String.valueOf(totalSessions));
        tvTotalDuration.setText(totalDuration + " min");
        tvTotalCalories.setText(totalCalories + " kcal");
    }
}
