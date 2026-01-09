package com.sadid.myhometutor;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminReportsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTotalUsers, tvTotalTutors, tvTotalStudents;
    private TextView tvTotalPosts, tvActivePosts, tvPendingPosts;
    private TextView tvTotalApplications, tvAcceptedApplications, tvPendingApplications;
    private TextView tvApprovedTutors, tvApprovedStudents, tvPendingApprovals;
    private TextView tvReportDate;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        loadReportData();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalTutors = findViewById(R.id.tvTotalTutors);
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvTotalPosts = findViewById(R.id.tvTotalPosts);
        tvActivePosts = findViewById(R.id.tvActivePosts);
        tvPendingPosts = findViewById(R.id.tvPendingPosts);
        tvTotalApplications = findViewById(R.id.tvTotalApplications);
        tvAcceptedApplications = findViewById(R.id.tvAcceptedApplications);
        tvPendingApplications = findViewById(R.id.tvPendingApplications);
        tvApprovedTutors = findViewById(R.id.tvApprovedTutors);
        tvApprovedStudents = findViewById(R.id.tvApprovedStudents);
        tvPendingApprovals = findViewById(R.id.tvPendingApprovals);
        tvReportDate = findViewById(R.id.tvReportDate);

        btnBack.setOnClickListener(v -> finish());

        // Set current date
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        tvReportDate.setText("Report Generated: " + sdf.format(new Date()));
    }

    private void loadReportData() {
        loadUserStatistics();
        loadTuitionPostStatistics();
        loadApplicationStatistics();
    }

    private void loadUserStatistics() {
        db.collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalUsers = queryDocumentSnapshots.size();
                    int totalTutors = 0;
                    int totalStudents = 0;
                    int approvedTutors = 0;
                    int approvedStudents = 0;
                    int pendingApprovals = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String userType = document.getString("userType");
                        String approvalStatus = document.getString("approvalStatus");

                        if ("Tutor".equals(userType)) {
                            totalTutors++;
                            if ("approved".equals(approvalStatus)) {
                                approvedTutors++;
                            } else if ("pending".equals(approvalStatus)) {
                                pendingApprovals++;
                            }
                        } else if ("Student".equals(userType)) {
                            totalStudents++;
                            if ("approved".equals(approvalStatus)) {
                                approvedStudents++;
                            } else if ("pending".equals(approvalStatus)) {
                                pendingApprovals++;
                            }
                        }
                    }

                    tvTotalUsers.setText(String.valueOf(totalUsers));
                    tvTotalTutors.setText(String.valueOf(totalTutors));
                    tvTotalStudents.setText(String.valueOf(totalStudents));
                    tvApprovedTutors.setText(String.valueOf(approvedTutors));
                    tvApprovedStudents.setText(String.valueOf(approvedStudents));
                    tvPendingApprovals.setText(String.valueOf(pendingApprovals));
                });
    }

    private void loadTuitionPostStatistics() {
        db.collection("tuition_posts").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int total = queryDocumentSnapshots.size();
                    int active = 0;
                    int pending = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String status = document.getString("status");
                        if ("active".equals(status) || "approved".equals(status)) {
                            active++;
                        } else if ("pending".equals(status)) {
                            pending++;
                        }
                    }

                    tvTotalPosts.setText(String.valueOf(total));
                    tvActivePosts.setText(String.valueOf(active));
                    tvPendingPosts.setText(String.valueOf(pending));
                });
    }

    private void loadApplicationStatistics() {
        db.collection("applications").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int total = queryDocumentSnapshots.size();
                    int accepted = 0;
                    int pending = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String status = document.getString("status");
                        if ("accepted".equals(status)) {
                            accepted++;
                        } else if ("pending".equals(status)) {
                            pending++;
                        }
                    }

                    tvTotalApplications.setText(String.valueOf(total));
                    tvAcceptedApplications.setText(String.valueOf(accepted));
                    tvPendingApplications.setText(String.valueOf(pending));
                });
    }
}
