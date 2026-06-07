package com.arcadefitness.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.arcadefitness.R;
import com.arcadefitness.data.local.entity.UserProfileEntity;
import com.arcadefitness.data.repository.FitnessRepository;
import com.arcadefitness.utils.SessionManager;

public class BmiCalculatorActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private FitnessRepository fitnessRepository;

    private EditText etHeight, etWeight;
    private Button btnMetric, btnImperial, btnCalculate;
    private LinearLayout resultCard;
    private TextView tvBmiValue, tvBmiCategory, tvBmiAdvice;
    private ProgressBar progressBar;

    private boolean isMetric = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_calculator);

        sessionManager = new SessionManager(this);
        fitnessRepository = FitnessRepository.getInstance(this);

        initViews();
        updateUnitHints();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        etHeight = findViewById(R.id.etBmiHeight);
        etWeight = findViewById(R.id.etBmiWeight);

        btnMetric = findViewById(R.id.btnMetric);
        btnImperial = findViewById(R.id.btnImperial);
        btnCalculate = findViewById(R.id.btnCalculate);

        resultCard = findViewById(R.id.resultCard);
        tvBmiValue = findViewById(R.id.tvBmiValue);
        tvBmiCategory = findViewById(R.id.tvBmiCategory);
        tvBmiAdvice = findViewById(R.id.tvBmiAdvice);
        progressBar = findViewById(R.id.progressBar);

        btnMetric.setOnClickListener(v -> {
            if (!isMetric) {
                isMetric = true;
                updateUnitHints();
                updateUnitToggleStyle();
                resultCard.setVisibility(View.GONE);
            }
        });

        btnImperial.setOnClickListener(v -> {
            if (isMetric) {
                isMetric = false;
                updateUnitHints();
                updateUnitToggleStyle();
                resultCard.setVisibility(View.GONE);
            }
        });

        btnCalculate.setOnClickListener(v -> calculateBmi());
    }

    private void updateUnitHints() {
        if (isMetric) {
            etHeight.setHint("Height (cm)");
            etWeight.setHint("Weight (kg)");
        } else {
            etHeight.setHint("Height (in)");
            etWeight.setHint("Weight (lbs)");
        }
    }

    private void updateUnitToggleStyle() {
        if (isMetric) {
            btnMetric.setBackgroundResource(R.drawable.bg_button_primary);
            btnMetric.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnImperial.setBackgroundResource(R.drawable.bg_button_secondary);
            btnImperial.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        } else {
            btnImperial.setBackgroundResource(R.drawable.bg_button_primary);
            btnImperial.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnMetric.setBackgroundResource(R.drawable.bg_button_secondary);
            btnMetric.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }
    }

    private void calculateBmi() {
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();

        boolean valid = true;

        if (TextUtils.isEmpty(heightStr)) {
            etHeight.setError("Height is required");
            valid = false;
        } else {
            etHeight.setError(null);
        }

        if (TextUtils.isEmpty(weightStr)) {
            etWeight.setError("Weight is required");
            valid = false;
        } else {
            etWeight.setError(null);
        }

        if (!valid) return;

        double heightInput, weightInput;

        try {
            heightInput = Double.parseDouble(heightStr);
            weightInput = Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            etHeight.setError("Enter a valid number");
            etWeight.setError("Enter a valid number");
            return;
        }

        if (heightInput <= 0) {
            etHeight.setError("Must be greater than 0");
            valid = false;
        }
        if (weightInput <= 0) {
            etWeight.setError("Must be greater than 0");
            valid = false;
        }
        if (!valid) return;

        double weightKg, heightM;

        if (isMetric) {
            weightKg = weightInput;
            heightM = heightInput / 100.0;
        } else {
            weightKg = weightInput * 0.453592;
            heightM = heightInput * 0.0254;
        }

        double bmi = weightKg / (heightM * heightM);

        String category;
        String advice;
        int colorRes;

        if (bmi < 18.5) {
            category = "Underweight";
            advice = "Consider increasing caloric intake";
            colorRes = R.color.bmi_underweight;
        } else if (bmi < 25) {
            category = "Normal weight";
            advice = "Great! Maintain your current lifestyle";
            colorRes = R.color.bmi_normal;
        } else if (bmi < 30) {
            category = "Overweight";
            advice = "Regular exercise and balanced diet recommended";
            colorRes = R.color.bmi_overweight;
        } else {
            category = "Obese";
            advice = "Consult a healthcare professional";
            colorRes = R.color.bmi_obese;
        }

        int color = ContextCompat.getColor(this, colorRes);

        tvBmiValue.setText(String.format("%.1f", bmi));
        tvBmiValue.setTextColor(color);
        tvBmiCategory.setText(category);
        tvBmiCategory.setTextColor(color);
        tvBmiAdvice.setText(advice);

        int progress = (int) Math.round(((bmi - 10) / 30.0) * 100);
        progress = Math.max(0, Math.min(100, progress));
        progressBar.setProgress(progress);

        resultCard.setVisibility(View.VISIBLE);

        saveBmiToProfile(weightKg, heightInput);
    }

    private void saveBmiToProfile(double weightKg, double heightCm) {
        String userEmail = sessionManager.getUserEmail();
        if (userEmail == null || userEmail.isEmpty()) return;

        fitnessRepository.getUserProfileByEmail(userEmail, new FitnessRepository.RepositoryCallback<UserProfileEntity>() {
            @Override
            public void onSuccess(UserProfileEntity profile) {
                if (profile != null) {
                    profile.setWeightKg(weightKg);
                    profile.setHeightCm(heightCm);
                    fitnessRepository.updateUserProfile(profile, new FitnessRepository.RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> Toast.makeText(BmiCalculatorActivity.this,
                                    "Profile updated", Toast.LENGTH_SHORT).show());
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
}
