package com.sadid.myhometutor.repository;

import com.google.firebase.firestore.WriteBatch;

/**
 * Account Delete Manager
 * Handles batch deletion of user accounts with all related data
 * Updates dashboard counters accordingly
 */
public class AccountDeleteManager extends FirestoreRepository {
    
    private final AdminDashboardRepository dashboardRepo;
    
    public AccountDeleteManager() {
        super();
        this.dashboardRepo = new AdminDashboardRepository();
    }
    
    /**
     * Delete student account and all related data
     * - Deletes student document
     * - Deletes all posts by student
     * - Deletes all applications to those posts
     * - Deletes all connections involving student
     * - Updates dashboard counters
     */
    public void deleteStudentAccount(String studentId, DeleteCallback callback) {
        // First, get student status to update correct counter
        getStudentsCollection()
            .document(studentId)
            .get()
            .addOnSuccessListener(studentDoc -> {
                if (!studentDoc.exists()) {
                    if (callback != null) callback.onFailure(new Exception("Student not found"));
                    return;
                }
                
                String status = studentDoc.getString("status");
                
                WriteBatch batch = db.batch();
                
                // 1. Delete student document
                batch.delete(getStudentsCollection().document(studentId));
                
                // 2. Delete all posts by this student
                getPostsCollection()
                    .whereEqualTo("studentId", studentId)
                    .get()
                    .addOnSuccessListener(postsSnapshot -> {
                        final int[] deletedPosts = {0};
                        final int[] pendingPosts = {0};
                        final int[] approvedPosts = {0};
                        
                        for (var postDoc : postsSnapshot.getDocuments()) {
                            batch.delete(postDoc.getReference());
                            deletedPosts[0]++;
                            
                            String postStatus = postDoc.getString("status");
                            if ("pending".equals(postStatus)) pendingPosts[0]++;
                            else if ("approved".equals(postStatus)) approvedPosts[0]++;
                        }
                        
                        // 3. Delete all applications to student's posts
                        getApplicationsCollection()
                            .whereEqualTo("studentId", studentId)
                            .get()
                            .addOnSuccessListener(appsSnapshot -> {
                                for (var appDoc : appsSnapshot.getDocuments()) {
                                    batch.delete(appDoc.getReference());
                                }
                                
                                // 4. Delete all connections involving this student
                                getConnectionsCollection()
                                    .whereEqualTo("studentId", studentId)
                                    .get()
                                    .addOnSuccessListener(connsSnapshot -> {
                                        final int deletedConnections = connsSnapshot.size();
                                        for (var connDoc : connsSnapshot.getDocuments()) {
                                            batch.delete(connDoc.getReference());
                                        }
                                        
                                        // Commit batch delete
                                        batch.commit()
                                            .addOnSuccessListener(aVoid -> {
                                                // Update dashboard counters
                                                updateCountersAfterStudentDelete(status, deletedPosts[0], 
                                                    pendingPosts[0], approvedPosts[0], deletedConnections, callback);
                                            })
                                            .addOnFailureListener(e -> {
                                                if (callback != null) callback.onFailure(e);
                                            });
                                    });
                            });
                    });
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onFailure(e);
            });
    }
    
    /**
     * Delete tutor account and all related data
     * - Deletes tutor document
     * - Deletes all applications by tutor
     * - Deletes all connections involving tutor
     * - Updates dashboard counters
     */
    public void deleteTutorAccount(String tutorId, DeleteCallback callback) {
        // First, get tutor status to update correct counter
        getTutorsCollection()
            .document(tutorId)
            .get()
            .addOnSuccessListener(tutorDoc -> {
                if (!tutorDoc.exists()) {
                    if (callback != null) callback.onFailure(new Exception("Tutor not found"));
                    return;
                }
                
                String status = tutorDoc.getString("status");
                
                WriteBatch batch = db.batch();
                
                // 1. Delete tutor document
                batch.delete(getTutorsCollection().document(tutorId));
                
                // 2. Delete all applications by this tutor
                getApplicationsCollection()
                    .whereEqualTo("tutorId", tutorId)
                    .get()
                    .addOnSuccessListener(appsSnapshot -> {
                        for (var appDoc : appsSnapshot.getDocuments()) {
                            batch.delete(appDoc.getReference());
                        }
                        
                        // 3. Delete all connections involving this tutor
                        getConnectionsCollection()
                            .whereEqualTo("tutorId", tutorId)
                            .get()
                            .addOnSuccessListener(connsSnapshot -> {
                                final int deletedConnections = connsSnapshot.size();
                                for (var connDoc : connsSnapshot.getDocuments()) {
                                    batch.delete(connDoc.getReference());
                                }
                                
                                // Commit batch delete
                                batch.commit()
                                    .addOnSuccessListener(aVoid -> {
                                        // Update dashboard counters
                                        updateCountersAfterTutorDelete(status, deletedConnections, callback);
                                    })
                                    .addOnFailureListener(e -> {
                                        if (callback != null) callback.onFailure(e);
                                    });
                            });
                    });
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onFailure(e);
            });
    }
    
