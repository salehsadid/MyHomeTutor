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
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.sadid.myhometutor.models.DashboardStats;
import com.sadid.myhometutor.repository.AdminStatsRepository;
import com.sadid.myhometutor.worker.AdminDigestWorker;

import java.util.concurrent.TimeUnit;
import java.util.Calendar;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvTotalPosts, tvPendingPosts, tvApprovedPosts;
    private TextView tvTotalConnections, tvPendingConnections;
    private TextView tvTotalStudents, tvPendingStudents, tvBannedStudents;
    private TextView tvTotalTutors, tvPendingTutors, tvBannedTutors;
    private TextView tvPendingReports, tvSolvedReports;
    private TextView menuDashboard, menuConnections, menuTutors, menuStudents, menuTuitionPosts;
    private TextView menuReports, menuBannedUsers, menuPendingApprovals, menuLogout;
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

        // Schedule Admin Digest (Every 12 Hours - 7:15 AM & 7:15 PM)
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();

        // Target 7:15 AM today
        dueDate.set(Calendar.HOUR_OF_DAY, 7);
        dueDate.set(Calendar.MINUTE, 15);
        dueDate.set(Calendar.SECOND, 0);

        if (dueDate.before(currentDate)) {
            // If 7:15 AM passed, target 7:15 PM (19:15)
            dueDate.add(Calendar.HOUR_OF_DAY, 12);
        }
        
        if (dueDate.before(currentDate)) {
            // If 7:15 PM passed, target 7:15 AM tomorrow
            dueDate.add(Calendar.HOUR_OF_DAY, 12);
        }

        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();

        PeriodicWorkRequest digestRequest = new PeriodicWorkRequest.Builder(
                AdminDigestWorker.class, 12, TimeUnit.HOURS)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .build();
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "AdminDigestWork",
                ExistingPeriodicWorkPolicy.REPLACE,
                digestRequest);
    }

    private void initializeViews() {
        // Statistics TextViews
        tvTotalPosts = findViewById(R.id.tvTotalPosts);
        tvPendingPosts = findViewById(R.id.tvPendingPosts);
        tvApprovedPosts = findViewById(R.id.tvApprovedPosts);
        
        tvTotalConnections = findViewById(R.id.tvTotalConnections);
        tvPendingConnections = findViewById(R.id.tvPendingConnections);
        
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvPendingStudents = findViewById(R.id.tvPendingStudents);
        tvBannedStudents = findViewById(R.id.tvBannedStudents);
        
        tvTotalTutors = findViewById(R.id.tvTotalTutors);
        tvPendingTutors = findViewById(R.id.tvPendingTutors);
        tvBannedTutors = findViewById(R.id.tvBannedTutors);
        
        tvPendingReports = findViewById(R.id.tvPendingReports);
        tvSolvedReports = findViewById(R.id.tvSolvedReports);

        // Menu items
        menuDashboard = findViewById(R.id.menuDashboard);
        menuConnections = findViewById(R.id.menuConnections);
        menuTutors = findViewById(R.id.menuTutors);
        menuStudents = findViewById(R.id.menuStudents);
        menuTuitionPosts = findViewById(R.id.menuTuitionPosts);
        menuReports = findViewById(R.id.menuReports);
        menuBannedUsers = findViewById(R.id.menuBannedUsers);
        menuPendingApprovals = findViewById(R.id.menuPendingApprovals);
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

        menuPendingApprovals.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminApplicationsActivity.class);
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
        tvPendingStudents.setText(String.valueOf(stats.getPendingStudents()));
        tvBannedStudents.setText(String.valueOf(stats.getBannedStudents()));

        // Tutor stats
        tvTotalTutors.setText(String.valueOf(stats.getTotalTutors()));
        tvPendingTutors.setText(String.valueOf(stats.getPendingTutors()));
        tvBannedTutors.setText(String.valueOf(stats.getBannedTutors()));

        // Post stats
        tvTotalPosts.setText(String.valueOf(stats.getTotalPosts()));
        tvPendingPosts.setText(String.valueOf(stats.getPendingPosts()));
        tvApprovedPosts.setText(String.valueOf(stats.getActivePosts()));

        // Connection stats
        tvTotalConnections.setText(String.valueOf(stats.getAcceptedApplications()));
        tvPendingConnections.setText(String.valueOf(stats.getPendingApplications()));

        // Report stats
        tvPendingReports.setText(String.valueOf(stats.getPendingReports()));
        tvSolvedReports.setText(String.valueOf(stats.getSolvedReports()));
    }

    /**
     * Set default values (0) for all statistics
     */
    private void setDefaultValues() {
        tvTotalStudents.setText("0");
        tvPendingStudents.setText("0");
        tvBannedStudents.setText("0");
        
        tvTotalTutors.setText("0");
        tvPendingTutors.setText("0");
        tvBannedTutors.setText("0");
        
        tvTotalPosts.setText("0");
        tvPendingPosts.setText("0");
        tvApprovedPosts.setText("0");
        
        tvTotalConnections.setText("0");
        tvPendingConnections.setText("0");
        
        tvPendingReports.setText("0");
        tvSolvedReports.setText("0");
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
