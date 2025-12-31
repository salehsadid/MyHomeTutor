package com.sadid.myhometutor;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.util.Random;

public class TutorRegistrationActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etCollegeName, etHscYear, etUniversityName, etDepartment, etYearSemester, etSession, etOtp;
    private EditText etPreferredDays, etPreferredTime, etLocationDetails, etPreferredFee, etExperience, etAbout;
    private TextInputEditText etPassword, etConfirmPassword;
    private Spinner spDivision, spDistrict, spArea, spCollegeGroup, spPreferredClass;
    private RadioGroup rgGender;
    private Button btnUploadPhoto, btnVerifyEmail, btnBackToLogin, btnNext, btnConfirmOtp;
    private TextView tvConstraintLength, tvConstraintAlpha, tvConstraintNumber, tvConstraintCase, tvConstraintSymbol;
    private LinearLayout layoutOtpVerification;
    private ImageView ivEmailVerified;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Uri profileImageUri;
    private String generatedOtp;
    private boolean isEmailVerified = false;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    profileImageUri = uri;
                    btnUploadPhoto.setText("Photo Selected");
                    btnUploadPhoto.setBackgroundColor(Color.GREEN);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_registration);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupSpinners();
        setupPasswordValidation();
        setupButtons();
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

        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnVerifyEmail = findViewById(R.id.btnVerifyEmail);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        btnNext = findViewById(R.id.btnNext);
        btnConfirmOtp = findViewById(R.id.btnConfirmOtp);

        tvConstraintLength = findViewById(R.id.tvConstraintLength);
        tvConstraintAlpha = findViewById(R.id.tvConstraintAlpha);
        tvConstraintNumber = findViewById(R.id.tvConstraintNumber);
        tvConstraintCase = findViewById(R.id.tvConstraintCase);
        tvConstraintSymbol = findViewById(R.id.tvConstraintSymbol);

        layoutOtpVerification = findViewById(R.id.layoutOtpVerification);
        ivEmailVerified = findViewById(R.id.ivEmailVerified);
    }

    private void setupSpinners() {
        // Dummy data for spinners
        ArrayAdapter<String> divisionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Select Division", "Dhaka", "Chittagong", "Sylhet"});
        divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDivision.setAdapter(divisionAdapter);

        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Select District", "Dhaka", "Gazipur", "Narayanganj"});
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDistrict.setAdapter(districtAdapter);

        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Select Area", "Mirpur", "Uttara", "Dhanmondi"});
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spArea.setAdapter(areaAdapter);

        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Select Group", "Science", "Commerce", "Arts"});
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCollegeGroup.setAdapter(groupAdapter);

        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Select Class", "Class 1-5", "Class 6-8", "Class 9-10", "HSC", "Admission Test"});
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPreferredClass.setAdapter(classAdapter);
    }

    private void setupPasswordValidation() {
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void validatePassword(String password) {
        updateConstraint(tvConstraintLength, password.length() >= 8);
        updateConstraint(tvConstraintAlpha, Pattern.compile("[a-zA-Z]").matcher(password).find());
        updateConstraint(tvConstraintNumber, Pattern.compile("[0-9]").matcher(password).find());
        updateConstraint(tvConstraintCase, Pattern.compile("[a-z]").matcher(password).find() && Pattern.compile("[A-Z]").matcher(password).find());
        updateConstraint(tvConstraintSymbol, Pattern.compile("[^a-zA-Z0-9]").matcher(password).find());
    }

    private void updateConstraint(TextView textView, boolean isValid) {
        if (isValid) {
            textView.setText("✓ " + textView.getText().toString().substring(2));
            textView.setTextColor(Color.GREEN);
        } else {
            textView.setText("✕ " + textView.getText().toString().substring(2));
            textView.setTextColor(Color.parseColor("#FF5252"));
        }
    }

    private void setupButtons() {
        btnBackToLogin.setOnClickListener(v -> finish());

        btnNext.setOnClickListener(v -> registerUser());

        btnUploadPhoto.setOnClickListener(v -> mGetContent.launch("image/*"));

        btnVerifyEmail.setOnClickListener(v -> {
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
            layoutOtpVerification.setVisibility(View.VISIBLE);

            // Simulate sending email
            Toast.makeText(this, "OTP sent to " + email + ": " + generatedOtp, Toast.LENGTH_LONG).show();
        });

        btnConfirmOtp.setOnClickListener(v -> {
            String enteredOtp = etOtp.getText().toString();
            if (enteredOtp.equals(generatedOtp)) {
                isEmailVerified = true;
                layoutOtpVerification.setVisibility(View.GONE);
                btnVerifyEmail.setVisibility(View.GONE);
                ivEmailVerified.setVisibility(View.VISIBLE);
                etEmail.setEnabled(false);
                Toast.makeText(this, "Email Verified Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser() {
        if (!isEmailVerified) {
            Toast.makeText(this, "Please verify your email first", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = etFullName.getText().toString();
        String email = etEmail.getText().toString();
        String phone = etPhone.getText().toString();
        if (etPassword.getText() == null || etConfirmPassword.getText() == null) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Educational Info
        String collegeName = etCollegeName.getText().toString();
        String hscYear = etHscYear.getText().toString();
        String universityName = etUniversityName.getText().toString();
        String department = etDepartment.getText().toString();
        String yearSemester = etYearSemester.getText().toString();
        String session = etSession.getText().toString();

        // Tuition Preferences
        String preferredDays = etPreferredDays.getText().toString();
        String preferredTime = etPreferredTime.getText().toString();
        String locationDetails = etLocationDetails.getText().toString();
        String preferredFee = etPreferredFee.getText().toString();
        String experience = etExperience.getText().toString();
        String about = etAbout.getText().toString();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || universityName.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
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
        userData.put("collegeGroup", spCollegeGroup.getSelectedItem().toString());
        userData.put("hscYear", hscYear);
        userData.put("universityName", universityName);
        userData.put("department", department);
        userData.put("yearSemester", yearSemester);
        userData.put("session", session);

        // Tuition Preferences
        userData.put("division", spDivision.getSelectedItem().toString());
        userData.put("district", spDistrict.getSelectedItem().toString());
        userData.put("area", spArea.getSelectedItem().toString());
        userData.put("preferredClass", spPreferredClass.getSelectedItem().toString());
        userData.put("preferredDays", preferredDays);
        userData.put("preferredTime", preferredTime);
        userData.put("locationDetails", locationDetails);
        userData.put("preferredFee", preferredFee);
        userData.put("experience", experience);
        userData.put("about", about);

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId != -1) {
            RadioButton rbGender = findViewById(selectedGenderId);
            userData.put("gender", rbGender.getText().toString());
        }

        Intent intent = new Intent(TutorRegistrationActivity.this, DocumentVerificationActivity.class);
        intent.putExtra("userData", userData);
        intent.putExtra("password", password);
        if (profileImageUri != null) {
            intent.putExtra("profileImageUri", profileImageUri.toString());
        }
        startActivity(intent);
    }

    // ...existing code...
}
