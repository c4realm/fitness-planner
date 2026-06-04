package com.arcadefitness.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arcadefitness.R;
import com.arcadefitness.adapter.ExerciseAdapter;
import com.arcadefitness.data.local.entity.ExerciseEntity;
import com.arcadefitness.data.local.repository.ExerciseRepository;

import java.util.List;

public class ExerciseLibraryActivity extends AppCompatActivity {

    private static final String FILTER_ALL = "All";

    private ExerciseRepository exerciseRepository;
    private ExerciseAdapter adapter;
    private View layoutEmpty;
    private RecyclerView rvExercises;

    private LiveData<List<ExerciseEntity>> currentSource;
    private Observer<List<ExerciseEntity>> currentObserver;

    private Button btnFilterAll;
    private Button btnFilterChest;
    private Button btnFilterBack;
    private Button btnFilterShoulders;
    private Button btnFilterLegs;
    private Button btnFilterArms;
    private Button btnFilterCore;
    private Button btnFilterFullBody;

    private String activeFilter = FILTER_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_library);

        exerciseRepository = new ExerciseRepository(this);

        initViews();
        setupRecyclerView();
        setupButtons();
        observeExercises(exerciseRepository.getAllExercises());
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        rvExercises = findViewById(R.id.rvExercises);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterChest = findViewById(R.id.btnFilterChest);
        btnFilterBack = findViewById(R.id.btnFilterBack);
        btnFilterShoulders = findViewById(R.id.btnFilterShoulders);
        btnFilterLegs = findViewById(R.id.btnFilterLegs);
        btnFilterArms = findViewById(R.id.btnFilterArms);
        btnFilterCore = findViewById(R.id.btnFilterCore);
        btnFilterFullBody = findViewById(R.id.btnFilterFullBody);
    }

    private void setupRecyclerView() {
        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        rvExercises.setHasFixedSize(true);
        adapter = new ExerciseAdapter();
        rvExercises.setAdapter(adapter);
    }

    private void setupButtons() {
        btnFilterAll.setOnClickListener(v -> showAllExercises());
        btnFilterChest.setOnClickListener(v -> showExercisesByGroup("Chest"));
        btnFilterBack.setOnClickListener(v -> showExercisesByGroup("Back"));
        btnFilterShoulders.setOnClickListener(v -> showExercisesByGroup("Shoulders"));
        btnFilterLegs.setOnClickListener(v -> showExercisesByGroup("Legs"));
        btnFilterArms.setOnClickListener(v -> showExercisesByGroup("Arms"));
        btnFilterCore.setOnClickListener(v -> showExercisesByGroup("Core"));
        btnFilterFullBody.setOnClickListener(v -> showExercisesByGroup("Full Body"));

        updateFilterStyles();
    }

    private void showAllExercises() {
        activeFilter = FILTER_ALL;
        updateFilterStyles();
        observeExercises(exerciseRepository.getAllExercises());
    }

    private void showExercisesByGroup(String group) {
        activeFilter = group;
        updateFilterStyles();
        observeExercises(exerciseRepository.getExercisesByMuscleGroup(group));
    }

    private void observeExercises(LiveData<List<ExerciseEntity>> source) {
        if (currentSource != null && currentObserver != null) {
            currentSource.removeObserver(currentObserver);
        }

        currentSource = source;
        currentObserver = exercises -> {
            adapter.setExercises(exercises);
            boolean empty = exercises == null || exercises.isEmpty();
            layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvExercises.setVisibility(empty ? View.GONE : View.VISIBLE);
        };

        currentSource.observe(this, currentObserver);
    }

    private void updateFilterStyles() {
        styleFilterButton(btnFilterAll, FILTER_ALL.equals(activeFilter));
        styleFilterButton(btnFilterChest, "Chest".equals(activeFilter));
        styleFilterButton(btnFilterBack, "Back".equals(activeFilter));
        styleFilterButton(btnFilterShoulders, "Shoulders".equals(activeFilter));
        styleFilterButton(btnFilterLegs, "Legs".equals(activeFilter));
        styleFilterButton(btnFilterArms, "Arms".equals(activeFilter));
        styleFilterButton(btnFilterCore, "Core".equals(activeFilter));
        styleFilterButton(btnFilterFullBody, "Full Body".equals(activeFilter));
    }

    private void styleFilterButton(Button button, boolean active) {
        button.setBackgroundColor(getColor(active ? R.color.orange_primary : R.color.bg_card));
        button.setTextColor(getColor(active ? R.color.text_primary : R.color.text_muted));
    }

    @Override
    protected void onDestroy() {
        if (currentSource != null && currentObserver != null) {
            currentSource.removeObserver(currentObserver);
        }
        super.onDestroy();
    }
}
