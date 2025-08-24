package qn.app.kidsafe_android;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // UI components
    private TextInputLayout emailInputLayout, passwordInputLayout;
    private TextInputEditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private TextView toggleModeText;
    private ProgressBar progressBar;

    // Firebase Auth (fallback to local auth)
    private FirebaseAuth mAuth;
    private LocalAuthService localAuth;
    private boolean useLocalAuth = false; // Use Firebase Auth first, local as backup

    // Mode: true = Login, false = Register
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Debug Firebase configuration
        try {
            FirebaseApp app = FirebaseApp.getInstance();
            Log.d(TAG, "Firebase Project ID: " + app.getOptions().getProjectId());
            Log.d(TAG, "Firebase App ID: " + app.getOptions().getApplicationId());
        } catch (Exception e) {
            Log.e(TAG, "Firebase configuration error: " + e.getMessage());
        }

        // Initialize Auth Services
        localAuth = new LocalAuthService(this);
        
        if (useLocalAuth) {
            // Use Local Auth Service
            Log.d(TAG, "Using Local Auth Service (Firebase disabled)");
            
            // Check if user is already logged in locally
            if (localAuth.isLoggedIn()) {
                Log.d(TAG, "User already logged in locally: " + localAuth.getCurrentUserEmail());
                goToMainActivity();
                return;
            }
        } else {
            // Use Firebase Auth (original)
            mAuth = FirebaseAuth.getInstance();
            
            // Check if user is already signed in
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                // User is signed in, go to main activity
                goToMainActivity();
                return;
            }
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        toggleModeText = findViewById(R.id.toggleModeText);
        progressBar = findViewById(R.id.progressBar);

        updateUIMode();
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> {
            if (isLoginMode) {
                performLogin();
            } else {
                performRegister();
            }
        });

        registerButton.setOnClickListener(v -> {
            toggleMode();
        });

        toggleModeText.setOnClickListener(v -> {
            toggleMode();
        });
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        updateUIMode();
    }

    private void updateUIMode() {
        if (isLoginMode) {
            loginButton.setText("Đăng Nhập");
            registerButton.setText("Chưa có tài khoản? Đăng ký");
            toggleModeText.setText("Tạo tài khoản mới");
        } else {
            loginButton.setText("Đăng Ký");
            registerButton.setText("Đã có tài khoản? Đăng nhập");
            toggleModeText.setText("Đăng nhập với tài khoản có sẵn");
        }
    }

    private void performLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!validateInput(email, password)) {
            return;
        }

        showProgress(true);

        // Try Firebase Auth first
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Firebase login success
                            showProgress(false);
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", 
                                    Toast.LENGTH_SHORT).show();
                            goToMainActivity();
                        } else {
                            // Firebase failed, try local auth as fallback
                            Log.w(TAG, "Firebase login failed, trying local auth...", task.getException());
                            
                            new Thread(() -> {
                                boolean success = localAuth.loginUser(email, password);
                                
                                runOnUiThread(() -> {
                                    showProgress(false);
                                    
                                    if (success) {
                                        Log.d(TAG, "Local login success: " + email);
                                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                        goToMainActivity();
                                    } else {
                                        Log.w(TAG, "Both Firebase and local login failed: " + email);
                                        Toast.makeText(LoginActivity.this, 
                                                "Đăng nhập thất bại: " + getErrorMessage(task.getException()),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            }).start();
                        }
                    }
                });
    }

    private void performRegister() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!validateInput(email, password)) {
            return;
        }

        if (password.length() < 6) {
            passwordInputLayout.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        showProgress(true);

        if (useLocalAuth) {
            // Use Local Auth Service
            new Thread(() -> {
                boolean success = localAuth.registerUser(email, password);
                
                runOnUiThread(() -> {
                    showProgress(false);
                    
                    if (success) {
                        Log.d(TAG, "Local registration success: " + email);
                        Toast.makeText(LoginActivity.this, "Tài khoản đã được tạo thành công!", Toast.LENGTH_SHORT).show();
                        goToMainActivity();
                    } else {
                        Log.w(TAG, "Local registration failed: " + email);
                        Toast.makeText(LoginActivity.this, "Tạo tài khoản thất bại: Email đã tồn tại hoặc thông tin không hợp lệ", Toast.LENGTH_LONG).show();
                    }
                });
            }).start();
        } else {
            // Use Firebase Auth (original)
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            showProgress(false);

                            if (task.isSuccessful()) {
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                
                                // Also save to local auth as backup
                                new Thread(() -> {
                                    boolean localSaved = localAuth.registerUser(email, password);
                                    Log.d(TAG, "Local backup saved: " + localSaved);
                                }).start();
                                
                                Toast.makeText(LoginActivity.this, "Tài khoản đã được tạo thành công!", 
                                        Toast.LENGTH_SHORT).show();
                                goToMainActivity();
                            } else {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, 
                                        "Tạo tài khoản thất bại: " + getErrorMessage(task.getException()),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private boolean validateInput(String email, String password) {
        // Clear previous errors
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);

        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError("Vui lòng nhập email");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Email không hợp lệ");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError("Vui lòng nhập mật khẩu");
            isValid = false;
        }

        return isValid;
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        registerButton.setEnabled(!show);
        emailEditText.setEnabled(!show);
        passwordEditText.setEnabled(!show);
    }

    private String getErrorMessage(Exception exception) {
        if (exception == null) return "Lỗi không xác định";
        
        String message = exception.getMessage();
        if (message == null) return "Lỗi không xác định";

        Log.e(TAG, "Firebase Auth Error: " + message);

        // Translate common Firebase Auth errors to Vietnamese
        if (message.contains("CONFIGURATION_NOT_FOUND")) {
            return "Lỗi cấu hình Firebase. Vui lòng kiểm tra google-services.json";
        } else if (message.contains("invalid-email")) {
            return "Email không hợp lệ";
        } else if (message.contains("user-disabled")) {
            return "Tài khoản đã bị vô hiệu hóa";
        } else if (message.contains("user-not-found")) {
            return "Không tìm thấy tài khoản với email này";
        } else if (message.contains("wrong-password")) {
            return "Mật khẩu không chính xác";
        } else if (message.contains("email-already-in-use")) {
            return "Email này đã được sử dụng cho tài khoản khác";
        } else if (message.contains("weak-password")) {
            return "Mật khẩu quá yếu";
        } else if (message.contains("network-request-failed")) {
            return "Lỗi kết nối mạng";
        } else if (message.contains("An internal error has occurred")) {
            return "Lỗi hệ thống Firebase. Vui lòng thử lại sau";
        }

        return message;
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

