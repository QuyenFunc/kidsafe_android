package qn.app.kidsafe_android;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimeRuleAdapter extends RecyclerView.Adapter<TimeRuleAdapter.TimeRuleViewHolder> {
    
    private List<TimeRule> timeRuleList;
    private OnTimeRuleClickListener deleteListener;
    private OnTimeRuleClickListener editListener;
    
    public interface OnTimeRuleClickListener {
        void onTimeRuleClick(TimeRule timeRule);
    }
    
    public TimeRuleAdapter(List<TimeRule> timeRuleList, OnTimeRuleClickListener deleteListener, OnTimeRuleClickListener editListener) {
        this.timeRuleList = timeRuleList;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }
    
    @NonNull
    @Override
    public TimeRuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_rule, parent, false);
        return new TimeRuleViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TimeRuleViewHolder holder, int position) {
        TimeRule timeRule = timeRuleList.get(position);
        holder.bind(timeRule);
    }
    
    @Override
    public int getItemCount() {
        return timeRuleList.size();
    }
    
    class TimeRuleViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView textRuleName;
        private TextView textRuleType;
        private TextView textRuleDescription;
        private TextView textRuleSummary;
        private TextView textRuleDays;
        private TextView textRuleCreated;
        private Switch switchActive;
        private View buttonEdit;
        private View buttonDelete;
        private View statusIndicator;
        
        public TimeRuleViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = itemView.findViewById(R.id.timeRuleCardView);
            textRuleName = itemView.findViewById(R.id.textRuleName);
            textRuleType = itemView.findViewById(R.id.textRuleType);
            textRuleDescription = itemView.findViewById(R.id.textRuleDescription);
            textRuleSummary = itemView.findViewById(R.id.textRuleSummary);
            textRuleDays = itemView.findViewById(R.id.textRuleDays);
            textRuleCreated = itemView.findViewById(R.id.textRuleCreated);
            switchActive = itemView.findViewById(R.id.switchActive);
            buttonEdit = itemView.findViewById(R.id.buttonEditRule);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteRule);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }
        
        public void bind(TimeRule timeRule) {
            // Set basic info
            textRuleName.setText(timeRule.getName());
            textRuleType.setText(timeRule.getRuleTypeDisplayName());
            
            // Set description or default text
            if (timeRule.getDescription() != null && !timeRule.getDescription().isEmpty()) {
                textRuleDescription.setText(timeRule.getDescription());
                textRuleDescription.setVisibility(View.VISIBLE);
            } else {
                textRuleDescription.setVisibility(View.GONE);
            }
            
            // Set rule summary based on type
            textRuleSummary.setText(timeRule.getDisplaySummary());
            
            // Set days display for schedule rules
            if ("access_schedule".equals(timeRule.getRuleType()) || "weekly_schedule".equals(timeRule.getRuleType())) {
                textRuleDays.setText("📅 " + timeRule.getDaysDisplayText());
                textRuleDays.setVisibility(View.VISIBLE);
            } else {
                textRuleDays.setVisibility(View.GONE);
            }
            
            // Set created time
            if (timeRule.getCreatedAt() > 0) {
                Date createdDate = new Date(timeRule.getCreatedAt());
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                textRuleCreated.setText("Tạo: " + sdf.format(createdDate));
            } else {
                textRuleCreated.setText("");
            }
            
            // Set active state
            switchActive.setChecked(timeRule.isActive());
            switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // TODO: Update rule active state in Firebase
                timeRule.setActive(isChecked);
            });
            
            // Set card background and status indicator based on rule type and active state
            updateCardAppearance(timeRule);
            
            // Set click listeners
            buttonEdit.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onTimeRuleClick(timeRule);
                }
            });
            
            buttonDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onTimeRuleClick(timeRule);
                }
            });
            
            // Card click for edit
            cardView.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onTimeRuleClick(timeRule);
                }
            });
        }
        
        private void updateCardAppearance(TimeRule timeRule) {
            // Set status indicator color and card appearance based on rule type and active state
            int indicatorColor;
            int cardElevation;
            
            if (!timeRule.isActive()) {
                indicatorColor = Color.GRAY;
                cardElevation = 2;
                cardView.setAlpha(0.7f);
            } else {
                cardView.setAlpha(1.0f);
                cardElevation = 4;
                
                switch (timeRule.getRuleType()) {
                    case "daily_limit":
                        indicatorColor = Color.parseColor("#FF5722"); // Deep Orange
                        break;
                    case "access_schedule":
                        indicatorColor = Color.parseColor("#2196F3"); // Blue
                        break;
                    case "break_rule":
                        indicatorColor = Color.parseColor("#4CAF50"); // Green
                        break;
                    case "weekly_schedule":
                        indicatorColor = Color.parseColor("#9C27B0"); // Purple
                        break;
                    default:
                        indicatorColor = Color.parseColor("#607D8B"); // Blue Grey
                        break;
                }
            }
            
            statusIndicator.setBackgroundColor(indicatorColor);
            cardView.setCardElevation(cardElevation);
        }
    }
}
