package com.arcadefitness.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.arcadefitness.R;
import com.arcadefitness.data.remote.ApiHelper;
import com.arcadefitness.data.remote.RetrofitClient;
import com.arcadefitness.utils.AppConstants;
import com.arcadefitness.utils.SessionManager;
import com.google.gson.JsonObject;

/**
 * LoginActivity.java
 *
 * Calls POST /api/auth/login with { email, password }.
 * On success: saves JWT + user info to SessionManager, goes to Dashboard.
 * Falls back to local credential check if server is unreachable.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnSignIn, btnGuest;
    private TextView tvCreateAccount, tvForgotPassword;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        RetrofitClient.init(getApplicationContext());

        sessionManager = new SessionManager(this);

        // Already logged in — skip straight to Dashboard
        if (sessionManager.isLoggedIn()) {
            goToDashboard();
            return;
        }

        initViews();
    }

    private void initViews() {
        etEmail       = findViewById(R.id.etEmail);
        etPassword    = findViewById(R.id.etPassword);
        btnSignIn     = findViewById(R.id.btnSignIn);
        btnGuest      = findViewById(R.id.btnGuest);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);
        progressBar   = findViewById(R.id.progressBar);

        // Password visibility toggle
        ImageButton btnTogglePassword = findViewById(R.id.btnTogglePassword);
        if (btnTogglePassword != null) {
            btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility(etPassword, btnTogglePassword));
        }

        btnSignIn.setOnClickListener(v -> attemptLogin());

        btnGuest.setOnClickListener(v -> {
            sessionManager.saveGuestSession();
            goToDashboard();
        });

        if (tvCreateAccount != null) {
            tvCreateAccount.setOnClickListener(v -> {
                startActivity(new Intent(this, RegisterActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }
    }

    // ── LOGIN ────────────────────────────────────────────────────────

    private void attemptLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validate(email, password)) return;

        setLoading(true);

        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);

        ApiHelper.call(
                RetrofitClient.getApi().login(body),
                new ApiHelper.ApiCallback<JsonObject>() {
                    @Override
                    public void onSuccess(JsonObject data) {
                        setLoading(false);
                        handleLoginSuccess(data, email);
                    }

                    @Override
                    public void onError(int code, String message) {
                        setLoading(false);
                        if (code == 0) {
                            // No network — try local fallback
                            attemptLocalLogin(email, password);
                        } else {
                            // Server responded with an error (wrong password, etc.)
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    /**
     * Parses the successful login response and saves the session.
     * Backend returns:
     *   { status: "success", data: { user: { user_id, email }, token } }
     */
    private void handleLoginSuccess(JsonObject data, String email) {
        try {
            String token  = data.has("token")   ? data.get("token").getAsString()   : "";
            String userId;
            String userName = email; // fallback display name

            if (data.has("user") && data.get("user").isJsonObject()) {
                JsonObject user = data.getAsJsonObject("user");
                userId = user.has("user_id") ? String.valueOf(user.get("user_id").getAsInt()) : email;
            } else {
                userId = email;
            }

            sessionManager.saveSession(userId, userName, email, token);
            goToDashboard();

        } catch (Exception e) {
            Toast.makeText(this, "Login error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Offline fallback: check SHA-256 hashed credentials stored locally
     * (written during a previous successful register while online).
     */
    private void attemptLocalLogin(String email, String password) {
        boolean valid = sessionManager.checkCredentials(email, password);
        if (valid) {
            // Use the locally-cached userId and name
            String userId = sessionManager.getCachedUserId(email);
            String name   = sessionManager.getCachedName(email);
            // Keep whatever token was stored; sync will get a fresh one when online
            String token  = sessionManager.getCachedToken(email);
            sessionManager.saveSession(userId, name, email, token != null ? token : "offline_token");
            Toast.makeText(this, "Logged in offline", Toast.LENGTH_SHORT).show();
            goToDashboard();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_LONG).show();
        }
    }

    // ── HELPERS ──────────────────────────────────────────────────────

    private boolean validate(String email, String password) {
        boolean ok = true;
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            ok = false;
        } else {
            etEmail.setError(null);
        }
        if (TextUtils.isEmpty(password) || password.length() < AppConstants.PASSWORD_MIN_LENGTH) {
            etPassword.setError("Password must be at least " + AppConstants.PASSWORD_MIN_LENGTH + " characters");
            ok = false;
        } else {
            etPassword.setError(null);
        }
        return ok;
    }

    private void setLoading(boolean loading) {
        btnSignIn.setEnabled(!loading);
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void togglePasswordVisibility(EditText field, ImageButton toggle) {
        if (field.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            field.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                    | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggle.setImageResource(R.drawable.ic_eye_off);
        } else {
            field.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                    | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggle.setImageResource(R.drawable.ic_eye);
        }
        field.setSelection(field.getText().length());
    }

    private void goToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}
