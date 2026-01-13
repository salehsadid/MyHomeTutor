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
 * ViewTutorProfileActivity - Role-based tutor profile viewing
 * 
 * View Modes:
 * 1. PUBLIC (Student viewing before connection)
 *    - Shows: Name, education, experience, preferences, location
 *    - Hides: Phone, email, verification document
 * 
 * 2. CONNECTED (Student viewing after connection)
 *    - Shows: All public info + phone + email
 *    - Hides: Verification document
 * 
 * 3. ADMIN (Admin viewing)
 *    - Shows: Everything including verification document
 *    - Has: Approve/Reject buttons (handled by AdminViewUserActivity)
 */
public class ViewTutorProfileActivity extends AppCompatActivity {

    private static final String TAG = "ViewTutorProfile";

    // UI Components
    private ImageView ivProfileImage, ivDocumentImage;
    private TextView tvUserName, tvUserTypeLabel;
    private TextView tvGender, tvEmail, tvPhone;
    private TextView tvDivision, tvDistrict, tvArea;
    private TextView tvCollege, tvGroup, tvHscYear;
    private TextView tvUniversity, tvDepartment, tvUniversityYear, tvSession;
    private TextView tvExperience, tvPreferredClass, tvPreferredDays;
    private TextView tvPreferredTime, tvPreferredLocation, tvPreferredFee;
    private TextView tvAdditionalInfo;
    private Button btnReportProfile;
    
    // Contact info section
    private CardView cardContactInfo;
    private LinearLayout layoutContactInfo;
    private TextView tvContactHeader;
    
    // Document section
    private CardView cardDocument;
    private LinearLayout layoutDocument;
    
    // Repositories
    private UserFilterRepository userRepo;
    private ConnectionRepository connectionRepo;
    private ReportRepository reportRepo;
    
    // Intent data
    private String tutorId;
    private String viewMode;
    private String currentUserId;
    
