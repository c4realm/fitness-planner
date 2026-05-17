package com.arcadefitness.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;

import com.arcadefitness.R;
import com.arcadefitness.utils.AppConstants;
import com.arcadefitness.utils.SessionManager;

/**
 * SplashActivity.java
 * Entry point. Shows the branded splash screen for SPLASH_DURATION_MS,
 * then routes to Dashboard (if logged in) or Login.
 */
public class SplashActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full screen — hide status bar
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);

        // Navigate after splash duration
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateNext,
            AppConstants.SPLASH_DURATION_MS);
    }

    private void navigateNext() {
        Intent intent;
        if (sessionManager.isLoggedIn()) {
            // User has an active session → go straight to Dashboard
            intent = new Intent(SplashActivity.this, DashboardActivity.class);
        } else {
            // No session → Login screen
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        // Slide transition: new screen slides in from right
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish(); // Remove splash from back stack
    }
}
