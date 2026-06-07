package com.arcadefitness.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arcadefitness.R;
import com.arcadefitness.adapter.ExerciseAdapter;
import com.arcadefitness.data.local.entity.ExerciseEntity;
import com.arcadefitness.viewmodel.DashboardViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExerciseLibraryActivity extends AppCompatActivity {

    private DashboardViewModel viewModel;
    private ExerciseAdapter adapter;
    private EditText etSearch;
    private View layoutEmpty;
    private LinearLayout layoutFilters;

    private String activeFilter = null;
    private final List<ExerciseEntity> allExercises = new ArrayList<>();

    private static final List<String> MUSCLE_GROUPS = Arrays.asList(
            "All", "Chest", "Back", "Shoulders", "Legs", "Arms", "Core", "Full Body"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_library);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        initViews();
        setupRecyclerView();
        setupViewModel();
        setupSearch();
        setupFilters();
    }

    private void initViews() {
        etSearch      = findViewById(R.id.etSearch);
        layoutEmpty   = findViewById(R.id.layoutEmpty);
        layoutFilters = findViewById(R.id.layoutFilters);
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rvExercises);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);
        adapter = new ExerciseAdapter();
        rv.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        viewModel.getAllExercises().observe(this, exercises -> {
            allExercises.clear();
            if (exercises != null) allExercises.addAll(exercises);
            applyFilters();
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { applyFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        layoutFilters.removeAllViews();
        for (String group : MUSCLE_GROUPS) {
            TextView chip = new TextView(this);
            chip.setText(group);
            chip.setPadding(32, 16, 32, 16);
            chip.setTextSize(12);
            chip.setBackgroundResource(R.drawable.bg_card);
            chip.setTextColor(getColor(R.color.text_muted));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMarginEnd(8);
            chip.setLayoutParams(params);

            chip.setOnClickListener(v -> {
                activeFilter = group.equals("All") ? null : group;
                applyFilters();
                updateFilterChips();
            });

            layoutFilters.addView(chip);
        }
        updateFilterChips();
    }

    private void applyFilters() {
        String query = etSearch.getText().toString().toLowerCase().trim();
        List<ExerciseEntity> filtered = new ArrayList<>();

        for (ExerciseEntity ex : allExercises) {
            boolean matchesFilter = activeFilter == null
                    || ex.getTargetMuscleGroup().equalsIgnoreCase(activeFilter);
            boolean matchesSearch = query.isEmpty()
                    || ex.getName().toLowerCase().contains(query)
                    || ex.getTargetMuscleGroup().toLowerCase().contains(query);
            if (matchesFilter && matchesSearch) filtered.add(ex);
        }

        adapter.setExercises(filtered);
        boolean empty = filtered.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        findViewById(R.id.rvExercises).setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void updateFilterChips() {
        for (int i = 0; i < layoutFilters.getChildCount(); i++) {
            View child = layoutFilters.getChildAt(i);
            if (child instanceof TextView) {
                TextView chip = (TextView) child;
                String text = chip.getText().toString();
                boolean isActive = (activeFilter == null && text.equals("All"))
                        || text.equalsIgnoreCase(activeFilter != null ? activeFilter : "");
                chip.setTextColor(getColor(isActive
                        ? R.color.orange_primary : R.color.text_muted));
                chip.setBackgroundResource(isActive
                        ? R.drawable.bg_button_secondary : R.drawable.bg_card);
            }
        }
    }
}
