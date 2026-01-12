package com.sadid.myhometutor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.sadid.myhometutor.utils.DataMigrationUtil;

/**
 * Data Migration Activity
 * Run this ONCE to migrate data from old structure to new structure
 * 
 * WARNING: This should only be run by admin in production ONCE
 */
public class DataMigrationActivity extends AppCompatActivity {
    
    private TextView tvMigrationStatus;
    private ProgressBar progressBar;
    private Button btnStartMigration, btnClose;
    private DataMigrationUtil migrationUtil;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_migration);
        
        migrationUtil = new DataMigrationUtil();
        
        initializeViews();
        setupListeners();
    }
    
    private void initializeViews() {
        tvMigrationStatus = findViewById(R.id.tvMigrationStatus);
        progressBar = findViewById(R.id.progressBar);
        btnStartMigration = findViewById(R.id.btnStartMigration);
        btnClose = findViewById(R.id.btnClose);
        
        progressBar.setVisibility(ProgressBar.GONE);
    }
    
    private void setupListeners() {
        btnStartMigration.setOnClickListener(v -> showConfirmationDialog());
        
        btnClose.setOnClickListener(v -> finish());
    }
    
    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Confirm Migration")
            .setMessage("⚠️ WARNING ⚠️\n\n" +
                "This will migrate all data from the old Firebase structure to the new structure.\n\n" +
                "This operation should only be run ONCE.\n\n" +
                "Make sure you have backed up your Firestore database before proceeding.\n\n" +
                "Continue?")
            .setPositiveButton("Yes, Migrate", (dialog, which) -> startMigration())
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }
    
    private void startMigration() {
        btnStartMigration.setEnabled(false);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        tvMigrationStatus.setText("Starting migration...\n");
        
        migrationUtil.runMigration(new DataMigrationUtil.MigrationCallback() {
            @Override
            public void onProgress(String message) {
                runOnUiThread(() -> {
                    String currentText = tvMigrationStatus.getText().toString();
                    tvMigrationStatus.setText(currentText + "\n" + message);
                });
            }
            
            @Override
            public void onComplete(DataMigrationUtil.MigrationResult result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    btnStartMigration.setEnabled(true);
                    
                    String currentText = tvMigrationStatus.getText().toString();
                    tvMigrationStatus.setText(currentText + "\n\n✅ " + result.toString());
                    
                    Toast.makeText(DataMigrationActivity.this, 
                        "Migration completed successfully!", Toast.LENGTH_LONG).show();
                });
            }
            
            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    btnStartMigration.setEnabled(true);
                    
                    String currentText = tvMigrationStatus.getText().toString();
                    tvMigrationStatus.setText(currentText + "\n\n❌ ERROR: " + e.getMessage());
                    
                    Toast.makeText(DataMigrationActivity.this, 
                        "Migration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
