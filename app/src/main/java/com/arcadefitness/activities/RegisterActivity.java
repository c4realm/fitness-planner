package com.arcadefitness.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
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
 * RegisterActivity.java
 * Collects: full name, email, age, gender, password, confirm password.
 * Also supports Google registration (same flow as login).
 *
 * Phase 2: replace TODO sections with Retrofit API calls.
 */
public class RegisterActivity extends AppCompatActivity {

    // Views
    private EditText    etFullName, etEmail, etAge, etPassword, etConfirmPassword;
    private Spinner     spinnerGender;
    private ImageButton btnTogglePassword, btnToggleConfirm;
    private Button      btnCreateAccount, btnGoogle;
    private TextView    tvSignIn;

    // State
    private boolean passwordVisible = false;
    private boolean confirmVisible  = false;

    // Utils
    private SessionManager    sessionManager;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sessionManager = new SessionManager(this);
        initViews();
        setupGenderSpinner();
        setupGoogleSignIn();
        setupClickListeners();
        setupInputFocusHighlight();
    }

    // ── INIT ────────────────────────────────────────────────────────

    private void initViews() {
        etFullName        = findViewById(R.id.etFullName);
        etEmail           = findViewById(R.id.etEmail);
        etAge             = findViewById(R.id.etAge);
        spinnerGender     = findViewById(R.id.spinnerGender);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnToggleConfirm  = findViewById(R.id.btnToggleConfirm);
        btnCreateAccount  = findViewById(R.id.btnCreateAccount);
        btnGoogle         = findViewById(R.id.btnGoogle);
        tvSignIn          = findViewById(R.id.tvSignIn);
    }

    private void setupGenderSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            AppConstants.GENDER_OPTIONS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(AppConstants.GOOGLE_WEB_CLIENT_ID)
            .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupClickListeners() {
        btnCreateAccount.setOnClickListener(v -> attemptRegistration());
        btnGoogle.setOnClickListener(v -> startGoogleSignIn());
        tvSignIn.setOnClickListener(v -> goToLogin());
        btnTogglePassword.setOnClickListener(v -> togglePassword());
        btnToggleConfirm.setOnClickListener(v -> toggleConfirm());
    }

    private void setupInputFocusHighlight() {
        View[] inputs = {etFullName, etEmail, etAge, etPassword, etConfirmPassword};
        for (View input : inputs) {
            input.setOnFocusChangeListener((v, hasFocus) -> {
                v.setBackground(hasFocus
                    ? getDrawable(R.drawable.bg_input_focused)
                    : getDrawable(R.drawable.bg_input_default));
            });
        }
    }

    // ── REGISTRATION ────────────────────────────────────────────────

    private void attemptRegistration() {
        String fullName = etFullName.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String ageStr   = etAge.getText().toString().trim();
        int    gender   = spinnerGender.getSelectedItemPosition();
        String password = etPassword.getText().toString();
        String confirm  = etConfirmPassword.getText().toString();

        // Validate all fields in order
        String nameError     = ValidationUtils.validateFullName(fullName);
        String emailError    = ValidationUtils.validateEmail(email);
        String ageError      = ValidationUtils.validateAge(ageStr);
        String genderError   = ValidationUtils.validateGender(gender);
        String passwordError = ValidationUtils.validatePassword(password);
        String confirmError  = ValidationUtils.validateConfirmPassword(password, confirm);

        if (nameError != null)     { etFullName.setError(nameError);       etFullName.requestFocus(); return; }
        if (emailError != null)    { etEmail.setError(emailError);         etEmail.requestFocus();    return; }
        if (ageError != null)      { etAge.setError(ageError);             etAge.requestFocus();      return; }
        if (genderError != null)   { Toast.makeText(this, genderError, Toast.LENGTH_SHORT).show();   return; }
        if (passwordError != null) { etPassword.setError(passwordError);   etPassword.requestFocus(); return; }
        if (confirmError != null)  { etConfirmPassword.setError(confirmError); etConfirmPassword.requestFocus(); return; }

        // ── TODO (Phase 2): Retrofit API call ────────────────────────
        // RegisterRequest req = new RegisterRequest(fullName, email, ageStr, gender, password);
        // ApiClient.getInstance().getApiService().register(req)
        //     .enqueue(new Callback<RegisterResponse>() {
        //         @Override public void onResponse(...) { handleSuccess(response); }
        //         @Override public void onFailure(...)  { showError("Network error"); }
        //     });
        // ─────────────────────────────────────────────────────────────

        // Temporary: save account locally and go to Dashboard
        sessionManager.saveRegisteredAccount(email, password, fullName);
        sessionManager.saveSession("user_" + email, fullName, email, "mock_token");
        goToDashboard();
    }

    // ── GOOGLE SIGN-IN ──────────────────────────────────────────────

    private void startGoogleSignIn() {
        startActivityForResult(googleSignInClient.getSignInIntent(), AppConstants.RC_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    sessionManager.saveGoogleSession(
                        account.getId() != null ? account.getId() : "",
                        account.getDisplayName() != null ? account.getDisplayName() : "",
                        account.getEmail() != null ? account.getEmail() : ""
                    );
                    goToDashboard();
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ── HELPERS ──────────────────────────────────────────────────────

    private void togglePassword() {
        passwordVisible = !passwordVisible;
        etPassword.setTransformationMethod(
            passwordVisible ? HideReturnsTransformationMethod.getInstance()
                            : PasswordTransformationMethod.getInstance());
        btnTogglePassword.setImageResource(
            passwordVisible ? R.drawable.ic_eye_off : R.drawable.ic_eye);
        etPassword.setSelection(etPassword.getText().length());
    }

    private void toggleConfirm() {
        confirmVisible = !confirmVisible;
        etConfirmPassword.setTransformationMethod(
            confirmVisible ? HideReturnsTransformationMethod.getInstance()
                           : PasswordTransformationMethod.getInstance());
        btnToggleConfirm.setImageResource(
            confirmVisible ? R.drawable.ic_eye_off : R.drawable.ic_eye);
        etConfirmPassword.setSelection(etConfirmPassword.getText().length());
    }

    private void goToLogin() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void goToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}
