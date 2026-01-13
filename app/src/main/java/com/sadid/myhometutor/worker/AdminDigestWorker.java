package com.sadid.myhometutor.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.sadid.myhometutor.repository.EmailNotificationService;

import java.util.concurrent.ExecutionException;

public class AdminDigestWorker extends Worker {

    private static final String TAG = "AdminDigestWorker";

    public AdminDigestWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting Admin Digest Work");

        String adminEmail = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;

        if (adminEmail == null) {
            Log.e(TAG, "No logged in admin found");
            return Result.failure();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        EmailNotificationService emailService = new EmailNotificationService();

        try {
            // Count Pending Users (Assuming there is a field 'status' or checking waiting approval)
            // Adjust queries based on your actual database structure
            // Using Tasks.await to run synchronously in Worker thread
            
            // 1. Pending Students
            QuerySnapshot pendingStudents = Tasks.await(db.collection("Users")
                    .whereEqualTo("userType", "Student")
                    .whereEqualTo("status", "Pending") 
                    .get());
            
            // 2. Pending Tutors
            QuerySnapshot pendingTutors = Tasks.await(db.collection("Users")
                    .whereEqualTo("userType", "Tutor")
                    .whereEqualTo("status", "Pending")
                    .get());

            int newRegistrations = pendingStudents.size() + pendingTutors.size();

            // 3. Pending Posts
            QuerySnapshot pendingPosts = Tasks.await(db.collection("TuitionPosts")
                    .whereEqualTo("status", "Pending")
                    .get());
            
            int newPosts = pendingPosts.size();

            // 4. Pending Connections (Tutor applications to posts)
            // This might differ based on schema. Assuming 'Connections' collection has status.
            QuerySnapshot pendingConnections = Tasks.await(db.collection("Connections")
                    //.whereEqualTo("status", "Pending") // Or just recent ones
                    // If we just want "Connection Apps" that are pending admin approval?
                    // Usually connection is Student <-> Tutor.
                    // If Admin needs to approve, query that.
                    // For now, let's assume 'Connections' count is useful.
                    .get());
            
            // Filter connections if needed, for now using size for checking activity
            // If the requirement is "New Connections", we need a timestamp check, but "Pending" is actionable.
            // Let's assume we want to report "Waitlist".
            
            int newConnections = 0; // Placeholder if no explicit "Pending Admin Approval" status for connections exists
            
            // Send Email
            if (newRegistrations > 0 || newPosts > 0 || newConnections > 0) {
                 emailService.sendAdminDigestNotification(adminEmail, newRegistrations, newPosts, newConnections);
                 Log.d(TAG, "Digest email sent");
            } else {
                Log.d(TAG, "No new activity to report");
            }

            return Result.success();

        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error fetching data", e);
            return Result.retry();
        }
    }
}
