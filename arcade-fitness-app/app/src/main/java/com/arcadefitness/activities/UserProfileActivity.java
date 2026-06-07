package com.arcadefitness.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.arcadefitness.R;
import com.arcadefitness.data.local.entity.UserProfileEntity;
import com.arcadefitness.data.repository.FitnessRepository;
import com.arcadefitness.utils.SessionManager;
import com.arcadefitness.utils.ThemeUtil;

public class UserProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private FitnessRepository fitnessRepository;

    private TextView tvAvatarInitials, tvProfileName, tvProfileEmail;

    private LinearLayout layoutViewMode, layoutEditMode;
    private TextView tvViewName, tvViewAge, tvViewHeight, tvViewWeight, tvViewWeeklyGoal, tvViewFitnessLevel;

    private EditText etDisplayName, etAge, etHeight, etWeight, etWeeklyGoal;
    private RadioGroup rgFitnessLevel;
    private Button btnSaveProfile;

    private UserProfileEntity currentProfile;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        sessionManager = new SessionManager(this);
        fitnessRepository = FitnessRepository.getInstance(this);

        initViews();
        loadProfile();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnSettings).setOnClickListener(v -> showSettingsDialog());

        tvAvatarInitials = findViewById(R.id.tvAvatarInitials);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);

        layoutViewMode = findViewById(R.id.layoutViewMode);
        layoutEditMode = findViewById(R.id.layoutEditMode);

        tvViewName = findViewById(R.id.tvViewName);
        tvViewAge = findViewById(R.id.tvViewAge);
        tvViewHeight = findViewById(R.id.tvViewHeight);
        tvViewWeight = findViewById(R.id.tvViewWeight);
        tvViewWeeklyGoal = findViewById(R.id.tvViewWeeklyGoal);
        tvViewFitnessLevel = findViewById(R.id.tvViewFitnessLevel);

        etDisplayName = findViewById(R.id.etDisplayName);
        etAge = findViewById(R.id.etAge);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        etWeeklyGoal = findViewById(R.id.etWeeklyGoal);

        rgFitnessLevel = findViewById(R.id.rgFitnessLevel);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnSaveProfile.setOnClickListener(v -> saveProfile());

        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            layoutViewMode.setVisibility(View.GONE);
            layoutEditMode.setVisibility(View.VISIBLE);
        });
    }

    private void showSettingsDialog() {
        boolean isDark = ThemeUtil.isDarkMode(this);
        String[] items = {
            isDark ? "\u2600\uFE0F Light Mode" : "\uD83C\uDF11 Dark Mode",
            "Logout"
        };
        new AlertDialog.Builder(this)
            .setTitle("Settings")
            .setItems(items, (dialog, which) -> {
                if (which == 0) {
                    ThemeUtil.setDarkMode(this, !ThemeUtil.isDarkMode(this));
                    recreate();
                } else if (which == 1) {
                    logout();
                }
            })
            .show();
    }

    private void loadProfile() {
        String name = sessionManager.getUserName();
        userEmail = sessionManager.getUserEmail();

        tvProfileName.setText(name);
        tvProfileEmail.setText(userEmail);

        if (name != null && !name.isEmpty()) {
            tvAvatarInitials.setText(String.valueOf(Character.toUpperCase(name.charAt(0))));
        }

        fitnessRepository.getUserProfileByEmail(userEmail, new FitnessRepository.RepositoryCallback<UserProfileEntity>() {
            @Override
            public void onSuccess(UserProfileEntity profile) {
                if (profile != null) {
                    currentProfile = profile;
                    runOnUiThread(() -> populateFields(profile));
                } else {
                    UserProfileEntity newProfile = new UserProfileEntity(name, userEmail, 0);
                    fitnessRepository.insertUserProfile(newProfile, new FitnessRepository.RepositoryCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer id) {
                            newProfile.setId(id);
                            currentProfile = newProfile;
                        }

                        @Override
                        public void onError(String errorMessage) {
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private void populateFields(UserProfileEntity profile) {
        populateViewFields(profile);

        etDisplayName.setText(profile.getDisplayName());
        if (profile.getAge() > 0) {
            etAge.setText(String.valueOf(profile.getAge()));
        } else {
            etAge.setText("");
        }
        if (profile.getHeightCm() > 0) {
            etHeight.setText(String.valueOf(profile.getHeightCm()));
        } else {
            etHeight.setText("");
        }
        if (profile.getWeightKg() > 0) {
            etWeight.setText(String.valueOf(profile.getWeightKg()));
        } else {
            etWeight.setText("");
        }
        if (profile.getWeeklyGoal() > 0) {
            etWeeklyGoal.setText(String.valueOf(profile.getWeeklyGoal()));
        } else {
            etWeeklyGoal.setText("");
        }

        String level = profile.getFitnessLevel();
        if (level != null) {
            switch (level) {
                case "BEGINNER":
                    rgFitnessLevel.check(R.id.rbBeginner);
                    break;
                case "INTERMEDIATE":
                    rgFitnessLevel.check(R.id.rbIntermediate);
                    break;
                case "ADVANCED":
                    rgFitnessLevel.check(R.id.rbAdvanced);
                    break;
            }
        }
    }

    private void populateViewFields(UserProfileEntity profile) {
        tvViewName.setText(profile.getDisplayName() != null ? profile.getDisplayName() : "-");
        tvViewAge.setText(profile.getAge() > 0 ? String.valueOf(profile.getAge()) : "-");
        tvViewHeight.setText(profile.getHeightCm() > 0 ? String.valueOf(profile.getHeightCm()) + " cm" : "-");
        tvViewWeight.setText(profile.getWeightKg() > 0 ? String.valueOf(profile.getWeightKg()) + " kg" : "-");
        tvViewWeeklyGoal.setText(profile.getWeeklyGoal() > 0 ? String.valueOf(profile.getWeeklyGoal()) + " days" : "-");

        String level = profile.getFitnessLevel();
        if (level != null) {
            switch (level) {
                case "BEGINNER":
                    tvViewFitnessLevel.setText(getString(R.string.beginner));
                    break;
                case "INTERMEDIATE":
                    tvViewFitnessLevel.setText(getString(R.string.intermediate));
                    break;
                case "ADVANCED":
                    tvViewFitnessLevel.setText(getString(R.string.advanced));
                    break;
            }
        } else {
            tvViewFitnessLevel.setText("-");
        }
    }

    private void saveProfile() {
        String name = etDisplayName.getText().toString().trim();
        if (name.isEmpty()) {
            etDisplayName.setError("Name is required");
            etDisplayName.requestFocus();
            return;
        }

        int age = 0;
        try {
            age = Integer.parseInt(etAge.getText().toString().trim());
        } catch (NumberFormatException ignored) {
        }
        if (age < 10 || age > 100) {
            etAge.setError("Enter a valid age (10-100)");
            etAge.requestFocus();
            return;
        }

        double height = 0;
        try {
            height = Double.parseDouble(etHeight.getText().toString().trim());
        } catch (NumberFormatException ignored) {
        }
        if (height <= 0) {
            etHeight.setError("Enter a valid height");
            etHeight.requestFocus();
            return;
        }

        double weight = 0;
        try {
            weight = Double.parseDouble(etWeight.getText().toString().trim());
        } catch (NumberFormatException ignored) {
        }
        if (weight <= 0) {
            etWeight.setError("Enter a valid weight");
            etWeight.requestFocus();
            return;
        }

        int weeklyGoal = 0;
        try {
            weeklyGoal = Integer.parseInt(etWeeklyGoal.getText().toString().trim());
        } catch (NumberFormatException ignored) {
        }
        if (weeklyGoal < 1 || weeklyGoal > 7) {
            etWeeklyGoal.setError("Enter a number between 1 and 7");
            etWeeklyGoal.requestFocus();
            return;
        }

        String fitnessLevel = "BEGINNER";
        int checkedId = rgFitnessLevel.getCheckedRadioButtonId();
        if (checkedId == R.id.rbIntermediate) {
            fitnessLevel = "INTERMEDIATE";
        } else if (checkedId == R.id.rbAdvanced) {
            fitnessLevel = "ADVANCED";
        }

        if (currentProfile == null) {
            currentProfile = new UserProfileEntity(name, userEmail, age);
            currentProfile.setHeightCm(height);
            currentProfile.setWeightKg(weight);
            currentProfile.setWeeklyGoal(weeklyGoal);
            currentProfile.setFitnessLevel(fitnessLevel);
            fitnessRepository.insertUserProfile(currentProfile, new FitnessRepository.RepositoryCallback<Integer>() {
                @Override
                public void onSuccess(Integer id) {
                    currentProfile.setId(id);
                    runOnUiThread(() -> {
                        Toast.makeText(UserProfileActivity.this, R.string.profile_saved, Toast.LENGTH_SHORT).show();
                        switchToViewMode();
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> Toast.makeText(UserProfileActivity.this, "Failed: " + errorMessage, Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            currentProfile.setDisplayName(name);
            currentProfile.setAge(age);
            currentProfile.setHeightCm(height);
            currentProfile.setWeightKg(weight);
            currentProfile.setWeeklyGoal(weeklyGoal);
            currentProfile.setFitnessLevel(fitnessLevel);
            fitnessRepository.updateUserProfile(currentProfile, new FitnessRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    runOnUiThread(() -> {
                        Toast.makeText(UserProfileActivity.this, R.string.profile_saved, Toast.LENGTH_SHORT).show();
                        switchToViewMode();
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> Toast.makeText(UserProfileActivity.this, "Failed: " + errorMessage, Toast.LENGTH_SHORT).show());
                }
            });
        }

        tvProfileName.setText(name);
        if (!name.isEmpty()) {
            tvAvatarInitials.setText(String.valueOf(Character.toUpperCase(name.charAt(0))));
        }
    }

    private void switchToViewMode() {
        if (currentProfile != null) {
            populateViewFields(currentProfile);
        }
        layoutEditMode.setVisibility(View.GONE);
        layoutViewMode.setVisibility(View.VISIBLE);
    }

    private void logout() {
        sessionManager.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}
