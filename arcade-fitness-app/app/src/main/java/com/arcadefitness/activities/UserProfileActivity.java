package com.arcadefitness.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
    private EditText etDisplayName, etAge, etHeight, etWeight, etWeeklyGoal;
    private RadioGroup rgFitnessLevel;
    private Button btnSaveProfile, btnLogout;

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

        tvAvatarInitials = findViewById(R.id.tvAvatarInitials);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);

        etDisplayName = findViewById(R.id.etDisplayName);
        etAge = findViewById(R.id.etAge);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        etWeeklyGoal = findViewById(R.id.etWeeklyGoal);

        rgFitnessLevel = findViewById(R.id.rgFitnessLevel);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnSaveProfile.setOnClickListener(v -> saveProfile());

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());

        TextView tvThemeLabel = findViewById(R.id.tvThemeLabel);
        TextView btnThemeToggle = findViewById(R.id.btnThemeToggle);
        boolean isDark = ThemeUtil.isDarkMode(this);
        tvThemeLabel.setText(isDark ? "Dark Mode" : "Light Mode");
        btnThemeToggle.setText(isDark ? "🌙" : "☀️");
        btnThemeToggle.setOnClickListener(v -> {
            ThemeUtil.setDarkMode(this, !ThemeUtil.isDarkMode(this));
            recreate();
        });
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
        etDisplayName.setText(profile.getDisplayName());
        if (profile.getAge() > 0) {
            etAge.setText(String.valueOf(profile.getAge()));
        }
        if (profile.getHeightCm() > 0) {
            etHeight.setText(String.valueOf(profile.getHeightCm()));
        }
        if (profile.getWeightKg() > 0) {
            etWeight.setText(String.valueOf(profile.getWeightKg()));
        }
        if (profile.getWeeklyGoal() > 0) {
            etWeeklyGoal.setText(String.valueOf(profile.getWeeklyGoal()));
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
                    runOnUiThread(() -> Toast.makeText(UserProfileActivity.this, R.string.profile_saved, Toast.LENGTH_SHORT).show());
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
                    runOnUiThread(() -> Toast.makeText(UserProfileActivity.this, R.string.profile_saved, Toast.LENGTH_SHORT).show());
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

    private void logout() {
        sessionManager.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}
