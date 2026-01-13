package com.sadid.myhometutor.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.sadid.myhometutor.models.Report;

import java.util.ArrayList;
import java.util.List;

/**
 * Report Repository
 * Manages user-submitted reports in Firestore
 */
public class ReportRepository {
    
    private final FirebaseFirestore db;
    private ListenerRegistration reportsListener;
    
    public interface ReportsListener {
        void onReportsUpdated(List<Report> reports);
        void onError(Exception e);
    }
    
    public ReportRepository() {
        this.db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Listen to all reports with real-time updates
     * Orders by timestamp (newest first)
     */
    public void listenToReports(ReportsListener listener) {
        reportsListener = db.collection("reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    listener.onError(error);
                    return;
                }
                
                if (snapshot != null) {
                    List<Report> reports = new ArrayList<>();
                    for (var doc : snapshot) {
                        Report report = doc.toObject(Report.class);
                        report.setReportId(doc.getId());
                        reports.add(report);
                    }
                    listener.onReportsUpdated(reports);
                }
            });
    }
    
    /**
     * Listen to pending reports only
     */
    public void listenToPendingReports(ReportsListener listener) {
        reportsListener = db.collection("reports")
            .whereEqualTo("status", "pending")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    listener.onError(error);
                    return;
                }
                
                if (snapshot != null) {
                    List<Report> reports = new ArrayList<>();
                    for (var doc : snapshot) {
                        Report report = doc.toObject(Report.class);
                        report.setReportId(doc.getId());
                        reports.add(report);
                    }
                    listener.onReportsUpdated(reports);
                }
            });
    }
    
    /**
     * Add a new report
     */
    public Task<Void> addReport(Report report) {
        return db.collection("reports")
            .add(report)
            .continueWith(task -> {
                if (task.isSuccessful()) {
                    return null;
                } else {
                    throw task.getException();
                }
            });
    }
    
    /**
     * Submit a profile report (simplified method for user profile reporting)
     * 
     * @param reporterId ID of the user submitting the report
     * @param reporterName Name of the user submitting the report
     * @param reporterType Type of reporter ("Student" or "Tutor")
     * @param reportedUserId ID of the reported user
     * @param reportedUserName Name of the reported user
     * @param reportedUserType Type of reported user ("Student" or "Tutor")
     * @param reportMessage Detailed report message from the reporter
     */
    public Task<Void> submitProfileReport(String reporterId, String reporterName, String reporterType,
                                          String reportedUserId, String reportedUserName, String reportedUserType,
                                          String reportMessage) {
        Report report = new Report(
            reporterId,
            reporterName,
            reporterType,
            reportedUserId,
            reportedUserName,
            reportedUserType,
            "User", // reportType
            reportedUserId, // reportedItemId
            "Profile Report", // reason
            reportMessage,
            "pending" // status
        );
        
        return addReport(report);
    }
    
    /**
     * Mark a report as resolved
     */
    public Task<Void> resolveReport(String reportId) {
        return db.collection("reports")
            .document(reportId)
            .update("status", "resolved");
    }
    
    /**
     * Delete a report
     */
    public Task<Void> deleteReport(String reportId) {
        return db.collection("reports")
            .document(reportId)
            .delete();
    }
    
    /**
     * Remove snapshot listener
     */
    public void removeListener() {
        if (reportsListener != null) {
            reportsListener.remove();
            reportsListener = null;
        }
    }
}
