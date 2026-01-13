package com.sadid.myhometutor;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sadid.myhometutor.repository.ConnectionRepository;
import com.sadid.myhometutor.repository.ReportRepository;
import com.sadid.myhometutor.repository.UserFilterRepository;
import com.sadid.myhometutor.utils.Base64ImageHelper;

/**
 * ViewStudentProfileActivity - Role-based student profile viewing
 * 
 * View Modes:
 * 1. PUBLIC (Tutor viewing before connection)
 *    - Shows: Name, institution, class, group, location
 *    - Hides: Phone, email, verification document
 * 
 * 2. CONNECTED (Tutor viewing after connection/approved application)
 *    - Shows: All public info + phone + email
 *    - Hides: Verification document
 * 
 * 3. ADMIN (Admin viewing)
 *    - Shows: Everything including verification document
 *    - Has: Approve/Reject buttons (handled by AdminViewUserActivity)
 * 
 * SECURITY: Tutors can NEVER see student verification documents
 */
public class ViewStudentProfileActivity extends AppCompatActivity {

    private static final String TAG = "ViewStudentProfile";

    // UI Components
    private ImageView ivProfileImage, ivDocumentImage;
    private TextView tvUserName, tvUserTypeLabel;
    private TextView tvGender, tvEmail, tvPhone;
    private TextView tvDivision, tvDistrict, tvArea;
    private TextView tvInstitute, tvClass, tvGroup;
    private TextView tvAdditionalInfo;
    private Button btnReportProfile;
    
    // Contact info section
    private CardView cardContactInfo;
    private LinearLayout layoutContactInfo;
    private TextView tvContactHeader;
    
    // Document section (ADMIN ONLY - NEVER for tutors)
    private CardView cardDocument;
    private LinearLayout layoutDocument;
    
    // Repositories
    private UserFilterRepository userRepo;
    private ConnectionRepository connectionRepo;
    private ReportRepository reportRepo;
    
    // Intent data
    private String studentId;
    private String viewMode;
    private String currentUserId;
    
