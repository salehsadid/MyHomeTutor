package com.sadid.myhometutor;

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
    private Button btnBack, btnRefresh;

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
        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnRefresh);
    }

    private void setupRecyclerView() {
        rvMyApplications.setLayoutManager(new LinearLayoutManager(this));
        applicationList = new ArrayList<>();
        adapter = new MyApplicationAdapter(applicationList, this::viewStudentProfile);
        rvMyApplications.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
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
        // TODO: Implement View Student Profile Activity
        Toast.makeText(this, "View Student Profile clicked for student: " + application.getStudentId(), Toast.LENGTH_SHORT).show();
    }
}
