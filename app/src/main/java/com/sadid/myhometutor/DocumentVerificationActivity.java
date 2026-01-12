package com.sadid.myhometutor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sadid.myhometutor.utils.Base64ImageHelper;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class DocumentVerificationActivity extends AppCompatActivity {

    private Spinner spDocumentType;
    private FrameLayout flUploadDocument;
    private ImageView ivDocumentPreview;
    private TextView tvUploadPlaceholder;
    private Button btnFinishRegistration;
    private CheckBox cbTerms;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private HashMap<String, Object> userData;
    private String password;
    private Uri documentUri;
    private Uri profileImageUri;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    startDocumentCrop(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Add logging at the very beginning
        Toast.makeText(this, "DocumentVerificationActivity started", Toast.LENGTH_SHORT).show();
        android.util.Log.d("DocumentVerification", "onCreate started");
        
        setContentView(R.layout.activity_document_verification);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Retrieve data passed from previous activity
        Intent intent = getIntent();
        if (intent != null) {
            userData = (HashMap<String, Object>) intent.getSerializableExtra("userData");
            password = intent.getStringExtra("password");
            String profileUriString = intent.getStringExtra("profileImageUri");
            if (profileUriString != null) {
                profileImageUri = Uri.parse(profileUriString);
            }
        }
        
        android.util.Log.d("DocumentVerification", "userData: " + (userData != null ? "exists" : "null"));
        android.util.Log.d("DocumentVerification", "password: " + (password != null ? "exists" : "null"));
        
        // Validate that we received the necessary data
        if (userData == null || password == null) {
            Toast.makeText(this, "Error: Registration data is missing. Please try again.", Toast.LENGTH_LONG).show();
            android.util.Log.e("DocumentVerification", "userData or password is null - finishing activity");
            finish();
            return;
        }

        initializeViews();
        setupSpinner();
        setupListeners();
        
        Toast.makeText(this, "Document Verification page loaded successfully", Toast.LENGTH_SHORT).show();
        android.util.Log.d("DocumentVerification", "onCreate completed successfully");
    }

    private void initializeViews() {
        spDocumentType = findViewById(R.id.spDocumentType);
        flUploadDocument = findViewById(R.id.flUploadDocument);
        ivDocumentPreview = findViewById(R.id.ivDocumentPreview);
        tvUploadPlaceholder = findViewById(R.id.tvUploadPlaceholder);
        btnFinishRegistration = findViewById(R.id.btnFinishRegistration);
        cbTerms = findViewById(R.id.cbTerms);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Select Document Type", "National ID (NID)", "Birth Certificate", "Student ID"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDocumentType.setAdapter(adapter);
    }

    private void setupListeners() {
        flUploadDocument.setOnClickListener(v -> mGetContent.launch("image/*"));

        btnFinishRegistration.setEnabled(false);
        btnFinishRegistration.setAlpha(0.5f);
        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnFinishRegistration.setEnabled(isChecked);
            btnFinishRegistration.setAlpha(isChecked ? 1.0f : 0.5f);
        });

        btnFinishRegistration.setOnClickListener(v -> finishRegistration());
    }
    
    private void startDocumentCrop(Uri uri) {
        String destinationFileName = "SampleCropDocument.jpg";
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        uCrop.withAspectRatio(16, 9);  // Document aspect ratio
        uCrop.withMaxResultSize(1920, 1080);
        uCrop.withOptions(getDocumentCropOptions());
        uCrop.start(this);
    }

    private UCrop.Options getDocumentCropOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(false);
        options.setShowCropFrame(true);
        options.setShowCropGrid(true);
        options.setCompressionQuality(80);
        options.setToolbarColor(ContextCompat.getColor(this, R.color.teal_button));
        options.setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.teal_button));
        options.setFreeStyleCropEnabled(true);  // Allow free rotation and cropping
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.white));
        options.setToolbarTitle("Crop Document");
        return options;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                documentUri = resultUri;
                ivDocumentPreview.setImageURI(resultUri);
                ivDocumentPreview.setVisibility(View.VISIBLE);
                tvUploadPlaceholder.setVisibility(View.GONE);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "Crop error: " + (cropError != null ? cropError.getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
        }
    }

    private void finishRegistration() {
        if (spDocumentType.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a document type", Toast.LENGTH_SHORT).show();
            return;
        }

        if (documentUri == null) {
            Toast.makeText(this, "Please upload a document image", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please agree to the terms and conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userData == null || password == null) {
            Toast.makeText(this, "Error: Missing registration data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update registration step to documents
        userData.put("registrationStep", "documents");
        
        String email = (String) userData.get("email");

        // Create User in Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        uploadImagesAndSaveData(userId);
                    } else {
                        Toast.makeText(DocumentVerificationActivity.this, "Registration failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImagesAndSaveData(String userId) {
        // Convert profile image to Base64
        if (profileImageUri != null) {
            String profileImageBase64 = Base64ImageHelper.convertUriToBase64(this, profileImageUri, 800, 75);
            if (profileImageBase64 != null) {
                // Validate size (keep under 1.5MB for better performance)
                if (Base64ImageHelper.isBase64SizeValid(profileImageBase64, 1500)) {
                    userData.put("profileImageBase64", profileImageBase64);
                    Toast.makeText(this, "Profile image converted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Profile image too large. Using lower quality.", Toast.LENGTH_SHORT).show();
                    // Try with lower quality
                    profileImageBase64 = Base64ImageHelper.convertUriToBase64(this, profileImageUri, 600, 60);
                    if (profileImageBase64 != null && Base64ImageHelper.isBase64SizeValid(profileImageBase64, 1500)) {
                        userData.put("profileImageBase64", profileImageBase64);
                    }
                }
            } else {
                Toast.makeText(this, "Failed to convert profile image", Toast.LENGTH_SHORT).show();
            }
        }
        uploadDocumentImage(userId);
    }

    private void uploadDocumentImage(String userId) {
        // Convert document image to Base64
        if (documentUri != null) {
            String documentImageBase64 = Base64ImageHelper.convertUriToBase64(this, documentUri, 1200, 80);
            if (documentImageBase64 != null) {
                // Validate size (keep under 2MB for documents)
                if (Base64ImageHelper.isBase64SizeValid(documentImageBase64, 2000)) {
                    userData.put("documentImageBase64", documentImageBase64);
                    Toast.makeText(this, "Document converted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Document image too large. Using lower quality.", Toast.LENGTH_SHORT).show();
                    // Try with lower quality
                    documentImageBase64 = Base64ImageHelper.convertUriToBase64(this, documentUri, 1000, 65);
                    if (documentImageBase64 != null && Base64ImageHelper.isBase64SizeValid(documentImageBase64, 2000)) {
                        userData.put("documentImageBase64", documentImageBase64);
                    } else {
                        Toast.makeText(this, "Failed to convert document image - too large", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "Failed to convert document image", Toast.LENGTH_SHORT).show();
            }
        }
        saveDataToFirestore(userId);
    }

    private void saveDataToFirestore(String userId) {
        // Add document info to user data
        userData.put("documentType", spDocumentType.getSelectedItem().toString());
        userData.put("documentStatus", "pending");
        
        // Add approval status - users need admin approval before they can login
        userData.put("approvalStatus", "pending");
        
        // Mark registration as completed
        userData.put("registrationStep", "completed");
        userData.put("registrationTimestamp", System.currentTimeMillis());

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DocumentVerificationActivity.this, "Registration Successful! Please wait for admin approval. You will receive an email once approved.", Toast.LENGTH_LONG).show();
                    // Sign out user after successful registration
                    mAuth.signOut();
                    // Redirect to login page
                    Intent intent = new Intent(DocumentVerificationActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DocumentVerificationActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