    /**
     * Update dashboard counters after student deletion
     */
    private void updateCountersAfterStudentDelete(String studentStatus, int deletedPosts,
                                                   int pendingPosts, int approvedPosts,
                                                   int deletedConnections, DeleteCallback callback) {
        // Decrement student count
        dashboardRepo.decrementStudentCount(studentStatus, new DashboardCallback() {
            @Override
            public void onSuccess() {
                // Decrement posts counts
                if (deletedPosts > 0) {
                    dashboardRepo.decrementPostCount("all", deletedPosts, new DashboardCallback() {
                        @Override
                        public void onSuccess() {
                            updatePostStatusCounts(pendingPosts, approvedPosts, deletedConnections, callback);
                        }
                        
                        @Override
                        public void onFailure(Exception e) {
                            if (callback != null) callback.onFailure(e);
                        }
                    });
                } else {
                    updatePostStatusCounts(0, 0, deletedConnections, callback);
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                if (callback != null) callback.onFailure(e);
            }
        });
    }
    
    private void updatePostStatusCounts(int pendingPosts, int approvedPosts, 
                                       int deletedConnections, DeleteCallback callback) {
        if (pendingPosts > 0) {
            dashboardRepo.decrementPostCount("pending", pendingPosts, new DashboardCallback() {
                @Override
                public void onSuccess() {
                    updateApprovedPostCount(approvedPosts, deletedConnections, callback);
                }
                
                @Override
                public void onFailure(Exception e) {
                    if (callback != null) callback.onFailure(e);
                }
            });
        } else {
            updateApprovedPostCount(approvedPosts, deletedConnections, callback);
        }
    }
    
    private void updateApprovedPostCount(int approvedPosts, int deletedConnections, 
                                        DeleteCallback callback) {
        if (approvedPosts > 0) {
            dashboardRepo.decrementPostCount("approved", approvedPosts, new DashboardCallback() {
                @Override
                public void onSuccess() {
                    updateConnectionCount(deletedConnections, callback);
                }
                
                @Override
                public void onFailure(Exception e) {
                    if (callback != null) callback.onFailure(e);
                }
            });
        } else {
            updateConnectionCount(deletedConnections, callback);
        }
    }
    
    private void updateConnectionCount(int deletedConnections, DeleteCallback callback) {
        if (deletedConnections > 0) {
            dashboardRepo.decrementConnectionCount(deletedConnections, new DashboardCallback() {
                @Override
                public void onSuccess() {
                    if (callback != null) callback.onSuccess();
                }
                
                @Override
                public void onFailure(Exception e) {
                    if (callback != null) callback.onFailure(e);
                }
            });
        } else {
            if (callback != null) callback.onSuccess();
        }
    }
    
    /**
     * Update dashboard counters after tutor deletion
     */
    private void updateCountersAfterTutorDelete(String tutorStatus, int deletedConnections, 
                                                DeleteCallback callback) {
        dashboardRepo.decrementTutorCount(tutorStatus, new DashboardCallback() {
            @Override
            public void onSuccess() {
                if (deletedConnections > 0) {
                    dashboardRepo.decrementConnectionCount(deletedConnections, new DashboardCallback() {
                        @Override
                        public void onSuccess() {
                            if (callback != null) callback.onSuccess();
                        }
                        
                        @Override
                        public void onFailure(Exception e) {
                            if (callback != null) callback.onFailure(e);
                        }
                    });
                } else {
                    if (callback != null) callback.onSuccess();
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                if (callback != null) callback.onFailure(e);
            }
        });
    }
    
    public interface DeleteCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}
