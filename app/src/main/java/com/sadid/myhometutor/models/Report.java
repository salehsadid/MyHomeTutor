package com.sadid.myhometutor.models;

import com.google.firebase.Timestamp;

/**
 * Report Model
 * Represents a user report submitted by students, tutors, or admins
 */
public class Report {
    
    private String reportId;
    private String reporterId;
    private String reporterName;
    private String reporterType; // "Student" or "Tutor"
    private String reportedUserId;
    private String reportedUserName;
    private String reportedUserType; // "Student" or "Tutor"
    private String reportType; // "User" or "Post"
    private String reportedItemId; // userId or postId
    private String reason;
    private String reportMessage; // Detailed report message
    private Timestamp timestamp;
    private String status; // "pending" or "resolved"
    
    public Report() {
        // Default constructor required for Firestore
    }
    
    public Report(String reporterId, String reporterName, String reporterType,
                  String reportedUserId, String reportedUserName, String reportedUserType,
                  String reportType, String reportedItemId, String reason, 
                  String reportMessage, String status) {
        this.reporterId = reporterId;
        this.reporterName = reporterName;
        this.reporterType = reporterType;
        this.reportedUserId = reportedUserId;
        this.reportedUserName = reportedUserName;
        this.reportedUserType = reportedUserType;
        this.reportType = reportType;
        this.reportedItemId = reportedItemId;
        this.reason = reason;
        this.reportMessage = reportMessage;
        this.timestamp = Timestamp.now();
        this.status = status;
    }

    // Getters and Setters
    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterType() {
        return reporterType;
    }

    public void setReporterType(String reporterType) {
        this.reporterType = reporterType;
    }

    public String getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(String reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public String getReportedUserType() {
        return reportedUserType;
    }

    public void setReportedUserType(String reportedUserType) {
        this.reportedUserType = reportedUserType;
    }

    public String getReportedUserName() {
        return reportedUserName;
    }

    public void setReportedUserName(String reportedUserName) {
        this.reportedUserName = reportedUserName;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getReportedItemId() {
        return reportedItemId;
    }

    public void setReportedItemId(String reportedItemId) {
        this.reportedItemId = reportedItemId;
    }

    public String getReportMessage() {
        return reportMessage;
    }

    public void setReportMessage(String reportMessage) {
        this.reportMessage = reportMessage;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
