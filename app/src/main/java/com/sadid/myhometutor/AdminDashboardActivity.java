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
import com.sadid.myhometutor.models.DashboardStats;
import com.sadid.myhometutor.repository.AdminStatsRepository;

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
    private AdminStatsRepository statsRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard_new);

        mAuth = FirebaseAuth.getInstance();
        statsRepository = new AdminStatsRepository();

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
        statsRepository.listenToDashboardStats(new AdminStatsRepository.StatsListener() {
            @Override
            public void onStatsUpdated(DashboardStats stats) {
                updateDashboardUI(stats);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminDashboardActivity.this, 
                    "Error loading dashboard: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                setDefaultValues();
            }
        });
    }

    /**
     * Update UI with dashboard statistics
     */
    private void updateDashboardUI(DashboardStats stats) {
        // Student stats
        tvTotalStudents.setText(String.valueOf(stats.getTotalStudents()));
        tvPendingStudents.setText(String.valueOf(stats.getTotalStudents() - stats.getApprovedStudents()));

        // Tutor stats
        tvTotalTutors.setText(String.valueOf(stats.getTotalTutors()));
        tvPendingTutors.setText(String.valueOf(stats.getTotalTutors() - stats.getApprovedTutors()));

        // Post stats
        tvTotalPosts.setText(String.valueOf(stats.getTotalPosts()));
        tvPendingPosts.setText(String.valueOf(stats.getPendingPosts()));
        tvApprovedPosts.setText(String.valueOf(stats.getActivePosts()));

        // Connection stats (using accepted applications as successful connections)
        tvTotalConnections.setText(String.valueOf(stats.getAcceptedApplications()));
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
            // Slide out (hide menu)
            llSideMenu.animate()
                .translationX(-llSideMenu.getWidth())
                .setDuration(300)
                .withEndAction(() -> isMenuVisible = false)
                .start();
        } else {
            // Slide in (show menu)
            llSideMenu.animate()
                .translationX(0)
                .setDuration(300)
                .withStartAction(() -> isMenuVisible = true)
                .start();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Listener already attached in onCreate
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove listeners when activity stops
        if (statsRepository != null) {
            statsRepository.removeListeners();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Safety cleanup
        if (statsRepository != null) {
            statsRepository.removeListeners();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reattach listener if needed
        if (statsRepository != null) {
            setupRealtimeDashboard();
        }
    }
}
