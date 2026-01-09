package com.sadid.myhometutor;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sadid.myhometutor.utils.Base64ImageHelper;

public class TutorDashboardActivity extends AppCompatActivity {

    private TextView tvName, tvCollegeName, tvCollegeGroupHsc, tvUniversityName, tvDeptYear, tvSession;
    private TextView tvDivision, tvDistrict, tvArea, tvGender, tvEmail, tvPhone;
    private TextView tvExperience, tvPreferredDays, tvPreferredTime, tvPrefLocation, tvPreferredFee, tvAdditionalInfo;
    private ImageView btnMenu, ivProfile;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        loadUserInfo();
        setupListeners();
    }

    private void initializeViews() {
        tvName = findViewById(R.id.tvName);
        tvCollegeName = findViewById(R.id.tvCollegeName);
        tvCollegeGroupHsc = findViewById(R.id.tvCollegeGroupHsc);
        tvUniversityName = findViewById(R.id.tvUniversityName);
        tvDeptYear = findViewById(R.id.tvDeptYear);
        tvSession = findViewById(R.id.tvSession);
        tvDivision = findViewById(R.id.tvDivision);
        tvDistrict = findViewById(R.id.tvDistrict);
        tvArea = findViewById(R.id.tvArea);
        tvGender = findViewById(R.id.tvGender);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvExperience = findViewById(R.id.tvExperience);
        tvPreferredDays = findViewById(R.id.tvPreferredDays);
        tvPreferredTime = findViewById(R.id.tvPreferredTime);
        tvPrefLocation = findViewById(R.id.tvPrefLocation);
        tvPreferredFee = findViewById(R.id.tvPreferredFee);
        tvAdditionalInfo = findViewById(R.id.tvAdditionalInfo);
        btnMenu = findViewById(R.id.btnMenu);
        ivProfile = findViewById(R.id.ivProfile);
    }

    private void setupListeners() {
        if (btnMenu != null) {
            btnMenu.setOnClickListener(this::showPopupMenu);
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.tutor_dashboard_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        popup.show();
    }

    private boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_explore) {
            startActivity(new Intent(this, ExploreTuitionsActivity.class));
            return true;
        } else if (id == R.id.action_my_applications) {
            startActivity(new Intent(this, MyApplicationsActivity.class));
            return true;
        } else if (id == R.id.action_edit_profile) {
            startActivity(new Intent(this, EditProfileActivity.class));
            return true;
        } else if (id == R.id.action_change_password) {
            startActivity(new Intent(this, ChangePasswordActivity.class));
            return true;
        } else if (id == R.id.action_delete_account) {
            showDeleteAccountDialog();
            return true;
        } else if (id == R.id.action_logout) {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone and all your data will be permanently deleted.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String userId = user.getUid();

        // 1. Delete applications
        db.collection("applications").whereEqualTo("tutorId", userId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        db.collection("applications").document(document.getId()).delete();
                    }
                    // 2. Delete user data
                    db.collection("users").document(userId).delete()
                            .addOnSuccessListener(aVoid -> {
                                // 3. Delete authentication user
                                user.delete().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(TutorDashboardActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(TutorDashboardActivity.this, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(TutorDashboardActivity.this, "Failed to delete account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            })
                            .addOnFailureListener(e -> Toast.makeText(TutorDashboardActivity.this, "Failed to delete user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(TutorDashboardActivity.this, "Failed to delete applications: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadUserInfo() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            if (tvName != null) tvName.setText(document.getString("name") != null ? document.getString("name") : "-");

                            // Educational Info
                            if (tvCollegeName != null) tvCollegeName.setText(document.getString("collegeName") != null ? document.getString("collegeName") : "-");
                            String collegeGroup = document.getString("collegeGroup") != null ? document.getString("collegeGroup") : "";
                            String hscYear = document.getString("hscYear") != null ? document.getString("hscYear") : "";
                            String groupHsc = collegeGroup + " | " + hscYear;
                            if (tvCollegeGroupHsc != null) tvCollegeGroupHsc.setText(groupHsc);
                            if (tvUniversityName != null) tvUniversityName.setText(document.getString("universityName") != null ? document.getString("universityName") : "-");
                            String department = document.getString("department") != null ? document.getString("department") : "";
                            String yearSemester = document.getString("yearSemester") != null ? document.getString("yearSemester") : "";
                            String deptYear = department + " | " + yearSemester;
                            if (tvDeptYear != null) tvDeptYear.setText(deptYear);
                            if (tvSession != null) tvSession.setText(document.getString("session") != null ? document.getString("session") : "-");

                            // Location Info
                            if (tvDivision != null) tvDivision.setText(document.getString("division") != null ? document.getString("division") : "-");
                            if (tvDistrict != null) tvDistrict.setText(document.getString("district") != null ? document.getString("district") : "-");
                            if (tvArea != null) tvArea.setText(document.getString("area") != null ? document.getString("area") : "-");

                            // Personal Info
                            if (tvGender != null) tvGender.setText(document.getString("gender") != null ? document.getString("gender") : "-");
                            if (tvEmail != null) tvEmail.setText(document.getString("email") != null ? document.getString("email") : "-");
                            if (tvPhone != null) tvPhone.setText(document.getString("phone") != null ? document.getString("phone") : "-");

                            // Tuition Details
                            if (tvExperience != null) tvExperience.setText(document.getString("experience") != null ? document.getString("experience") : "-");
                            if (tvPreferredDays != null) tvPreferredDays.setText(document.getString("preferredDays") != null ? document.getString("preferredDays") : "-");
                            if (tvPreferredTime != null) tvPreferredTime.setText(document.getString("preferredTime") != null ? document.getString("preferredTime") : "-");
                            if (tvPrefLocation != null) tvPrefLocation.setText(document.getString("locationDetails") != null ? document.getString("locationDetails") : "-");
                            String preferredFee = document.getString("preferredFee");
                            if (tvPreferredFee != null) tvPreferredFee.setText(preferredFee != null ? preferredFee + " BDT" : "- BDT");
                            if (tvAdditionalInfo != null) tvAdditionalInfo.setText(document.getString("about") != null ? document.getString("about") : "-");

                            // Load profile image - support both Base64 and URL
                            String profileImageBase64 = document.getString("profileImageBase64");
                            String profileImageUrl = document.getString("profileImageUrl");
                            
                            if (profileImageBase64 != null && !profileImageBase64.isEmpty() && ivProfile != null) {
                                // Load from Base64
                                Base64ImageHelper.loadBase64IntoImageViewWithGlide(this, ivProfile, profileImageBase64, R.mipmap.ic_launcher);
                            } else if (profileImageUrl != null && !profileImageUrl.isEmpty() && ivProfile != null) {
                                // Fallback to URL for backward compatibility
                                Glide.with(this).load(profileImageUrl).placeholder(R.mipmap.ic_launcher).into(ivProfile);
                            }
                        } else {
                            Toast.makeText(this, "User profile not found. Please complete registration.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to load profile: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
