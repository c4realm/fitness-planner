package com.arcadefitness.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
 * RegisterActivity.java
 *
 * Calls POST /api/auth/register with { email, password }.
 * On success: saves JWT + user info, also caches credentials locally
 * for offline fallback, then goes to Dashboard.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etAge, etPassword, etConfirmPassword;
    private Spinner  spinnerGender;
    private Button   btnCreateAccount, btnGuest;
    private TextView tvSignIn;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        RetrofitClient.init(getApplicationContext());

        sessionManager = new SessionManager(this);
        initViews();
    }

    private void initViews() {
        etFullName       = findViewById(R.id.etFullName);
        etEmail          = findViewById(R.id.etEmail);
        etAge            = findViewById(R.id.etAge);
        etPassword       = findViewById(R.id.etPassword);
        etConfirmPassword= findViewById(R.id.etConfirmPassword);
        spinnerGender    = findViewById(R.id.spinnerGender);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnGuest         = findViewById(R.id.btnGuest);
        tvSignIn         = findViewById(R.id.tvSignIn);
        progressBar      = findViewById(R.id.progressBar);

        // Password visibility toggles
        ImageButton btnTogglePassword = findViewById(R.id.btnTogglePassword);
        if (btnTogglePassword != null) {
            btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility(etPassword, btnTogglePassword));
        }
        ImageButton btnToggleConfirm = findViewById(R.id.btnToggleConfirm);
        if (btnToggleConfirm != null) {
            btnToggleConfirm.setOnClickListener(v -> togglePasswordVisibility(etConfirmPassword, btnToggleConfirm));
        }

        btnCreateAccount.setOnClickListener(v -> attemptRegister());

        btnGuest.setOnClickListener(v -> {
            sessionManager.saveGuestSession();
            goToDashboard();
        });

        if (tvSignIn != null) {
            tvSignIn.setOnClickListener(v -> {
                startActivity(new Intent(this, LoginActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            });
        }
    }

    // ── REGISTER ─────────────────────────────────────────────────────

    private void attemptRegister() {
        String name     = etFullName.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm  = etConfirmPassword.getText().toString().trim();

        if (!validate(name, email, password, confirm)) return;

        setLoading(true);

        // Backend only needs email + password for registration
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);

        ApiHelper.call(
                RetrofitClient.getApi().register(body),
                new ApiHelper.ApiCallback<JsonObject>() {
                    @Override
                    public void onSuccess(JsonObject data) {
                        setLoading(false);
                        handleRegisterSuccess(data, name, email, password);
                    }

                    @Override
                    public void onError(int code, String message) {
                        setLoading(false);
                        if (code == 409) {
                            etEmail.setError("An account with this email already exists");
                            etEmail.requestFocus();
                        } else if (code == 0) {
                            // No network — save locally and continue
                            registerOffline(name, email, password);
                        } else {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    /**
     * Backend returns:
     *   { status: "success", data: { user: { user_id, email }, token } }
     */
    private void handleRegisterSuccess(JsonObject data, String name, String email, String password) {
        try {
            String token = data.has("token") ? data.get("token").getAsString() : "";
            String userId;

            if (data.has("user") && data.get("user").isJsonObject()) {
                JsonObject user = data.getAsJsonObject("user");
                userId = user.has("user_id") ? String.valueOf(user.get("user_id").getAsInt()) : email;
            } else {
                userId = email;
            }

            // Cache credentials locally for offline fallback
            sessionManager.cacheCredentials(email, password, userId, name, token);
            sessionManager.saveSession(userId, name, email, token);

            goToDashboard();

        } catch (Exception e) {
            Toast.makeText(this, "Registration error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * No network: store locally and continue.
     * The sync queue will push the account to the server when connectivity returns.
     */
    private void registerOffline(String name, String email, String password) {
        String userId = "local_" + System.currentTimeMillis();
        sessionManager.cacheCredentials(email, password, userId, name, "offline_token");
        sessionManager.saveSession(userId, name, email, "offline_token");
        Toast.makeText(this, "Account created offline — will sync when connected",
                Toast.LENGTH_LONG).show();
        goToDashboard();
    }

    // ── HELPERS ──────────────────────────────────────────────────────

    private boolean validate(String name, String email, String password, String confirm) {
        boolean ok = true;

        if (TextUtils.isEmpty(name) || name.length() < AppConstants.NAME_MIN_LENGTH) {
            etFullName.setError("Name must be at least " + AppConstants.NAME_MIN_LENGTH + " characters");
            ok = false;
        } else {
            etFullName.setError(null);
        }

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

        if (!password.equals(confirm)) {
            etConfirmPassword.setError("Passwords do not match");
            ok = false;
        } else {
            etConfirmPassword.setError(null);
        }

        return ok;
    }

    private void setLoading(boolean loading) {
        btnCreateAccount.setEnabled(!loading);
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
