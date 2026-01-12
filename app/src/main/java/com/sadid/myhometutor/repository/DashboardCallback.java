package com.sadid.myhometutor.repository;

/**
 * Callback interface for dashboard operations
 */
public interface DashboardCallback {
    void onSuccess();
    void onFailure(Exception e);
}
