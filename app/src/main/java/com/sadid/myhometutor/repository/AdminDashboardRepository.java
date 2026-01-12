package com.sadid.myhometutor.repository;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin Dashboard Repository
 * Manages real-time dashboard statistics with Firestore transactions
 */
public class AdminDashboardRepository extends FirestoreRepository {
    
    private ListenerRegistration dashboardListener;
    
    public interface DashboardListener {
        void onDashboardUpdated(Map<String, Object> dashboardData);
        void onError(Exception e);
    }
    
    /**
     * Initialize dashboard with default values if it doesn't exist
     */
    public Task<Void> initializeDashboard() {
        Map<String, Object> initialData = new HashMap<>();
        initialData.put("totalStudents", 0);
        initialData.put("pendingStudents", 0);
        initialData.put("totalTutors", 0);
        initialData.put("pendingTutors", 0);
        initialData.put("totalPosts", 0);
        initialData.put("approvedPosts", 0);
        initialData.put("pendingPosts", 0);
        initialData.put("successfulConnections", 0);
        initialData.put("lastUpdated", FieldValue.serverTimestamp());
        
        return getDashboardDocument().get().continueWithTask(task -> {
            if (task.isSuccessful() && !task.getResult().exists()) {
                return getDashboardDocument().set(initialData);
            }
            return Tasks.forResult(null);
        });
    }
    
    /**
     * Listen to real-time dashboard updates
     */
    public ListenerRegistration listenToDashboard(DashboardListener listener) {
        return getDashboardDocument().addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                listener.onError(error);
                return;
            }
            
