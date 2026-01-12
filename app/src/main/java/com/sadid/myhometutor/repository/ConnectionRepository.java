package com.sadid.myhometutor.repository;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Connection Repository
 * Manages successful student-tutor connections with real-time updates
 * 
 * A connection is established when:
 * 1. Tutor applies to a post (status: pending)
 * 2. Student approves (status: student_approved)
 * 3. Admin approves (status: approved) -> Connection established!
 * 
 * Only users with "approved" status can see each other's contact information.
 */
public class ConnectionRepository extends FirestoreRepository {
    
    private static final String TAG = "ConnectionRepository";
    
    // Use the same status constant as ApplicationRepository
    private static final String STATUS_APPROVED = "approved";
    
    public interface ConnectionsListener {
        void onConnectionsUpdated(QuerySnapshot snapshot);
        void onError(Exception e);
    }
    
    /**
     * Listen to all connections (admin-approved applications) with real-time updates
     * Only applications with status="approved" are considered connections
     */
    public ListenerRegistration listenToAllConnections(ConnectionsListener listener) {
        Log.d(TAG, "Setting up listener for all connections (status=approved)");
        return db.collection("applications")
                .whereEqualTo("status", STATUS_APPROVED)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to connections", error);
                        listener.onError(error);
                        return;
                    }
                    
                    if (snapshot != null) {
                        Log.d(TAG, "Connections updated: " + snapshot.size() + " documents");
                        listener.onConnectionsUpdated(snapshot);
                    }
                });
    }
    
    /**
     * Listen to connections by student (where student's applications are approved)
     */
    public ListenerRegistration listenToConnectionsByStudent(String studentId, ConnectionsListener listener) {
        Log.d(TAG, "Setting up listener for student connections: " + studentId);
        return db.collection("applications")
                .whereEqualTo("status", STATUS_APPROVED)
                .whereEqualTo("studentId", studentId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to student connections", error);
                        listener.onError(error);
                        return;
                    }
                    
                    if (snapshot != null) {
                        Log.d(TAG, "Student connections updated: " + snapshot.size() + " documents");
                        listener.onConnectionsUpdated(snapshot);
                    }
                });
    }
    
    /**
     * Listen to connections by tutor (where tutor's applications are approved)
     */
    public ListenerRegistration listenToConnectionsByTutor(String tutorId, ConnectionsListener listener) {
        Log.d(TAG, "Setting up listener for tutor connections: " + tutorId);
        return db.collection("applications")
                .whereEqualTo("status", STATUS_APPROVED)
                .whereEqualTo("tutorId", tutorId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to tutor connections", error);
                        listener.onError(error);
                        return;
                    }
                    
                    if (snapshot != null) {
                        Log.d(TAG, "Tutor connections updated: " + snapshot.size() + " documents");
                        listener.onConnectionsUpdated(snapshot);
                    }
                });
    }
    
    /**
     * Create a new connection record in the connections collection
     * This is called when admin approves an application
     */
    public Task<String> createConnection(String studentId, String tutorId, String postId, String applicationId) {
        Map<String, Object> connectionData = new HashMap<>();
        connectionData.put("studentId", studentId);
        connectionData.put("tutorId", tutorId);
        connectionData.put("postId", postId);
        connectionData.put("applicationId", applicationId);
        connectionData.put("timestamp", FieldValue.serverTimestamp());
        
        return getConnectionsCollection().add(connectionData).continueWith(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Connection created: " + task.getResult().getId());
                return task.getResult().getId();
            }
            throw task.getException();
        });
    }
    
    /**
     * Check if connection exists between student and tutor for a post
     * A connection exists if there's an approved application
     */
    public Task<Boolean> connectionExists(String studentId, String tutorId, String postId) {
        return db.collection("applications")
                .whereEqualTo("status", STATUS_APPROVED)
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("tutorId", tutorId)
                .whereEqualTo("postId", postId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        boolean exists = !task.getResult().isEmpty();
                        Log.d(TAG, "Connection exists check: " + exists);
                        return exists;
                    }
                    return false;
                });
    }
    
    /**
     * Check if user can view another user's contact info
     * Contact info is only visible when they have an approved connection
     */
    public Task<Boolean> canViewContactInfo(String userId1, String userId2) {
        // Check if there's an approved application where both users are involved
        return db.collection("applications")
                .whereEqualTo("status", STATUS_APPROVED)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        for (var doc : task.getResult()) {
                            String studentId = doc.getString("studentId");
                            String tutorId = doc.getString("tutorId");
                            
                            // Check if both users are part of this connection
                            if ((userId1.equals(studentId) && userId2.equals(tutorId)) ||
                                (userId1.equals(tutorId) && userId2.equals(studentId))) {
                                return true;
                            }
                        }
                    }
                    return false;
                });
    }
    
    /**
     * Delete connection
     */
    public Task<Void> deleteConnection(String connectionId) {
        return deleteDocument(getConnectionDocument(connectionId));
    }
}
