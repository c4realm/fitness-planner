package com.arcadefitness.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arcadefitness.R;
import com.arcadefitness.data.local.entity.GoalEntity;

import java.util.ArrayList;
import java.util.List;

public class ProgressAdapter extends RecyclerView.Adapter<ProgressAdapter.GoalViewHolder> {

    private final List<GoalEntity> goals = new ArrayList<>();

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal_progress, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        GoalEntity goal = goals.get(position);
        holder.bind(goal);
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    public void setGoals(List<GoalEntity> goals) {
        this.goals.clear();
        if (goals != null) {
            this.goals.addAll(goals);
        }
        notifyDataSetChanged();
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvGoalTitle;
        private final TextView tvGoalStatus;
        private final TextView tvGoalDescription;
        private final TextView tvGoalProgress;
        private final TextView tvGoalTarget;
        private final View viewProgressFill;

        GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGoalTitle = itemView.findViewById(R.id.tvGoalTitle);
            tvGoalStatus = itemView.findViewById(R.id.tvGoalStatus);
            tvGoalDescription = itemView.findViewById(R.id.tvGoalDescription);
            tvGoalProgress = itemView.findViewById(R.id.tvGoalProgress);
            tvGoalTarget = itemView.findViewById(R.id.tvGoalTarget);
            viewProgressFill = itemView.findViewById(R.id.viewProgressFill);
        }

        void bind(GoalEntity goal) {
            tvGoalTitle.setText(goal.getTitle());
            tvGoalStatus.setText("COMPLETED".equals(goal.getStatus()) ? "Done" : "Active");
            tvGoalStatus.setTextColor("COMPLETED".equals(goal.getStatus())
                    ? itemView.getContext().getColor(R.color.text_muted)
                    : itemView.getContext().getColor(R.color.orange_primary));

            if (goal.getDescription() != null && !goal.getDescription().isEmpty()) {
                tvGoalDescription.setVisibility(View.VISIBLE);
                tvGoalDescription.setText(goal.getDescription());
            } else {
                tvGoalDescription.setVisibility(View.GONE);
            }

            String unit = goal.getUnit() != null ? " " + goal.getUnit() : "";
            tvGoalProgress.setText(String.format("%.0f%s", goal.getCurrentValue(), unit));
            tvGoalTarget.setText(String.format("%.0f%s", goal.getTargetValue(), unit));

            double progress = goal.getTargetValue() > 0
                    ? Math.min(1.0, goal.getCurrentValue() / goal.getTargetValue())
                    : 0.0;

            int totalWidth = itemView.getMeasuredWidth() - itemView.getPaddingLeft() - itemView.getPaddingRight();
            if (totalWidth > 0) {
                ViewGroup.LayoutParams params = viewProgressFill.getLayoutParams();
                params.width = (int) (totalWidth * progress);
                viewProgressFill.setLayoutParams(params);
            }
        }
    }
}
