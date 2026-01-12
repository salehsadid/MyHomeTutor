package com.sadid.myhometutor.repository;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Post Repository
 * Manages tuition post operations with real-time updates
 */
public class PostRepository extends FirestoreRepository {
    
    private static final String TAG = "PostRepository";
    
    public interface PostsListener {
        void onPostsUpdated(QuerySnapshot snapshot);
        void onError(Exception e);
    }
    
    /**
     * Listen to all posts with real-time updates
     */
    public ListenerRegistration listenToAllPosts(PostsListener listener) {
        Log.d(TAG, "Setting up listener for all posts");
        return db.collection("tuition_posts")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to posts", error);
                        listener.onError(error);
                        return;
                    }
                    
                    if (snapshot != null) {
                        Log.d(TAG, "Posts updated: " + snapshot.size() + " documents");
                        listener.onPostsUpdated(snapshot);
                    }
                });
    }
    
    /**
     * Listen to posts by student with real-time updates
     */
    public ListenerRegistration listenToPostsByStudent(String studentId, PostsListener listener) {
        Log.d(TAG, "Setting up listener for student posts: " + studentId);
        return db.collection("tuition_posts")
                .whereEqualTo("studentId", studentId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to student posts", error);
                        listener.onError(error);
                        return;
                    }
                    
                    if (snapshot != null) {
                        Log.d(TAG, "Student posts updated: " + snapshot.size() + " documents");
                        listener.onPostsUpdated(snapshot);
                    }
                });
    }
    
    /**
     * Listen to posts by status with real-time updates
     */
    public ListenerRegistration listenToPostsByStatus(String status, PostsListener listener) {
        Log.d(TAG, "Setting up listener for posts with status: " + status);
        return db.collection("tuition_posts")
                .whereEqualTo("status", status)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to posts by status", error);
                        listener.onError(error);
                        return;
                    }
                    
                    if (snapshot != null) {
                        Log.d(TAG, "Posts with status " + status + " updated: " + snapshot.size() + " documents");
                        listener.onPostsUpdated(snapshot);
                    }
                });
    }
    
    /**
     * Listen to approved posts (visible to tutors)
     */
    public ListenerRegistration listenToApprovedPosts(PostsListener listener) {
        return listenToPostsByStatus("approved", listener);
    }
    
    /**
     * Create a new tuition post
     */
    public Task<String> createPost(String studentId, Map<String, Object> postData) {
        postData.put("studentId", studentId);
        postData.put("status", "pending");
        postData.put("timestamp", FieldValue.serverTimestamp());
        
        return db.collection("tuition_posts").add(postData).continueWith(task -> {
            if (task.isSuccessful()) {
                return task.getResult().getId();
            }
            throw task.getException();
        });
    }
    
    /**
     * Update post status
     */
    public Task<Void> updatePostStatus(String postId, String newStatus) {
        return db.collection("tuition_posts").document(postId).update("status", newStatus);
    }
    
    /**
     * Delete post
     */
    public Task<Void> deletePost(String postId) {
        return db.collection("tuition_posts").document(postId).delete();
    }
    
    /**
     * Get post by ID (one-time read)
     */
    public Task<com.google.firebase.firestore.DocumentSnapshot> getPost(String postId) {
        return db.collection("tuition_posts").document(postId).get();
    }
}
