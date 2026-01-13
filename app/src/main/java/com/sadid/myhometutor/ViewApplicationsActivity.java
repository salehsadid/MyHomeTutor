package com.sadid.myhometutor;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.sadid.myhometutor.adapters.ApplicationAdapter;
import com.sadid.myhometutor.repository.ApplicationRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewApplicationsActivity - Student reviews tutor applications
 * 
 * Workflow:
 * 1. Tutor applies to post (status: pending)
 * 2. Student reviews applications here
 * 3. Student can Accept (status -> student_approved) or Reject (status -> rejected)
 * 4. Accepted applications go to admin for final approval
 * 
 * PRIVACY: Student can see tutor profile to make decision
 */
public class ViewApplicationsActivity extends AppCompatActivity implements ApplicationAdapter.OnApplicationActionListener {

    private static final String TAG = "ViewApplicationsActivity";

    private RecyclerView rvApplications;
    private TextView tvEmptyState, tvTitle;
    private ImageView btnBack;
    
    private ApplicationAdapter adapter;
    private List<TuitionApplication> applicationList;
    private FirebaseFirestore db;
    private ApplicationRepository applicationRepo;
    private ListenerRegistration applicationsListener;
    
    private String postId;
    private String postSubject;
    private String postGrade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_applications);

        // Get post details from intent
        postId = getIntent().getStringExtra("postId");
        postSubject = getIntent().getStringExtra("postSubject");
        postGrade = getIntent().getStringExtra("postGrade");
        
        if (postId == null) {
            Toast.makeText(this, "Error: Post ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        applicationRepo = new ApplicationRepository();

        initializeViews();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeListener();
    }

    private void initializeViews() {
        rvApplications = findViewById(R.id.rvApplications);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.btnBack);

        // Set title with post info
        String title = "Applications";
        if (postSubject != null && postGrade != null) {
            title = String.format("Applications for %s • %s", postSubject, postGrade);
        }
        tvTitle.setText(title);
        
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        applicationList = new ArrayList<>();
        adapter = new ApplicationAdapter(applicationList, this, true); // true = show actions for student
        rvApplications.setLayoutManager(new LinearLayoutManager(this));
        rvApplications.setAdapter(adapter);
    }

    private void attachListener() {
        Log.d(TAG, "Attaching applications listener for post: " + postId);
        
        applicationsListener = applicationRepo.listenToApplicationsByPost(postId, 
            new ApplicationRepository.ApplicationsListener() {
                @Override
                public void onApplicationsUpdated(com.google.firebase.firestore.QuerySnapshot snapshot) {
                    Log.d(TAG, "Applications updated: " + snapshot.size());
                    applicationList.clear();
                    
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        TuitionApplication application = document.toObject(TuitionApplication.class);
                        if (application != null) {
                            application.setApplicationId(document.getId());
                            // Load tutor details
                            loadTutorDetails(application);
                            applicationList.add(application);
                        }
                    }
                    
                    updateUI();
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error loading applications", e);
                    Toast.makeText(ViewApplicationsActivity.this, 
                        "Error loading applications: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void removeListener() {
        if (applicationsListener != null) {
            applicationsListener.remove();
            applicationsListener = null;
        }
    }

    private void loadTutorDetails(TuitionApplication application) {
        String tutorId = application.getTutorId();
        if (tutorId == null) return;
        
        db.collection("users").document(tutorId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Store tutor name in application for display
                    String tutorName = documentSnapshot.getString("name");
                    if (tutorName == null) {
                        tutorName = documentSnapshot.getString("fullName");
                    }
                    // We'll update the adapter with tutor info
                    adapter.notifyDataSetChanged();
                }
            });
    }

    private void updateUI() {
        if (applicationList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No applications yet");
            rvApplications.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvApplications.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAcceptClick(TuitionApplication application) {
        new AlertDialog.Builder(this)
            .setTitle("Accept Application")
            .setMessage("Accept this tutor's application? It will be sent to admin for final approval.")
            .setPositiveButton("Accept", (dialog, which) -> {
                applicationRepo.studentApproveApplication(application.getApplicationId())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Application accepted! Waiting for admin approval.", 
                            Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onRejectClick(TuitionApplication application) {
        new AlertDialog.Builder(this)
            .setTitle("Reject Application")
            .setMessage("Are you sure you want to reject this application?")
            .setPositiveButton("Reject", (dialog, which) -> {
                applicationRepo.rejectApplication(application.getApplicationId())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Application rejected", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onViewProfileClick(TuitionApplication application) {
        // Student can view tutor profile to make decision
        // Use PUBLIC mode since no connection exists yet
        android.content.Intent intent = new android.content.Intent(this, ViewTutorProfileActivity.class);
        intent.putExtra(ProfileViewMode.EXTRA_USER_ID, application.getTutorId());
        intent.putExtra(ProfileViewMode.EXTRA_VIEW_MODE, ProfileViewMode.VIEW_MODE_PUBLIC);
        intent.putExtra(ProfileViewMode.EXTRA_CURRENT_USER_ID, 
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid());
        startActivity(intent);
    }
}
