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
import com.google.firebase.firestore.ListenerRegistration;
import com.sadid.myhometutor.repository.AdminDashboardRepository;

import java.util.Map;

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
    private AdminDashboardRepository dashboardRepo;
    private ListenerRegistration dashboardListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard_new);

        mAuth = FirebaseAuth.getInstance();
        dashboardRepo = new AdminDashboardRepository();

        initializeViews();
        setupMenuListeners();
        setupRealtimeDashboard();
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
            // Already on dashboard, just close menu
            if (isMenuVisible) {
                toggleMenu();
            }
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

    /**
     * Setup real-time dashboard with snapshot listener
     * Dashboard updates automatically when data changes
     */
    private void setupRealtimeDashboard() {
        dashboardListener = dashboardRepo.listenToDashboard(new AdminDashboardRepository.DashboardListener() {
            @Override
            public void onDashboardUpdated(Map<String, Object> dashboardData) {
                updateDashboardUI(dashboardData);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminDashboardActivity.this, 
                    "Error loading dashboard: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                // Set all to 0 on error
                setDefaultValues();
            }
        });
    }

    /**
     * Update UI with dashboard statistics
     */
    private void updateDashboardUI(Map<String, Object> dashboardData) {
        // Student stats
        tvTotalStudents.setText(String.valueOf(getIntValue(dashboardData, "totalStudents")));
        tvPendingStudents.setText(String.valueOf(getIntValue(dashboardData, "pendingStudents")));

        // Tutor stats
        tvTotalTutors.setText(String.valueOf(getIntValue(dashboardData, "totalTutors")));
        tvPendingTutors.setText(String.valueOf(getIntValue(dashboardData, "pendingTutors")));

        // Post stats
        tvTotalPosts.setText(String.valueOf(getIntValue(dashboardData, "totalPosts")));
        tvPendingPosts.setText(String.valueOf(getIntValue(dashboardData, "pendingPosts")));
        tvApprovedPosts.setText(String.valueOf(getIntValue(dashboardData, "approvedPosts")));

        // Connection stats
        tvTotalConnections.setText(String.valueOf(getIntValue(dashboardData, "successfulConnections")));
    }
    
    /**
     * Safely get integer value from map
     */
    private int getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Integer) {
            return (Integer) value;
        }
        return 0;
    }

    /**
     * Set default values (0) for all statistics
     */
    private void setDefaultValues() {
        tvTotalStudents.setText("0");
        tvPendingStudents.setText("0");
        tvTotalTutors.setText("0");
        tvPendingTutors.setText("0");
        tvTotalPosts.setText("0");
        tvPendingPosts.setText("0");
        tvApprovedPosts.setText("0");
        tvTotalConnections.setText("0");
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
    protected void onStart() {
        super.onStart();
        // Listener already attached in onCreate via setupRealtimeDashboard()
        // No need to reattach here since it persists across resume/pause
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove listener when activity stops
        if (dashboardListener != null) {
            dashboardListener.remove();
            dashboardListener = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listener to prevent memory leaks (safety net)
        if (dashboardListener != null) {
            dashboardListener.remove();
            dashboardListener = null;
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reattach listener if it was removed in onStop
        if (dashboardListener == null) {
            setupRealtimeDashboard();
        }
    }
}
