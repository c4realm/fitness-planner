package com.arcadefitness.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager.java
 * Handles saving and reading user session data via SharedPreferences.
 * Used by all activities to check login state and store/clear tokens.
 */
public class SessionManager {

    private final Context context;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        this.context = context;
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

    // ── REGISTERED ACCOUNTS (temporary — replace with backend) ─────
    // Uses a separate SharedPreferences file so logout doesn't wipe accounts

    private SharedPreferences getAccountPrefs() {
        return context.getSharedPreferences("registered_accounts", Context.MODE_PRIVATE);
    }

    public void saveRegisteredAccount(String email, String password, String fullName) {
        SharedPreferences accountPrefs = getAccountPrefs();
        accountPrefs.edit()
            .putString("account_pwd_" + email, password)
            .putString("account_name_" + email, fullName)
            .apply();
    }

    public boolean checkCredentials(String email, String password) {
        SharedPreferences accountPrefs = getAccountPrefs();
        String storedPassword = accountPrefs.getString("account_pwd_" + email, null);
        return storedPassword != null && storedPassword.equals(password);
    }

    public String getRegisteredUserName(String email) {
        return getAccountPrefs().getString("account_name_" + email, "");
    }

    // ── CLEAR SESSION ─────────────────────────────────────────────────

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
