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
import com.arcadefitness.utils.AppConstants;
import com.arcadefitness.utils.SessionManager;
import com.arcadefitness.utils.ValidationUtils;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

/**
 * LoginActivity.java
 * Handles email/password login and Google Sign-In.
 *
 * Phase 2: replace the TODO sections with actual Retrofit API calls.
 */
public class LoginActivity extends AppCompatActivity {

    // Views
    private EditText    etEmail, etPassword;
    private ImageButton btnTogglePassword;
    private Button      btnSignIn, btnGoogle;
    private TextView    tvForgotPassword, tvCreateAccount;

    // State
    private boolean     isPasswordVisible = false;

    // Utilities
    private SessionManager    sessionManager;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        initViews();
        setupGoogleSignIn();
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
        tvForgotPassword  = findViewById(R.id.tvForgotPassword);
        tvCreateAccount   = findViewById(R.id.tvCreateAccount);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(AppConstants.GOOGLE_WEB_CLIENT_ID)
            .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupClickListeners() {
        btnSignIn.setOnClickListener(v -> attemptEmailLogin());
        btnGoogle.setOnClickListener(v -> startGoogleSignIn());
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

        // Validate
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

        // ── TODO (Phase 2): Replace with Retrofit API call ──────────
        // ApiService.login(email, password) → on success → sessionManager.saveSession(...)
        //
        // Example:
        // showLoading(true);
        // ApiClient.getInstance().getApiService().login(new LoginRequest(email, password))
        //     .enqueue(new Callback<LoginResponse>() {
        //         @Override public void onResponse(...) { handleLoginSuccess(response); }
        //         @Override public void onFailure(...)  { showError("Network error"); }
        //     });
        // ────────────────────────────────────────────────────────────

        // Check against locally registered accounts (remove when backend is connected)
        if (!sessionManager.checkCredentials(email, password)) {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            return;
        }

        String userName = sessionManager.getRegisteredUserName(email);
        sessionManager.saveSession("user_" + email, userName, email, "mock_token");
        goToDashboard();
    }

    // ── GOOGLE SIGN-IN ──────────────────────────────────────────────

    private void startGoogleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, AppConstants.RC_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String userId   = account.getId() != null ? account.getId() : "";
                String userName = account.getDisplayName() != null ? account.getDisplayName() : "";
                String email    = account.getEmail() != null ? account.getEmail() : "";
                String idToken  = account.getIdToken() != null ? account.getIdToken() : "";

                // ── TODO (Phase 2): Send idToken to your Node.js backend ──
                // ApiClient.getInstance().getApiService().googleAuth(new GoogleAuthRequest(idToken))
                //     .enqueue(...) → on success → sessionManager.saveGoogleSession(...)
                // ──────────────────────────────────────────────────────────

                // Temporary: save Google session directly
                sessionManager.saveGoogleSession(userId, userName, email);
                goToDashboard();
            }
        } catch (ApiException e) {
            String message;
            if (e.getStatusCode() == 10) {
                message = "Google Sign-In error (code 10). Replace google-services.json with YOUR real Firebase file, then add SHA-1 in Firebase Console. Also set GOOGLE_WEB_CLIENT_ID to your Web OAuth client ID.";
            } else {
                message = "Google Sign-In failed: " + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
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
        // TODO (Phase 2): Open ForgotPasswordActivity or dialog
        Toast.makeText(this, "Forgot password — coming in Phase 2", Toast.LENGTH_SHORT).show();
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
