package com.sadid.myhometutor.model;

import com.google.firebase.Timestamp;

public class Notification {
    private String id;
    private String userId;
    private String userType;
    private String title;
    private String message;
    private String type; // APPLY, APPROVAL, PASSWORD, CONNECTION, POST_DELETED, APPLICATION_ACCEPTED
    private String referenceId;
    private boolean isRead;
    private Timestamp timestamp;

    public Notification() {
        // Required empty constructor for Firestore
    }

    public Notification(String id, String userId, String userType, String title, String message, String type, String referenceId, boolean isRead, Timestamp timestamp) {
        this.id = id;
        this.userId = userId;
        this.userType = userType;
        this.title = title;
        this.message = message;
        this.type = type;
        this.referenceId = referenceId;
        this.isRead = isRead;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
