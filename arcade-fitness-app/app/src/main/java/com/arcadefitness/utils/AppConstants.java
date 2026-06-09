package com.arcadefitness.utils;

/**
 * AppConstants.java
 * Central place for all app-wide constants.
 *
 * ⚠️  BEFORE DEPLOYING: Replace BASE_URL with your real server address.
 *     Format must end with /api/ (trailing slash required by Retrofit).
 *
 *     Local emulator:  "http://10.0.2.2:3000/api/"
 *     Local device:    "http://192.168.x.x:3000/api/"   (your machine's LAN IP)
 *     Production:      "https://your-domain.com/api/"
 */
public final class AppConstants {

    private AppConstants() {} // prevent instantiation

    // ── API ──────────────────────────────────────────────────────────
    // 10.0.2.2 is the special Android emulator alias for localhost.
    // Change this to your LAN IP when testing on a real device.
    public static final String BASE_URL               = "http://192.168.100.250:3000/api/";
    public static final int    NETWORK_TIMEOUT_SECONDS = 30;

    // ── GOOGLE SIGN-IN ───────────────────────────────────────────────
    // Replace with your Web Client ID from Google Cloud Console
    public static final String GOOGLE_WEB_CLIENT_ID   = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com";

    // ── SHARED PREFERENCES ──────────────────────────────────────────
    public static final String PREF_NAME        = "arcade_fitness_prefs";
    public static final String KEY_USER_TOKEN   = "user_token";
    public static final String KEY_USER_ID      = "user_id";
    public static final String KEY_USER_NAME    = "user_name";
    public static final String KEY_USER_EMAIL   = "user_email";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_GOOGLE_LOGIN = "google_login";
    public static final String KEY_IS_GUEST     = "is_guest";
    public static final String GUEST_USER_ID    = "guest";

    // ── INTENT EXTRAS ────────────────────────────────────────────────
    public static final String EXTRA_USER_ID    = "extra_user_id";
    public static final String EXTRA_WORKOUT_ID = "extra_workout_id";
    public static final String EXTRA_EXERCISE_ID= "extra_exercise_id";
    public static final String EXTRA_GOAL_ID    = "extra_goal_id";

    // ── SPLASH ───────────────────────────────────────────────────────
    public static final int SPLASH_DURATION_MS  = 2500;

    // ── VALIDATION ───────────────────────────────────────────────────
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int NAME_MIN_LENGTH     = 2;
    public static final int AGE_MIN             = 10;
    public static final int AGE_MAX             = 100;

    // ── REQUEST CODES ────────────────────────────────────────────────
    public static final int RC_GOOGLE_SIGN_IN   = 9001;

    // ── GENDER OPTIONS ───────────────────────────────────────────────
    public static final String[] GENDER_OPTIONS = {"Select gender", "Male", "Female", "Prefer not to say"};
}