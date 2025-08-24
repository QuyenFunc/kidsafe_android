package qn.app.kidsafe_android;

import java.util.ArrayList;
import java.util.List;

public class TimeRule {
    private String id;
    private String profileId;
    private String name;
    private String description;
    private String ruleType; // "daily_limit", "access_schedule", "break_rule", "weekly_schedule"
    
    // Schedule fields
    private String startTime;   // HH:MM format
    private String endTime;     // HH:MM format
    private List<Integer> days; // weekdays (0=Sunday, 1=Monday, etc.)
    
    // Limit fields
    private int dailyLimitMinutes; // Total minutes allowed per day
    
    // Break fields
    private int breakIntervalMinutes; // How often to enforce breaks
    private int breakDurationMinutes; // How long breaks last
    
    private boolean isActive;
    private long createdAt;
    private long updatedAt;

    // Default constructor required for Firebase
    public TimeRule() {
        this.days = new ArrayList<>();
    }

    public TimeRule(String name, String description, String ruleType) {
        this();
        this.name = name;
        this.description = description;
        this.ruleType = ruleType;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Factory methods for different rule types
    public static TimeRule createDailyLimit(String name, int dailyLimitMinutes) {
        TimeRule rule = new TimeRule(name, "Giới hạn thời gian sử dụng hàng ngày", "daily_limit");
        rule.setDailyLimitMinutes(dailyLimitMinutes);
        return rule;
    }

    public static TimeRule createAccessSchedule(String name, String startTime, String endTime, List<Integer> days) {
        TimeRule rule = new TimeRule(name, "Lịch trình truy cập internet", "access_schedule");
        rule.setStartTime(startTime);
        rule.setEndTime(endTime);
        rule.setDays(days);
        return rule;
    }

    public static TimeRule createBreakRule(String name, int intervalMinutes, int durationMinutes) {
        TimeRule rule = new TimeRule(name, "Quy tắc nghỉ ngơi bắt buộc", "break_rule");
        rule.setBreakIntervalMinutes(intervalMinutes);
        rule.setBreakDurationMinutes(durationMinutes);
        return rule;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public List<Integer> getDays() {
        return days;
    }

    public void setDays(List<Integer> days) {
        this.days = days;
    }

    public int getDailyLimitMinutes() {
        return dailyLimitMinutes;
    }

    public void setDailyLimitMinutes(int dailyLimitMinutes) {
        this.dailyLimitMinutes = dailyLimitMinutes;
    }

    public int getBreakIntervalMinutes() {
        return breakIntervalMinutes;
    }

    public void setBreakIntervalMinutes(int breakIntervalMinutes) {
        this.breakIntervalMinutes = breakIntervalMinutes;
    }

    public int getBreakDurationMinutes() {
        return breakDurationMinutes;
    }

    public void setBreakDurationMinutes(int breakDurationMinutes) {
        this.breakDurationMinutes = breakDurationMinutes;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public String getRuleTypeDisplayName() {
        switch (ruleType) {
            case "daily_limit":
                return "Giới hạn hàng ngày";
            case "access_schedule":
                return "Lịch trình truy cập";
            case "break_rule":
                return "Nghỉ ngơi bắt buộc";
            case "weekly_schedule":
                return "Lịch theo tuần";
            default:
                return "Không xác định";
        }
    }

    public String getDisplaySummary() {
        switch (ruleType) {
            case "daily_limit":
                return String.format("Tối đa %d phút/ngày", dailyLimitMinutes);
            case "access_schedule":
                return String.format("Từ %s đến %s", startTime, endTime);
            case "break_rule":
                return String.format("Nghỉ %d phút mỗi %d phút", breakDurationMinutes, breakIntervalMinutes);
            default:
                return description;
        }
    }

    public String getDaysDisplayText() {
        if (days == null || days.isEmpty()) {
            return "Tất cả các ngày";
        }

        String[] dayNames = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < days.size(); i++) {
            if (i > 0) sb.append(", ");
            int day = days.get(i);
            if (day >= 0 && day < dayNames.length) {
                sb.append(dayNames[day]);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "TimeRule{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", ruleType='" + ruleType + '\'' +
                ", dailyLimitMinutes=" + dailyLimitMinutes +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
