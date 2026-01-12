package com.sadid.myhometutor;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostTuitionActivity extends AppCompatActivity {

    private Spinner spSubject, spClass, spTuitionType, spGroup, spDivision, spDistrict, spThana;
    private EditText etDaysPerWeek, etHoursPerDay, etPreferredTiming, etSalary, etArea, etDetailedAddress, etAdditionalReq;
    private Button btnCancel, btnPostTuition;
    private Switch switchUrgent;
    private LinearLayout layoutLocationSection;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_tuition);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupSpinners();
        setupListeners();
    }

    private void initializeViews() {
        spSubject = findViewById(R.id.spSubject);
        spClass = findViewById(R.id.spClass);
        spTuitionType = findViewById(R.id.spTuitionType);
        spGroup = findViewById(R.id.spGroup);
        spDivision = findViewById(R.id.spDivision);
        spDistrict = findViewById(R.id.spDistrict);
        spThana = findViewById(R.id.spThana);

        etDaysPerWeek = findViewById(R.id.etDaysPerWeek);
        etHoursPerDay = findViewById(R.id.etHoursPerDay);
        etPreferredTiming = findViewById(R.id.etPreferredTiming);
        etSalary = findViewById(R.id.etSalary);
        etArea = findViewById(R.id.etArea);
        etDetailedAddress = findViewById(R.id.etDetailedAddress);
        etAdditionalReq = findViewById(R.id.etAdditionalReq);

        layoutLocationSection = findViewById(R.id.layoutLocationSection);

        btnCancel = findViewById(R.id.btnCancel);
        btnPostTuition = findViewById(R.id.btnPostTuition);
        // switchUrgent is optional - commented out since it doesn't exist in layout
        // switchUrgent = findViewById(R.id.switchUrgent);
    }

    private void setupSpinners() {
        // Setup basic spinners with static data
        setupSpinner(spSubject, new String[]{"Select Subject", "Bangla", "English", "Math", "Physics", "Chemistry", "Biology", "All Subjects"});
        setupSpinner(spClass, new String[]{"Select Class", "Class 1", "Class 2", "Class 3", "Class 4", "Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "HSC"});
        setupSpinner(spTuitionType, new String[]{"Select Type", "Offline", "Online"});
        setupSpinner(spGroup, new String[]{"Select Group", "Science", "Commerce", "Arts", "N/A"});
        
        // Setup location spinners with LocationDataHelper
        setupLocationSpinners();
        
        // Setup tuition type listener to toggle location visibility
        setupTuitionTypeListener();
    }

    private void setupLocationSpinners() {
        // Setup Division dropdown
        List<String> divisions = new ArrayList<>();
        divisions.add("Select Division");
        divisions.addAll(LocationDataHelper.getDivisions());
        ArrayAdapter<String> divisionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, divisions);
        divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDivision.setAdapter(divisionAdapter);

        // Setup District dropdown (initially empty, will populate when division is selected)
        List<String> districts = new ArrayList<>();
        districts.add("Select District");
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, districts);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDistrict.setAdapter(districtAdapter);

        // Setup Thana dropdown (initially empty, will populate when district is selected)
        List<String> thanas = new ArrayList<>();
        thanas.add("Select Thana");
        ArrayAdapter<String> thanaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, thanas);
        thanaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spThana.setAdapter(thanaAdapter);

        // Division selection listener
        spDivision.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String selectedDivision = divisions.get(position);
                    List<String> districtList = new ArrayList<>();
                    districtList.add("Select District");
                    districtList.addAll(LocationDataHelper.getDistricts(selectedDivision));
                    
                    ArrayAdapter<String> newDistrictAdapter = new ArrayAdapter<>(PostTuitionActivity.this, 
                            android.R.layout.simple_spinner_item, districtList);
                    newDistrictAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spDistrict.setAdapter(newDistrictAdapter);
                    spDistrict.setSelection(0);
                } else {
                    // Reset district and thana if no division selected
                    List<String> emptyDistricts = new ArrayList<>();
                    emptyDistricts.add("Select District");
                    ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(PostTuitionActivity.this, 
                            android.R.layout.simple_spinner_item, emptyDistricts);
                    emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spDistrict.setAdapter(emptyAdapter);
                    
                    List<String> emptyThanas = new ArrayList<>();
                    emptyThanas.add("Select Thana");
                    ArrayAdapter<String> emptyThanaAdapter = new ArrayAdapter<>(PostTuitionActivity.this, 
                            android.R.layout.simple_spinner_item, emptyThanas);
                    emptyThanaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spThana.setAdapter(emptyThanaAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // District selection listener
        spDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && spDivision.getSelectedItemPosition() > 0) {
                    String selectedDivision = spDivision.getSelectedItem().toString();
                    String selectedDistrict = spDistrict.getSelectedItem().toString();
                    
                    List<String> thanaList = new ArrayList<>();
                    thanaList.add("Select Thana");
                    thanaList.addAll(LocationDataHelper.getThanas(selectedDivision, selectedDistrict));
                    
                    ArrayAdapter<String> newThanaAdapter = new ArrayAdapter<>(PostTuitionActivity.this, 
                            android.R.layout.simple_spinner_item, thanaList);
                    newThanaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spThana.setAdapter(newThanaAdapter);
                    spThana.setSelection(0);
                } else {
                    // Reset thana if no district selected
                    List<String> emptyThanas = new ArrayList<>();
                    emptyThanas.add("Select Thana");
                    ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(PostTuitionActivity.this, 
                            android.R.layout.simple_spinner_item, emptyThanas);
                    emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spThana.setAdapter(emptyAdapter);
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

    private void setupTuitionTypeListener() {
        spTuitionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = spTuitionType.getSelectedItem().toString();
                
                if ("Online".equals(selectedType)) {
                    // Hide location section for online tuition
                    layoutLocationSection.setVisibility(View.GONE);
                } else if ("Offline".equals(selectedType)) {
                    // Show location section for offline tuition
                    layoutLocationSection.setVisibility(View.VISIBLE);
                } else {
                    // Default: hide if "Select Type" is chosen
                    layoutLocationSection.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Hide location section by default
                layoutLocationSection.setVisibility(View.GONE);
            }
        });
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> finish());
        btnPostTuition.setOnClickListener(v -> postTuition());
    }

    private void postTuition() {
        String selectedTuitionType = spTuitionType.getSelectedItem().toString();
        boolean isOnline = "Online".equals(selectedTuitionType);
        
        // Basic validation
        if (spSubject.getSelectedItemPosition() == 0 || spClass.getSelectedItemPosition() == 0 ||
            spTuitionType.getSelectedItemPosition() == 0 || spGroup.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Location validation only for offline tuition
        if (!isOnline) {
            if (spDivision.getSelectedItemPosition() == 0 || spDistrict.getSelectedItemPosition() == 0 ||
                spThana.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Please select all location fields for offline tuition", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String daysPerWeek = etDaysPerWeek.getText().toString();
        String hoursPerDay = etHoursPerDay.getText().toString();
        String preferredTiming = etPreferredTiming.getText().toString();
        String salary = etSalary.getText().toString();
        String area = etArea != null ? etArea.getText().toString() : "";
        String detailedAddress = etDetailedAddress != null ? etDetailedAddress.getText().toString() : "";
        String additionalReq = etAdditionalReq.getText().toString();

        // Text field validation (different for online vs offline)
        if (daysPerWeek.isEmpty() || hoursPerDay.isEmpty() || preferredTiming.isEmpty() || salary.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Additional validation for offline tuition
        if (!isOnline && (area.isEmpty() || detailedAddress.isEmpty())) {
            Toast.makeText(this, "Please fill location details for offline tuition", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> post = new HashMap<>();
        post.put("studentId", mAuth.getCurrentUser().getUid());
        post.put("subject", spSubject.getSelectedItem().toString());
        post.put("class", spClass.getSelectedItem().toString());
        post.put("tuitionType", selectedTuitionType);
        post.put("group", spGroup.getSelectedItem().toString());
        post.put("daysPerWeek", daysPerWeek);
        post.put("hoursPerDay", hoursPerDay);
        post.put("preferredTiming", preferredTiming);
        post.put("salary", salary);
        
        // Location fields: use actual values for offline, N/A for online
        if (isOnline) {
            post.put("division", "N/A");
            post.put("district", "N/A");
            post.put("thana", "N/A");
            post.put("area", "N/A");
            post.put("detailedAddress", "N/A");
        } else {
            post.put("division", spDivision.getSelectedItem().toString());
            post.put("district", spDistrict.getSelectedItem().toString());
            post.put("thana", spThana.getSelectedItem().toString());
            post.put("area", area);
            post.put("detailedAddress", detailedAddress);
        }
        
        post.put("additionalReq", additionalReq);
        post.put("isUrgent", switchUrgent != null && switchUrgent.isChecked());
        post.put("status", "open");
        post.put("timestamp", System.currentTimeMillis());

        db.collection("tuition_posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(PostTuitionActivity.this, "Post added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PostTuitionActivity.this, "Error adding post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
