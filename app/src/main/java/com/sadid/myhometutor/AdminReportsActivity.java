package com.sadid.myhometutor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sadid.myhometutor.adapters.AdminReportsAdapter;
import com.sadid.myhometutor.models.Report;
import com.sadid.myhometutor.repository.ReportRepository;

import java.util.List;

public class AdminReportsActivity extends AppCompatActivity implements AdminReportsAdapter.OnReportActionListener {

    private ImageView btnBack;
    private RecyclerView rvReports;
    private AdminReportsAdapter adapter;
    private ReportRepository reportRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        reportRepository = new ReportRepository();

        initializeViews();
        setupRecyclerView();
        loadReports();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        rvReports = findViewById(R.id.rvReports);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AdminReportsAdapter(this);
        adapter.setOnReportActionListener(this);
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        rvReports.setAdapter(adapter);
    }

    private void loadReports() {
        reportRepository.listenToReports(new ReportRepository.ReportsListener() {
            @Override
            public void onReportsUpdated(List<Report> reports) {
                adapter.setReports(reports);
                if (reports.isEmpty()) {
                    Toast.makeText(AdminReportsActivity.this, 
                        "No reports found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminReportsActivity.this, 
                    "Error loading reports: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResolveClick(Report report) {
        new AlertDialog.Builder(this)
            .setTitle("Resolve Report")
            .setMessage("Mark this report as resolved?")
            .setPositiveButton("Yes", (dialog, which) -> {
                reportRepository.resolveReport(report.getReportId())
                    .addOnSuccessListener(aVoid -> 
                        Toast.makeText(this, "Report resolved", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> 
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            })
            .setNegativeButton("No", null)
            .show();
    }

    @Override
    public void onDeleteClick(Report report) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Report")
            .setMessage("Are you sure you want to delete this report?")
            .setPositiveButton("Yes", (dialog, which) -> {
                reportRepository.deleteReport(report.getReportId())
                    .addOnSuccessListener(aVoid -> 
                        Toast.makeText(this, "Report deleted", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> 
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            })
            .setNegativeButton("No", null)
            .show();
    }

    @Override
    public void onViewReportedUserClick(String userId) {
        Intent intent = new Intent(this, AdminViewUserActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reportRepository != null) {
            reportRepository.removeListener();
        }
    }
}
