package com.sadid.myhometutor.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.sadid.myhometutor.model.Notification;

import java.util.HashMap;
import java.util.Map;

public class NotificationRepository extends FirestoreRepository {

    public static final String COLLECTION_NOTIFICATIONS = "notifications";
    
    // Notification Types
    public static final String TYPE_APPLY = "APPLY";
    public static final String TYPE_POST_APPROVED = "POST_APPROVED";
    public static final String TYPE_POST_DELETED = "POST_DELETED";
    public static final String TYPE_PASSWORD_CHANGED = "PASSWORD_CHANGED";
    public static final String TYPE_APPLICATION_ACCEPTED = "APPLICATION_ACCEPTED";
    public static final String TYPE_CONNECTION_CREATED = "CONNECTION_CREATED";
    public static final String TYPE_APPLICATION_REJECTED = "APPLICATION_REJECTED"; // Added for completeness

    public NotificationRepository() {
        super();
    }

    public Task<Void> sendNotification(String userId, String userType, String title, String message, String type, String referenceId) {
        String id = db.collection(COLLECTION_NOTIFICATIONS).document().getId();
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("userId", userId); // The recipient
        data.put("userType", userType);
        data.put("title", title);
        data.put("message", message);
        data.put("type", type);
        data.put("referenceId", referenceId);
        data.put("isRead", false);
        data.put("timestamp", FieldValue.serverTimestamp());
        
        return db.collection(COLLECTION_NOTIFICATIONS).document(id).set(data);
    }

    public Query getNotificationsQuery(String userId) {
        return db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }

    public Query getUnreadCountQuery(String userId) {
         return db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false);
    }

    public Task<Void> markAsRead(String notificationId) {
        return db.collection(COLLECTION_NOTIFICATIONS).document(notificationId)
                .update("isRead", true);
    }
}
