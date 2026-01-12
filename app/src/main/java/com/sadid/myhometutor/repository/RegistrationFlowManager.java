package com.sadid.myhometutor.repository;

import com.google.firebase.firestore.WriteBatch;

/**
 * Student Registration Flow Manager
 * Handles complete student registration with proper subcollection structure
 */
public class RegistrationFlowManager extends FirestoreRepository {
    
    private final AdminDashboardRepository dashboardRepo;
    
    public RegistrationFlowManager() {
        super();
        this.dashboardRepo = new AdminDashboardRepository();
    }
    
    /**
     * Register new student with pending status
     * Creates student document in users/students/ subcollection
     */
    public void registerStudent(String userId, String name, String email, 
                                String phone, String location, String profileImageUrl,
                                RegistrationCallback callback) {
        
        StudentProfile student = new StudentProfile();
        student.studentId = userId;
        student.name = name;
        student.email = email;
        student.phone = phone;
        student.location = location;
        student.profileImageUrl = profileImageUrl;
        student.status = "pending"; // Pending admin approval
        student.createdAt = System.currentTimeMillis();
        
        getStudentsCollection()
            .document(userId)
            .set(student)
            .addOnSuccessListener(aVoid -> {
                // Increment pending students count
                dashboardRepo.incrementStudentCount("pending", new DashboardCallback() {
                    @Override
                    public void onSuccess() {
                        if (callback != null) callback.onSuccess();
                    }
                    
                    @Override
                    public void onFailure(Exception e) {
                        if (callback != null) callback.onFailure(e);
                    }
                });
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onFailure(e);
            });
    }
    
    /**
     * Update student profile
     */
    public void updateStudentProfile(String studentId, StudentProfile updates, RegistrationCallback callback) {
        getStudentsCollection()
            .document(studentId)
            .update(
                "name", updates.name,
                "phone", updates.phone,
                "location", updates.location,
                "profileImageUrl", updates.profileImageUrl
            )
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onFailure(e);
            });
    }
    
    /**
     * Check if student exists in new structure
     */
    public void studentExists(String studentId, ExistsCallback callback) {
        getStudentsCollection()
            .document(studentId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (callback != null) {
                    callback.onResult(documentSnapshot.exists());
                }
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onResult(false);
            });
    }
    
    /**
     * Student Profile Data Model
     */
    public static class StudentProfile {
        public String studentId;
        public String name;
        public String email;
        public String phone;
        public String location;
        public String profileImageUrl;
        public String status; // pending, approved, rejected, banned
        public long createdAt;
        
        public StudentProfile() {
            // Required for Firestore
        }
    }
    
    public interface RegistrationCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
    
    public interface ExistsCallback {
        void onResult(boolean exists);
    }
}
