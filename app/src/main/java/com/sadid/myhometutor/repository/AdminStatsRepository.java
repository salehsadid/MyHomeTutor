package com.sadid.myhometutor.repository;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sadid.myhometutor.models.DashboardStats;

/**
 * Admin Statistics Repository
 * Calculates real-time dashboard statistics from Firestore
 */
public class AdminStatsRepository {
    
    private final FirebaseFirestore db;
    private ListenerRegistration usersListener;
    private ListenerRegistration postsListener;
    private ListenerRegistration applicationsListener;
    private ListenerRegistration reportsListener;
    
    public interface StatsListener {

        void onStatsUpdated(DashboardStats stats);
        void onError(Exception e);
    }
    
    public AdminStatsRepository() {
        this.db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Get dashboard statistics (one-time fetch)
     */
    public Task<DashboardStats> getDashboardStats() {
        return Tasks.whenAllSuccess(
            db.collection("users").get(),
            db.collection("tuition_posts").get(),
            db.collection("applications").get(),
            db.collection("reports").get()
        ).continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            
            DashboardStats stats = new DashboardStats();
            
            // Calculate user statistics
            var usersSnapshot = (com.google.firebase.firestore.QuerySnapshot) task.getResult().get(0);
            calculateUserStats(usersSnapshot, stats);
            
            // Calculate post statistics
            var postsSnapshot = (com.google.firebase.firestore.QuerySnapshot) task.getResult().get(1);
            calculatePostStats(postsSnapshot, stats);
            
            // Calculate application statistics
            var appsSnapshot = (com.google.firebase.firestore.QuerySnapshot) task.getResult().get(2);
            calculateApplicationStats(appsSnapshot, stats);
            
            // Calculate report statistics
            var reportsSnapshot = (com.google.firebase.firestore.QuerySnapshot) task.getResult().get(3);
            calculateReportStats(reportsSnapshot, stats);
            
            return stats;
        });
    }
    
    /**
     * Listen to real-time dashboard statistics updates
     */
    public void listenToDashboardStats(StatsListener listener) {
        final DashboardStats stats = new DashboardStats();
        final boolean[] usersLoaded = {false};
        final boolean[] postsLoaded = {false};
        final boolean[] appsLoaded = {false};
        final boolean[] reportsLoaded = {false};
        
        // Listen to users collection
        usersListener = db.collection("users")
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    listener.onError(error);
                    return;
                }
                if (snapshot != null) {
                    calculateUserStats(snapshot, stats);
                    usersLoaded[0] = true;
                    if (postsLoaded[0] && appsLoaded[0] && reportsLoaded[0]) {
                        listener.onStatsUpdated(stats);
                    }
                }
            });
        
        // Listen to tuition_posts collection
        postsListener = db.collection("tuition_posts")
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    listener.onError(error);
                    return;
                }
                if (snapshot != null) {
                    calculatePostStats(snapshot, stats);
                    postsLoaded[0] = true;
                    if (usersLoaded[0] && appsLoaded[0] && reportsLoaded[0]) {
                        listener.onStatsUpdated(stats);
                    }
                }
            });
        
        // Listen to applications collection
        applicationsListener = db.collection("applications")
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    listener.onError(error);
                    return;
                }
                if (snapshot != null) {
                    calculateApplicationStats(snapshot, stats);
                    appsLoaded[0] = true;
                    if (usersLoaded[0] && postsLoaded[0] && reportsLoaded[0]) {
                        listener.onStatsUpdated(stats);
                    }
                }
            });
            
        // Listen to reports collection
        reportsListener = db.collection("reports")
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    listener.onError(error);
                    return;
                }
                if (snapshot != null) {
                    calculateReportStats(snapshot, stats);
                    reportsLoaded[0] = true;
                    if (usersLoaded[0] && postsLoaded[0] && appsLoaded[0]) {
                        listener.onStatsUpdated(stats);
                    }
                }
            });
    }
    
    /**
     * Calculate user statistics from snapshot
     */
    private void calculateUserStats(com.google.firebase.firestore.QuerySnapshot snapshot, DashboardStats stats) {
        int totalUsers = snapshot.size();
        
        int totalTutors = 0;
        int approvedTutors = 0;
        int pendingTutors = 0;
        int bannedTutors = 0;
        
        int totalStudents = 0;
        int approvedStudents = 0;
        int pendingStudents = 0;
        int bannedStudents = 0;
        
        for (QueryDocumentSnapshot document : snapshot) {
            String userType = document.getString("userType");
            String approvalStatus = document.getString("approvalStatus");
            Boolean isBanned = document.getBoolean("isBanned");
            boolean banned = isBanned != null && isBanned;
            
            if ("Tutor".equals(userType)) {
                totalTutors++;
                
                if (banned) {
                    bannedTutors++;
                } else if ("approved".equals(approvalStatus)) {
                    approvedTutors++;
                } else if ("pending".equals(approvalStatus)) {
                    pendingTutors++;
                } 
            } else if ("Student".equals(userType)) {
                totalStudents++;
                
                if (banned) {
                    bannedStudents++;
                } else if ("approved".equals(approvalStatus)) {
                    approvedStudents++;
                } else if ("pending".equals(approvalStatus)) {
                    pendingStudents++;
                }
            }
        }
        
        stats.setTotalUsers(totalUsers);
        
        stats.setTotalTutors(totalTutors);
        stats.setApprovedTutors(approvedTutors);
        stats.setPendingTutors(pendingTutors);
        stats.setBannedTutors(bannedTutors);
        
        stats.setTotalStudents(totalStudents);
        stats.setApprovedStudents(approvedStudents);
        stats.setPendingStudents(pendingStudents);
        stats.setBannedStudents(bannedStudents);
    }
    
    /**
     * Calculate tuition post statistics from snapshot
     */
    private void calculatePostStats(com.google.firebase.firestore.QuerySnapshot snapshot, DashboardStats stats) {
        int total = snapshot.size();
        int active = 0;
        int pending = 0;
        
        for (QueryDocumentSnapshot document : snapshot) {
            String status = document.getString("status");
            if ("active".equals(status) || "approved".equals(status)) {
                active++;
            } else if ("pending".equals(status)) {
                pending++;
            }
        }
        
        stats.setTotalPosts(total);
        stats.setActivePosts(active);
        stats.setPendingPosts(pending);
    }
    
    /**
     * Calculate application statistics from snapshot
     * 
     * Status values:
     * - pending: Tutor applied, waiting for student
     * - student_approved: Student accepted, waiting for admin
     * - approved: Admin approved (this counts as a connection!)
     * - rejected: Student rejected
     * - admin_rejected: Admin rejected
     */
    private void calculateApplicationStats(com.google.firebase.firestore.QuerySnapshot snapshot, DashboardStats stats) {
        int total = snapshot.size();
        int approved = 0;  // Admin-approved = actual connections
        int pending = 0;
        int studentApproved = 0;  // Awaiting admin approval
        
        for (QueryDocumentSnapshot document : snapshot) {
            String status = document.getString("status");
            if ("approved".equals(status)) {
                // Admin approved = connection established
                approved++;
            } else if ("pending".equals(status)) {
                pending++;
            } else if ("student_approved".equals(status)) {
                studentApproved++;
            }
        }
        
        stats.setTotalApplications(total);
        stats.setAcceptedApplications(approved);  // This represents actual connections
        stats.setPendingApplications(pending + studentApproved);  // Total needing action
    }

    private void calculateReportStats(com.google.firebase.firestore.QuerySnapshot snapshot, DashboardStats stats) {
        int pending = 0;
        int solved = 0;
        
        for (QueryDocumentSnapshot document : snapshot) {
            String status = document.getString("status");
            if ("pending".equals(status)) {
                pending++;
            } else if ("resolved".equals(status)) {
                solved++;
            }
        }
        
        stats.setPendingReports(pending);
        stats.setSolvedReports(solved);
    }

    /**
     * Remove all snapshot listeners
     */
    public void removeListeners() {
        if (usersListener != null) {
            usersListener.remove();
            usersListener = null;
        }
        if (postsListener != null) {
            postsListener.remove();
            postsListener = null;
        }
        if (applicationsListener != null) {
            applicationsListener.remove();
            applicationsListener = null;
        }
        if (reportsListener != null) {
            reportsListener.remove();
            reportsListener = null;
        }
    }
}

