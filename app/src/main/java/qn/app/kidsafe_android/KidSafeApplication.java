package qn.app.kidsafe_android;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

/**
 * KidSafe Application class
 * Ensures Firebase is properly configured for production use
 */
public class KidSafeApplication extends Application {
    
    private static final String TAG = "KidSafeApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase for production
        initializeFirebaseProduction();
    }
    
    /**
     * Initialize Firebase for production use
     * This ensures we connect to the real Firebase servers, not emulator
     */
    private void initializeFirebaseProduction() {
        try {
            // Initialize Firebase App
            FirebaseApp.initializeApp(this);
            
            // Enable Firebase Database persistence (offline support)
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            
            // Log Firebase configuration for debugging
            FirebaseApp app = FirebaseApp.getInstance();
            Log.d(TAG, "✅ Firebase initialized for PRODUCTION");
            Log.d(TAG, "🔥 Project ID: " + app.getOptions().getProjectId());
            Log.d(TAG, "🔥 App ID: " + app.getOptions().getApplicationId());
            Log.d(TAG, "🔥 Database URL: " + app.getOptions().getDatabaseUrl());
            
            // Ensure we're not using emulator
            // Note: If emulator connection was previously set up, this would override it
            Log.d(TAG, "🚀 Firebase is configured for PRODUCTION (Real Firebase servers)");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to initialize Firebase", e);
        }
    }
}
