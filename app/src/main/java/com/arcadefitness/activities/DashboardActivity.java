package com.arcadefitness.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.arcadefitness.R;
import com.arcadefitness.utils.SessionManager;

/**
 * DashboardActivity.java
 * Main shell activity — hosts the bottom navigation and all core screens.
 *
 * Phase 2 Note:
 * Each nav item will load a Fragment inside fragment_container.
 * For now, we display the static dashboard layout.
 *
 * Navigation tabs:
 *   Home | Planner | + (FAB) | History | Profile
 */
public class DashboardActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    // Dashboard views
    private TextView tvUserName;
    private TextView tvStreakValue, tvWeekValue, tvCaloriesValue;

    // Bottom nav items
    private View navHome, navPlanner, navFab, navHistory, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionManager = new SessionManager(this);

        initViews();
        populateUserData();
        setupBottomNav();
        setupClickListeners();
    }

    // ── INIT ────────────────────────────────────────────────────────

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
    }

    private void populateUserData() {
        // Get first name from session
        String fullName  = sessionManager.getUserName();
        String firstName = fullName.contains(" ")
            ? fullName.substring(0, fullName.indexOf(" "))
            : fullName;
        if (tvUserName != null) tvUserName.setText(firstName);

        // ── TODO (Phase 2): Load real stats from API ─────────────────
        // Fetch user stats (streak, weekly hours, calories) and populate:
        // tvStreakValue.setText(String.valueOf(stats.streak));
        // tvWeekValue.setText(String.format("%.1f", stats.weeklyHours));
        // tvCaloriesValue.setText(String.valueOf(stats.calories));
        // ─────────────────────────────────────────────────────────────

        // Temporary mock data
        if (tvStreakValue   != null) tvStreakValue.setText("12");
        if (tvWeekValue     != null) tvWeekValue.setText("3.2");
        if (tvCaloriesValue != null) tvCaloriesValue.setText("680");
    }

    // ── BOTTOM NAVIGATION ───────────────────────────────────────────

    private void setupBottomNav() {
        setActiveNav(navHome); // Home is active on launch
    }

    private void setupClickListeners() {
        navHome.setOnClickListener(v -> {
            setActiveNav(v);
            // TODO (Phase 2): loadFragment(new HomeFragment());
        });
        navPlanner.setOnClickListener(v -> {
            setActiveNav(v);
            // TODO (Phase 2): startActivity(new Intent(this, WorkoutPlannerActivity.class));
        });
        navFab.setOnClickListener(v -> {
            // FAB — quick add workout
            // TODO (Phase 2): show bottom sheet dialog for quick add
        });
        navHistory.setOnClickListener(v -> {
            setActiveNav(v);
            // TODO (Phase 2): startActivity(new Intent(this, WorkoutHistoryActivity.class));
        });
        navProfile.setOnClickListener(v -> {
            setActiveNav(v);
            // TODO (Phase 2): startActivity(new Intent(this, UserProfileActivity.class));
        });

        // Today's workout banner tap
        View todayBanner = findViewById(R.id.cardTodayWorkout);
        if (todayBanner != null) {
            todayBanner.setOnClickListener(v -> {
                // TODO (Phase 2): startActivity(new Intent(this, WorkoutTrackingActivity.class));
            });
        }

        // Continue workout button
        View btnContinue = findViewById(R.id.btnContinueWorkout);
        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> {
                // TODO (Phase 2): startActivity(new Intent(this, WorkoutTrackingActivity.class));
            });
        }

        // Goal card — view all
        View tvViewAll = findViewById(R.id.tvViewAll);
        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(v -> {
                // TODO (Phase 2): startActivity(new Intent(this, GoalSettingActivity.class));
            });
        }
    }

    private void setActiveNav(View activeNav) {
        // Reset all
        View[] navItems = {navHome, navPlanner, navHistory, navProfile};
        for (View nav : navItems) {
            if (nav == null) continue;
            TextView label = nav.findViewWithTag("nav_label");
            View     icon  = nav.findViewWithTag("nav_icon");
            if (label != null) label.setTextColor(getColor(R.color.nav_bar));
            // Icon tint reset handled via selector drawables in Phase 2
        }
        // Set active state — highlight handled by selector drawables
    }

    // ── LIFECYCLE ───────────────────────────────────────────────────

    @Override
    public void onBackPressed() {
        // Prevent going back to Login once logged in
        // Show exit confirmation dialog in Phase 2
        moveTaskToBack(true);
    }
}
