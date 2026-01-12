package com.sadid.myhometutor.utils;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.sadid.myhometutor.repository.AdminDashboardRepository;
import com.sadid.myhometutor.repository.DashboardCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Data Migration Utility
 * Migrates data from old flat structure to new hierarchical structure
 * 
 * OLD STRUCTURE:
 * - users/ (with userType field)
 * - tuition_posts/
 * 
 * NEW STRUCTURE:
 * - users/students/
 * - users/tutors/
 * - posts/
 * - admin/dashboard
 */
public class DataMigrationUtil {
    
    private static final String TAG = "DataMigration";
    private final FirebaseFirestore db;
    private final AdminDashboardRepository dashboardRepo;
    
    public interface MigrationCallback {
        void onProgress(String message);
        void onComplete(MigrationResult result);
        void onError(Exception e);
    }
    
    public static class MigrationResult {
        public int studentsMigrated = 0;
        public int tutorsMigrated = 0;
        public int postsMigrated = 0;
        public int applicationsMigrated = 0;
        public boolean dashboardCreated = false;
        
        @Override
        public String toString() {
            return "Migration Complete:\n" +
                   "Students migrated: " + studentsMigrated + "\n" +
                   "Tutors migrated: " + tutorsMigrated + "\n" +
                   "Posts migrated: " + postsMigrated + "\n" +
                   "Applications migrated: " + applicationsMigrated + "\n" +
                   "Dashboard created: " + dashboardCreated;
        }
    }
    
    public DataMigrationUtil() {
        this.db = FirebaseFirestore.getInstance();
        this.dashboardRepo = new AdminDashboardRepository();
    }
    
    /**
     * Run complete migration
     * WARNING: This should only be run ONCE on first deployment
     */
    public void runMigration(MigrationCallback callback) {
        MigrationResult result = new MigrationResult();
        
        callback.onProgress("Starting migration...");
        
        // Step 1: Migrate users to students/tutors subcollections
        migrateUsers(result, callback, () -> {
            // Step 2: Migrate tuition_posts to posts
            migratePosts(result, callback, () -> {
                // Step 3: Initialize dashboard
                initializeDashboard(result, callback);
            });
        });
    }
    
    /**
     * Migrate users collection to students/tutors subcollections
     */
    private void migrateUsers(MigrationResult result, MigrationCallback callback, Runnable onComplete) {
        callback.onProgress("Migrating users...");
        
        db.collection("users")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                WriteBatch batch = db.batch();
                int[] batchCount = {0}; // Use array to make it effectively final
                
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String userType = doc.getString("userType");
                    String userId = doc.getId();
                    
                    if ("Student".equals(userType)) {
                        // Migrate to users/students/
                        Map<String, Object> studentData = new HashMap<>();
                        studentData.put("studentId", userId);
                        studentData.put("name", doc.getString("name"));
                        studentData.put("email", doc.getString("email"));
                        studentData.put("phone", doc.getString("phone"));
                        studentData.put("location", doc.getString("location"));
                        studentData.put("profileImageUrl", doc.getString("profileImageUrl"));
                        
                        // Map old approvalStatus to new status
                        String oldStatus = doc.getString("approvalStatus");
                        String newStatus = mapStatus(oldStatus);
                        studentData.put("status", newStatus);
                        
                        studentData.put("createdAt", doc.getLong("createdAt") != null ? 
                            doc.getLong("createdAt") : System.currentTimeMillis());
                        
                        batch.set(db.collection("users").document("students")
                            .collection("students").document(userId), studentData);
                        
                        result.studentsMigrated++;
                        
                    } else if ("Tutor".equals(userType)) {
                        // Migrate to users/tutors/
                        Map<String, Object> tutorData = new HashMap<>();
                        tutorData.put("tutorId", userId);
                        tutorData.put("name", doc.getString("name"));
                        tutorData.put("email", doc.getString("email"));
                        tutorData.put("phone", doc.getString("phone"));
                        tutorData.put("location", doc.getString("location"));
                        tutorData.put("profileImageUrl", doc.getString("profileImageUrl"));
                        tutorData.put("qualifications", doc.getString("qualifications"));
                        tutorData.put("experience", doc.getString("experience"));
                        tutorData.put("subjects", doc.get("subjects"));
                        tutorData.put("documentsUrl", doc.getString("documentsUrl"));
                        
                        // Set registration step based on documents
                        String documentsUrl = doc.getString("documentsUrl");
                        String registrationStep = (documentsUrl != null && !documentsUrl.isEmpty()) ? 
                            "completed" : "profile";
                        tutorData.put("registrationStep", registrationStep);
                        
                        // Map old approvalStatus to new status
                        String oldStatus = doc.getString("approvalStatus");
                        String newStatus = mapStatus(oldStatus);
                        tutorData.put("status", newStatus);
                        
                        tutorData.put("createdAt", doc.getLong("createdAt") != null ? 
                            doc.getLong("createdAt") : System.currentTimeMillis());
                        
                        batch.set(db.collection("users").document("tutors")
                            .collection("tutors").document(userId), tutorData);
                        
                        result.tutorsMigrated++;
                    }
                    
                    batchCount[0]++;
                    
                    // Firestore batch limit is 500 operations
                    if (batchCount[0] >= 400) {
                        final WriteBatch currentBatch = batch;
                        final int currentCount = batchCount[0];
                        currentBatch.commit()
                            .addOnSuccessListener(aVoid -> 
                                Log.d(TAG, "Batch committed: " + currentCount + " users"))
                            .addOnFailureListener(e -> 
                                Log.e(TAG, "Batch commit failed", e));
                        batch = db.batch();
                        batchCount[0] = 0;
                    }
                }
                
