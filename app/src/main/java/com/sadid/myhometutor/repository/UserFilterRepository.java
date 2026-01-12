package com.sadid.myhometutor.repository;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * User Filter Repository
 * Handles filtered queries for students and tutors by status with real-time updates
 */
public class UserFilterRepository extends FirestoreRepository {
    
    private static final String TAG = "UserFilterRepository";
    
    public interface UsersListener {
        void onUsersUpdated(QuerySnapshot snapshot);
        void onError(Exception e);
    }
    
    /**
     * Listen to all students with real-time updates
     */
    public ListenerRegistration listenToAllStudents(UsersListener listener) {
        Log.d(TAG, "Setting up listener for all students");
        return db.collection("users")
                .whereEqualTo("userType", "Student")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to students", error);
                        listener.onError(error);
                        return;
                    }
                    
                    if (snapshot != null) {
                        Log.d(TAG, "Students updated: " + snapshot.size() + " documents");
                        listener.onUsersUpdated(snapshot);
                    } else {
                        Log.w(TAG, "Students snapshot is null");
                    }
                });
    }
    
    /**
     * Listen to students by status with real-time updates
     * @param status - pending, approved, banned, rejected
     */
    public ListenerRegistration listenToStudentsByStatus(String status, UsersListener listener) {
        Log.d(TAG, "Setting up listener for students with status: " + status);
        return db.collection("users")
                .whereEqualTo("userType", "Student")
                .whereEqualTo("approvalStatus", status)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to students by status", error);
                        listener.onError(error);
                        return;
                    }
                    
                    if (snapshot != null) {
                        Log.d(TAG, "Students with status " + status + " updated: " + snapshot.size() + " documents");
                        listener.onUsersUpdated(snapshot);
                    }
                });
    }
    
    /**
     * Listen to all tutors with real-time updates
     */
    public ListenerRegistration listenToAllTutors(UsersListener listener) {
        Log.d(TAG, "Setting up listener for all tutors");
        return db.collection("users")
                .whereEqualTo("userType", "Tutor")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to tutors", error);
                        listener.onError(error);
                        return;
                    }
                    
                    if (snapshot != null) {
                        Log.d(TAG, "Tutors updated: " + snapshot.size() + " documents");
                        listener.onUsersUpdated(snapshot);
                    }
                });
    }
    
    /**
     * Listen to tutors by status with real-time updates
     * @param status - pending, approved, banned, rejected
     */
    public ListenerRegistration listenToTutorsByStatus(String status, UsersListener listener) {
        Log.d(TAG, "Setting up listener for tutors with status: " + status);
        return db.collection("users")
                .whereEqualTo("userType", "Tutor")
                .whereEqualTo("approvalStatus", status)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to tutors by status", error);
                        listener.onError(error);
                        return;
                    }
                    
                    if (snapshot != null) {
                        Log.d(TAG, "Tutors with status " + status + " updated: " + snapshot.size() + " documents");
                        listener.onUsersUpdated(snapshot);
                    }
                });
    }
    
    /**
     * Get student by ID (one-time read)
     */
    public Task<com.google.firebase.firestore.DocumentSnapshot> getStudent(String studentId) {
        return db.collection("users").document(studentId).get();
    }
    
    /**
     * Get tutor by ID (one-time read)
     */
    public Task<com.google.firebase.firestore.DocumentSnapshot> getTutor(String tutorId) {
        return db.collection("users").document(tutorId).get();
    }
    
    /**
     * Update student status
     */
    public Task<Void> updateStudentStatus(String studentId, String newStatus) {
        return db.collection("users").document(studentId).update("approvalStatus", newStatus);
    }
    
    /**
     * Update tutor status
     */
    public Task<Void> updateTutorStatus(String tutorId, String newStatus) {
        return db.collection("users").document(tutorId).update("approvalStatus", newStatus);
    }
}
