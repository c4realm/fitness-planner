package com.arcadefitness.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import java.util.Locale;
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
    private Button btnMetric;
    private Button btnImperial;
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
        View back = findViewById(R.id.btnBack);
        if (back != null) back.setOnClickListener(v -> finish());

        etHeight = findViewById(R.id.etBmiHeight);
        etWeight = findViewById(R.id.etBmiWeight);

        btnMetric = findViewById(R.id.btnMetric);
        btnImperial = findViewById(R.id.btnImperial);
        Button btnCalculate = findViewById(R.id.btnCalculate);

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
            etHeight.setHint(getString(R.string.bmi_hint_height_cm));
            etWeight.setHint(getString(R.string.bmi_hint_weight_kg));
        } else {
            etHeight.setHint(getString(R.string.bmi_hint_height_in));
            etWeight.setHint(getString(R.string.bmi_hint_weight_lbs));
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
            etHeight.setError(getString(R.string.bmi_error_height_required));
            valid = false;
        } else {
            etHeight.setError(null);
        }

        if (TextUtils.isEmpty(weightStr)) {
            etWeight.setError(getString(R.string.bmi_error_weight_required));
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
            etHeight.setError(getString(R.string.bmi_error_invalid_number));
            etWeight.setError(getString(R.string.bmi_error_invalid_number));
            return;
        }

        if (heightInput <= 0) {
            etHeight.setError(getString(R.string.bmi_error_must_be_positive));
            valid = false;
        }
        if (weightInput <= 0) {
            etWeight.setError(getString(R.string.bmi_error_must_be_positive));
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
            category = getString(R.string.bmi_underweight);
            advice = getString(R.string.bmi_underweight_advice);
            colorRes = R.color.bmi_underweight;
        } else if (bmi < 25) {
            category = getString(R.string.bmi_normal);
            advice = getString(R.string.bmi_normal_advice);
            colorRes = R.color.bmi_normal;
        } else if (bmi < 30) {
            category = getString(R.string.bmi_overweight);
            advice = getString(R.string.bmi_overweight_advice);
            colorRes = R.color.bmi_overweight;
        } else {
            category = getString(R.string.bmi_obese);
            advice = getString(R.string.bmi_obese_advice);
            colorRes = R.color.bmi_obese;
        }

        int color = ContextCompat.getColor(this, colorRes);

        tvBmiValue.setText(String.format(Locale.US, "%.1f", bmi));
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

        fitnessRepository.getUserProfileByEmail(userEmail, new FitnessRepository.RepositoryCallback<>() {
            @Override
            public void onSuccess(UserProfileEntity profile) {
                if (profile != null) {
                    profile.setWeightKg(weightKg);
                    profile.setHeightCm(heightCm);
                    fitnessRepository.updateUserProfile(profile, new FitnessRepository.RepositoryCallback<>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> Toast.makeText(BmiCalculatorActivity.this,
                                    R.string.bmi_profile_updated, Toast.LENGTH_SHORT).show());
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
