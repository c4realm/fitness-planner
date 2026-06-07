package com.arcadefitness.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import androidx.appcompat.app.AppCompatActivity;

import com.arcadefitness.R;
import com.arcadefitness.utils.AppConstants;
import com.arcadefitness.utils.SessionManager;

/**
 * SplashActivity.java
 * Entry point. Shows branded splash with rotating concentric rings,
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

        startRingAnimations();

        new Handler(Looper.getMainLooper()).postDelayed(
            this::navigateNext,
            AppConstants.SPLASH_DURATION_MS
        );
    }

    // ── RING ANIMATIONS ─────────────────────────────────────────────

    private void startRingAnimations() {
        View ringOuter  = findViewById(R.id.ringOuter);
        View ringMiddle = findViewById(R.id.ringMiddle);
        View ringInner  = findViewById(R.id.ringInner);

        // Outer ring — slow clockwise, 18 seconds per revolution
        applyRotation(ringOuter,  18000, false);

        // Middle ring — medium counter-clockwise, 11 seconds
        applyRotation(ringMiddle, 11000, true);

        // Inner ring — fastest clockwise, 7 seconds
        applyRotation(ringInner,   7000, false);
    }

    /**
     * Applies an infinite linear rotation to a view.
     *
     * @param view         the ring View to animate
     * @param durationMs   milliseconds per full 360° revolution
     * @param counterClock true = counter-clockwise, false = clockwise
     */
    private void applyRotation(View view, long durationMs, boolean counterClock) {
        if (view == null) return;

        float fromDeg = counterClock ?   0f : 360f;
        float toDeg   = counterClock ? 360f :   0f;

        RotateAnimation rotate = new RotateAnimation(
            fromDeg, toDeg,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(durationMs);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setRepeatMode(Animation.RESTART);
        rotate.setInterpolator(new LinearInterpolator());

        view.startAnimation(rotate);
    }

    // ── NAVIGATION ──────────────────────────────────────────────────

    private void navigateNext() {
        Intent intent = sessionManager.isLoggedIn()
            ? new Intent(this, DashboardActivity.class)
            : new Intent(this, LoginActivity.class);

        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}
