package com.sadid.myhometutor;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.core.content.ContextCompat;
import com.yalantis.ucrop.UCrop;
import java.io.File;
import java.util.Random;

public class TutorRegistrationActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etCollegeName, etHscYear, etUniversityName, etDepartment, etYearSemester, etSession, etOtp;
    private EditText etPreferredDays, etPreferredTime, etLocationDetails, etPreferredFee, etExperience, etAbout;
    private TextInputEditText etPassword, etConfirmPassword;
    private Spinner spDivision, spDistrict, spArea, spCollegeGroup, spPreferredClass;
    private RadioGroup rgGender;
    private Button btnVerifyEmail, btnBackToLogin, btnNext, btnConfirmOtp;
    private ImageView ivProfilePhoto;
    private TextView tvUploadPhoto, tvPasswordMatch, tvOtpTimer;
    private TextView tvConstraintLength, tvConstraintAlpha, tvConstraintNumber, tvConstraintCase, tvConstraintSymbol;
    private LinearLayout layoutOtpVerification;
    private ImageView ivEmailVerified;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Uri profileImageUri;
    private String generatedOtp;
    private boolean isEmailVerified = false;
    private CountDownTimer otpTimer;
    private boolean canResendOtp = true;
    
    private LinkedHashMap<String, LinkedHashMap<String, List<String>>> locationData;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    startCrop(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_tutor_registration);

            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            initializeViews();
            setupSpinners();
            setupPasswordValidation();
            setupButtons();
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing registration: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    private void initializeViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etOtp = findViewById(R.id.etOtp);

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
        etAbout = findViewById(R.id.etAbout);

        spDivision = findViewById(R.id.spDivision);
        spDistrict = findViewById(R.id.spDistrict);
        spArea = findViewById(R.id.spArea);
        spCollegeGroup = findViewById(R.id.spCollegeGroup);
        spPreferredClass = findViewById(R.id.spPreferredClass);

        rgGender = findViewById(R.id.rgGender);

        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvUploadPhoto = findViewById(R.id.tvUploadPhoto);
        btnVerifyEmail = findViewById(R.id.btnVerifyEmail);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        btnNext = findViewById(R.id.btnNext);
        btnConfirmOtp = findViewById(R.id.btnConfirmOtp);

        tvConstraintLength = findViewById(R.id.tvConstraintLength);
        tvConstraintAlpha = findViewById(R.id.tvConstraintAlpha);
        tvConstraintNumber = findViewById(R.id.tvConstraintNumber);
        tvConstraintCase = findViewById(R.id.tvConstraintCase);
        tvConstraintSymbol = findViewById(R.id.tvConstraintSymbol);
        tvPasswordMatch = findViewById(R.id.tvPasswordMatch);
        tvOtpTimer = findViewById(R.id.tvOtpTimer);

        layoutOtpVerification = findViewById(R.id.layoutOtpVerification);
        ivEmailVerified = findViewById(R.id.ivEmailVerified);
        
        locationData = LocationDataHelper.getLocationData();
    }

    private void setupSpinners() {
        // Setup Division Spinner with listener
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
                        updateDistrictSpinner(selectedDivision);
                    } else {
                        if (spDistrict != null) {
                            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(TutorRegistrationActivity.this, 
                                android.R.layout.simple_spinner_item, Arrays.asList("Select District"));
                            emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spDistrict.setAdapter(emptyAdapter);
                        }
                        if (spArea != null) {
                            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(TutorRegistrationActivity.this, 
                                android.R.layout.simple_spinner_item, Arrays.asList("Select Area"));
                            emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spArea.setAdapter(emptyAdapter);
                        }
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
                        updateAreaSpinner(selectedDistrict);
                    } else {
                        if (spArea != null) {
                            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(TutorRegistrationActivity.this, 
                                android.R.layout.simple_spinner_item, Arrays.asList("Select Area"));
                            emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spArea.setAdapter(emptyAdapter);
                        }
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

        if (spCollegeGroup != null) {
            ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Select Group", "Science", "Commerce", "Arts"});
            groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spCollegeGroup.setAdapter(groupAdapter);
        }

        if (spPreferredClass != null) {
            ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Select Class", "Class 1-5", "Class 6-8", "Class 9-10", "HSC", "Admission Test"});
            classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spPreferredClass.setAdapter(classAdapter);
        }
    }
    
    private void updateDistrictSpinner(String division) {
        if (spDistrict != null && locationData.containsKey(division)) {
            List<String> districts = new ArrayList<>();
            districts.add("Select District");
            districts.addAll(locationData.get(division).keySet());
            ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, districts);
            districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spDistrict.setAdapter(districtAdapter);
        }
    }
    
    private void updateAreaSpinner(String district) {
        if (spArea != null) {
            List<String> areas = new ArrayList<>();
            areas.add("Select Area");
            
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

    private void setupPasswordValidation() {
        if (etPassword == null) return;
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
                checkPasswordMatch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        if (etConfirmPassword == null) return;
        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkPasswordMatch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void checkPasswordMatch() {
        if (etPassword == null || etConfirmPassword == null || tvPasswordMatch == null) return;
        
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";
        
        if (confirmPassword.isEmpty()) {
            tvPasswordMatch.setText("");
            return;
        }
        
        if (password.equals(confirmPassword)) {
            tvPasswordMatch.setText("✓ Password matched");
            tvPasswordMatch.setTextColor(Color.GREEN);
        } else {
            tvPasswordMatch.setText("✕ Password doesn't match");
            tvPasswordMatch.setTextColor(Color.parseColor("#FF5252"));
        }
    }

    private void validatePassword(String password) {
        updateConstraintUI(tvConstraintLength, password.length() >= 8);
        updateConstraintUI(tvConstraintAlpha, Pattern.compile("[a-zA-Z]").matcher(password).find());
        updateConstraintUI(tvConstraintNumber, Pattern.compile("[0-9]").matcher(password).find());
        updateConstraintUI(tvConstraintCase, Pattern.compile("[a-z]").matcher(password).find() && Pattern.compile("[A-Z]").matcher(password).find());
        updateConstraintUI(tvConstraintSymbol, Pattern.compile("[^a-zA-Z0-9]").matcher(password).find());
    }

    private void updateConstraintUI(TextView textView, boolean isValid) {
        if (textView == null || textView.getText() == null) return;
        String text = textView.getText().toString();
        // Remove any existing prefix (✓ or ✕)
        if (text.length() > 2 && (text.startsWith("✓ ") || text.startsWith("✕ "))) {
            text = text.substring(2);
        }
        if (isValid) {
            textView.setText("✓ " + text);
            textView.setTextColor(Color.GREEN);
        } else {
            textView.setText("✕ " + text);
            textView.setTextColor(Color.parseColor("#FF5252"));
        }
    }

    private void setupButtons() {
        if (btnBackToLogin != null) {
            btnBackToLogin.setOnClickListener(v -> finish());
        }

        if (btnNext != null) {
            btnNext.setOnClickListener(v -> registerUser());
        }

        if (ivProfilePhoto != null) {
            ivProfilePhoto.setOnClickListener(v -> mGetContent.launch("image/*"));
        }
        if (tvUploadPhoto != null) {
            tvUploadPhoto.setOnClickListener(v -> mGetContent.launch("image/*"));
        }

        if (btnVerifyEmail != null) {
            btnVerifyEmail.setOnClickListener(v -> {
                if (etEmail == null) {
                    Toast.makeText(this, "Email field not found", Toast.LENGTH_SHORT).show();
                    return;
                }
                String email = etEmail.getText().toString();
                if (email.isEmpty()) {
                    Toast.makeText(this, "Please enter email first", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Generate 6 digit OTP
                Random random = new Random();
                int otp = 100000 + random.nextInt(900000);
                generatedOtp = String.valueOf(otp);

                // Show OTP layout
                if (layoutOtpVerification != null) {
                    layoutOtpVerification.setVisibility(View.VISIBLE);
                }

                // Send OTP via Email
                com.sadid.myhometutor.utils.EmailSender.sendOTP(this, email, generatedOtp);
                
                // Start 30-second countdown timer
                startOtpTimer();
            });
        }

        if (btnConfirmOtp != null) {
            btnConfirmOtp.setOnClickListener(v -> {
                if (etOtp == null) return;
                String enteredOtp = etOtp.getText().toString();
                if (enteredOtp.equals(generatedOtp)) {
                    isEmailVerified = true;
                    // Cancel the timer when OTP is verified
                    if (otpTimer != null) {
                        otpTimer.cancel();
                    }
                    if (tvOtpTimer != null) tvOtpTimer.setVisibility(View.GONE);
                    if (layoutOtpVerification != null) layoutOtpVerification.setVisibility(View.GONE);
                    if (btnVerifyEmail != null) btnVerifyEmail.setVisibility(View.GONE);
                    if (ivEmailVerified != null) ivEmailVerified.setVisibility(View.VISIBLE);
                    if (etEmail != null) etEmail.setEnabled(false);
                    Toast.makeText(this, "Email Verified Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void startCrop(Uri uri) {
        String destinationFileName = "SampleCropImage.jpg";
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
        options.setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.teal_button));
        options.setFreeStyleCropEnabled(false);
        options.setHideBottomControls(false);
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.white));
        // Use custom UCrop activity with proper insets handling
        options.setToolbarTitle("Crop Profile Photo");
        return options;
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
                tvUploadPhoto.setTextColor(Color.GREEN);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "Crop error: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUser() {
        if (!isEmailVerified) {
            Toast.makeText(this, "Please verify your email with OTP first!", Toast.LENGTH_LONG).show();
            // Scroll to email field to make it visible
            if (etEmail != null) {
                etEmail.requestFocus();
            }
            return;
        }

        String fullName = etFullName != null ? etFullName.getText().toString() : "";
        String email = etEmail != null ? etEmail.getText().toString() : "";
        String phone = etPhone != null ? etPhone.getText().toString() : "";
        if (etPassword == null || etPassword.getText() == null || etConfirmPassword == null || etConfirmPassword.getText() == null) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Educational Info
        String collegeName = etCollegeName != null ? etCollegeName.getText().toString() : "";
        String hscYear = etHscYear != null ? etHscYear.getText().toString() : "";
        String universityName = etUniversityName != null ? etUniversityName.getText().toString() : "";
        String department = etDepartment != null ? etDepartment.getText().toString() : "";
        String yearSemester = etYearSemester != null ? etYearSemester.getText().toString() : "";
        String session = etSession != null ? etSession.getText().toString() : "";

        // Tuition Preferences
        String preferredDays = etPreferredDays != null ? etPreferredDays.getText().toString() : "";
        String preferredTime = etPreferredTime != null ? etPreferredTime.getText().toString() : "";
        String locationDetails = etLocationDetails != null ? etLocationDetails.getText().toString() : "";
        String preferredFee = etPreferredFee != null ? etPreferredFee.getText().toString() : "";
        String experience = etExperience != null ? etExperience.getText().toString() : "";
        String about = etAbout != null ? etAbout.getText().toString() : "";

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || universityName.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields (Name, Email, Phone, Password, University)", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> userData = new HashMap<>();
        userData.put("name", fullName);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("userType", "Tutor");

        // Educational Info
        userData.put("collegeName", collegeName);
        userData.put("collegeGroup", spCollegeGroup != null ? spCollegeGroup.getSelectedItem().toString() : "");
        userData.put("hscYear", hscYear);
        userData.put("universityName", universityName);
        userData.put("department", department);
        userData.put("yearSemester", yearSemester);
        userData.put("session", session);

        // Tuition Preferences
        userData.put("division", spDivision != null ? spDivision.getSelectedItem().toString() : "");
        userData.put("district", spDistrict != null ? spDistrict.getSelectedItem().toString() : "");
        userData.put("area", spArea != null ? spArea.getSelectedItem().toString() : "");
        userData.put("preferredClass", spPreferredClass != null ? spPreferredClass.getSelectedItem().toString() : "");
        userData.put("preferredDays", preferredDays);
        userData.put("preferredTime", preferredTime);
        userData.put("locationDetails", locationDetails);
        userData.put("preferredFee", preferredFee);
        userData.put("experience", experience);
        userData.put("about", about);

        int selectedGenderId = rgGender != null ? rgGender.getCheckedRadioButtonId() : -1;
        if (selectedGenderId != -1) {
            RadioButton rbGender = findViewById(selectedGenderId);
            userData.put("gender", rbGender.getText().toString());
        }

        // Add registration step tracking
        userData.put("registrationStep", "profile");
        userData.put("approvalStatus", "pending");
        userData.put("registrationTimestamp", System.currentTimeMillis());
        
        // Proceed to Document Verification without creating auth account yet
        Toast.makeText(this, "Proceeding to Document Verification...", Toast.LENGTH_SHORT).show();
        
        Intent intent = new Intent(TutorRegistrationActivity.this, DocumentVerificationActivity.class);
        intent.putExtra("userData", userData);
        intent.putExtra("password", password);
        if (profileImageUri != null) {
            intent.putExtra("profileImageUri", profileImageUri.toString());
        }
        startActivity(intent);
    }
    
    private void startOtpTimer() {
        canResendOtp = false;
        btnVerifyEmail.setEnabled(false);
        btnVerifyEmail.setAlpha(0.5f);
        tvOtpTimer.setVisibility(View.VISIBLE);
        
        if (otpTimer != null) {
            otpTimer.cancel();
        }
        
        otpTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                tvOtpTimer.setText("Resend OTP in " + secondsRemaining + " seconds");
            }

            @Override
            public void onFinish() {
                canResendOtp = true;
                btnVerifyEmail.setEnabled(true);
                btnVerifyEmail.setAlpha(1.0f);
                btnVerifyEmail.setText("Resend OTP");
                tvOtpTimer.setVisibility(View.GONE);
            }
        }.start();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpTimer != null) {
            otpTimer.cancel();
        }
    }

    // ...existing code...
}
