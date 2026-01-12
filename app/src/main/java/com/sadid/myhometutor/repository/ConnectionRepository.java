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
 */
public class ConnectionRepository extends FirestoreRepository {
    
    private static final String TAG = "ConnectionRepository";
    
    public interface ConnectionsListener {
        void onConnectionsUpdated(QuerySnapshot snapshot);
        void onError(Exception e);
    }
    
    /**
     * Listen to all connections (accepted applications) with real-time updates
     */
    public ListenerRegistration listenToAllConnections(ConnectionsListener listener) {
        Log.d(TAG, "Setting up listener for all connections");
        return db.collection("applications")
                .whereEqualTo("status", "accepted")
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
     * Listen to connections by student
     */
    public ListenerRegistration listenToConnectionsByStudent(String studentId, ConnectionsListener listener) {
        Log.d(TAG, "Setting up listener for student connections: " + studentId);
        return db.collection("applications")
                .whereEqualTo("status", "accepted")
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
     * Listen to connections by tutor
     */
    public ListenerRegistration listenToConnectionsByTutor(String tutorId, ConnectionsListener listener) {
        Log.d(TAG, "Setting up listener for tutor connections: " + tutorId);
        return db.collection("applications")
                .whereEqualTo("status", "accepted")
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
     * Create a new connection (when student accepts tutor)
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
                return task.getResult().getId();
            }
            throw task.getException();
        });
    }
    
    /**
     * Check if connection exists between student and tutor for a post
     */
    public Task<Boolean> connectionExists(String studentId, String tutorId, String postId) {
        return db.collection("applications")
                .whereEqualTo("status", "accepted")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("tutorId", tutorId)
                .whereEqualTo("postId", postId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return !task.getResult().isEmpty();
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