            if (snapshot != null && snapshot.exists()) {
                listener.onDashboardUpdated(snapshot.getData());
            }
        });
    }
    
    /**
     * Stop listening to dashboard updates
     */
    public void stopListening() {
        if (dashboardListener != null) {
            dashboardListener.remove();
            dashboardListener = null;
        }
    }
    
    /**
     * Increment student count
     */
    public Task<Void> incrementStudentCount(boolean isPending) {
        return db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(getDashboardDocument());
            
            long totalStudents = snapshot.contains("totalStudents") ? 
                    snapshot.getLong("totalStudents") : 0;
            long pendingStudents = snapshot.contains("pendingStudents") ? 
                    snapshot.getLong("pendingStudents") : 0;
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("totalStudents", totalStudents + 1);
            if (isPending) {
                updates.put("pendingStudents", pendingStudents + 1);
            }
            updates.put("lastUpdated", FieldValue.serverTimestamp());
            
            transaction.update(getDashboardDocument(), updates);
            return null;
        });
    }
    
    /**
     * Decrement student count
     */
    public Task<Void> decrementStudentCount(boolean wasPending) {
        return db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(getDashboardDocument());
            
            long totalStudents = snapshot.contains("totalStudents") ? 
                    snapshot.getLong("totalStudents") : 0;
            long pendingStudents = snapshot.contains("pendingStudents") ? 
                    snapshot.getLong("pendingStudents") : 0;
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("totalStudents", Math.max(0, totalStudents - 1));
            if (wasPending) {
                updates.put("pendingStudents", Math.max(0, pendingStudents - 1));
            }
            updates.put("lastUpdated", FieldValue.serverTimestamp());
            
            transaction.update(getDashboardDocument(), updates);
            return null;
        });
    }
    
    /**
     * Update student approval status
     */
    public Task<Void> updateStudentApprovalStatus(String oldStatus, String newStatus) {
        return db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(getDashboardDocument());
            
            long pendingStudents = snapshot.contains("pendingStudents") ? 
                    snapshot.getLong("pendingStudents") : 0;
            
            Map<String, Object> updates = new HashMap<>();
            
            if ("pending".equals(oldStatus) && "approved".equals(newStatus)) {
                updates.put("pendingStudents", Math.max(0, pendingStudents - 1));
            } else if ("approved".equals(oldStatus) && "pending".equals(newStatus)) {
                updates.put("pendingStudents", pendingStudents + 1);
            }
            
            updates.put("lastUpdated", FieldValue.serverTimestamp());
            
            transaction.update(getDashboardDocument(), updates);
            return null;
        });
    }
    
    /**
     * Increment tutor count
     */
    public Task<Void> incrementTutorCount(boolean isPending) {
        return db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(getDashboardDocument());
            
            long totalTutors = snapshot.contains("totalTutors") ? 
                    snapshot.getLong("totalTutors") : 0;
            long pendingTutors = snapshot.contains("pendingTutors") ? 
                    snapshot.getLong("pendingTutors") : 0;
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("totalTutors", totalTutors + 1);
            if (isPending) {
                updates.put("pendingTutors", pendingTutors + 1);
            }
            updates.put("lastUpdated", FieldValue.serverTimestamp());
            
            transaction.update(getDashboardDocument(), updates);
            return null;
        });
    }
    
    /**
     * Decrement tutor count
     */
    public Task<Void> decrementTutorCount(boolean wasPending) {
        return db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(getDashboardDocument());
            
            long totalTutors = snapshot.contains("totalTutors") ? 
                    snapshot.getLong("totalTutors") : 0;
            long pendingTutors = snapshot.contains("pendingTutors") ? 
                    snapshot.getLong("pendingTutors") : 0;
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("totalTutors", Math.max(0, totalTutors - 1));
            if (wasPending) {
                updates.put("pendingTutors", Math.max(0, pendingTutors - 1));
            }
            updates.put("lastUpdated", FieldValue.serverTimestamp());
            
            transaction.update(getDashboardDocument(), updates);
            return null;
        });
    }
    
    /**
     * Update tutor approval status
     */
    public Task<Void> updateTutorApprovalStatus(String oldStatus, String newStatus) {
        return db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(getDashboardDocument());
            
            long pendingTutors = snapshot.contains("pendingTutors") ? 
                    snapshot.getLong("pendingTutors") : 0;
            
            Map<String, Object> updates = new HashMap<>();
            
            if ("pending".equals(oldStatus) && "approved".equals(newStatus)) {
                updates.put("pendingTutors", Math.max(0, pendingTutors - 1));
            } else if ("approved".equals(oldStatus) && "pending".equals(newStatus)) {
                updates.put("pendingTutors", pendingTutors + 1);
            }
            
            updates.put("lastUpdated", FieldValue.serverTimestamp());
            
            transaction.update(getDashboardDocument(), updates);
            return null;
        });
    }
    
    /**
     * Increment post count
     */
    public Task<Void> incrementPostCount(String status) {
        return db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(getDashboardDocument());
            
            long totalPosts = snapshot.contains("totalPosts") ? 
                    snapshot.getLong("totalPosts") : 0;
            long approvedPosts = snapshot.contains("approvedPosts") ? 
                    snapshot.getLong("approvedPosts") : 0;
            long pendingPosts = snapshot.contains("pendingPosts") ? 
                    snapshot.getLong("pendingPosts") : 0;
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("totalPosts", totalPosts + 1);
            
            if ("approved".equals(status)) {
                updates.put("approvedPosts", approvedPosts + 1);
            } else if ("pending".equals(status)) {
                updates.put("pendingPosts", pendingPosts + 1);
            }
            
            updates.put("lastUpdated", FieldValue.serverTimestamp());
            
            transaction.update(getDashboardDocument(), updates);
            return null;
        });
    }
    
    /**
     * Update post status
     */
    public Task<Void> updatePostStatus(String oldStatus, String newStatus) {
        return db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(getDashboardDocument());
            
            long approvedPosts = snapshot.contains("approvedPosts") ? 
                    snapshot.getLong("approvedPosts") : 0;
            long pendingPosts = snapshot.contains("pendingPosts") ? 
                    snapshot.getLong("pendingPosts") : 0;
            
            Map<String, Object> updates = new HashMap<>();
            
            if ("pending".equals(oldStatus) && "approved".equals(newStatus)) {
                updates.put("pendingPosts", Math.max(0, pendingPosts - 1));
                updates.put("approvedPosts", approvedPosts + 1);
            } else if ("approved".equals(oldStatus) && "pending".equals(newStatus)) {
                updates.put("approvedPosts", Math.max(0, approvedPosts - 1));
                updates.put("pendingPosts", pendingPosts + 1);
            }
            
            updates.put("lastUpdated", FieldValue.serverTimestamp());
            
            transaction.update(getDashboardDocument(), updates);
            return null;
        });
    }
    
    /**
     * Increment successful connections
     */
    public Task<Void> incrementSuccessfulConnections() {
        return db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(getDashboardDocument());
            
            long connections = snapshot.contains("successfulConnections") ? 
                    snapshot.getLong("successfulConnections") : 0;
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("successfulConnections", connections + 1);
            updates.put("lastUpdated", FieldValue.serverTimestamp());
            
            transaction.update(getDashboardDocument(), updates);
            return null;
        });
    }
    
    /**
     * Recalculate all dashboard counts (use for migration or correction)
     */
    public Task<Void> recalculateDashboard() {
        return Tasks.whenAllComplete(
            getStudentsCollection().get(),
            getTutorsCollection().get(),
            getPostsCollection().get(),
            getConnectionsCollection().get()
        ).continueWithTask(task -> {
            int totalStudents = 0;
            int pendingStudents = 0;
            int totalTutors = 0;
            int pendingTutors = 0;
            int totalPosts = 0;
            int approvedPosts = 0;
            int pendingPosts = 0;
            int successfulConnections = 0;
            
            // Count students
            if (task.getResult().get(0).isSuccessful()) {
                QuerySnapshot studentSnapshot = (QuerySnapshot) task.getResult().get(0).getResult();
                totalStudents = studentSnapshot.size();
                for (DocumentSnapshot doc : studentSnapshot.getDocuments()) {
                    String status = doc.getString("status");
                    if ("pending".equals(status)) pendingStudents++;
                }
            }
            
            // Count tutors
            if (task.getResult().get(1).isSuccessful()) {
                QuerySnapshot tutorSnapshot = (QuerySnapshot) task.getResult().get(1).getResult();
                totalTutors = tutorSnapshot.size();
                for (DocumentSnapshot doc : tutorSnapshot.getDocuments()) {
                    String status = doc.getString("status");
                    if ("pending".equals(status)) pendingTutors++;
                }
            }
            
            // Count posts
            if (task.getResult().get(2).isSuccessful()) {
                QuerySnapshot postSnapshot = (QuerySnapshot) task.getResult().get(2).getResult();
                totalPosts = postSnapshot.size();
                for (DocumentSnapshot doc : postSnapshot.getDocuments()) {
                    String status = doc.getString("status");
                    if ("approved".equals(status)) approvedPosts++;
                    else if ("pending".equals(status)) pendingPosts++;
                }
            }
            
            // Count connections
            if (task.getResult().get(3).isSuccessful()) {
                QuerySnapshot connectionSnapshot = (QuerySnapshot) task.getResult().get(3).getResult();
                successfulConnections = connectionSnapshot.size();
            }
            
            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("totalStudents", totalStudents);
            dashboardData.put("pendingStudents", pendingStudents);
            dashboardData.put("totalTutors", totalTutors);
            dashboardData.put("pendingTutors", pendingTutors);
            dashboardData.put("totalPosts", totalPosts);
            dashboardData.put("approvedPosts", approvedPosts);
            dashboardData.put("pendingPosts", pendingPosts);
            dashboardData.put("successfulConnections", successfulConnections);
            dashboardData.put("lastUpdated", FieldValue.serverTimestamp());
            
            return getDashboardDocument().set(dashboardData);
        });
    }
    
    // ========== Callback-based wrapper methods ==========
    
    /**
     * Increment student count with callback
     */
    public void incrementStudentCount(String status, DashboardCallback callback) {
        boolean isPending = "pending".equals(status);
        incrementStudentCount(isPending)
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onFailure(e);
            });
    }
    
    /**
     * Decrement student count with callback
     */
    public void decrementStudentCount(String status, DashboardCallback callback) {
        boolean wasPending = "pending".equals(status);
        decrementStudentCount(wasPending)
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onFailure(e);
            });
    }
    
    /**
     * Increment tutor count with callback
     */
    public void incrementTutorCount(String status, DashboardCallback callback) {
        boolean isPending = "pending".equals(status);
        incrementTutorCount(isPending)
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onFailure(e);
            });
    }
    
    /**
     * Decrement tutor count with callback
     */
    public void decrementTutorCount(String status, DashboardCallback callback) {
        boolean wasPending = "pending".equals(status);
        decrementTutorCount(wasPending)
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onFailure(e);
            });
    }
    
    /**
     * Decrement post count with callback
     */
    public void decrementPostCount(String status, int count, DashboardCallback callback) {
        // Since we don't have a decrementPostCount method, create one inline
        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(getDashboardDocument());
            
            long totalPosts = snapshot.contains("totalPosts") ? 
                    snapshot.getLong("totalPosts") : 0;
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("totalPosts", Math.max(0, totalPosts - count));
            
            if ("pending".equals(status)) {
                long pendingPosts = snapshot.contains("pendingPosts") ? 
                        snapshot.getLong("pendingPosts") : 0;
                updates.put("pendingPosts", Math.max(0, pendingPosts - count));
            } else if ("approved".equals(status)) {
                long approvedPosts = snapshot.contains("approvedPosts") ? 
                        snapshot.getLong("approvedPosts") : 0;
                updates.put("approvedPosts", Math.max(0, approvedPosts - count));
            }
            
            updates.put("lastUpdated", FieldValue.serverTimestamp());
            transaction.update(getDashboardDocument(), updates);
            return null;
        })
        .addOnSuccessListener(aVoid -> {
            if (callback != null) callback.onSuccess();
        })
        .addOnFailureListener(e -> {
            if (callback != null) callback.onFailure(e);
        });
    }
    
    /**
     * Decrement connection count with callback
     */
    public void decrementConnectionCount(int count, DashboardCallback callback) {
        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(getDashboardDocument());
            
            long successfulConnections = snapshot.contains("successfulConnections") ? 
                    snapshot.getLong("successfulConnections") : 0;
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("successfulConnections", Math.max(0, successfulConnections - count));
            updates.put("lastUpdated", FieldValue.serverTimestamp());
            
            transaction.update(getDashboardDocument(), updates);
            return null;
        })
        .addOnSuccessListener(aVoid -> {
            if (callback != null) callback.onSuccess();
        })
        .addOnFailureListener(e -> {
            if (callback != null) callback.onFailure(e);
        });
    }
    
    /**
     * Recalculate dashboard with callback
     */
    public void recalculateDashboard(DashboardCallback callback) {
        recalculateDashboard()
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onFailure(e);
            });
    }
}

