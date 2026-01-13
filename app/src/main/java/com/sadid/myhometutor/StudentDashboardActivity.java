package com.sadid.myhometutor;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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
import com.sadid.myhometutor.repository.NotificationRepository;
import com.sadid.myhometutor.utils.Base64ImageHelper;
import com.google.firebase.firestore.ListenerRegistration;

public class StudentDashboardActivity extends AppCompatActivity {

    private TextView tvName, tvInstitute, tvClass, tvGroup, tvDivision, tvDistrict, tvArea, tvGender, tvEmail, tvPhone, tvAbout;
    private ImageView btnMenu, ivProfile;
    private View btnNotification, viewNotificationBadge;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration notificationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        loadUserInfo();
        setupMenu();
        setupNotificationListener();
    }

    private void initializeViews() {
        btnNotification = findViewById(R.id.btnNotification);
        viewNotificationBadge = findViewById(R.id.viewNotificationBadge);
        btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));

        tvName = findViewById(R.id.tvName);
        tvInstitute = findViewById(R.id.tvInstitute);
        tvClass = findViewById(R.id.tvClass);
        tvGroup = findViewById(R.id.tvGroup);
        tvDivision = findViewById(R.id.tvDivision);
        tvDistrict = findViewById(R.id.tvDistrict);
        tvArea = findViewById(R.id.tvArea);
        tvGender = findViewById(R.id.tvGender);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAbout = findViewById(R.id.tvAbout);
        btnMenu = findViewById(R.id.btnMenu);
        ivProfile = findViewById(R.id.ivProfile);
    }

    private void setupMenu() {
        if (btnMenu != null) {
            btnMenu.setOnClickListener(this::showPopupMenu);
        } else {
            Toast.makeText(this, "Menu button not found in layout", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.student_dashboard_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        popup.show();
    }

    private boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_post_tuition) {
            startActivity(new Intent(this, PostTuitionActivity.class));
            return true;
        } else if (id == R.id.action_my_posts) {
            startActivity(new Intent(this, MyPostsActivity.class));
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

        // 1. Delete tuition posts
        db.collection("tuition_posts").whereEqualTo("studentId", userId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        db.collection("tuition_posts").document(document.getId()).delete();
                    }
                    // 2. Delete user data
                    db.collection("users").document(userId).delete()
                            .addOnSuccessListener(aVoid -> {
                                // 3. Delete authentication user
                                user.delete().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(StudentDashboardActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(StudentDashboardActivity.this, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(StudentDashboardActivity.this, "Failed to delete account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            })
                            .addOnFailureListener(e -> Toast.makeText(StudentDashboardActivity.this, "Failed to delete user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(StudentDashboardActivity.this, "Failed to delete posts: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
                            if (tvInstitute != null) tvInstitute.setText(document.getString("institute") != null ? document.getString("institute") : "-");
                            if (tvClass != null) tvClass.setText(document.getString("class") != null ? document.getString("class") : "-");
                            if (tvGroup != null) tvGroup.setText(document.getString("group") != null ? document.getString("group") : "-");
                            if (tvDivision != null) tvDivision.setText(document.getString("division") != null ? document.getString("division") : "-");
                            if (tvDistrict != null) tvDistrict.setText(document.getString("district") != null ? document.getString("district") : "-");
                            if (tvArea != null) tvArea.setText(document.getString("area") != null ? document.getString("area") : "-");
                            if (tvGender != null) tvGender.setText(document.getString("gender") != null ? document.getString("gender") : "-");
                            if (tvEmail != null) tvEmail.setText(document.getString("email") != null ? document.getString("email") : "-");
                            if (tvPhone != null) tvPhone.setText(document.getString("phone") != null ? document.getString("phone") : "-");
                            if (tvAbout != null) tvAbout.setText(document.getString("about") != null ? document.getString("about") : "-");

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

    private void setupNotificationListener() {
        if (mAuth.getCurrentUser() != null) {
            notificationListener = new NotificationRepository().getUnreadCountQuery(mAuth.getCurrentUser().getUid())
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) return;
                        if (viewNotificationBadge != null) {
                            if (snapshots != null && !snapshots.isEmpty()) {
                                viewNotificationBadge.setVisibility(View.VISIBLE);
                            } else {
                                viewNotificationBadge.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationListener != null) {
            notificationListener.remove();
        }
    }
}
