package qn.app.kidsafe_android;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeManagementActivity extends AppCompatActivity {
    
    private static final String TAG = "TimeManagementActivity";
    
    private RecyclerView recyclerView;
    private TimeRuleAdapter timeRuleAdapter;
    private List<TimeRule> timeRuleList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fab;
    
    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference timeRulesRef;
    private ValueEventListener rulesListener;
    
    // Local Auth Service (fallback)
    private LocalAuthService localAuth;
    private boolean useLocalAuth = true;
    
    // Current user
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_management);
        
        // Initialize Auth Service
        localAuth = new LocalAuthService(this);
        
        String userUID = null;
        String userEmail = null;
        
        if (useLocalAuth) {
            // Use Local Auth Service
            if (!localAuth.isLoggedIn()) {
                goToLoginActivity();
                return;
            }
            userUID = localAuth.getCurrentUserUID();
            userEmail = localAuth.getCurrentUserEmail();
            Log.d(TAG, "Using Local Auth - UID: " + userUID + ", Email: " + userEmail);
        } else {
            // Use Firebase Auth (original)
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();
            
            // Check if user is signed in
            if (currentUser == null) {
                // User is not signed in, go to login
                goToLoginActivity();
                return;
            }
            userUID = currentUser.getUid();
            userEmail = currentUser.getEmail();
        }
        
        initViews();
        initFirebase();
        setupRecyclerView();
        setupListeners();
        loadTimeRules();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.timeRulesRecyclerView);
        swipeRefreshLayout = findViewById(R.id.timeSwipeRefreshLayout);
        fab = findViewById(R.id.timeManagementFab);
        
        // Setup toolbar
        setSupportActionBar(findViewById(R.id.timeToolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("🕐 Quản Lý Thời Gian");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Show user email in subtitle
            String email = useLocalAuth ? localAuth.getCurrentUserEmail() : 
                          (currentUser != null ? currentUser.getEmail() : null);
            if (email != null) {
                getSupportActionBar().setSubtitle(email);
            }
        }
    }
    
    private void initFirebase() {
        try {
            // Initialize Firebase Database with custom URL
            String databaseUrl = "https://kidsafe-control-default-rtdb.asia-southeast1.firebasedatabase.app/";
            firebaseDatabase = FirebaseDatabase.getInstance(databaseUrl);
            
            // Use user UID as family ID for data isolation
            String userUid = useLocalAuth ? localAuth.getCurrentUserUID() : currentUser.getUid();
            Log.d(TAG, "🔥 Initializing Firebase with UID: " + userUid);
            
            // Use structured path for proper data organization and PC compatibility
            String firebasePath = "kidsafe/families/" + userUid + "/timeRules";
            Log.d(TAG, "🔥 Firebase path: " + firebasePath);
            
            timeRulesRef = firebaseDatabase.getReference("kidsafe")
                    .child("families")
                    .child(userUid)
                    .child("timeRules");
                    
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to initialize Firebase", e);
            Toast.makeText(this, "Lỗi khởi tạo Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void setupRecyclerView() {
        timeRuleList = new ArrayList<>();
        timeRuleAdapter = new TimeRuleAdapter(timeRuleList, this::showDeleteConfirmDialog, this::showEditRuleDialog);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(timeRuleAdapter);
    }
    
    private void setupListeners() {
        // FAB click listener
        fab.setOnClickListener(v -> showAddRuleDialog());
        
        // Swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
        
        // Set refresh colors
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary_color,
                R.color.secondary_color
        );
    }
    
    private void loadTimeRules() {
        rulesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "🔥 Firebase onDataChange - Time Rules count: " + dataSnapshot.getChildrenCount());
                
                timeRuleList.clear();
                int loadedCount = 0;
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Log.d(TAG, "🔥 Processing time rule snapshot key: " + snapshot.getKey());
                        
                        TimeRule timeRule = snapshot.getValue(TimeRule.class);
                        if (timeRule != null) {
                            timeRule.setId(snapshot.getKey());
                            timeRuleList.add(timeRule);
                            loadedCount++;
                            Log.d(TAG, "✅ Loaded time rule: " + timeRule.getName());
                        } else {
                            Log.w(TAG, "⚠️ Failed to parse time rule from snapshot: " + snapshot.getKey());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error parsing time rule from snapshot: " + snapshot.getKey(), e);
                    }
                }
                
                Log.d(TAG, "🔥 Total time rules loaded: " + loadedCount);
                timeRuleAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
                
                // Update subtitle
                updateSubtitle();
                
                if (loadedCount > 0) {
                    Toast.makeText(TimeManagementActivity.this, "Đã tải " + loadedCount + " quy tắc", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "❌ Firebase onCancelled: " + databaseError.getMessage(), databaseError.toException());
                Toast.makeText(TimeManagementActivity.this, 
                    "Lỗi tải dữ liệu: " + databaseError.getMessage(), 
                    Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        };
        
        timeRulesRef.addValueEventListener(rulesListener);
    }
    
    private void updateSubtitle() {
        if (getSupportActionBar() != null) {
            String subtitle = timeRuleList.size() + " quy tắc thời gian";
            getSupportActionBar().setSubtitle(subtitle);
        }
    }
    
    private void showAddRuleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_time_rule, null);
        
        EditText editTextName = dialogView.findViewById(R.id.editTextRuleName);
        EditText editTextDescription = dialogView.findViewById(R.id.editTextRuleDescription);
        Spinner spinnerRuleType = dialogView.findViewById(R.id.spinnerRuleType);
        
        // Configure rule type options
        String[] ruleTypes = {"Giới hạn hàng ngày", "Lịch trình truy cập", "Nghỉ ngơi bắt buộc"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ruleTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRuleType.setAdapter(adapter);
        
        // Configure specific fields based on rule type
        LinearLayout scheduleFields = dialogView.findViewById(R.id.scheduleFields);
        LinearLayout limitFields = dialogView.findViewById(R.id.limitFields);
        LinearLayout breakFields = dialogView.findViewById(R.id.breakFields);
        
        spinnerRuleType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Hide all specific fields first
                scheduleFields.setVisibility(View.GONE);
                limitFields.setVisibility(View.GONE);
                breakFields.setVisibility(View.GONE);
                
                // Show relevant fields based on selection
                switch (position) {
                    case 0: // Daily limit
                        limitFields.setVisibility(View.VISIBLE);
                        break;
                    case 1: // Access schedule
                        scheduleFields.setVisibility(View.VISIBLE);
                        break;
                    case 2: // Break rule
                        breakFields.setVisibility(View.VISIBLE);
                        break;
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        builder.setView(dialogView)
                .setTitle("Thêm quy tắc thời gian")
                .setPositiveButton("Thêm", null) // Set null first to override later
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        
        // Override positive button to prevent auto-dismiss
        dialog.setOnShowListener(dialogInterface -> {
            // Setup time picker buttons
            Button buttonSelectStartTime = dialogView.findViewById(R.id.buttonSelectStartTime);
            Button buttonSelectEndTime = dialogView.findViewById(R.id.buttonSelectEndTime);
            TextView textStartTime = dialogView.findViewById(R.id.textStartTime);
            TextView textEndTime = dialogView.findViewById(R.id.textEndTime);
            
            setupTimePickerButton(buttonSelectStartTime, textStartTime);
            setupTimePickerButton(buttonSelectEndTime, textEndTime);
            
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (validateAndAddRule(dialogView)) {
                    dialog.dismiss();
                }
            });
        });
        
        dialog.show();
    }
    
    private boolean validateAndAddRule(View dialogView) {
        EditText editTextName = dialogView.findViewById(R.id.editTextRuleName);
        EditText editTextDescription = dialogView.findViewById(R.id.editTextRuleDescription);
        Spinner spinnerRuleType = dialogView.findViewById(R.id.spinnerRuleType);
        
        String name = editTextName.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        
        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên quy tắc", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        TimeRule timeRule;
        int ruleTypePosition = spinnerRuleType.getSelectedItemPosition();
        
        try {
            switch (ruleTypePosition) {
                case 0: // Daily limit
                    EditText editTextLimit = dialogView.findViewById(R.id.editTextDailyLimit);
                    int dailyLimit = Integer.parseInt(editTextLimit.getText().toString().trim());
                    timeRule = TimeRule.createDailyLimit(name, dailyLimit);
                    break;
                    
                case 1: // Access schedule
                    TextView textStartTime = dialogView.findViewById(R.id.textStartTime);
                    TextView textEndTime = dialogView.findViewById(R.id.textEndTime);
                    
                    String startTime = textStartTime.getText().toString();
                    String endTime = textEndTime.getText().toString();
                    
                    if (startTime.equals("Chọn giờ bắt đầu") || endTime.equals("Chọn giờ kết thúc")) {
                        Toast.makeText(this, "Vui lòng chọn thời gian bắt đầu và kết thúc", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    
                    // Get selected days
                    List<Integer> selectedDays = getSelectedDays(dialogView);
                    timeRule = TimeRule.createAccessSchedule(name, startTime, endTime, selectedDays);
                    break;
                    
                case 2: // Break rule
                    EditText editTextInterval = dialogView.findViewById(R.id.editTextBreakInterval);
                    EditText editTextDuration = dialogView.findViewById(R.id.editTextBreakDuration);
                    
                    int interval = Integer.parseInt(editTextInterval.getText().toString().trim());
                    int duration = Integer.parseInt(editTextDuration.getText().toString().trim());
                    
                    timeRule = TimeRule.createBreakRule(name, interval, duration);
                    break;
                    
                default:
                    Toast.makeText(this, "Vui lòng chọn loại quy tắc", Toast.LENGTH_SHORT).show();
                    return false;
            }
            
            if (!description.isEmpty()) {
                timeRule.setDescription(description);
            }
            
            addTimeRuleToFirebase(timeRule);
            return true;
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tạo quy tắc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    
    private List<Integer> getSelectedDays(View dialogView) {
        List<Integer> selectedDays = new ArrayList<>();
        
        CheckBox[] dayCheckboxes = {
                dialogView.findViewById(R.id.checkboxSunday),
                dialogView.findViewById(R.id.checkboxMonday),
                dialogView.findViewById(R.id.checkboxTuesday),
                dialogView.findViewById(R.id.checkboxWednesday),
                dialogView.findViewById(R.id.checkboxThursday),
                dialogView.findViewById(R.id.checkboxFriday),
                dialogView.findViewById(R.id.checkboxSaturday)
        };
        
        for (int i = 0; i < dayCheckboxes.length; i++) {
            if (dayCheckboxes[i] != null && dayCheckboxes[i].isChecked()) {
                selectedDays.add(i);
            }
        }
        
        return selectedDays;
    }
    
    private void addTimeRuleToFirebase(TimeRule timeRule) {
        try {
            Log.d(TAG, "Adding time rule to Firebase: " + timeRule.getName());
            
            Map<String, Object> ruleData = new HashMap<>();
            ruleData.put("name", timeRule.getName());
            ruleData.put("description", timeRule.getDescription());
            ruleData.put("ruleType", timeRule.getRuleType());
            ruleData.put("startTime", timeRule.getStartTime());
            ruleData.put("endTime", timeRule.getEndTime());
            ruleData.put("days", timeRule.getDays());
            ruleData.put("dailyLimitMinutes", timeRule.getDailyLimitMinutes());
            ruleData.put("breakIntervalMinutes", timeRule.getBreakIntervalMinutes());
            ruleData.put("breakDurationMinutes", timeRule.getBreakDurationMinutes());
            ruleData.put("active", timeRule.isActive());
            ruleData.put("createdAt", ServerValue.TIMESTAMP);
            ruleData.put("updatedAt", ServerValue.TIMESTAMP);
            ruleData.put("addedBy", "parent_android");
            
            DatabaseReference newRuleRef = timeRulesRef.push();
            String pushId = newRuleRef.getKey();
            Log.d(TAG, "Generated push ID: " + pushId);
            
            newRuleRef.setValue(ruleData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Successfully added time rule to Firebase: " + timeRule.getName());
                        Toast.makeText(TimeManagementActivity.this, 
                            "Đã thêm quy tắc: " + timeRule.getName(), 
                            Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Failed to add time rule to Firebase: " + timeRule.getName(), e);
                        Toast.makeText(TimeManagementActivity.this, 
                            "Lỗi thêm quy tắc: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    });
                    
        } catch (Exception e) {
            Log.e(TAG, "Exception in addTimeRuleToFirebase: " + timeRule.getName(), e);
            Toast.makeText(this, "Lỗi không mong đợi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void showEditRuleDialog(TimeRule timeRule) {
        // Implementation for editing time rules
        Toast.makeText(this, "Chỉnh sửa quy tắc: " + timeRule.getName(), Toast.LENGTH_SHORT).show();
    }
    
    private void showDeleteConfirmDialog(TimeRule timeRule) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa quy tắc này?\n\n" + 
                           timeRule.getName() + "\n" + timeRule.getDisplaySummary())
                .setPositiveButton("Xóa", (dialog, which) -> deleteTimeRule(timeRule))
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private void deleteTimeRule(TimeRule timeRule) {
        timeRulesRef.child(timeRule.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TimeManagementActivity.this, 
                        "Đã xóa quy tắc: " + timeRule.getName(), 
                        Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TimeManagementActivity.this, 
                        "Lỗi xóa quy tắc: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }
    
    private void refreshData() {
        swipeRefreshLayout.setRefreshing(true);
    }
    
    private void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.time_management_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.menu_time_usage_stats) {
            // Show usage statistics
            showUsageStats();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void showUsageStats() {
        Toast.makeText(this, "Thống kê sử dụng sẽ được hiển thị ở đây", Toast.LENGTH_SHORT).show();
    }
    
    public void setupTimePickerButton(Button button, final TextView targetTextView) {
        button.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    TimeManagementActivity.this,
                    (TimePicker view, int selectedHour, int selectedMinute) -> {
                        String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                        targetTextView.setText(time);
                    },
                    hour, minute, true);
            
            timePickerDialog.show();
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove Firebase listener to prevent memory leaks
        if (rulesListener != null && timeRulesRef != null) {
            timeRulesRef.removeEventListener(rulesListener);
        }
    }
}
