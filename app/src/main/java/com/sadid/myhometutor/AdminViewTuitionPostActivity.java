package com.sadid.myhometutor;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class AdminViewTuitionPostActivity extends AppCompatActivity {

    private TextView tvSubject, tvStudentName, tvMedium, tvClass, tvDaysPerWeek, tvSalary, tvLocation, tvStatus;
    private Button btnApprove, btnReject;
    private ImageView btnBack;
    private FirebaseFirestore db;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_tuition_post);

        db = FirebaseFirestore.getInstance();
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
        tvDaysPerWeek = findViewById(R.id.tvDaysPerWeek);
        tvSalary = findViewById(R.id.tvSalary);
        tvLocation = findViewById(R.id.tvLocation);
        tvStatus = findViewById(R.id.tvStatus);
        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);

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
                        tvSubject.setText(documentSnapshot.getString("subject"));
                        tvMedium.setText(documentSnapshot.getString("medium"));
                        tvClass.setText(documentSnapshot.getString("class"));
                        tvDaysPerWeek.setText(documentSnapshot.getString("daysPerWeek"));
                        tvSalary.setText(documentSnapshot.getString("salary"));
                        tvLocation.setText(documentSnapshot.getString("location"));

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
                        String studentId = documentSnapshot.getString("studentId");
                        if (studentId != null) {
                            loadStudentName(studentId);
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
    }

    private void updatePostStatus(String newStatus) {
        db.collection("tuition_posts").document(postId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Post " + newStatus, Toast.LENGTH_SHORT).show();
                    loadPostDetails(); // Reload to update UI
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error updating status", Toast.LENGTH_SHORT).show()
                );
    }
}
