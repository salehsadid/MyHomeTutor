package com.sadid.myhometutor;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sadid.myhometutor.utils.Base64ImageHelper;
import com.sadid.myhometutor.utils.EmailSender;
import com.sadid.myhometutor.repository.EmailNotificationService;

public class AdminViewUserActivity extends AppCompatActivity {

    private ImageView ivProfileImage, ivDocumentImage;
    private TextView tvUserName, tvUserTypeLabel, tvInstitute, tvClass, tvGroup;
    private TextView tvGender, tvEmail, tvPhone, tvDivision, tvDistrict, tvArea;
    private TextView tvAdditionalInfo;
    private Button btnApprove, btnReject, btnClose, btnBanUser;

    private FirebaseFirestore db;
    private String userId;
    private String userType;
    private String userEmail;
    private String userName;
    private boolean isBanned = false;
    private EmailNotificationService emailService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_user);

        db = FirebaseFirestore.getInstance();
        emailService = new EmailNotificationService();

        // Get user ID from intent
        userId = getIntent().getStringExtra("userId");
        userType = getIntent().getStringExtra("userType");

        if (userId == null) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupListeners();
        loadUserData();
    }

    private void initializeViews() {
        ivProfileImage = findViewById(R.id.ivProfileImage);
        ivDocumentImage = findViewById(R.id.ivDocumentImage);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserTypeLabel = findViewById(R.id.tvUserTypeLabel);
        tvInstitute = findViewById(R.id.tvInstitute);
        tvClass = findViewById(R.id.tvClass);
        tvGroup = findViewById(R.id.tvGroup);
        tvGender = findViewById(R.id.tvGender);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvDivision = findViewById(R.id.tvDivision);
        tvDistrict = findViewById(R.id.tvDistrict);
        tvArea = findViewById(R.id.tvArea);
        tvAdditionalInfo = findViewById(R.id.tvAdditionalInfo);
        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);
        btnClose = findViewById(R.id.btnClose);
        btnBanUser = findViewById(R.id.btnBanUser);
    }

    private void setupListeners() {
        btnApprove.setOnClickListener(v -> approveUser());
        btnReject.setOnClickListener(v -> rejectUser());
        btnClose.setOnClickListener(v -> finish());
        btnBanUser.setOnClickListener(v -> toggleBanStatus());
    }

    private void loadUserData() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        displayUserData(document);
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading user data: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayUserData(DocumentSnapshot document) {
        String name = document.getString("name");
        userEmail = document.getString("email");
        String phone = document.getString("phone");
        String gender = document.getString("gender");
        String division = document.getString("division");
        String district = document.getString("district");
        String area = document.getString("area");
        String status = document.getString("approvalStatus");
        
        // Check if user is banned
        Boolean bannedStatus = document.getBoolean("isBanned");
        isBanned = bannedStatus != null && bannedStatus;

        tvUserName.setText(name != null ? name.toUpperCase() : "N/A");
        tvUserTypeLabel.setText(userType);
        tvEmail.setText(userEmail != null ? userEmail : "N/A");
        tvPhone.setText(phone != null ? phone : "N/A");
        tvGender.setText(gender != null ? gender : "N/A");
        tvDivision.setText(division != null ? division : "N/A");
        tvDistrict.setText(district != null ? district : "N/A");
        tvArea.setText(area != null ? area : "N/A");

        // Type-specific fields
        if ("Student".equals(userType)) {
            String institute = document.getString("institute");
            String studentClass = document.getString("class");
            String group = document.getString("group");
            String about = document.getString("about");

            tvInstitute.setText(institute != null ? institute : "N/A");
            tvClass.setText(studentClass != null ? studentClass : "N/A");
            tvGroup.setText(group != null ? group : "N/A");
            tvAdditionalInfo.setText(about != null ? about : "No additional information provided.");
        } else if ("Tutor".equals(userType)) {
            String university = document.getString("universityName");
            String department = document.getString("department");
            String experience = document.getString("experience");
            String about = document.getString("about");

            tvInstitute.setText(university != null ? university : "N/A");
            tvClass.setText(department != null ? department : "N/A");
            tvGroup.setText(experience != null ? experience + " years" : "N/A");
            tvAdditionalInfo.setText(about != null ? about : "No additional information provided.");
        }

        // Load profile image
        String profileImageBase64 = document.getString("profileImageBase64");
        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
            Bitmap profileBitmap = Base64ImageHelper.convertBase64ToBitmap(profileImageBase64);
            if (profileBitmap != null) {
                ivProfileImage.setImageBitmap(profileBitmap);
            }
        }

        // Load document image
        String documentImageBase64 = document.getString("documentImageBase64");
        if (documentImageBase64 != null && !documentImageBase64.isEmpty()) {
            Bitmap documentBitmap = Base64ImageHelper.convertBase64ToBitmap(documentImageBase64);
            if (documentBitmap != null) {
                ivDocumentImage.setImageBitmap(documentBitmap);
            }
        }

        // Update button visibility based on approval status
        if ("approved".equals(status)) {
            btnApprove.setVisibility(View.GONE);
            btnReject.setVisibility(View.VISIBLE);
            btnReject.setText("Revoke Approval");
        } else if ("rejected".equals(status)) {
            btnApprove.setVisibility(View.VISIBLE);
            btnReject.setVisibility(View.GONE);
        } else {
            btnApprove.setVisibility(View.VISIBLE);
            btnReject.setVisibility(View.VISIBLE);
        }
        
        // Update ban button text based on ban status
        if (isBanned) {
            btnBanUser.setText("Unban User");
            btnBanUser.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            btnBanUser.setText("Ban User");
            btnBanUser.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void approveUser() {
        db.collection("users").document(userId)
                .update("approvalStatus", "approved")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User approved successfully", Toast.LENGTH_SHORT).show();
                    // Send approval email using new service
                    if (userEmail != null && !userEmail.isEmpty()) {
                        emailService.sendAccountApprovedNotification(userEmail, userType);
                    }
                    finish();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error approving user: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show()
                );
    }

    private void rejectUser() {
        db.collection("users").document(userId)
                .update("approvalStatus", "rejected")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User rejected", Toast.LENGTH_SHORT).show();
                    // Send rejection email using new service
                    if (userEmail != null && !userEmail.isEmpty()) {
                        emailService.sendAccountRejectedNotification(userEmail, userType);
                    }
                    finish();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error rejecting user: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show()
                );
    }

    private void sendApprovalEmail() {
        if (userEmail == null || userEmail.isEmpty()) {
            return;
        }

        new Thread(() -> {
            try {
                String subject = "MyHomeTutor - Account Approved";
                String body = "Dear User,\n\n" +
                        "Congratulations! Your MyHomeTutor account has been approved by our admin team.\n\n" +
                        "You can now log in to your account and start using all the features of MyHomeTutor.\n\n" +
                        "Thank you for choosing MyHomeTutor!\n\n" +
                        "Best regards,\n" +
                        "MyHomeTutor Team";

                EmailSender.sendEmail(userEmail, subject, body);
                
                runOnUiThread(() -> 
                    Toast.makeText(this, "Approval email sent to " + userEmail, 
                            Toast.LENGTH_SHORT).show()
                );
            } catch (Exception e) {
                runOnUiThread(() -> 
                    Toast.makeText(this, "Failed to send email: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void sendRejectionEmail() {
        if (userEmail == null || userEmail.isEmpty()) {
            return;
        }

        new Thread(() -> {
            try {
                String subject = "MyHomeTutor - Account Status Update";
                String body = "Dear User,\n\n" +
                        "We regret to inform you that your MyHomeTutor account application has been reviewed and could not be approved at this time.\n\n" +
                        "If you believe this is an error or would like more information, please contact our support team.\n\n" +
                        "Best regards,\n" +
                        "MyHomeTutor Team";

                EmailSender.sendEmail(userEmail, subject, body);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void toggleBanStatus() {
        boolean newBanStatus = !isBanned;
        String reason = newBanStatus ? "Policy violation" : null;
        
        db.collection("users").document(userId)
                .update("isBanned", newBanStatus)
                .addOnSuccessListener(aVoid -> {
                    isBanned = newBanStatus;
                    String message = newBanStatus ? "User banned successfully" : "User unbanned successfully";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    
                    // Send email notification
                    if (userEmail != null && !userEmail.isEmpty() && userName != null) {
                        if (newBanStatus) {
                            emailService.sendAccountBannedNotification(userEmail, userName, reason);
                        } else {
                            emailService.sendAccountUnbannedNotification(userEmail, userName);
                        }
                    }
                    
                    // Update button appearance
                    if (isBanned) {
                        btnBanUser.setText("Unban User");
                        btnBanUser.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                    } else {
                        btnBanUser.setText("Ban User");
                        btnBanUser.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error updating ban status: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show()
                );
    }
}
