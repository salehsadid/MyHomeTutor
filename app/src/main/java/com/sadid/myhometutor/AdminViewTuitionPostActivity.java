package com.sadid.myhometutor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.sadid.myhometutor.repository.NotificationRepository;
import com.sadid.myhometutor.repository.PostRepository;

public class AdminViewTuitionPostActivity extends AppCompatActivity {

    private TextView tvSubject, tvStudentName, tvMedium, tvClass, tvGroup, tvGender, tvType, tvDaysPerWeek, tvTiming, tvSalary, tvLocation, tvStatus;
    private Button btnApprove, btnReject, btnViewProfile;
    private ImageView btnBack;
    private FirebaseFirestore db;
    private String postId;
    private String studentId;
    private PostRepository postRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_tuition_post);

        db = FirebaseFirestore.getInstance();
        postRepo = new PostRepository();
        postId = getIntent().getStringExtra("postId");

        initializeViews();
        loadPostDetails();
        setupListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvSubject = findViewById(R.id.tvSubject);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvMedium = findViewById(R.id.tvMedium);
        tvClass = findViewById(R.id.tvClass);
        tvGroup = findViewById(R.id.tvGroup);
        tvGender = findViewById(R.id.tvGender);
        tvType = findViewById(R.id.tvType);
        tvDaysPerWeek = findViewById(R.id.tvDaysPerWeek);
        tvTiming = findViewById(R.id.tvTiming);
        tvSalary = findViewById(R.id.tvSalary);
        tvLocation = findViewById(R.id.tvLocation);
        tvStatus = findViewById(R.id.tvStatus);
        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);
        btnViewProfile = findViewById(R.id.btnViewProfile);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadPostDetails() {
        if (postId == null) {
            Toast.makeText(this, "Error: Post ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("tuition_posts").document(postId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        tvSubject.setText(documentSnapshot.getString("subject") != null ? documentSnapshot.getString("subject") : "N/A");
                        tvMedium.setText(documentSnapshot.getString("medium") != null ? documentSnapshot.getString("medium") : "N/A");
                        tvClass.setText(documentSnapshot.getString("grade") != null ? documentSnapshot.getString("grade") : "N/A");
                        tvGroup.setText(documentSnapshot.getString("group") != null ? documentSnapshot.getString("group") : "N/A");
                        tvGender.setText(documentSnapshot.getString("preferredGender") != null ? documentSnapshot.getString("preferredGender") : "Any");
                        tvType.setText(documentSnapshot.getString("tuitionType") != null ? documentSnapshot.getString("tuitionType") : "N/A");
                        tvDaysPerWeek.setText(documentSnapshot.getString("daysPerWeek") != null ? documentSnapshot.getString("daysPerWeek") : "N/A");
                        tvTiming.setText(documentSnapshot.getString("preferredTiming") != null ? documentSnapshot.getString("preferredTiming") : "N/A");
                        tvSalary.setText(documentSnapshot.getString("salary") != null ? documentSnapshot.getString("salary") + " BDT" : "0 BDT");
                        
                        String address = documentSnapshot.getString("detailedAddress") != null ? documentSnapshot.getString("detailedAddress") : "N/A";
                        String area = documentSnapshot.getString("area") != null ? documentSnapshot.getString("area") : "";
                        String district = documentSnapshot.getString("district") != null ? documentSnapshot.getString("district") : "";
                        
                        if (area.isEmpty() && district.isEmpty()) {
                            tvLocation.setText(address);
                        } else if (area.isEmpty()) {
                            tvLocation.setText(String.format("%s, %s", address, district));
                        } else if (district.isEmpty()) {
                            tvLocation.setText(String.format("%s, %s", address, area));
                        } else {
                            tvLocation.setText(String.format("%s, %s, %s", address, area, district));
                        }

                        String status = documentSnapshot.getString("status");
                        tvStatus.setText(status != null ? status.toUpperCase() : "UNKNOWN");

                        // Set status color
                        if (status != null) {
                            switch (status.toLowerCase()) {
                                case "pending":
                                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
                                    btnApprove.setVisibility(View.VISIBLE);
                                    btnReject.setVisibility(View.VISIBLE);
                                    break;
                                case "active":
                                case "approved":
                                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                                    btnApprove.setVisibility(View.GONE);
                                    btnReject.setVisibility(View.VISIBLE);
                                    break;
                                case "rejected":
                                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                                    btnApprove.setVisibility(View.VISIBLE);
                                    btnReject.setVisibility(View.GONE);
                                    break;
                            }
                        }

                        // Load student name
                        studentId = documentSnapshot.getString("studentId");
                        if (studentId != null) {
                            loadStudentName(studentId);
                        } else {
                            tvStudentName.setText("Unknown");
                        }
                    } else {
                        Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading post details", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadStudentName(String studentId) {
        db.collection("users").document(studentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        tvStudentName.setText(documentSnapshot.getString("fullName"));
                    }
                });
    }

    private void setupListeners() {
        btnApprove.setOnClickListener(v -> updatePostStatus("approved"));
        btnReject.setOnClickListener(v -> updatePostStatus("rejected"));
        btnViewProfile.setOnClickListener(v -> {
            if (studentId != null) {
                Intent intent = new Intent(AdminViewTuitionPostActivity.this, AdminViewUserActivity.class);
                intent.putExtra("userId", studentId);
                intent.putExtra("userType", "Student");
                startActivity(intent);
            } else {
                Toast.makeText(this, "Student information not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePostStatus(String newStatus) {
        // Use PostRepository since it already includes notification logic
        postRepo.updatePostStatus(postId, newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Post " + newStatus, Toast.LENGTH_SHORT).show();
                    loadPostDetails(); // Reload to update UI
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error updating status", Toast.LENGTH_SHORT).show()
                );
    }
}
