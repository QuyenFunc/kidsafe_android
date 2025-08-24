package qn.app.kidsafe_android;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Local Authentication Service as fallback for Firebase
 * Stores user credentials locally and syncs with PC via Firebase Database only
 */
public class LocalAuthService {
    
    private static final String TAG = "LocalAuthService";
    private static final String PREFS_NAME = "KidSafeAuth";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_UID = "user_uid";
    private static final String KEY_PASSWORD_HASH = "password_hash";
    
    private Context context;
    private SharedPreferences prefs;
    
    public LocalAuthService(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Register new user locally
     */
    public boolean registerUser(String email, String password) {
        try {
            // Check if user already exists
            if (isUserRegistered(email)) {
                Log.w(TAG, "User already registered: " + email);
                return false;
            }
            
            // Validate input
            if (!isValidEmail(email) || !isValidPassword(password)) {
                Log.w(TAG, "Invalid email or password");
                return false;
            }
            
            // Generate user UID
            String userUID = generateUserUID(email);
            
            // Hash password
            String passwordHash = hashPassword(password);
            
            // Save to SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_EMAIL, email);
            editor.putString(KEY_USER_UID, userUID);
            editor.putString(KEY_PASSWORD_HASH, passwordHash);
            editor.apply();
            
            Log.i(TAG, "User registered successfully: " + email);
            Log.i(TAG, "Generated UID: " + userUID);
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Registration failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Login user with email and password
     */
    public boolean loginUser(String email, String password) {
        try {
            // Get stored credentials
            String storedEmail = prefs.getString(KEY_USER_EMAIL, null);
            String storedPasswordHash = prefs.getString(KEY_PASSWORD_HASH, null);
            
            if (storedEmail == null || storedPasswordHash == null) {
                Log.w(TAG, "No user registered");
                return false;
            }
            
            // Check email match
            if (!storedEmail.equals(email)) {
                Log.w(TAG, "Email mismatch");
                return false;
            }
            
            // Check password hash
            String inputPasswordHash = hashPassword(password);
            if (!storedPasswordHash.equals(inputPasswordHash)) {
                Log.w(TAG, "Password mismatch");
                return false;
            }
            
            Log.i(TAG, "User logged in successfully: " + email);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Login failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get current user UID
     */
    public String getCurrentUserUID() {
        return prefs.getString(KEY_USER_UID, null);
    }
    
    /**
     * Get current user email
     */
    public String getCurrentUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return getCurrentUserEmail() != null && getCurrentUserUID() != null;
    }
    
    /**
     * Check if user is already registered
     */
    public boolean isUserRegistered(String email) {
        String storedEmail = prefs.getString(KEY_USER_EMAIL, null);
        return storedEmail != null && storedEmail.equals(email);
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        Log.i(TAG, "User logged out");
    }
    
    /**
     * Generate consistent UID from email
     */
    private String generateUserUID(String email) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(email.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return "user_" + hexString.toString().substring(0, 16);
            
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Failed to generate UID: " + e.getMessage());
            return "user_" + email.hashCode();
        }
    }
    
    /**
     * Hash password with simple SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Failed to hash password: " + e.getMessage());
            return password; // Fallback (not secure)
        }
    }
    
    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        return email != null && 
               email.contains("@") && 
               email.contains(".") && 
               email.length() > 5;
    }
    
    /**
     * Validate password strength
     */
    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    
    /**
     * Clear saved credentials (logout)
     */
    public void clearSavedCredentials() {
        try {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            Log.i(TAG, "✅ All saved credentials cleared");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error clearing credentials: " + e.getMessage());
        }
    }
}





