                // Commit remaining operations
                if (batchCount[0] > 0) {
                    batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            callback.onProgress("Users migrated: " + result.studentsMigrated + 
                                " students, " + result.tutorsMigrated + " tutors");
                            onComplete.run();
                        })
                        .addOnFailureListener(e -> callback.onError(e));
                } else {
                    callback.onProgress("Users migrated: " + result.studentsMigrated + 
                        " students, " + result.tutorsMigrated + " tutors");
                    onComplete.run();
                }
            })
            .addOnFailureListener(e -> callback.onError(e));
    }
    
    /**
     * Migrate tuition_posts to posts collection
     */
    private void migratePosts(MigrationResult result, MigrationCallback callback, Runnable onComplete) {
        callback.onProgress("Migrating posts...");
        
        db.collection("tuition_posts")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                WriteBatch batch = db.batch();
                int[] batchCount = {0}; // Use array to make it effectively final
                
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String postId = doc.getId();
                    
                    Map<String, Object> postData = new HashMap<>();
                    postData.put("postId", postId);
                    postData.put("studentId", doc.getString("studentId"));
                    postData.put("subject", doc.getString("subject"));
                    postData.put("location", doc.getString("location"));
                    postData.put("salary", doc.getString("salary"));
                    postData.put("gender", doc.getString("gender"));
                    postData.put("medium", doc.getString("medium"));
                    postData.put("tutorRequirement", doc.getString("tutorRequirement"));
                    postData.put("contactNumber", doc.getString("contactNumber"));
                    postData.put("additionalInfo", doc.getString("additionalInfo"));
                    
                    // Map status: "active" -> "approved"
                    String oldStatus = doc.getString("status");
                    String newStatus = "active".equals(oldStatus) ? "approved" : oldStatus;
                    postData.put("status", newStatus);
                    
                    postData.put("createdAt", doc.getLong("createdAt") != null ? 
                        doc.getLong("createdAt") : System.currentTimeMillis());
                    
                    batch.set(db.collection("posts").document(postId), postData);
                    result.postsMigrated++;
                    batchCount[0]++;
                    
                    if (batchCount[0] >= 400) {
                        final WriteBatch currentBatch = batch;
                        final int currentCount = batchCount[0];
                        currentBatch.commit()
                            .addOnSuccessListener(aVoid -> 
                                Log.d(TAG, "Batch committed: " + currentCount + " posts"))
                            .addOnFailureListener(e -> 
                                Log.e(TAG, "Batch commit failed", e));
                        batch = db.batch();
                        batchCount[0] = 0;
                    }
                }
                
                if (batchCount[0] > 0) {
                    batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            callback.onProgress("Posts migrated: " + result.postsMigrated);
                            onComplete.run();
                        })
                        .addOnFailureListener(e -> callback.onError(e));
                } else {
                    callback.onProgress("Posts migrated: " + result.postsMigrated);
                    onComplete.run();
                }
            })
            .addOnFailureListener(e -> callback.onError(e));
    }
    
    /**
     * Initialize dashboard with calculated counts
     */
    private void initializeDashboard(MigrationResult result, MigrationCallback callback) {
        callback.onProgress("Initializing dashboard...");
        
        dashboardRepo.recalculateDashboard(new DashboardCallback() {
            @Override
            public void onSuccess() {
                result.dashboardCreated = true;
                callback.onProgress("Dashboard initialized");
                callback.onComplete(result);
            }
            
            @Override
            public void onFailure(Exception e) {
                callback.onError(e);
            }
        });
    }
    
    /**
     * Map old approval status to new status
     */
    private String mapStatus(String oldStatus) {
        if (oldStatus == null) return "pending";
        
        switch (oldStatus.toLowerCase()) {
            case "approved": return "approved";
            case "rejected": return "rejected";
            case "banned": return "banned";
            case "pending":
            default: return "pending";
        }
    }
}
