package com.arcadefitness.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager.java
 * Handles saving and reading user session data via SharedPreferences.
 * Used by all activities to check login state and store/clear tokens.
 */
public class SessionManager {

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs  = context.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ── SAVE SESSION ─────────────────────────────────────────────────

    public void saveSession(String userId, String userName, String email, String token) {
        editor.putBoolean(AppConstants.KEY_IS_LOGGED_IN, true);
        editor.putString(AppConstants.KEY_USER_ID,       userId);
        editor.putString(AppConstants.KEY_USER_NAME,     userName);
        editor.putString(AppConstants.KEY_USER_EMAIL,    email);
        editor.putString(AppConstants.KEY_USER_TOKEN,    token);
        editor.apply();
    }

    public void saveGoogleSession(String userId, String userName, String email) {
        editor.putBoolean(AppConstants.KEY_IS_LOGGED_IN, true);
        editor.putBoolean(AppConstants.KEY_GOOGLE_LOGIN,  true);
        editor.putString(AppConstants.KEY_USER_ID,        userId);
        editor.putString(AppConstants.KEY_USER_NAME,      userName);
        editor.putString(AppConstants.KEY_USER_EMAIL,     email);
        editor.apply();
    }

    // ── READ SESSION ─────────────────────────────────────────────────

    public boolean isLoggedIn()  { return prefs.getBoolean(AppConstants.KEY_IS_LOGGED_IN, false); }
    public boolean isGoogleUser(){ return prefs.getBoolean(AppConstants.KEY_GOOGLE_LOGIN,  false); }
    public String  getUserId()   { return prefs.getString(AppConstants.KEY_USER_ID,    ""); }
    public String  getUserName() { return prefs.getString(AppConstants.KEY_USER_NAME,  ""); }
    public String  getUserEmail(){ return prefs.getString(AppConstants.KEY_USER_EMAIL, ""); }
    public String  getToken()    { return prefs.getString(AppConstants.KEY_USER_TOKEN, ""); }

    // ── CLEAR SESSION ─────────────────────────────────────────────────

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
