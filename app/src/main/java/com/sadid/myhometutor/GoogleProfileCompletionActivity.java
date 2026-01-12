package com.sadid.myhometutor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sadid.myhometutor.utils.Base64ImageHelper;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * GoogleProfileCompletionActivity
 * For users who signed in with Google but haven't completed registration
 * 
 * Flow:
 * 1. Set password (link email/password to Google account)
 * 2. Complete profile fields
 * 3. Upload profile photo
 * 4. Go to DocumentVerificationActivity
 */
public class GoogleProfileCompletionActivity extends AppCompatActivity {

    private CircleImageView ivProfilePhoto;
    private TextView tvUploadPhoto, tvTitle, tvInstruction;
    private EditText etFullName, etPhone, etInstitute, etAbout;
    private EditText etPassword, etConfirmPassword;
    private Spinner spDivision, spDistrict, spArea, spClass, spGroup;
    private RadioGroup rgGender;
    private Button btnNext, btnSkipPhoto;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    private String userType;
    private String userEmail;
    private String userName;
    private Uri profileImageUri;

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
        setContentView(R.layout.activity_google_profile_completion);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userId = currentUser.getUid();
        userEmail = currentUser.getEmail();
        userName = currentUser.getDisplayName();

        // Get user type from intent
        userType = getIntent().getStringExtra("userType");
        if (userType == null) {
            Toast.makeText(this, "User type not specified", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupSpinners();
        setupListeners();
        prefillUserData();
    }

    private void initializeViews() {
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvUploadPhoto = findViewById(R.id.tvUploadPhoto);
        tvTitle = findViewById(R.id.tvTitle);
        tvInstruction = findViewById(R.id.tvInstruction);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etInstitute = findViewById(R.id.etInstitute);
        etAbout = findViewById(R.id.etAbout);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        spDivision = findViewById(R.id.spDivision);
        spDistrict = findViewById(R.id.spDistrict);
        spArea = findViewById(R.id.spArea);
        spClass = findViewById(R.id.spClass);
        spGroup = findViewById(R.id.spGroup);
        rgGender = findViewById(R.id.rgGender);
        btnNext = findViewById(R.id.btnNext);
        btnSkipPhoto = findViewById(R.id.btnSkipPhoto);

        tvTitle.setText("Complete Your Profile");
        tvInstruction.setText("You signed in with Google. Please complete your profile and set a password for security.");
    }

    private void setupSpinners() {
        LinkedHashMap<String, LinkedHashMap<String, List<String>>> locationData = LocationDataHelper.getLocationData();
        
        // Setup Division Spinner
        if (spDivision != null) {
            List<String> divisions = new ArrayList<>();
            divisions.add("Select Division");
            divisions.addAll(locationData.keySet());
            ArrayAdapter<String> divisionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, divisions);
            divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spDivision.setAdapter(divisionAdapter);
            
            spDivision.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedDivision = parent.getItemAtPosition(position).toString();
                    if (!selectedDivision.equals("Select Division") && locationData.containsKey(selectedDivision)) {
                        updateDistrictSpinner(selectedDivision, locationData);
                    } else {
                        resetDistrictAndAreaSpinners();
                    }
                }
                
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        // Setup District Spinner with listener
        if (spDistrict != null) {
            ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList("Select District"));
            districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spDistrict.setAdapter(districtAdapter);
            
            spDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedDistrict = parent.getItemAtPosition(position).toString();
                    if (!selectedDistrict.equals("Select District")) {
                        updateAreaSpinner(selectedDistrict, locationData);
                    } else {
                        resetAreaSpinner();
                    }
                }
                
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        // Setup Area Spinner
        if (spArea != null) {
            ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList("Select Area"));
            areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spArea.setAdapter(areaAdapter);
        }
        
        // Class spinner
        String[] classes = {"Select Class", "Class 1", "Class 2", "Class 3", "Class 4", "Class 5", 
                           "Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "HSC"};
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classes);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spClass.setAdapter(classAdapter);

        // Group spinner
        String[] groups = {"Select Group", "Science", "Commerce", "Arts", "N/A"};
        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groups);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGroup.setAdapter(groupAdapter);
    }
    
    private void updateDistrictSpinner(String division, LinkedHashMap<String, LinkedHashMap<String, List<String>>> locationData) {
        if (spDistrict != null && locationData.containsKey(division)) {
            List<String> districts = new ArrayList<>();
            districts.add("Select District");
            districts.addAll(locationData.get(division).keySet());
            ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, districts);
            districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spDistrict.setAdapter(districtAdapter);
        }
    }
    
    private void updateAreaSpinner(String district, LinkedHashMap<String, LinkedHashMap<String, List<String>>> locationData) {
        if (spArea != null) {
            List<String> areas = new ArrayList<>();
            areas.add("Select Area");
            
            // Find the district in all divisions
            for (LinkedHashMap<String, List<String>> division : locationData.values()) {
                if (division.containsKey(district)) {
                    areas.addAll(division.get(district));
                    break;
                }
            }
            
            ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, areas);
            areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spArea.setAdapter(areaAdapter);
        }
    }
    
    private void resetDistrictAndAreaSpinners() {
        if (spDistrict != null) {
            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, Arrays.asList("Select District"));
            emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spDistrict.setAdapter(emptyAdapter);
        }
        resetAreaSpinner();
    }
    
    private void resetAreaSpinner() {
        if (spArea != null) {
            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, Arrays.asList("Select Area"));
            emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spArea.setAdapter(emptyAdapter);
        }
    }

    private void setupListeners() {
        tvUploadPhoto.setOnClickListener(v -> mGetContent.launch("image/*"));
        btnSkipPhoto.setOnClickListener(v -> Toast.makeText(this, "Photo upload is recommended for profile completion", Toast.LENGTH_SHORT).show());
        btnNext.setOnClickListener(v -> validateAndProceed());
    }

    private void prefillUserData() {
        if (userName != null && !userName.isEmpty()) {
            etFullName.setText(userName);
        }
    }

    private void startCrop(Uri uri) {
        String destinationFileName = "GoogleProfileCrop.jpg";
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        uCrop.withAspectRatio(1, 1);
        uCrop.withMaxResultSize(450, 450);
        uCrop.withOptions(getCropOptions());
        uCrop.start(this);
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
                profileImageUri = resultUri;
                ivProfilePhoto.setImageURI(resultUri);
                tvUploadPhoto.setText("Photo Selected");
                tvUploadPhoto.setTextColor(getResources().getColor(R.color.teal_button));
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "Crop error: " + (cropError != null ? cropError.getMessage() : "Unknown"), Toast.LENGTH_SHORT).show();
        }
    }

    private void validateAndProceed() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String institute = etInstitute.getText().toString().trim();
        String about = etAbout.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (fullName.isEmpty()) {
            etFullName.setError("Name is required");
            etFullName.requestFocus();
            return;
        }

        if (phone.isEmpty() || phone.length() < 10) {
            etPhone.setError("Valid phone number is required");
            etPhone.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required for account security");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        String division = spDivision.getSelectedItem().toString();
        String district = spDistrict.getSelectedItem().toString();
        String area = spArea.getSelectedItem().toString();
        String studentClass = spClass.getSelectedItem().toString();
        String group = spGroup.getSelectedItem().toString();

        if (division.equals("Select Division")) {
            Toast.makeText(this, "Please select division", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton rbGender = findViewById(selectedGenderId);
        String gender = rbGender.getText().toString();

        // Link email/password credential to Google account
        linkPasswordToGoogleAccount(password, fullName, phone, institute, about, gender, 
                                    division, district, area, studentClass, group);
    }

    private void linkPasswordToGoogleAccount(String password, String fullName, String phone, 
                                            String institute, String about, String gender,
                                            String division, String district, String area, 
                                            String studentClass, String group) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || userEmail == null) {
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
            return;
        }

        btnNext.setEnabled(false);
        btnNext.setText("Processing...");

        // Create email/password credential
        AuthCredential credential = EmailAuthProvider.getCredential(userEmail, password);

        // Link to existing Google account
        user.linkWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Password linked successfully, now save profile
                        saveGoogleUserProfile(fullName, phone, institute, about, gender, 
                                             division, district, area, studentClass, group);
                    } else {
                        btnNext.setEnabled(true);
                        btnNext.setText("Next");
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Failed to set password: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveGoogleUserProfile(String fullName, String phone, String institute, String about, 
                                      String gender, String division, String district, String area, 
                                      String studentClass, String group) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", fullName);
        userData.put("email", userEmail);
        userData.put("phone", phone);
        userData.put("institute", institute);
        userData.put("about", about);
        userData.put("gender", gender);
        userData.put("division", division);
        userData.put("district", district);
        userData.put("area", area);
        userData.put("userType", userType);
        userData.put("loginProvider", "google");
        userData.put("registrationStep", "PROFILE_COMPLETED");
        userData.put("approvalStatus", "pending");
        userData.put("registrationTimestamp", System.currentTimeMillis());

        // Add Student-specific fields
        if ("Student".equals(userType)) {
            userData.put("class", studentClass);
            userData.put("group", group);
        }

        // Save profile image as Base64 if available
        if (profileImageUri != null) {
            String base64Image = Base64ImageHelper.convertUriToBase64(this, profileImageUri);
            if (base64Image != null) {
                userData.put("profileImageBase64", base64Image);
            }
        }

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // Profile saved, now go to document verification
                    Intent intent = new Intent(GoogleProfileCompletionActivity.this, DocumentVerificationActivity.class);
                    intent.putExtra("userData", (HashMap<String, Object>) userData);
                    intent.putExtra("fromGoogleSignIn", true);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnNext.setEnabled(true);
                    btnNext.setText("Next");
                    Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