    // Student data for reporting
    private String studentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_student_profile);

        // Initialize repositories
        userRepo = new UserFilterRepository();
        connectionRepo = new ConnectionRepository();
        reportRepo = new ReportRepository();

        // Get intent extras
        studentId = getIntent().getStringExtra(ProfileViewMode.EXTRA_USER_ID);
        viewMode = getIntent().getStringExtra(ProfileViewMode.EXTRA_VIEW_MODE);
        currentUserId = getIntent().getStringExtra(ProfileViewMode.EXTRA_CURRENT_USER_ID);

        // Validate inputs
        if (studentId == null) {
            Toast.makeText(this, "Error: Student ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Default to public mode if not specified
        if (viewMode == null) {
            viewMode = ProfileViewMode.VIEW_MODE_PUBLIC;
        }

        // Get current user if not provided
        if (currentUserId == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        initializeViews();
        setupListeners();
        loadStudentProfile();
    }

    private void initializeViews() {
        // Images
        ivProfileImage = findViewById(R.id.ivProfileImage);
        ivDocumentImage = findViewById(R.id.ivDocumentImage);

        // Basic info
        tvUserName = findViewById(R.id.tvUserName);
        tvUserTypeLabel = findViewById(R.id.tvUserTypeLabel);
        tvGender = findViewById(R.id.tvGender);

        // Contact info (conditionally shown)
        cardContactInfo = findViewById(R.id.cardContactInfo);
        layoutContactInfo = findViewById(R.id.layoutContactInfo);
        tvContactHeader = findViewById(R.id.tvContactHeader);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);

        // Location
        tvDivision = findViewById(R.id.tvDivision);
        tvDistrict = findViewById(R.id.tvDistrict);
        tvArea = findViewById(R.id.tvArea);

        // Education - Student specific
        tvInstitute = findViewById(R.id.tvInstitute);
        tvClass = findViewById(R.id.tvClass);
        tvGroup = findViewById(R.id.tvGroup);
        
        tvAdditionalInfo = findViewById(R.id.tvAdditionalInfo);

        // Document section (admin only - NEVER for tutors)
        cardDocument = findViewById(R.id.cardDocument);
        
        // Report button
        btnReportProfile = findViewById(R.id.btnReportProfile);
        
        // Hide report button for admin view
        if (ProfileViewMode.VIEW_MODE_ADMIN.equals(viewMode)) {
            btnReportProfile.setVisibility(View.GONE);
        }
    }
    
    private void setupListeners() {
        btnReportProfile.setOnClickListener(v -> showReportDialog());
        layoutDocument = findViewById(R.id.layoutDocument);
    }

    private void loadStudentProfile() {
        Log.d(TAG, "Loading student profile - ID: " + studentId + ", Mode: " + viewMode);

        // For PUBLIC mode, check if connection exists to upgrade to CONNECTED
        if (ProfileViewMode.VIEW_MODE_PUBLIC.equals(viewMode) && currentUserId != null) {
            checkConnectionAndLoadProfile();
        } else {
            // Load profile directly for other modes
            fetchAndDisplayProfile();
        }
    }

    private void checkConnectionAndLoadProfile() {
        connectionRepo.canViewContactInfo(currentUserId, studentId)
                .addOnSuccessListener(hasConnection -> {
                    if (hasConnection) {
                        Log.d(TAG, "Connection exists - upgrading to CONNECTED mode");
                        viewMode = ProfileViewMode.VIEW_MODE_CONNECTED;
                    } else {
                        Log.d(TAG, "No connection - using PUBLIC mode");
                    }
                    fetchAndDisplayProfile();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking connection", e);
                    // Continue with PUBLIC mode on error
                    fetchAndDisplayProfile();
                });
    }

    private void fetchAndDisplayProfile() {
        Log.d(TAG, "Fetching student profile with mode: " + viewMode);

        if (ProfileViewMode.VIEW_MODE_ADMIN.equals(viewMode)) {
            // Admin sees everything including document
            userRepo.getAdminStudentProfile(studentId)
                    .addOnSuccessListener(this::displayStudentProfile)
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading admin student profile", e);
                        Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else if (ProfileViewMode.VIEW_MODE_CONNECTED.equals(viewMode)) {
            // Connected tutor sees contact info but NOT document
            userRepo.getConnectedStudentProfile(studentId)
                    .addOnSuccessListener(this::displayStudentProfile)
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading connected student profile", e);
                        Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            // Public mode - minimal info, no contact, no document
            userRepo.getPublicStudentProfile(studentId)
                    .addOnSuccessListener(this::displayStudentProfile)
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading public student profile", e);
                        Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }

    private void displayStudentProfile(DocumentSnapshot document) {
        if (!document.exists()) {
            Toast.makeText(this, "Student profile not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Displaying student profile in " + viewMode + " mode");

        // Basic information (visible in all modes)
        String name = document.getString("name");
        studentName = name; // Store for reporting
        String gender = document.getString("gender");
        String institute = document.getString("institute");
        String studentClass = document.getString("class");
        String group = document.getString("group");
        String division = document.getString("division");
        String district = document.getString("district");
        String area = document.getString("area");
        String about = document.getString("about");

        tvUserName.setText(name != null ? name.toUpperCase() : "N/A");
        tvUserTypeLabel.setText("Student");
        tvGender.setText(gender != null ? gender : "N/A");
        tvInstitute.setText(institute != null ? institute : "N/A");
        tvClass.setText(studentClass != null ? studentClass : "N/A");
        tvGroup.setText(group != null ? group : "N/A");
        tvDivision.setText(division != null ? division : "N/A");
        tvDistrict.setText(district != null ? district : "N/A");
        tvArea.setText(area != null ? area : "N/A");
        tvAdditionalInfo.setText(about != null ? about : "No additional information provided.");

        // Profile image (visible in all modes)
        String profileImageBase64 = document.getString("profileImageBase64");
        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
            Bitmap bitmap = Base64ImageHelper.convertBase64ToBitmap(profileImageBase64);
            if (bitmap != null) {
                ivProfileImage.setImageBitmap(bitmap);
            }
        }

        // Configure view mode specific visibility
        configureViewMode(document);
    }

    private void configureViewMode(DocumentSnapshot document) {
        if (ProfileViewMode.VIEW_MODE_ADMIN.equals(viewMode)) {
            // ADMIN MODE: Show everything including document
            Log.d(TAG, "Configuring ADMIN mode - showing all fields");
            
            // Show contact info
            cardContactInfo.setVisibility(View.VISIBLE);
            String email = document.getString("email");
            String phone = document.getString("phone");
            tvEmail.setText(email != null ? email : "N/A");
            tvPhone.setText(phone != null ? phone : "N/A");

            // Show verification document
            cardDocument.setVisibility(View.VISIBLE);
            String documentImageBase64 = document.getString("documentImageBase64");
            if (documentImageBase64 != null && !documentImageBase64.isEmpty()) {
                Bitmap bitmap = Base64ImageHelper.convertBase64ToBitmap(documentImageBase64);
                if (bitmap != null) {
                    ivDocumentImage.setImageBitmap(bitmap);
                }
            }

        } else if (ProfileViewMode.VIEW_MODE_CONNECTED.equals(viewMode)) {
            // CONNECTED MODE: Show contact info but HIDE document (SECURITY)
            Log.d(TAG, "Configuring CONNECTED mode - showing contact, hiding document");
            
            // Show contact info
            cardContactInfo.setVisibility(View.VISIBLE);
            String email = document.getString("email");
            String phone = document.getString("phone");
            tvEmail.setText(email != null ? email : "N/A");
            tvPhone.setText(phone != null ? phone : "N/A");

            // SECURITY: Hide verification document from tutors
            cardDocument.setVisibility(View.GONE);

        } else {
            // PUBLIC MODE: Hide contact info and document
            Log.d(TAG, "Configuring PUBLIC mode - hiding contact and document");
            
            // Hide contact info
            cardContactInfo.setVisibility(View.GONE);

            // SECURITY: Hide verification document
            cardDocument.setVisibility(View.GONE);
        }
    }
    
    /**
     * Show report dialog for submitting a profile report
     */
    private void showReportDialog() {
        if (currentUserId == null) {
            Toast.makeText(this, "Please log in to report", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create multiline input field
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setHint("Describe the issue...");
        input.setMinLines(4);
        input.setMaxLines(8);
        input.setPadding(50, 30, 50, 30);
        input.setTextColor(0xFFFFFFFF); // White text
        input.setHintTextColor(0xFF94A3B8); // Gray hint
        input.setBackgroundColor(0xFF1A1B2E); // Dark background

        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
                .setTitle("Report Profile")
                .setMessage("Please describe the issue with this student's profile")
                .setView(input)
                .setPositiveButton("Submit Report", (dialog, which) -> {
                    String reportMessage = input.getText().toString().trim();
                    if (reportMessage.isEmpty()) {
                        Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    submitReport(reportMessage);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Submit the report to Firestore
     */
    private void submitReport(String reportMessage) {
        // Get current user info from Firestore
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to report", Toast.LENGTH_SHORT).show();
            return;
        }

        String reporterId = auth.getCurrentUser().getUid();

        // Fetch reporter details
        userRepo.getUserById(reporterId)
                .addOnSuccessListener(reporterDoc -> {
                    if (!reporterDoc.exists()) {
                        Toast.makeText(this, "Reporter profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String reporterName = reporterDoc.getString("name");
                    String reporterType = reporterDoc.getString("userType");

                    // Submit report
                    reportRepo.submitProfileReport(
                            reporterId,
                            reporterName != null ? reporterName : "Unknown",
                            reporterType != null ? reporterType : "Unknown",
                            studentId,
                            studentName != null ? studentName : "Unknown Student",
                            "Student",
                            reportMessage
                    ).addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Report submitted successfully", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Report submitted for student: " + studentId);
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to submit report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error submitting report", e);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading reporter info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading reporter info", e);
                });
    }
}
