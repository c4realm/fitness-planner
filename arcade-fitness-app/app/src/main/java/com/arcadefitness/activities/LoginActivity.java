package com.arcadefitness.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.arcadefitness.R;
import com.arcadefitness.utils.SessionManager;
import com.arcadefitness.utils.ValidationUtils;

/**
 * LoginActivity.java
 * Handles email/password login.
 * Google Sign-In: UI preserved, wired to Phase 3 placeholder toast.
 * Guest access: bypasses login, lands directly on Dashboard.
 */
public class LoginActivity extends AppCompatActivity {

    // Views
    private EditText    etEmail, etPassword;
    private ImageButton btnTogglePassword;
    private Button      btnSignIn, btnGoogle, btnGuest;
    private TextView    tvForgotPassword, tvCreateAccount;

    // State
    private boolean isPasswordVisible = false;

    // Utilities
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        initViews();
        setupClickListeners();
        setupInputFocusHighlight();
    }

    // ── INIT ────────────────────────────────────────────────────────

    private void initViews() {
        etEmail           = findViewById(R.id.etEmail);
        etPassword        = findViewById(R.id.etPassword);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnSignIn         = findViewById(R.id.btnSignIn);
        btnGoogle         = findViewById(R.id.btnGoogle);
        btnGuest          = findViewById(R.id.btnGuest);
        tvForgotPassword  = findViewById(R.id.tvForgotPassword);
        tvCreateAccount   = findViewById(R.id.tvCreateAccount);
    }

    private void setupClickListeners() {
        btnSignIn.setOnClickListener(v -> attemptEmailLogin());
        btnGoogle.setOnClickListener(v ->
            Toast.makeText(this,
                "Google Sign-In coming in Phase 3", Toast.LENGTH_SHORT).show());
        btnGuest.setOnClickListener(v -> goToDashboard());
        tvCreateAccount.setOnClickListener(v -> goToRegister());
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
    }

    /** Orange border when field is focused — visual feedback */
    private void setupInputFocusHighlight() {
        View[] inputs = {etEmail, etPassword};
        for (View input : inputs) {
            input.setOnFocusChangeListener((v, hasFocus) -> {
                v.setBackground(hasFocus
                    ? getDrawable(R.drawable.bg_input_focused)
                    : getDrawable(R.drawable.bg_input_default));
            });
        }
    }

    // ── EMAIL / PASSWORD LOGIN ───────────────────────────────────────

    private void attemptEmailLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        String emailError    = ValidationUtils.validateEmail(email);
        String passwordError = ValidationUtils.validatePassword(password);

        if (emailError != null) {
            etEmail.setError(emailError);
            etEmail.requestFocus();
            return;
        }
        if (passwordError != null) {
            etPassword.setError(passwordError);
            etPassword.requestFocus();
            return;
        }

        // ── TODO (Phase 3): Replace with Retrofit API call ──────────
        // ApiClient.getInstance().getApiService()
        //     .login(new LoginRequest(email, password))
        //     .enqueue(...);
        // ────────────────────────────────────────────────────────────

        if (!sessionManager.checkCredentials(email, password)) {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            return;
        }

        String userName = sessionManager.getRegisteredUserName(email);
        sessionManager.saveSession("user_" + email, userName, email, "mock_token");
        goToDashboard();
    }

    // ── HELPERS ──────────────────────────────────────────────────────

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        etPassword.setTransformationMethod(
            isPasswordVisible
                ? HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance()
        );
        btnTogglePassword.setImageResource(
            isPasswordVisible ? R.drawable.ic_eye_off : R.drawable.ic_eye
        );
        etPassword.setSelection(etPassword.getText().length());
    }

    private void handleForgotPassword() {
        Toast.makeText(this, "Forgot password — coming in Phase 3", Toast.LENGTH_SHORT).show();
    }

    private void goToRegister() {
        startActivity(new Intent(this, RegisterActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void goToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}
