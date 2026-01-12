package com.sadid.myhometutor.repository;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Application Repository
 * Manages tutor applications to tuition posts
 * 
 * Status Flow:
 * - pending: Tutor has applied, waiting for student review
 * - student_approved: Student accepted, waiting for admin approval
 * - approved: Admin approved, connection established (can see contact info)
 * - rejected: Student rejected the application
 * - admin_rejected: Admin rejected the application
 */
public class ApplicationRepository extends FirestoreRepository {
    
    private static final String TAG = "ApplicationRepository";
    
    // Application status constants
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_STUDENT_APPROVED = "student_approved";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_ADMIN_REJECTED = "admin_rejected";
    
    public interface ApplicationsListener {
        void onApplicationsUpdated(QuerySnapshot snapshot);
        void onError(Exception e);
    }
    
    /**
     * Create a new application
     */
    public Task<String> createApplication(String postId, String tutorId, String studentId) {
        Map<String, Object> applicationData = new HashMap<>();
        applicationData.put("postId", postId);
        applicationData.put("tutorId", tutorId);
        applicationData.put("studentId", studentId);
        applicationData.put("status", STATUS_PENDING);
        applicationData.put("timestamp", FieldValue.serverTimestamp());
        applicationData.put("createdAt", FieldValue.serverTimestamp());
        
        return getApplicationsCollection().add(applicationData).continueWith(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Application created: " + task.getResult().getId());
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
     * Get pending applications for a post (for student to review)
     */
    public Task<QuerySnapshot> getPendingApplicationsByPost(String postId) {
        return getApplicationsCollection()
            .whereEqualTo("postId", postId)
            .whereEqualTo("status", STATUS_PENDING)
            .get();
    }
    
    /**
     * Listen to applications for a specific post with real-time updates
     * Note: Results are not ordered to avoid requiring composite index.
     * Sort client-side if needed.
     */
    public ListenerRegistration listenToApplicationsByPost(String postId, ApplicationsListener listener) {
        return getApplicationsCollection()
            .whereEqualTo("postId", postId)
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error listening to applications", error);
                    listener.onError(error);
                    return;
                }
                if (snapshot != null) {
                    listener.onApplicationsUpdated(snapshot);
                }
            });
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
     * Get student_approved applications (for admin review)
     */
    public Task<QuerySnapshot> getStudentApprovedApplications() {
        return getApplicationsCollection()
            .whereEqualTo("status", STATUS_STUDENT_APPROVED)
            .get();
    }
    
    /**
     * Listen to student_approved applications (for admin real-time updates)
     * Note: Results are not ordered to avoid requiring composite index.
     * Sort client-side if needed.
     */
    public ListenerRegistration listenToStudentApprovedApplications(ApplicationsListener listener) {
        return getApplicationsCollection()
            .whereEqualTo("status", STATUS_STUDENT_APPROVED)
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error listening to student_approved applications", error);
                    listener.onError(error);
                    return;
                }
                if (snapshot != null) {
                    listener.onApplicationsUpdated(snapshot);
                }
            });
    }
    
    /**
     * Update application status
     */
    public Task<Void> updateApplicationStatus(String applicationId, String newStatus) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("updatedAt", FieldValue.serverTimestamp());
        return getApplicationDocument(applicationId).update(updates);
    }
    
    /**
     * Student approves application (moves to student_approved status for admin review)
     */
    public Task<Void> studentApproveApplication(String applicationId) {
        Log.d(TAG, "Student approving application: " + applicationId);
        return updateApplicationStatus(applicationId, STATUS_STUDENT_APPROVED);
    }
    
    /**
     * Admin approves application (final approval - creates connection)
     */
    public Task<Void> adminApproveApplication(String applicationId) {
        Log.d(TAG, "Admin approving application: " + applicationId);
        return updateApplicationStatus(applicationId, STATUS_APPROVED);
    }
    
    /**
     * Admin rejects application
     */
    public Task<Void> adminRejectApplication(String applicationId) {
        Log.d(TAG, "Admin rejecting application: " + applicationId);
        return updateApplicationStatus(applicationId, STATUS_ADMIN_REJECTED);
    }
    
    /**
     * Student rejects application
     */
    public Task<Void> rejectApplication(String applicationId) {
        Log.d(TAG, "Student rejecting application: " + applicationId);
        return updateApplicationStatus(applicationId, STATUS_REJECTED);
    }
    
    /**
     * Legacy accept method - now routes to studentApproveApplication
     * @deprecated Use studentApproveApplication() instead for proper workflow
     */
    @Deprecated
    public Task<Void> acceptApplication(String applicationId) {
        return studentApproveApplication(applicationId);
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
