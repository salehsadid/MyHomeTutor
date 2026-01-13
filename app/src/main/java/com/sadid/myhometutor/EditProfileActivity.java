package com.sadid.myhometutor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sadid.myhometutor.utils.Base64ImageHelper;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private CircleImageView ivProfilePhoto;
    private TextView tvChangePhoto;
    private EditText etFullName, etEmail, etSecondaryEmail, etPhone, etInstitute, etAbout;
    private EditText etCollegeName, etHscYear, etUniversityName, etDepartment, etYearSemester, etSession;
    private EditText etPreferredDays, etPreferredTime, etLocationDetails, etPreferredFee, etExperience;
    private Spinner spGender, spDivision, spDistrict, spArea, spClass, spGroup, spCollegeGroup, spPreferredClass;
    private LinearLayout layoutStudentFields, layoutTutorFields;
    private Button btnCancel, btnSaveChanges;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String userType;
    private Uri newProfileImageUri;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    startCrop(uri);
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initializeViews();
        setupSpinners();
        loadUserData();
        setupListeners();
    }

    private void initializeViews() {
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvChangePhoto = findViewById(R.id.tvChangePhoto);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etSecondaryEmail = findViewById(R.id.etSecondaryEmail);
        etPhone = findViewById(R.id.etPhone);
        etInstitute = findViewById(R.id.etInstitute);
        etAbout = findViewById(R.id.etAbout);

        etCollegeName = findViewById(R.id.etCollegeName);
        etHscYear = findViewById(R.id.etHscYear);
        etUniversityName = findViewById(R.id.etUniversityName);
        etDepartment = findViewById(R.id.etDepartment);
        etYearSemester = findViewById(R.id.etYearSemester);
        etSession = findViewById(R.id.etSession);
        etPreferredDays = findViewById(R.id.etPreferredDays);
        etPreferredTime = findViewById(R.id.etPreferredTime);
        etLocationDetails = findViewById(R.id.etLocationDetails);
        etPreferredFee = findViewById(R.id.etPreferredFee);
        etExperience = findViewById(R.id.etExperience);

        spGender = findViewById(R.id.spGender);
        spDivision = findViewById(R.id.spDivision);
        spDistrict = findViewById(R.id.spDistrict);
        spArea = findViewById(R.id.spArea);
        spClass = findViewById(R.id.spClass);
        spGroup = findViewById(R.id.spGroup);
        spCollegeGroup = findViewById(R.id.spCollegeGroup);
        spPreferredClass = findViewById(R.id.spPreferredClass);

        layoutStudentFields = findViewById(R.id.layoutStudentFields);
        layoutTutorFields = findViewById(R.id.layoutTutorFields);

        btnCancel = findViewById(R.id.btnCancel);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
    }

    private void setupSpinners() {
        setupSpinner(spGender, new String[]{"Select Gender", "Male", "Female"});
        
        // Setup Division Spinner with LocationDataHelper
        List<String> divisionsList = LocationDataHelper.getDivisions();
        List<String> divisions = new ArrayList<>(divisionsList);
        divisions.add(0, "Select Division");
        setupSpinner(spDivision, divisions.toArray(new String[0]));
        
        // Setup District Spinner (initially empty)
        setupSpinner(spDistrict, new String[]{"Select District"});
        
        // Setup Area/Thana Spinner (initially empty)
        setupSpinner(spArea, new String[]{"Select Area"});
        
        setupSpinner(spClass, new String[]{"Select Class", "Class 1", "Class 2", "Class 3", "Class 4", "Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "Class 11 (HSC)", "Class 12 (HSC)"});
        setupSpinner(spGroup, new String[]{"Select Group", "Science", "Commerce", "Arts", "N/A"});
        setupSpinner(spCollegeGroup, new String[]{"Select Group", "Science", "Commerce", "Arts"});
        setupSpinner(spPreferredClass, new String[]{"Select Class", "Class 1-5", "Class 6-8", "Class 9-10", "HSC", "Admission Test"});
        
        // Setup Division Spinner Listener
        spDivision.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDivision = parent.getItemAtPosition(position).toString();
                if (!selectedDivision.equals("Select Division")) {
                    List<String> districtsList = LocationDataHelper.getDistricts(selectedDivision);
                    List<String> districts = new ArrayList<>(districtsList);
                    districts.add(0, "Select District");
                    setupSpinner(spDistrict, districts.toArray(new String[0]));
                } else {
                    setupSpinner(spDistrict, new String[]{"Select District"});
                    setupSpinner(spArea, new String[]{"Select Area"});
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Setup District Spinner Listener
        spDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDistrict = parent.getItemAtPosition(position).toString();
                String selectedDivision = spDivision.getSelectedItem().toString();
                if (!selectedDistrict.equals("Select District") && !selectedDivision.equals("Select Division")) {
                    List<String> thanasList = LocationDataHelper.getThanas(selectedDivision, selectedDistrict);
                    List<String> thanas = new ArrayList<>(thanasList);
                    thanas.add(0, "Select Area");
                    setupSpinner(spArea, thanas.toArray(new String[0]));
                } else {
                    setupSpinner(spArea, new String[]{"Select Area"});
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        userType = document.getString("userType");
                        populateCommonFields(document);

                        if ("Student".equals(userType)) {
                            layoutStudentFields.setVisibility(View.VISIBLE);
                            layoutTutorFields.setVisibility(View.GONE);
                            populateStudentFields(document);
                        } else if ("Tutor".equals(userType)) {
                            layoutStudentFields.setVisibility(View.GONE);
                            layoutTutorFields.setVisibility(View.VISIBLE);
                            populateTutorFields(document);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void populateCommonFields(DocumentSnapshot document) {
        etFullName.setText(document.getString("name"));
        etEmail.setText(document.getString("email"));
        etSecondaryEmail.setText(document.getString("secondaryEmail"));
        etPhone.setText(document.getString("phone"));
        etAbout.setText(document.getString("about"));

        // Load profile image - support both Base64 and URL (for backward compatibility)
        String profileImageBase64 = document.getString("profileImageBase64");
        String profileImageUrl = document.getString("profileImageUrl");
        
        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
            // Load from Base64
            Base64ImageHelper.loadBase64IntoImageView(ivProfilePhoto, profileImageBase64, R.drawable.ic_person);
        } else if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            // Fallback to URL for backward compatibility
            Picasso.get().load(profileImageUrl).placeholder(R.drawable.ic_person).into(ivProfilePhoto);
        }

        setSpinnerSelection(spGender, document.getString("gender"));
        setSpinnerSelection(spDivision, document.getString("division"));
        setSpinnerSelection(spDistrict, document.getString("district"));
        setSpinnerSelection(spArea, document.getString("area"));
    }

    private void populateStudentFields(DocumentSnapshot document) {
        etInstitute.setText(document.getString("institute"));
        setSpinnerSelection(spClass, document.getString("class"));
        setSpinnerSelection(spGroup, document.getString("group"));
    }

    private void populateTutorFields(DocumentSnapshot document) {
        etCollegeName.setText(document.getString("collegeName"));
        etHscYear.setText(document.getString("hscYear"));
        etUniversityName.setText(document.getString("universityName"));
        etDepartment.setText(document.getString("department"));
        etYearSemester.setText(document.getString("yearSemester"));
        etSession.setText(document.getString("session"));
        etPreferredDays.setText(document.getString("preferredDays"));
        etPreferredTime.setText(document.getString("preferredTime"));
        etLocationDetails.setText(document.getString("locationDetails"));
        etPreferredFee.setText(document.getString("preferredFee"));
        etExperience.setText(document.getString("experience"));

        setSpinnerSelection(spCollegeGroup, document.getString("collegeGroup"));
        setSpinnerSelection(spPreferredClass, document.getString("preferredClass"));
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            String item = adapter.getItem(i);
            if (item != null && item.equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setupListeners() {
        tvChangePhoto.setOnClickListener(v -> mGetContent.launch("image/*"));
        btnCancel.setOnClickListener(v -> finish());
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        // If profile image changed, upload it first
        if (newProfileImageUri != null) {
            uploadProfileImageAndSave(userId);
        } else {
            saveProfileData(userId, null);
        }
    }

    private void uploadProfileImageAndSave(String userId) {
        // Convert image to Base64
        String profileImageBase64 = Base64ImageHelper.convertUriToBase64(this, newProfileImageUri, 800, 75);
        
        if (profileImageBase64 != null) {
            // Validate size
            if (Base64ImageHelper.isBase64SizeValid(profileImageBase64, 1500)) {
                saveProfileData(userId, profileImageBase64);
            } else {
                Toast.makeText(this, "Image too large. Compressing...", Toast.LENGTH_SHORT).show();
                // Try with lower quality
                profileImageBase64 = Base64ImageHelper.convertUriToBase64(this, newProfileImageUri, 600, 60);
                if (profileImageBase64 != null && Base64ImageHelper.isBase64SizeValid(profileImageBase64, 1500)) {
                    saveProfileData(userId, profileImageBase64);
                } else {
                    Toast.makeText(this, "Failed to upload image - too large", Toast.LENGTH_SHORT).show();
                    saveProfileData(userId, null);
                }
            }
        } else {
            Toast.makeText(this, "Failed to convert image", Toast.LENGTH_SHORT).show();
            saveProfileData(userId, null);
        }
    }

    private void saveProfileData(String userId, String newProfileImageBase64) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", etFullName.getText().toString());
        updates.put("secondaryEmail", etSecondaryEmail.getText().toString());
        updates.put("phone", etPhone.getText().toString());
        updates.put("about", etAbout.getText().toString());
        updates.put("gender", spGender.getSelectedItem().toString());
        updates.put("division", spDivision.getSelectedItem().toString());
        updates.put("district", spDistrict.getSelectedItem().toString());
        updates.put("area", spArea.getSelectedItem().toString());

        if (newProfileImageBase64 != null) {
            updates.put("profileImageBase64", newProfileImageBase64);
        }

        if ("Student".equals(userType)) {
            updates.put("institute", etInstitute.getText().toString());
            updates.put("class", spClass.getSelectedItem().toString());
            updates.put("group", spGroup.getSelectedItem().toString());
        } else if ("Tutor".equals(userType)) {
            updates.put("collegeName", etCollegeName.getText().toString());
            updates.put("hscYear", etHscYear.getText().toString());
            updates.put("universityName", etUniversityName.getText().toString());
            updates.put("department", etDepartment.getText().toString());
            updates.put("yearSemester", etYearSemester.getText().toString());
            updates.put("session", etSession.getText().toString());
            updates.put("preferredDays", etPreferredDays.getText().toString());
            updates.put("preferredTime", etPreferredTime.getText().toString());
            updates.put("locationDetails", etLocationDetails.getText().toString());
            updates.put("preferredFee", etPreferredFee.getText().toString());
            updates.put("experience", etExperience.getText().toString());
            updates.put("collegeGroup", spCollegeGroup.getSelectedItem().toString());
            updates.put("preferredClass", spPreferredClass.getSelectedItem().toString());
        }

        db.collection("users").document(userId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void startCrop(Uri uri) {
        String destinationFileName = "ProfileCrop.jpg";
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        uCrop.withAspectRatio(1, 1);
        uCrop.withMaxResultSize(450, 450);
        uCrop.withOptions(getCropOptions());
        Intent intent = uCrop.getIntent(this);
        intent.setClass(this, CustomUCropActivity.class);
        startActivityForResult(intent, UCrop.REQUEST_CROP);
    }

    private UCrop.Options getCropOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setShowCropFrame(false);
        options.setShowCropGrid(false);
        options.setCompressionQuality(80);
        options.setToolbarColor(ContextCompat.getColor(this, R.color.teal_button));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.teal_button));
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.teal_button));
        options.setFreeStyleCropEnabled(false);
        options.setHideBottomControls(false);
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.white));        // Use custom UCrop activity with proper insets handling
        options.setToolbarTitle("Crop Profile Photo");        return options;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                newProfileImageUri = resultUri;
                ivProfilePhoto.setImageURI(resultUri);
                Toast.makeText(this, "Profile photo updated", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "Crop error: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
