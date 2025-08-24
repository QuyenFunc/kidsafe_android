package qn.app.kidsafe_android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "MainActivity";
    
    private RecyclerView recyclerView;
    private UrlAdapter urlAdapter;
    private List<BlockedUrl> urlList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ExtendedFloatingActionButton fab;
    
    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference blockedUrlsRef;
    private ValueEventListener urlsListener;
    
    // Local Auth Service (fallback)
    private LocalAuthService localAuth;
    private boolean useLocalAuth = true; // Temporary fallback until Firebase Auth is enabled
    
    // Current user
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize Firebase Auth
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
        loadBlockedUrls();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        fab = findViewById(R.id.fab);
        
        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("KidSafe - Phụ Huynh");
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
            Log.d(TAG, "🔥 Firebase database URL: " + databaseUrl);
            
            String firebasePath = "kidsafe/families/" + userUid + "/blockedUrls";
            Log.d(TAG, "🔥 Firebase path: " + firebasePath);
            
            blockedUrlsRef = firebaseDatabase.getReference("kidsafe")
                    .child("families")
                    .child(userUid)
                    .child("blockedUrls");
                    
            // Test Firebase connection
            blockedUrlsRef.getDatabase().getReference(".info/connected")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean connected = snapshot.getValue(Boolean.class);
                            Log.d(TAG, "🔥 Firebase connected: " + connected);
                            if (connected) {
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, "✅ Kết nối Firebase thành công", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "🔥 Firebase connection error: " + error.getMessage());
                        }
                    });
                    
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to initialize Firebase", e);
            Toast.makeText(this, "Lỗi khởi tạo Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void setupRecyclerView() {
        urlList = new ArrayList<>();
        urlAdapter = new UrlAdapter(urlList, this::showDeleteConfirmDialog);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(urlAdapter);
    }
    
    private void setupListeners() {
        // FAB click listener
        fab.setOnClickListener(v -> showAddUrlDialog());
        
        // Swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
        
        // Set refresh colors
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary_color,
                R.color.secondary_color
        );
    }
    
    private void loadBlockedUrls() {
        urlsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "🔥 Firebase onDataChange - Snapshot exists: " + dataSnapshot.exists());
                Log.d(TAG, "🔥 Firebase onDataChange - Children count: " + dataSnapshot.getChildrenCount());
                
                urlList.clear();
                int loadedCount = 0;
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Log.d(TAG, "🔥 Processing snapshot key: " + snapshot.getKey());
                        Log.d(TAG, "🔥 Snapshot value: " + snapshot.getValue());
                        
                        BlockedUrl blockedUrl = snapshot.getValue(BlockedUrl.class);
                        if (blockedUrl != null) {
                            blockedUrl.setId(snapshot.getKey());
                            urlList.add(blockedUrl);
                            loadedCount++;
                            Log.d(TAG, "✅ Loaded URL: " + blockedUrl.getUrl());
                        } else {
                            Log.w(TAG, "⚠️ Failed to parse URL from snapshot: " + snapshot.getKey());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error parsing blocked URL from snapshot: " + snapshot.getKey(), e);
                    }
                }
                
                Log.d(TAG, "🔥 Total URLs loaded: " + loadedCount);
                urlAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
                
                // Update subtitle
                updateSubtitle();
                
                if (loadedCount > 0) {
                    Toast.makeText(MainActivity.this, "Đã tải " + loadedCount + " URL", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "❌ Firebase onCancelled: " + databaseError.getMessage(), databaseError.toException());
                Toast.makeText(MainActivity.this, 
                    "Lỗi tải dữ liệu: " + databaseError.getMessage(), 
                    Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        };
        
        blockedUrlsRef.addValueEventListener(urlsListener);
    }
    
    private void updateSubtitle() {
        if (getSupportActionBar() != null) {
            String subtitle = urlList.size() + " trang web bị chặn";
            getSupportActionBar().setSubtitle(subtitle);
        }
    }
    
    private void showAddUrlDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_url, null);
        
        EditText editTextUrl = dialogView.findViewById(R.id.editTextUrl);
        
        builder.setView(dialogView)
                .setTitle("Thêm trang web cần chặn")
                .setPositiveButton("Thêm", null) // Set null first to override later
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        
        // Override positive button to prevent auto-dismiss
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String url = editTextUrl.getText().toString().trim();
                if (validateAndAddUrl(url)) {
                    dialog.dismiss();
                }
            });
        });
        
        dialog.show();
    }
    
    private boolean validateAndAddUrl(String inputUrl) {
        if (TextUtils.isEmpty(inputUrl)) {
            Toast.makeText(this, "Vui lòng nhập URL", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Clean and validate URL
        String cleanUrl = cleanUrl(inputUrl);
        if (!isValidUrl(cleanUrl)) {
            Toast.makeText(this, "URL không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Check if URL already exists
        for (BlockedUrl existing : urlList) {
            if (existing.getUrl().equals(cleanUrl)) {
                Toast.makeText(this, "URL này đã có trong danh sách", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        
        addUrlToFirebase(cleanUrl);
        return true;
    }
    
    private String cleanUrl(String inputUrl) {
        String url = inputUrl.toLowerCase().trim();
        
        // Remove protocols if present
        if (url.startsWith("http://")) {
            url = url.substring(7);
        } else if (url.startsWith("https://")) {
            url = url.substring(8);
        }
        
        // Remove www if present
        if (url.startsWith("www.")) {
            url = url.substring(4);
        }
        
        // Add https protocol for validation
        return "https://" + url;
    }
    
    private boolean isValidUrl(String url) {
        try {
            new URL(url);
            return Patterns.WEB_URL.matcher(url).matches();
        } catch (MalformedURLException e) {
            return false;
        }
    }
    
    private void addUrlToFirebase(String url) {
        try {
            Log.d(TAG, "Adding URL to Firebase: " + url);
            
            Map<String, Object> urlData = new HashMap<>();
            urlData.put("url", url);
            urlData.put("addedAt", ServerValue.TIMESTAMP);
            urlData.put("addedBy", "parent_android");
            urlData.put("status", "active");
            
            // Get user UID for proper path
            String userUid = useLocalAuth ? localAuth.getCurrentUserUID() : 
                           (currentUser != null ? currentUser.getUid() : "anonymous");
            
            Log.d(TAG, "Using UID for Firebase path: " + userUid);
            
            DatabaseReference newUrlRef = blockedUrlsRef.push();
            String pushId = newUrlRef.getKey();
            Log.d(TAG, "Generated push ID: " + pushId);
            
            newUrlRef.setValue(urlData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Successfully added URL to Firebase: " + url);
                        Snackbar.make(recyclerView, "Đã thêm URL: " + formatUrlForDisplay(url), 
                                Snackbar.LENGTH_LONG).show();
                        updateSyncStatus();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Failed to add URL to Firebase: " + url, e);
                        Toast.makeText(MainActivity.this, 
                            "Lỗi thêm URL: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    });
                    
        } catch (Exception e) {
            Log.e(TAG, "Exception in addUrlToFirebase: " + url, e);
            Toast.makeText(this, "Lỗi không mong đợi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void showDeleteConfirmDialog(BlockedUrl blockedUrl) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa URL này khỏi danh sách chặn?\n\n" + 
                           formatUrlForDisplay(blockedUrl.getUrl()))
                .setPositiveButton("Xóa", (dialog, which) -> deleteUrl(blockedUrl))
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private void deleteUrl(BlockedUrl blockedUrl) {
        blockedUrlsRef.child(blockedUrl.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Snackbar.make(recyclerView, "Đã xóa URL khỏi danh sách chặn", 
                            Snackbar.LENGTH_SHORT).show();
                    updateSyncStatus();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, 
                        "Lỗi xóa URL: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }
    
    private void updateSyncStatus() {
        // Determine UID and email based on auth mode
        String userUid = useLocalAuth ? localAuth.getCurrentUserUID() : (currentUser != null ? currentUser.getUid() : null);
        String userEmail = useLocalAuth ? localAuth.getCurrentUserEmail() : (currentUser != null ? currentUser.getEmail() : null);

        if (userUid == null || firebaseDatabase == null) {
            Log.w(TAG, "updateSyncStatus skipped: missing userUid or firebaseDatabase");
            return;
        }

        DatabaseReference syncStatusRef = firebaseDatabase.getReference("kidsafe")
                .child("families")
                .child(userUid)
                .child("syncStatus");

        Map<String, Object> syncData = new HashMap<>();
        syncData.put("lastUpdated", ServerValue.TIMESTAMP);
        syncData.put("updatedBy", "parent_android");
        syncData.put("status", "online");
        if (userEmail != null) {
            syncData.put("userEmail", userEmail);
        }

        syncStatusRef.setValue(syncData);
    }
    
    private void refreshData() {
        // Firebase automatically refreshes due to ValueEventListener
        // Just show refreshing animation for user feedback
        swipeRefreshLayout.setRefreshing(true);
    }
    
    private String formatUrlForDisplay(String url) {
        if (url == null) return "";
        
        // Remove protocol for cleaner display
        String displayUrl = url;
        if (displayUrl.startsWith("https://")) {
            displayUrl = displayUrl.substring(8);
        } else if (displayUrl.startsWith("http://")) {
            displayUrl = displayUrl.substring(7);
        }
        
        return displayUrl;
    }
    
    private void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.menu_logout) {
            showLogoutDialog();
            return true;
        } else if (id == R.id.menu_time_management) {
            // Open Time Management Activity
            Intent intent = new Intent(this, TimeManagementActivity.class);
            startActivity(intent);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
            .setPositiveButton("Đăng xuất", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    performLogout();
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    private void performLogout() {
        try {
            // Remove Firebase listener
            if (urlsListener != null && blockedUrlsRef != null) {
                blockedUrlsRef.removeEventListener(urlsListener);
            }
            
            // Clear local auth if using local auth
            if (useLocalAuth && localAuth != null) {
                localAuth.clearSavedCredentials();
                Log.d(TAG, "✅ Local credentials cleared");
            }
            
            // Sign out from Firebase if using Firebase auth
            if (mAuth != null && currentUser != null) {
                mAuth.signOut();
                Log.d(TAG, "✅ Firebase sign out completed");
            }
            
            // Show success message
            Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
            
            // Return to login screen
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Logout error: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi đăng xuất: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove Firebase listener to prevent memory leaks
        if (urlsListener != null && blockedUrlsRef != null) {
            blockedUrlsRef.removeEventListener(urlsListener);
        }
    }
}