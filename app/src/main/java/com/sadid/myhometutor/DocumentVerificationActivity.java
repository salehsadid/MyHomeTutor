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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
                    documentUri = uri;
                    ivDocumentPreview.setImageURI(uri);
                    ivDocumentPreview.setVisibility(View.VISIBLE);
                    tvUploadPlaceholder.setVisibility(View.GONE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        initializeViews();
        setupSpinner();
        setupListeners();
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

        btnFinishRegistration.setOnClickListener(v -> finishRegistration());
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
        if (profileImageUri != null) {
            StorageReference profileRef = storage.getReference().child("profile_images/" + userId + ".jpg");
            profileRef.putFile(profileImageUri)
                    .addOnSuccessListener(taskSnapshot -> profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        userData.put("profileImageUrl", uri.toString());
                        uploadDocumentImage(userId);
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to upload profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        uploadDocumentImage(userId); // Continue even if profile upload fails
                    });
        } else {
            uploadDocumentImage(userId);
        }
    }

    private void uploadDocumentImage(String userId) {
        StorageReference docRef = storage.getReference().child("documents/" + userId + "_" + UUID.randomUUID().toString() + ".jpg");
        docRef.putFile(documentUri)
                .addOnSuccessListener(taskSnapshot -> docRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    userData.put("documentImageUrl", uri.toString());
                    saveDataToFirestore(userId);
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    saveDataToFirestore(userId);
                });
    }

    private void saveDataToFirestore(String userId) {
        // Add document info to user data
        userData.put("documentType", spDocumentType.getSelectedItem().toString());
        userData.put("documentStatus", "pending");

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DocumentVerificationActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    Intent intent;
                    String userType = (String) userData.get("userType");
                    if ("Student".equals(userType)) {
                        intent = new Intent(DocumentVerificationActivity.this, StudentDashboardActivity.class);
                    } else {
                        intent = new Intent(DocumentVerificationActivity.this, TutorDashboardActivity.class);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DocumentVerificationActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
