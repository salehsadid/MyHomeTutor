package com.sadid.myhometutor.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Application Repository
 * Manages tutor applications to tuition posts
 */
public class ApplicationRepository extends FirestoreRepository {
    
    /**
     * Create a new application
     */
    public Task<String> createApplication(String postId, String tutorId, String studentId) {
        Map<String, Object> applicationData = new HashMap<>();
        applicationData.put("postId", postId);
        applicationData.put("tutorId", tutorId);
        applicationData.put("studentId", studentId);
        applicationData.put("status", "pending");
        applicationData.put("createdAt", FieldValue.serverTimestamp());
        
        return getApplicationsCollection().add(applicationData).continueWith(task -> {
            if (task.isSuccessful()) {
                return task.getResult().getId();
            }
            throw task.getException();
        });
    }
    
    /**
     * Get applications for a post
     */
    public Task<QuerySnapshot> getApplicationsByPost(String postId) {
        return queryByField(getApplicationsCollection(), "postId", postId).get();
    }
    
    /**
     * Get applications by tutor
     */
    public Task<QuerySnapshot> getApplicationsByTutor(String tutorId) {
        return queryByField(getApplicationsCollection(), "tutorId", tutorId).get();
    }
    
    /**
     * Get applications by student
     */
    public Task<QuerySnapshot> getApplicationsByStudent(String studentId) {
        return queryByField(getApplicationsCollection(), "studentId", studentId).get();
    }
    
    /**
     * Update application status
     */
    public Task<Void> updateApplicationStatus(String applicationId, String newStatus) {
        return updateDocument(getApplicationDocument(applicationId), "status", newStatus);
    }
    
    /**
     * Accept application
     */
    public Task<Void> acceptApplication(String applicationId) {
        return updateApplicationStatus(applicationId, "accepted");
    }
    
    /**
     * Reject application
     */
    public Task<Void> rejectApplication(String applicationId) {
        return updateApplicationStatus(applicationId, "rejected");
    }
    
    /**
     * Get application by ID
     */
    public Task<com.google.firebase.firestore.DocumentSnapshot> getApplication(String applicationId) {
        return getApplicationDocument(applicationId).get();
    }
    
    /**
     * Delete application
     */
    public Task<Void> deleteApplication(String applicationId) {
        return deleteDocument(getApplicationDocument(applicationId));
    }
}
