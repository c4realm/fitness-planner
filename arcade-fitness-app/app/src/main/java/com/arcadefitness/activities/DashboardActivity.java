package com.arcadefitness.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arcadefitness.R;
import com.arcadefitness.adapter.ExerciseAdapter;
import com.arcadefitness.data.local.entity.ExerciseEntity;
import com.arcadefitness.utils.SessionManager;
import com.arcadefitness.viewmodel.DashboardViewModel;

import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DashboardViewModel dashboardViewModel;
    private ExerciseAdapter exerciseAdapter;

    private TextView tvUserName;
    private TextView tvStreakValue, tvWeekValue, tvCaloriesValue;

    private View navHome, navPlanner, navFab, navHistory, navProfile;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionManager = new SessionManager(this);

        initViews();
        setupRecyclerView();
        setupViewModel();
        populateUserData();
        setupBottomNav();
        setupClickListeners();
    }

    private void initViews() {
        tvUserName      = findViewById(R.id.tvUserName);
        tvStreakValue   = findViewById(R.id.tvStreakValue);
        tvWeekValue     = findViewById(R.id.tvWeekValue);
        tvCaloriesValue = findViewById(R.id.tvCaloriesValue);

        navHome    = findViewById(R.id.navHome);
        navPlanner = findViewById(R.id.navPlanner);
        navFab     = findViewById(R.id.navFab);
        navHistory = findViewById(R.id.navHistory);
        navProfile = findViewById(R.id.navProfile);

        btnLogout  = findViewById(R.id.btnLogout);
    }

    private void setupRecyclerView() {
        RecyclerView rvExercises = findViewById(R.id.rvExercises);
        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        rvExercises.setHasFixedSize(true);
        exerciseAdapter = new ExerciseAdapter();
        rvExercises.setAdapter(exerciseAdapter);
    }

    private void setupViewModel() {
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        dashboardViewModel.getAllExercises().observe(this, new Observer<List<ExerciseEntity>>() {
            @Override
            public void onChanged(List<ExerciseEntity> exercises) {
                exerciseAdapter.setExercises(exercises);
            }
        });

        // Real stats from Room
        dashboardViewModel.getWeeklySessionCount().observe(this, count -> {
            if (tvWeekValue != null) tvWeekValue.setText(String.valueOf(count));
        });
        dashboardViewModel.getWeeklyCalories().observe(this, kcal -> {
            if (tvCaloriesValue != null) tvCaloriesValue.setText(String.valueOf(kcal));
        });
        // Streak = total completed sessions (simple proxy until streak logic built)
        dashboardViewModel.getWeeklySessionCount().observe(this, count -> {
            if (tvStreakValue != null) tvStreakValue.setText(String.valueOf(count));
        });
    }

    private void populateUserData() {
        String fullName  = sessionManager.getUserName();
        String firstName = fullName.contains(" ")
                ? fullName.substring(0, fullName.indexOf(" "))
                : fullName;
        if (firstName.isEmpty()) firstName = "there";
        if (tvUserName != null) tvUserName.setText(firstName);
        // Stats populated by ViewModel observers — no hardcoded values
    }

    private void setupBottomNav() {
        setActiveNav(navHome);
    }

    private void setupClickListeners() {
        navHome.setOnClickListener(v -> setActiveNav(v));
        navPlanner.setOnClickListener(v -> {
            setActiveNav(v);
            startActivity(new Intent(this, WorkoutPlannerActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        navFab.setOnClickListener(v -> {
            startActivity(new Intent(this, WorkoutTrackingActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        navHistory.setOnClickListener(v -> {
            setActiveNav(v);
            startActivity(new Intent(this, ProgressTrackingActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        navProfile.setOnClickListener(v -> setActiveNav(v));

        View todayBanner = findViewById(R.id.cardTodayWorkout);
        if (todayBanner != null) {
            todayBanner.setOnClickListener(v -> {
                startActivity(new Intent(this, WorkoutTrackingActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        View btnContinue = findViewById(R.id.btnContinueWorkout);
        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> {
                startActivity(new Intent(this, WorkoutTrackingActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        View tvViewAll = findViewById(R.id.tvViewAll);
        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(v -> {
                startActivity(new Intent(this, ExerciseLibraryActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        btnLogout.setOnClickListener(v -> logout());

        // Quick action grid
        findViewById(R.id.cardActionPlanner).setOnClickListener(v -> {
            startActivity(new Intent(this, WorkoutPlannerActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        findViewById(R.id.cardActionTrack).setOnClickListener(v -> {
            startActivity(new Intent(this, WorkoutTrackingActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        findViewById(R.id.cardActionExercises).setOnClickListener(v -> {
            startActivity(new Intent(this, ExerciseLibraryActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        findViewById(R.id.cardActionProgress).setOnClickListener(v -> {
            startActivity(new Intent(this, ProgressTrackingActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void setActiveNav(View activeNav) {
        View[] navItems = {navHome, navPlanner, navHistory, navProfile};
        for (View nav : navItems) {
            if (nav == null) continue;
            TextView label = nav.findViewWithTag("nav_label");
            if (label != null) label.setTextColor(getColor(R.color.text_muted));
        }
        if (activeNav != null) {
            TextView label = activeNav.findViewWithTag("nav_label");
            if (label != null) label.setTextColor(getColor(R.color.orange_primary));
        }
    }

    private void logout() {
        sessionManager.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dashboardViewModel != null) dashboardViewModel.loadWeeklyStats();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
