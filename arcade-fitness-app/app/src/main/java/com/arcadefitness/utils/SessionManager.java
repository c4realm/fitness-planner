package com.arcadefitness.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * SessionManager.java
 *
 * Manages:
 *  - Active session (JWT token, userId, name, email, isGuest)
 *  - Credential cache for offline login fallback
 *    (email → SHA-256(password) stored in a separate encrypted prefs file)
 *
 * All data is stored in EncryptedSharedPreferences backed by Android Keystore.
 */
public class SessionManager {

    private static final String TAG = "SessionManager";

    // Active session prefs
    private static final String SESSION_PREFS = "arcade_session_prefs";
    // Credential cache prefs (for offline fallback)
    private static final String CREDENTIAL_PREFS = "arcade_credential_prefs";

    private final SharedPreferences sessionPrefs;
    private final SharedPreferences credentialPrefs;

    public SessionManager(Context context) {
        sessionPrefs    = buildEncryptedPrefs(context, SESSION_PREFS);
        credentialPrefs = buildEncryptedPrefs(context, CREDENTIAL_PREFS);
    }

    private SharedPreferences buildEncryptedPrefs(Context context, String fileName) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    context,
                    fileName,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e(TAG, "EncryptedSharedPreferences unavailable, falling back to plaintext", e);
            return context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        }
    }

    // ── ACTIVE SESSION ───────────────────────────────────────────────

    /**
     * Save a real authenticated session (email/password or API login).
     */
    public void saveSession(String userId, String userName, String email, String token) {
        sessionPrefs.edit()
                .putBoolean(AppConstants.KEY_IS_LOGGED_IN, true)
                .putBoolean(AppConstants.KEY_IS_GUEST,     false)
                .putString(AppConstants.KEY_USER_ID,       userId)
                .putString(AppConstants.KEY_USER_NAME,     userName)
                .putString(AppConstants.KEY_USER_EMAIL,    email)
                .putString(AppConstants.KEY_USER_TOKEN,    token)
                .apply();
    }

    /**
     * Save a guest session — no credentials, limited features.
     */
    public void saveGuestSession() {
        sessionPrefs.edit()
                .putBoolean(AppConstants.KEY_IS_LOGGED_IN, true)
                .putBoolean(AppConstants.KEY_IS_GUEST,     true)
                .putString(AppConstants.KEY_USER_ID,       AppConstants.GUEST_USER_ID)
                .putString(AppConstants.KEY_USER_NAME,     "Guest")
                .putString(AppConstants.KEY_USER_EMAIL,    "")
                .putString(AppConstants.KEY_USER_TOKEN,    "")
                .apply();
    }

    public void clearSession() {
        sessionPrefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return sessionPrefs.getBoolean(AppConstants.KEY_IS_LOGGED_IN, false);
    }

    public boolean isGuest() {
        return sessionPrefs.getBoolean(AppConstants.KEY_IS_GUEST, false);
    }

    public String getUserId() {
        return sessionPrefs.getString(AppConstants.KEY_USER_ID, "");
    }

    public String getUserName() {
        return sessionPrefs.getString(AppConstants.KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return sessionPrefs.getString(AppConstants.KEY_USER_EMAIL, "");
    }

    /** Returns the stored JWT. Empty string if guest or not logged in. */
    public String getToken() {
        return sessionPrefs.getString(AppConstants.KEY_USER_TOKEN, "");
    }

    /** True when the stored token is a mock/offline placeholder. */
    public boolean isUsingMockToken() {
        String t = getToken();
        return t.equals("mock_token") || t.equals("offline_token") || t.isEmpty();
    }

    // ── CREDENTIAL CACHE (offline login fallback) ────────────────────

    /**
     * Store hashed credentials and user metadata for offline login.
     * Called after a successful online register or login.
     */
    public void cacheCredentials(String email, String password,
                                 String userId, String name, String token) {
        String key = "cred_" + email.toLowerCase();
        credentialPrefs.edit()
                .putString(key + "_hash",   sha256(password))
                .putString(key + "_userId", userId)
                .putString(key + "_name",   name)
                .putString(key + "_token",  token)
                .apply();
    }

    /**
     * Verify email + password against the locally cached hash.
     * Returns true if credentials match a cached account.
     */
    public boolean checkCredentials(String email, String password) {
        String key    = "cred_" + email.toLowerCase();
        String stored = credentialPrefs.getString(key + "_hash", null);
        if (stored == null) return false;
        return stored.equals(sha256(password));
    }

    public String getCachedUserId(String email) {
        return credentialPrefs.getString("cred_" + email.toLowerCase() + "_userId", email);
    }

    public String getCachedName(String email) {
        return credentialPrefs.getString("cred_" + email.toLowerCase() + "_name", email);
    }

    public String getCachedToken(String email) {
        return credentialPrefs.getString("cred_" + email.toLowerCase() + "_token", null);
    }

    // ── HASH ─────────────────────────────────────────────────────────

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "SHA-256 failed", e);
            return input; // unsafe fallback, should never happen
        }
    }
}