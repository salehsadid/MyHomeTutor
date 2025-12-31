package com.sadid.myhometutor;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etSecondaryEmail, etPhone, etInstitute, etAbout;
    private EditText etCollegeName, etHscYear, etUniversityName, etDepartment, etYearSemester, etSession;
    private EditText etPreferredDays, etPreferredTime, etLocationDetails, etPreferredFee, etExperience;
    private Spinner spGender, spDivision, spDistrict, spArea, spClass, spGroup, spCollegeGroup, spPreferredClass;
    private LinearLayout layoutStudentFields, layoutTutorFields;
    private Button btnCancel, btnSaveChanges;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupSpinners();
        loadUserData();
        setupListeners();
    }

    private void initializeViews() {
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
        setupSpinner(spDivision, new String[]{"Select Division", "Dhaka", "Chittagong", "Sylhet", "Khulna", "Rajshahi", "Barisal", "Rangpur", "Mymensingh"});
        setupSpinner(spDistrict, new String[]{"Select District", "Dhaka", "Gazipur", "Narayanganj", "Khulna"});
        setupSpinner(spArea, new String[]{"Select Area", "Mirpur", "Uttara", "Dhanmondi", "Gulshan", "Area 1"});
        setupSpinner(spClass, new String[]{"Select Class", "Class 1", "Class 2", "Class 3", "Class 4", "Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "HSC"});
        setupSpinner(spGroup, new String[]{"Select Group", "Science", "Commerce", "Arts", "N/A"});
        setupSpinner(spCollegeGroup, new String[]{"Select Group", "Science", "Commerce", "Arts"});
        setupSpinner(spPreferredClass, new String[]{"Select Class", "Class 1-5", "Class 6-8", "Class 9-10", "HSC", "Admission Test"});
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
        btnCancel.setOnClickListener(v -> finish());
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", etFullName.getText().toString());
        updates.put("secondaryEmail", etSecondaryEmail.getText().toString());
        updates.put("phone", etPhone.getText().toString());
        updates.put("about", etAbout.getText().toString());
        updates.put("gender", spGender.getSelectedItem().toString());
        updates.put("division", spDivision.getSelectedItem().toString());
        updates.put("district", spDistrict.getSelectedItem().toString());
        updates.put("area", spArea.getSelectedItem().toString());

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
}