    // Tutor data for reporting
    private String tutorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tutor_profile);

        // Initialize repositories
        userRepo = new UserFilterRepository();
        connectionRepo = new ConnectionRepository();
        reportRepo = new ReportRepository();

        // Get intent extras
        tutorId = getIntent().getStringExtra(ProfileViewMode.EXTRA_USER_ID);
        viewMode = getIntent().getStringExtra(ProfileViewMode.EXTRA_VIEW_MODE);
        currentUserId = getIntent().getStringExtra(ProfileViewMode.EXTRA_CURRENT_USER_ID);

        // Validate inputs
        if (tutorId == null) {
            Toast.makeText(this, "Error: Tutor ID not found", Toast.LENGTH_SHORT).show();
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
        loadTutorProfile();
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

        // Education - College
        tvCollege = findViewById(R.id.tvCollege);
        tvGroup = findViewById(R.id.tvGroup);
        tvHscYear = findViewById(R.id.tvHscYear);

        // Education - University
        tvUniversity = findViewById(R.id.tvUniversity);
        tvDepartment = findViewById(R.id.tvDepartment);
        tvUniversityYear = findViewById(R.id.tvUniversityYear);
        tvSession = findViewById(R.id.tvSession);

        // Teaching preferences
        tvExperience = findViewById(R.id.tvExperience);
        tvPreferredClass = findViewById(R.id.tvPreferredClass);
        tvPreferredDays = findViewById(R.id.tvPreferredDays);
        tvPreferredTime = findViewById(R.id.tvPreferredTime);
        tvPreferredLocation = findViewById(R.id.tvPreferredLocation);
        tvPreferredFee = findViewById(R.id.tvPreferredFee);
        tvAdditionalInfo = findViewById(R.id.tvAdditionalInfo);

        // Document section (admin only)
        cardDocument = findViewById(R.id.cardDocument);
        layoutDocument = findViewById(R.id.layoutDocument);
        
        // Report button
        btnReportProfile = findViewById(R.id.btnReportProfile);
        
        // Hide report button for admin view
        if (ProfileViewMode.VIEW_MODE_ADMIN.equals(viewMode)) {
            btnReportProfile.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        btnReportProfile.setOnClickListener(v -> showReportDialog());
    }

    private void loadTutorProfile() {
        Log.d(TAG, "Loading tutor profile - ID: " + tutorId + ", Mode: " + viewMode);

        // For PUBLIC mode, check if connection exists to upgrade to CONNECTED
        if (ProfileViewMode.VIEW_MODE_PUBLIC.equals(viewMode) && currentUserId != null) {
            checkConnectionAndLoadProfile();
        } else {
            // Load profile directly for other modes
            fetchAndDisplayProfile();
        }
    }

    private void checkConnectionAndLoadProfile() {
        connectionRepo.canViewContactInfo(currentUserId, tutorId)
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
                    Log.e(TAG, "Error checking connection: " + e.getMessage());
                    // Continue with PUBLIC mode on error
                    fetchAndDisplayProfile();
                });
    }

    private void fetchAndDisplayProfile() {
        userRepo.getTutor(tutorId)
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        displayTutorProfile(document);
                        configureViewMode();
                    } else {
                        Toast.makeText(this, "Tutor not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading tutor profile", e);
                    finish();
                });
    }

    private void displayTutorProfile(DocumentSnapshot document) {
        // Basic information (always visible)
        String name = document.getString("name");
        tutorName = name; // Store for reporting
        String gender = document.getString("gender");
        String division = document.getString("division");
        String district = document.getString("district");
        String area = document.getString("area");

        tvUserName.setText(name != null ? name.toUpperCase() : "N/A");
        tvUserTypeLabel.setText("Tutor");
        tvGender.setText(gender != null ? gender : "N/A");
        tvDivision.setText(division != null ? division : "N/A");
        tvDistrict.setText(district != null ? district : "N/A");
        tvArea.setText(area != null ? area : "N/A");

        // Contact information (conditionally visible)
        String email = document.getString("email");
        String phone = document.getString("phone");
        tvEmail.setText(email != null ? email : "N/A");
        tvPhone.setText(phone != null ? phone : "N/A");

        // College information
        String college = document.getString("collegeName");
        String group = document.getString("collegeGroup");
        String hscYear = document.getString("hscYear");
        
        tvCollege.setText(college != null ? college : "N/A");
        tvGroup.setText(group != null ? group : "N/A");
        tvHscYear.setText(hscYear != null ? hscYear : "N/A");

        // University information
        String university = document.getString("universityName");
        String department = document.getString("department");
        String universityYear = document.getString("universityYear");
        String session = document.getString("session");
        
        tvUniversity.setText(university != null ? university : "N/A");
        tvDepartment.setText(department != null ? department : "N/A");
        tvUniversityYear.setText(universityYear != null ? universityYear : "N/A");
        tvSession.setText(session != null ? session : "N/A");

        // Teaching preferences
        String experience = document.getString("experience");
        String preferredClass = document.getString("preferredClass");
        String preferredDays = document.getString("preferredDays");
        String preferredTime = document.getString("preferredTime");
        String preferredLocation = document.getString("preferredLocation");
        String preferredFee = document.getString("preferredFee");
        String additionalInfo = document.getString("additionalInfo");

        tvExperience.setText(experience != null ? experience + " years" : "N/A");
        tvPreferredClass.setText(preferredClass != null ? preferredClass : "N/A");
        tvPreferredDays.setText(preferredDays != null ? preferredDays : "N/A");
        tvPreferredTime.setText(preferredTime != null ? preferredTime : "N/A");
        tvPreferredLocation.setText(preferredLocation != null ? preferredLocation : "N/A");
        tvPreferredFee.setText(preferredFee != null ? "BDT " + preferredFee : "N/A");
        tvAdditionalInfo.setText(additionalInfo != null ? additionalInfo : "No additional information provided.");

        // Load profile image (always visible)
        String profileImageBase64 = document.getString("profileImageBase64");
        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
            Bitmap profileBitmap = Base64ImageHelper.convertBase64ToBitmap(profileImageBase64);
            if (profileBitmap != null) {
                ivProfileImage.setImageBitmap(profileBitmap);
            }
        }

        // Load document image (admin only - handled in configureViewMode)
        String documentImageBase64 = document.getString("documentImageBase64");
        if (documentImageBase64 != null && !documentImageBase64.isEmpty()) {
            Bitmap documentBitmap = Base64ImageHelper.convertBase64ToBitmap(documentImageBase64);
            if (documentBitmap != null) {
                ivDocumentImage.setImageBitmap(documentBitmap);
            }
        }
    }

    private void configureViewMode() {
        Log.d(TAG, "Configuring view mode: " + viewMode);

        switch (viewMode) {
            case ProfileViewMode.VIEW_MODE_PUBLIC:
                // Hide contact info
                cardContactInfo.setVisibility(View.GONE);
                layoutContactInfo.setVisibility(View.GONE);
                
                // Hide document
                cardDocument.setVisibility(View.GONE);
                layoutDocument.setVisibility(View.GONE);
                
                Log.d(TAG, "PUBLIC mode: Contact info and documents hidden");
                break;

            case ProfileViewMode.VIEW_MODE_CONNECTED:
                // Show contact info
                cardContactInfo.setVisibility(View.VISIBLE);
                layoutContactInfo.setVisibility(View.VISIBLE);
                tvContactHeader.setText("Contact Information (Connection Established)");
                
                // Hide document
                cardDocument.setVisibility(View.GONE);
                layoutDocument.setVisibility(View.GONE);
                
                Log.d(TAG, "CONNECTED mode: Contact info visible, documents hidden");
                break;

            case ProfileViewMode.VIEW_MODE_ADMIN:
                // Show everything (but admin should use AdminViewUserActivity)
                // This is a fallback - typically admin uses AdminViewUserActivity
                cardContactInfo.setVisibility(View.VISIBLE);
                layoutContactInfo.setVisibility(View.VISIBLE);
                tvContactHeader.setText("Contact Information");
                
                cardDocument.setVisibility(View.VISIBLE);
                layoutDocument.setVisibility(View.VISIBLE);
                
                Log.d(TAG, "ADMIN mode: All information visible");
                break;

            default:
                // Default to PUBLIC mode for safety
                cardContactInfo.setVisibility(View.GONE);
                layoutContactInfo.setVisibility(View.GONE);
                cardDocument.setVisibility(View.GONE);
                layoutDocument.setVisibility(View.GONE);
                
                Log.w(TAG, "Unknown mode - defaulting to PUBLIC");
                break;
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
                .setMessage("Please describe the issue with this tutor's profile")
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
                            tutorId,
                            tutorName != null ? tutorName : "Unknown Tutor",
                            "Tutor",
                            reportMessage
                    ).addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Report submitted successfully", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Report submitted for tutor: " + tutorId);
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
