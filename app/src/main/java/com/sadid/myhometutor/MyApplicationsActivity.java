package com.sadid.myhometutor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MyApplicationsActivity extends AppCompatActivity {

    private RecyclerView rvMyApplications;
    private MyApplicationAdapter adapter;
    private List<TuitionApplication> applicationList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Button btnRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_applications);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupRecyclerView();
        setupListeners();
        loadMyApplications();
    }

    private void initializeViews() {
        rvMyApplications = findViewById(R.id.rvMyApplications);
        btnRefresh = findViewById(R.id.btnRefresh);
    }

    private void setupRecyclerView() {
        rvMyApplications.setLayoutManager(new LinearLayoutManager(this));
        applicationList = new ArrayList<>();
        adapter = new MyApplicationAdapter(applicationList, this::viewStudentProfile);
        rvMyApplications.setAdapter(adapter);
    }

    private void setupListeners() {
        btnRefresh.setOnClickListener(v -> loadMyApplications());
    }

    private void loadMyApplications() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("applications")
                .whereEqualTo("tutorId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    applicationList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        TuitionApplication application = document.toObject(TuitionApplication.class);
                        if (application != null) {
                            application.setApplicationId(document.getId());
                            fetchPostDetails(application);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MyApplicationsActivity.this, "Error loading applications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchPostDetails(TuitionApplication application) {
        db.collection("tuition_posts").document(application.getPostId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    TuitionPost post = documentSnapshot.toObject(TuitionPost.class);
                    if (post != null) {
                        post.setId(documentSnapshot.getId());
                        application.setTuitionPost(post);
                        applicationList.add(application);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void viewStudentProfile(TuitionApplication application) {
        if (application.getStudentId() != null) {
            // Tutors view student profile based on application status
            // If approved (connected), show contact info
            // If pending/rejected, show limited profile
            // NEVER show verification document to tutors
            String viewMode = ProfileViewMode.VIEW_MODE_PUBLIC;
            
            // Check if this application is approved (connection established)
            if ("approved".equals(application.getStatus())) {
                viewMode = ProfileViewMode.VIEW_MODE_CONNECTED;
            }
            
            // Use ViewStudentProfileActivity for role-based secure viewing
            Intent intent = new Intent(MyApplicationsActivity.this, ViewStudentProfileActivity.class);
            intent.putExtra(ProfileViewMode.EXTRA_USER_ID, application.getStudentId());
            intent.putExtra(ProfileViewMode.EXTRA_VIEW_MODE, viewMode);
            intent.putExtra(ProfileViewMode.EXTRA_CURRENT_USER_ID, mAuth.getCurrentUser().getUid());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Student information not available", Toast.LENGTH_SHORT).show();
        }
    }
}
