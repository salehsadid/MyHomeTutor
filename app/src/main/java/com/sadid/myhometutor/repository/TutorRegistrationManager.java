package com.sadid.myhometutor.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Tutor Registration Manager
 * Handles the multi-step tutor registration flow
 */
public class TutorRegistrationManager extends FirestoreRepository {
    
    // Registration steps
    public static final String STEP_PROFILE = "profile";
    public static final String STEP_DOCUMENTS = "documents";
    public static final String STEP_COMPLETED = "completed";
    
    /**
     * Create tutor profile (Step 1)
     */
    public Task<Void> createTutorProfile(String tutorId, Map<String, Object> profileData) {
        profileData.put("registrationStep", STEP_PROFILE);
        profileData.put("status", "pending");
        profileData.put("createdAt", FieldValue.serverTimestamp());
        
        return getTutorDocument(tutorId).set(profileData);
    }
    
    /**
     * Update tutor documents (Step 2)
     */
    public Task<Void> updateTutorDocuments(String tutorId, Map<String, Object> documentsData) {
        Map<String, Object> updates = new HashMap<>(documentsData);
        updates.put("registrationStep", STEP_DOCUMENTS);
        
        return getTutorDocument(tutorId).update(updates);
    }
    
    /**
     * Complete tutor registration (Step 3)
     */
    public Task<Void> completeTutorRegistration(String tutorId) {
        return updateDocument(getTutorDocument(tutorId), "registrationStep", STEP_COMPLETED);
    }
    
    /**
     * Get current registration step
     */
    public Task<String> getRegistrationStep(String tutorId) {
        return getTutorDocument(tutorId).get().continueWith(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String step = task.getResult().getString("registrationStep");
                return step != null ? step : STEP_PROFILE;
            }
            return STEP_PROFILE;
        });
    }
    
    /**
     * Check if registration is completed
     */
    public Task<Boolean> isRegistrationCompleted(String tutorId) {
        return getRegistrationStep(tutorId).continueWith(task -> {
            if (task.isSuccessful()) {
                return STEP_COMPLETED.equals(task.getResult());
            }
            return false;
        });
    }
    
    /**
     * Check if tutor is approved
     */
    public Task<Boolean> isTutorApproved(String tutorId) {
        return getTutorDocument(tutorId).get().continueWith(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String status = task.getResult().getString("status");
                return "approved".equals(status);
            }
            return false;
        });
    }
    
    /**
     * Get tutor data
     */
    public Task<DocumentSnapshot> getTutorData(String tutorId) {
        return getTutorDocument(tutorId).get();
    }
}
