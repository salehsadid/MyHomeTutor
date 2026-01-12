package com.sadid.myhometutor.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.Map;

/**
 * Base Firestore Repository
 * Provides common database operations for all repositories
 */
public class FirestoreRepository {
    
    protected final FirebaseFirestore db;
    
    // Collection paths
    public static final String USERS = "users";
    public static final String STUDENTS = "students";
    public static final String TUTORS = "tutors";
    public static final String POSTS = "posts";
    public static final String APPLICATIONS = "applications";
    public static final String CONNECTIONS = "connections";
    public static final String ADMIN = "admin";
    public static final String DASHBOARD = "dashboard";
    
    public FirestoreRepository() {
        this.db = FirebaseFirestore.getInstance();
    }
    
    // Student operations
    protected CollectionReference getStudentsCollection() {
        return db.collection(USERS).document(STUDENTS).collection(STUDENTS);
    }
    
    protected DocumentReference getStudentDocument(String studentId) {
        return getStudentsCollection().document(studentId);
    }
    
    // Tutor operations
    protected CollectionReference getTutorsCollection() {
        return db.collection(USERS).document(TUTORS).collection(TUTORS);
    }
    
    protected DocumentReference getTutorDocument(String tutorId) {
        return getTutorsCollection().document(tutorId);
    }
    
    // Post operations
    protected CollectionReference getPostsCollection() {
        return db.collection(POSTS);
    }
    
    protected DocumentReference getPostDocument(String postId) {
        return getPostsCollection().document(postId);
    }
    
    // Application operations
    protected CollectionReference getApplicationsCollection() {
        return db.collection(APPLICATIONS);
    }
    
    protected DocumentReference getApplicationDocument(String applicationId) {
        return getApplicationsCollection().document(applicationId);
    }
    
    // Connection operations
    protected CollectionReference getConnectionsCollection() {
        return db.collection(CONNECTIONS);
    }
    
    protected DocumentReference getConnectionDocument(String connectionId) {
        return getConnectionsCollection().document(connectionId);
    }
    
    // Admin dashboard
    protected DocumentReference getDashboardDocument() {
        return db.collection(ADMIN).document(DASHBOARD);
    }
    
    // Generic CRUD operations
    protected Task<DocumentSnapshot> getDocument(DocumentReference docRef) {
        return docRef.get();
    }
    
    protected Task<QuerySnapshot> getCollection(CollectionReference collectionRef) {
        return collectionRef.get();
    }
    
    protected Task<Void> setDocument(DocumentReference docRef, Map<String, Object> data) {
        return docRef.set(data);
    }
    
    protected Task<Void> updateDocument(DocumentReference docRef, Map<String, Object> updates) {
        return docRef.update(updates);
    }
    
    protected Task<Void> updateDocument(DocumentReference docRef, String field, Object value) {
        return docRef.update(field, value);
    }
    
    protected Task<Void> deleteDocument(DocumentReference docRef) {
        return docRef.delete();
    }
    
    protected Query queryByField(CollectionReference collectionRef, String field, Object value) {
        return collectionRef.whereEqualTo(field, value);
    }
    
    protected WriteBatch createBatch() {
        return db.batch();
    }
}
