package com.arcadefitness.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arcadefitness.R;
import com.arcadefitness.adapter.WorkoutSessionAdapter;
import com.arcadefitness.data.local.entity.WorkoutEntity;
import com.arcadefitness.viewmodel.WorkoutPlannerViewModel;

import java.util.Arrays;
import java.util.List;

public class WorkoutPlannerActivity extends AppCompatActivity {

    private static final String[] MUSCLE_GROUPS = {
            "Chest", "Back", "Shoulders", "Legs", "Arms", "Core", "Full Body"
    };

    private WorkoutPlannerViewModel viewModel;
    private WorkoutSessionAdapter adapter;
    private View layoutEmpty;
    private RecyclerView rvWorkouts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_planner);

        initViews();
        setupRecyclerView();
        setupViewModel();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCreateWorkout).setOnClickListener(v -> {
            showCreateWorkoutDialog();
        });
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        rvWorkouts = findViewById(R.id.rvWorkoutSessions);
        rvWorkouts.setLayoutManager(new LinearLayoutManager(this));
        rvWorkouts.setHasFixedSize(true);
        adapter = new WorkoutSessionAdapter();
        adapter.setOnWorkoutClickListener(workout -> {
            android.content.Intent intent = new android.content.Intent(this, WorkoutTrackingActivity.class);
            intent.putExtra("workout_id", workout.getId());
            startActivity(intent);
        });
        rvWorkouts.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(WorkoutPlannerViewModel.class);

        viewModel.getAllWorkouts().observe(this, new Observer<List<WorkoutEntity>>() {
            @Override
            public void onChanged(List<WorkoutEntity> workouts) {
                adapter.setWorkouts(workouts);
                layoutEmpty.setVisibility(workouts == null || workouts.isEmpty() ? View.VISIBLE : View.GONE);
                rvWorkouts.setVisibility(workouts == null || workouts.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void showCreateWorkoutDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        EditText etName = new EditText(this);
        etName.setHint("Workout name");
        etName.setTextColor(getColor(R.color.text_primary));
        etName.setHintTextColor(getColor(R.color.text_muted));
        etName.setBackgroundColor(getColor(R.color.bg_input));
        etName.setPadding(padding, padding, padding, padding);
        container.addView(etName);

        Spinner spinnerMuscleGroup = new Spinner(this);
        ArrayAdapter<String> muscleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.asList(MUSCLE_GROUPS)
        );
        muscleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMuscleGroup.setAdapter(muscleAdapter);
        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        spinnerParams.topMargin = padding;
        spinnerMuscleGroup.setLayoutParams(spinnerParams);
        container.addView(spinnerMuscleGroup);

        EditText etDuration = new EditText(this);
        etDuration.setHint("Estimated duration (minutes)");
        etDuration.setInputType(InputType.TYPE_CLASS_NUMBER);
        etDuration.setTextColor(getColor(R.color.text_primary));
        etDuration.setHintTextColor(getColor(R.color.text_muted));
        etDuration.setBackgroundColor(getColor(R.color.bg_input));
        etDuration.setPadding(padding, padding, padding, padding);
        LinearLayout.LayoutParams durationParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        durationParams.topMargin = padding;
        etDuration.setLayoutParams(durationParams);
        container.addView(etDuration);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Create Workout")
                .setView(container)
                .setPositiveButton("Create", null)
                .setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError("Workout name is required");
                etName.requestFocus();
                return;
            }

            String durationText = etDuration.getText().toString().trim();
            int durationMinutes;
            try {
                durationMinutes = Integer.parseInt(durationText);
            } catch (NumberFormatException e) {
                etDuration.setError("Enter a valid number");
                etDuration.requestFocus();
                return;
            }

            String muscleGroup = (String) spinnerMuscleGroup.getSelectedItem();
            WorkoutEntity workout = new WorkoutEntity();
            workout.setName(name);
            workout.setTargetMuscleGroup(muscleGroup);
            workout.setEstimatedDurationMinutes(durationMinutes);

            viewModel.getWorkoutRepository().insert(workout, () -> {
                Toast.makeText(WorkoutPlannerActivity.this, "Workout created", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }));

        dialog.show();
    }
}
