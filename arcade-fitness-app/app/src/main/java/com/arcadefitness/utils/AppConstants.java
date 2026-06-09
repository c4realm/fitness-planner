package com.arcadefitness.utils;

public class AppConstants {

    // ─────────────────────────────────────────────────────────────────────────
    //  CHANGE THIS TO YOUR MACHINE'S LOCAL IP ADDRESS
    //  How to find it:
    //    Windows → open CMD → type: ipconfig  → look for "IPv4 Address"
    //    Mac/Linux → open Terminal → type: ifconfig → look for "inet"
    //
    //  If using the Android Emulator (not a real phone) use: 10.0.2.2
    //  If using a real phone on the same Wi-Fi, use your PC's local IP e.g. 192.168.1.105
    // ─────────────────────────────────────────────────────────────────────────
    private static final String SERVER_IP   = "192.168.1.105";   // <-- CHANGE THIS
    private static final String SERVER_PORT = "3000";            // <-- CHANGE THIS if your server uses a different port

    public static final String BASE_URL = "http://" + SERVER_IP + ":" + SERVER_PORT + "/api/";

    // ── Endpoints ─────────────────────────────────────────────────────────────
    public static final String ENDPOINT_REGISTER      = "auth/register";
    public static final String ENDPOINT_LOGIN         = "auth/login";
    public static final String ENDPOINT_LOGOUT        = "auth/logout";
    public static final String ENDPOINT_PROFILE       = "user/profile";
    public static final String ENDPOINT_WORKOUTS      = "workouts";
    public static final String ENDPOINT_WORKOUT_BY_ID = "workouts/{id}";
    public static final String ENDPOINT_EXERCISES     = "exercises";
    public static final String ENDPOINT_SESSIONS      = "sessions";
    public static final String ENDPOINT_SET_RECORDS   = "set-records";
    public static final String ENDPOINT_GOALS         = "goals";
    public static final String ENDPOINT_SYNC_BATCH    = "sync/batch";
    public static final String ENDPOINT_HEALTH_CHECK  = "health";

    // ── Legacy shared prefs keys (RetrofitClient) ─────────────────────────────
    public static final String PREF_FILE              = "arcade_fitness_prefs";
    public static final String PREF_AUTH_TOKEN        = "auth_token";
    public static final String PREF_USER_ID           = "user_id";
    public static final String PREF_USER_EMAIL        = "user_email";

    // ── Session keys (SessionManager) ─────────────────────────────────────────
    public static final String KEY_IS_LOGGED_IN       = "is_logged_in";
    public static final String KEY_IS_GUEST           = "is_guest";
    public static final String KEY_USER_ID            = "user_id";
    public static final String KEY_USER_NAME          = "user_name";
    public static final String KEY_USER_EMAIL         = "user_email";
    public static final String KEY_USER_TOKEN         = "user_token";

    // ── Guest ─────────────────────────────────────────────────────────────────
    public static final String GUEST_USER_ID          = "guest";

    // ── Timeouts (seconds) ────────────────────────────────────────────────────
    public static final int TIMEOUT_CONNECT           = 15;
    public static final int TIMEOUT_READ              = 30;
    public static final int TIMEOUT_WRITE             = 30;

    // ── Sync ──────────────────────────────────────────────────────────────────
    public static final int SYNC_MAX_RETRIES          = 3;
    public static final int SYNC_BATCH_SIZE           = 50;
}