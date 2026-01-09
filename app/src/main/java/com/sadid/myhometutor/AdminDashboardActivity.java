package com.sadid.myhometutor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvTotalPosts, tvPendingPosts, tvApprovedPosts, tvTotalConnections;
    private TextView tvTotalStudents, tvPendingStudents, tvTotalTutors, tvPendingTutors;
    private TextView menuDashboard, menuConnections, menuTutors, menuStudents, menuTuitionPosts;
    private TextView menuReports, menuBannedUsers, menuLogout;
    private CardView cardTotalTutors;
    private LinearLayout llSideMenu;
    private ImageView btnMenu;
    private boolean isMenuVisible = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard_new);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupMenuListeners();
        loadDashboardStatistics();
    }

    private void initializeViews() {
        // Statistics TextViews
        tvTotalPosts = findViewById(R.id.tvTotalPosts);
        tvPendingPosts = findViewById(R.id.tvPendingPosts);
        tvApprovedPosts = findViewById(R.id.tvApprovedPosts);
        tvTotalConnections = findViewById(R.id.tvTotalConnections);
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvPendingStudents = findViewById(R.id.tvPendingStudents);
        tvTotalTutors = findViewById(R.id.tvTotalTutors);
        tvPendingTutors = findViewById(R.id.tvPendingTutors);

        // Menu items
        menuDashboard = findViewById(R.id.menuDashboard);
        menuConnections = findViewById(R.id.menuConnections);
        menuTutors = findViewById(R.id.menuTutors);
        menuStudents = findViewById(R.id.menuStudents);
        menuTuitionPosts = findViewById(R.id.menuTuitionPosts);
        menuReports = findViewById(R.id.menuReports);
        menuBannedUsers = findViewById(R.id.menuBannedUsers);
        menuLogout = findViewById(R.id.menuLogout);
        
        cardTotalTutors = findViewById(R.id.cardTotalTutors);
        llSideMenu = findViewById(R.id.llSideMenu);
        btnMenu = findViewById(R.id.btnMenu);
        
        // Setup menu button click listener
        btnMenu.setOnClickListener(v -> toggleMenu());
    }

    private void setupMenuListeners() {
        menuDashboard.setOnClickListener(v -> {
            // Already on dashboard, just reload stats and close menu
            if (isMenuVisible) {
                toggleMenu();
            }
            loadDashboardStatistics();
        });

        menuStudents.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminStudentsActivity.class);
            startActivity(intent);
        });

        menuTutors.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminTutorsActivity.class);
            startActivity(intent);
        });

        menuTuitionPosts.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminTuitionPostsActivity.class);
            startActivity(intent);
        });

        menuConnections.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminConnectionsActivity.class);
            startActivity(intent);
        });

        menuReports.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminReportsActivity.class);
            startActivity(intent);
        });

        menuBannedUsers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminBannedUsersActivity.class);
            startActivity(intent);
        });

        menuLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadDashboardStatistics() {
        // Load total and pending students
        loadStudentStatistics();

        // Load total and pending tutors
        loadTutorStatistics();

        // Load tuition posts statistics
        loadTuitionPostStatistics();

        // Load connections (successful applications)
        loadConnectionsStatistics();
    }

    private void loadStudentStatistics() {
        db.collection("users")
                .whereEqualTo("userType", "Student")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int total = 0;
                    int pending = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        total++;
                        String status = document.getString("approvalStatus");
                        if ("pending".equals(status)) {
                            pending++;
                        }
                    }

                    tvTotalStudents.setText(String.valueOf(total));
                    tvPendingStudents.setText(String.valueOf(pending));
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error loading student statistics", Toast.LENGTH_SHORT).show()
                );
    }

    private void loadTutorStatistics() {
        db.collection("users")
                .whereEqualTo("userType", "Tutor")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int total = 0;
                    int pending = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        total++;
                        String status = document.getString("approvalStatus");
                        if ("pending".equals(status)) {
                            pending++;
                        }
                    }

                    tvTotalTutors.setText(String.valueOf(total));
                    tvPendingTutors.setText(String.valueOf(pending));
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error loading tutor statistics", Toast.LENGTH_SHORT).show()
                );
    }

    private void loadTuitionPostStatistics() {
        db.collection("tuition_posts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int total = queryDocumentSnapshots.size();
                    int pending = 0;
                    int approved = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String status = document.getString("status");
                        if ("pending".equals(status)) {
                            pending++;
                        } else if ("active".equals(status) || "approved".equals(status)) {
                            approved++;
                        }
                    }

                    tvTotalPosts.setText(String.valueOf(total));
                    tvPendingPosts.setText(String.valueOf(pending));
                    tvApprovedPosts.setText(String.valueOf(approved));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading tuition statistics", Toast.LENGTH_SHORT).show();
                    // Set defaults
                    tvTotalPosts.setText("0");
                    tvPendingPosts.setText("0");
                    tvApprovedPosts.setText("0");
                });
    }

    private void loadConnectionsStatistics() {
        db.collection("applications")
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int connections = queryDocumentSnapshots.size();
                    tvTotalConnections.setText(String.valueOf(connections));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading connections statistics", Toast.LENGTH_SHORT).show();
                    tvTotalConnections.setText("0");
                });
    }

    private void toggleMenu() {
        if (isMenuVisible) {
            llSideMenu.setVisibility(View.GONE);
            isMenuVisible = false;
        } else {
            llSideMenu.setVisibility(View.VISIBLE);
            isMenuVisible = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload statistics when returning to this activity
        loadDashboardStatistics();
    }
}
