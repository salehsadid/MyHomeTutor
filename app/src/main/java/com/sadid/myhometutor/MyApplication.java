package com.sadid.myhometutor;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this);
            
            // Initialize Firebase App Check
            FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
            
            // Use Debug provider for development, Play Integrity for production
            if (BuildConfig.DEBUG) {
                firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                );
            } else {
                firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                );
            }
            
            // Enable Firestore offline persistence
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            db.setFirestoreSettings(settings);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
