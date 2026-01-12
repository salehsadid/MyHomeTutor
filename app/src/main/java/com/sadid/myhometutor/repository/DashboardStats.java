package com.sadid.myhometutor.repository;

/**
 * Dashboard Statistics Data Model
 */
public class DashboardStats {
    public int totalStudents = 0;
    public int pendingStudents = 0;
    public int approvedStudents = 0;
    public int rejectedStudents = 0;
    public int bannedStudents = 0;
    
    public int totalTutors = 0;
    public int pendingTutors = 0;
    public int approvedTutors = 0;
    public int rejectedTutors = 0;
    public int bannedTutors = 0;
    
    public int totalPosts = 0;
    public int pendingPosts = 0;
    public int approvedPosts = 0;
    public int rejectedPosts = 0;
    
    public int totalConnections = 0;
    
    public DashboardStats() {
        // Empty constructor
    }
}
