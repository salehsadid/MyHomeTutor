package com.sadid.myhometutor.models;

public class DashboardStats {
    private int totalUsers;
    private int totalStudents;
    private int totalTutors;
    private int approvedStudents;
    private int approvedTutors;
    private int bannedStudents;
    private int pendingStudents;
    private int bannedTutors;
    private int pendingTutors;
    private int totalPosts;
    private int activePosts; // Approved posts
    private int pendingPosts;
    private int totalApplications;
    private int acceptedApplications;
    private int pendingApplications;
    private int pendingReports;
    private int solvedReports;

    public DashboardStats() {
        // Default constructor
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getTotalTutors() {
        return totalTutors;
    }

    public void setTotalTutors(int totalTutors) {
        this.totalTutors = totalTutors;
    }

    public int getApprovedStudents() {
        return approvedStudents;
    }

    public void setApprovedStudents(int approvedStudents) {
        this.approvedStudents = approvedStudents;
    }
    
    public int getBannedStudents() {
        return bannedStudents;
    }

    public void setBannedStudents(int bannedStudents) {
        this.bannedStudents = bannedStudents;
    }

    public int getPendingStudents() {
        return pendingStudents;
    }

    public void setPendingStudents(int pendingStudents) {
        this.pendingStudents = pendingStudents;
    }

    public int getApprovedTutors() {
        return approvedTutors;
    }

    public void setApprovedTutors(int approvedTutors) {
        this.approvedTutors = approvedTutors;
    }

    public int getBannedTutors() {
        return bannedTutors;
    }

    public void setBannedTutors(int bannedTutors) {
        this.bannedTutors = bannedTutors;
    }

    public int getPendingTutors() {
        return pendingTutors;
    }

    public void setPendingTutors(int pendingTutors) {
        this.pendingTutors = pendingTutors;
    }

    public int getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(int totalPosts) {
        this.totalPosts = totalPosts;
    }

    public int getActivePosts() {
        return activePosts;
    }

    public void setActivePosts(int activePosts) {
        this.activePosts = activePosts;
    }

    public int getPendingPosts() {
        return pendingPosts;
    }

    public void setPendingPosts(int pendingPosts) {
        this.pendingPosts = pendingPosts;
    }

    public int getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(int totalApplications) {
        this.totalApplications = totalApplications;
    }

    public int getAcceptedApplications() {
        return acceptedApplications;
    }

    public void setAcceptedApplications(int acceptedApplications) {
        this.acceptedApplications = acceptedApplications;
    }

    public int getPendingApplications() {
        return pendingApplications;
    }

    public void setPendingApplications(int pendingApplications) {
        this.pendingApplications = pendingApplications;
    }

    public int getPendingReports() {
        return pendingReports;
    }

    public void setPendingReports(int pendingReports) {
        this.pendingReports = pendingReports;
    }

    public int getSolvedReports() {
        return solvedReports;
    }

    public void setSolvedReports(int solvedReports) {
        this.solvedReports = solvedReports;
    }
}
