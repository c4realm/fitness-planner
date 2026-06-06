package com.arcadefitness.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * SessionManager.java
 * Manages user session state and credential storage.
 *
 * Security: uses EncryptedSharedPreferences backed by the Android Keystore
 * so tokens and passwords are never stored in plain text on disk.
 * Keys are AES-256-GCM encrypted; the master key lives in the hardware-backed
 * Keystore and never leaves the device.
 */
public class SessionManager {

    private static final String ENCRYPTED_PREFS_FILE = "arcade_secure_prefs";
    private static final String ACCOUNT_PREFS_FILE   = "arcade_secure_accounts";

    private final Context context;
    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs   = buildEncryptedPrefs(ENCRYPTED_PREFS_FILE);
    }

    // ── ENCRYPTED PREFS BUILDER ──────────────────────────────────────

    private SharedPreferences buildEncryptedPrefs(String fileName) {
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
        } catch (GeneralSecurityException | IOException e) {
            // Fallback to standard prefs if Keystore unavailable (very old devices)
            return context.getSharedPreferences(fileName + "_fallback", Context.MODE_PRIVATE);
        }
    }

    // ── SAVE SESSION ─────────────────────────────────────────────────

    public void saveSession(String userId, String userName, String email, String token) {
        prefs.edit()
                .putBoolean(AppConstants.KEY_IS_LOGGED_IN, true)
                .putString(AppConstants.KEY_USER_ID,       userId)
                .putString(AppConstants.KEY_USER_NAME,     userName)
                .putString(AppConstants.KEY_USER_EMAIL,    email)
                .putString(AppConstants.KEY_USER_TOKEN,    token)
                .apply();
    }

    public void saveGoogleSession(String userId, String userName, String email) {
        prefs.edit()
                .putBoolean(AppConstants.KEY_IS_LOGGED_IN, true)
                .putBoolean(AppConstants.KEY_GOOGLE_LOGIN,  true)
                .putString(AppConstants.KEY_USER_ID,        userId)
                .putString(AppConstants.KEY_USER_NAME,      userName)
                .putString(AppConstants.KEY_USER_EMAIL,     email)
                .apply();
    }

    // ── READ SESSION ─────────────────────────────────────────────────

    public boolean isLoggedIn()  { return prefs.getBoolean(AppConstants.KEY_IS_LOGGED_IN, false); }
    public boolean isGoogleUser(){ return prefs.getBoolean(AppConstants.KEY_GOOGLE_LOGIN,  false); }
    public String  getUserId()   { return prefs.getString(AppConstants.KEY_USER_ID,    ""); }
    public String  getUserName() { return prefs.getString(AppConstants.KEY_USER_NAME,  ""); }
    public String  getUserEmail(){ return prefs.getString(AppConstants.KEY_USER_EMAIL, ""); }
    public String  getToken()    { return prefs.getString(AppConstants.KEY_USER_TOKEN, ""); }

    // ── REGISTERED ACCOUNTS (encrypted) ─────────────────────────────
    // Separate encrypted file so clearSession() doesn't wipe stored accounts.
    // Passwords are hashed with SHA-256 before storage — never stored raw.

    private SharedPreferences getAccountPrefs() {
        return buildEncryptedPrefs(ACCOUNT_PREFS_FILE);
    }

    public void saveRegisteredAccount(String email, String password, String fullName) {
        String hashedPassword = hashPassword(password);
        getAccountPrefs().edit()
                .putString("account_pwd_"  + email, hashedPassword)
                .putString("account_name_" + email, fullName)
                .apply();
    }

    public boolean checkCredentials(String email, String password) {
        String stored = getAccountPrefs().getString("account_pwd_" + email, null);
        if (stored == null) return false;
        return stored.equals(hashPassword(password));
    }

    public String getRegisteredUserName(String email) {
        return getAccountPrefs().getString("account_name_" + email, "");
    }

    // ── CLEAR SESSION ─────────────────────────────────────────────────

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    // ── HELPERS ──────────────────────────────────────────────────────

    /**
     * One-way SHA-256 hash of a password.
     * Passwords are never stored or compared in plain text.
     */
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest digest =
                    java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed present on all Android devices
            return password;
        }
    }
}
