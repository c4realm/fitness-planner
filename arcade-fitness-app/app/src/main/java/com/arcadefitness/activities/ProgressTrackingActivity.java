package com.arcadefitness.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arcadefitness.R;
import com.arcadefitness.adapter.ProgressAdapter;
import com.arcadefitness.data.local.entity.GoalEntity;
import com.arcadefitness.data.local.entity.WorkoutSessionEntity;
import com.arcadefitness.data.repository.FitnessRepository;
import com.arcadefitness.viewmodel.ProgressViewModel;

import java.util.List;

public class ProgressTrackingActivity extends AppCompatActivity {

    private ProgressViewModel viewModel;
    private ProgressAdapter progressAdapter;
    private View layoutNoGoals;
    private RecyclerView rvGoals;
    private TextView tvWeekWorkouts, tvWeekDuration, tvWeekCalories, tvTotalSessions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_tracking);

        initViews();
        setupRecyclerView();
        setupViewModel();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddGoal).setOnClickListener(v -> showAddGoalDialog());
        findViewById(R.id.btnViewHistory).setOnClickListener(v -> {
            startActivity(new Intent(this, WorkoutHistoryActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void initViews() {
        tvWeekWorkouts = findViewById(R.id.tvWeekWorkouts);
        tvWeekDuration = findViewById(R.id.tvWeekDuration);
        tvWeekCalories = findViewById(R.id.tvWeekCalories);
        tvTotalSessions = findViewById(R.id.tvTotalSessions);
        layoutNoGoals = findViewById(R.id.layoutNoGoals);
    }

    private void setupRecyclerView() {
        rvGoals = findViewById(R.id.rvGoals);
        rvGoals.setLayoutManager(new LinearLayoutManager(this));
        rvGoals.setHasFixedSize(true);
        progressAdapter = new ProgressAdapter();
        rvGoals.setAdapter(progressAdapter);

        ItemTouchHelper.SimpleCallback swipeCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                    @Override
                    public boolean onMove(@androidx.annotation.NonNull RecyclerView recyclerView,
                                          @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder,
                                          @androidx.annotation.NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int pos = viewHolder.getAdapterPosition();
                        GoalEntity goal = progressAdapter.getGoalAt(pos);
                        FitnessRepository.getInstance(ProgressTrackingActivity.this)
                                .deleteGoal(goal, new FitnessRepository.RepositoryCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void r) {
                                        viewModel.refresh();
                                        Toast.makeText(ProgressTrackingActivity.this,
                                                "Goal deleted", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onError(String e) {
                                        progressAdapter.notifyItemChanged(pos);
                                    }
                                });
                    }
                };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvGoals);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);

        viewModel.getWeeklyStats().observe(this, new Observer<int[]>() {
            @Override
            public void onChanged(int[] stats) {
                if (stats != null && stats.length >= 3) {
                    tvWeekWorkouts.setText(String.valueOf(stats[0]));
                    tvWeekDuration.setText(stats[1] + " min");
                    tvWeekCalories.setText(stats[2] + " kcal");
                }
            }
        });

        viewModel.getGoals().observe(this, new Observer<List<GoalEntity>>() {
            @Override
            public void onChanged(List<GoalEntity> goals) {
                progressAdapter.setGoals(goals);
                boolean empty = goals == null || goals.isEmpty();
                layoutNoGoals.setVisibility(empty ? View.VISIBLE : View.GONE);
                rvGoals.setVisibility(empty ? View.GONE : View.VISIBLE);
            }
        });

        viewModel.getCompletedSessions().observe(this, new Observer<List<WorkoutSessionEntity>>() {
            @Override
            public void onChanged(List<WorkoutSessionEntity> sessions) {
                int count = sessions == null ? 0 : sessions.size();
                tvTotalSessions.setText(count + " sessions completed");
            }
        });
    }

    private void showAddGoalDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        EditText etTitle = new EditText(this);
        etTitle.setHint("Goal title");
        etTitle.setTextColor(getColor(R.color.text_primary));
        etTitle.setHintTextColor(getColor(R.color.text_muted));
        etTitle.setBackgroundColor(getColor(R.color.bg_input));
        etTitle.setPadding(padding, padding, padding, padding);
        container.addView(etTitle);

        String[] goalTypes = {"WEIGHT_LOSS", "MUSCLE_GAIN", "ENDURANCE", "CUSTOM"};
        Spinner spinnerType = new Spinner(this);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                goalTypes
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        LinearLayout.LayoutParams typeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        typeParams.topMargin = padding;
        spinnerType.setLayoutParams(typeParams);
        container.addView(spinnerType);

        EditText etTarget = new EditText(this);
        etTarget.setHint("Target value");
        etTarget.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etTarget.setTextColor(getColor(R.color.text_primary));
        etTarget.setHintTextColor(getColor(R.color.text_muted));
        etTarget.setBackgroundColor(getColor(R.color.bg_input));
        etTarget.setPadding(padding, padding, padding, padding);
        LinearLayout.LayoutParams targetParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        targetParams.topMargin = padding;
        etTarget.setLayoutParams(targetParams);
        container.addView(etTarget);

        EditText etUnit = new EditText(this);
        etUnit.setHint("Unit (e.g. kg, sessions, km)");
        etUnit.setTextColor(getColor(R.color.text_primary));
        etUnit.setHintTextColor(getColor(R.color.text_muted));
        etUnit.setBackgroundColor(getColor(R.color.bg_input));
        etUnit.setPadding(padding, padding, padding, padding);
        LinearLayout.LayoutParams unitParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        unitParams.topMargin = padding;
        etUnit.setLayoutParams(unitParams);
        container.addView(etUnit);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Goal")
                .setView(container)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String targetText = etTarget.getText().toString().trim();

            if (title.isEmpty()) {
                etTitle.setError("Goal title is required");
                etTitle.requestFocus();
                return;
            }

            double targetValue;
            try {
                targetValue = Double.parseDouble(targetText);
            } catch (NumberFormatException e) {
                etTarget.setError("Enter a valid number");
                etTarget.requestFocus();
                return;
            }

            String selectedType = (String) spinnerType.getSelectedItem();
            String unitText = etUnit.getText().toString().trim();
            if (unitText.isEmpty()) unitText = "sessions";
            GoalEntity goal = new GoalEntity(title, selectedType, targetValue, unitText, 0L);
            FitnessRepository.getInstance(this).insertGoal(goal, new FitnessRepository.RepositoryCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    dialog.dismiss();
                    viewModel.refresh();
                    Toast.makeText(ProgressTrackingActivity.this, "Goal saved", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(ProgressTrackingActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }));

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh();
        }
    }
}
