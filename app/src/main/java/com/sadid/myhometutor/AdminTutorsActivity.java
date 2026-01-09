package com.sadid.myhometutor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sadid.myhometutor.adapters.AdminUserAdapter;
import com.sadid.myhometutor.models.PendingUser;

import java.util.ArrayList;
import java.util.List;

public class AdminTutorsActivity extends AppCompatActivity {

    private RecyclerView rvTutors;
    private TextView tvEmptyState;
    private ImageView btnBack;
    private Spinner spinnerFilter;
    private AdminUserAdapter adapter;
    private FirebaseFirestore db;
    private List<PendingUser> tutorsList;
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_tutors);

        db = FirebaseFirestore.getInstance();
        tutorsList = new ArrayList<>();

        initializeViews();
        setupSpinner();
        setupRecyclerView();
        loadTutors("All");
    }

    private void initializeViews() {
        rvTutors = findViewById(R.id.rvTutors);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnBack = findViewById(R.id.btnBack);
        spinnerFilter = findViewById(R.id.spinnerFilter);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupSpinner() {
        String[] filters = {"All", "Pending", "Approved", "Rejected"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, 
                android.R.layout.simple_spinner_item, 
                filters
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(spinnerAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = filters[position];
                loadTutors(currentFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new AdminUserAdapter(this, tutorsList, user -> {
            Intent intent = new Intent(AdminTutorsActivity.this, AdminViewUserActivity.class);
            intent.putExtra("userId", user.getUserId());
            intent.putExtra("userType", "Tutor");
            startActivity(intent);
        });

        rvTutors.setLayoutManager(new LinearLayoutManager(this));
        rvTutors.setAdapter(adapter);
    }

    private void loadTutors(String filter) {
        tutorsList.clear();
        
        var query = db.collection("users")
                .whereEqualTo("userType", "Tutor");

        if (!filter.equals("All")) {
            String status = filter.toLowerCase();
            query = query.whereEqualTo("approvalStatus", status);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        PendingUser user = documentToPendingUser(document);
                        if (user != null) {
                            tutorsList.add(user);
                        }
                    }

                    if (tutorsList.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        rvTutors.setVisibility(View.GONE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                        rvTutors.setVisibility(View.VISIBLE);
                    }
                    
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading tutors", Toast.LENGTH_SHORT).show();
                    tvEmptyState.setVisibility(View.VISIBLE);
                    rvTutors.setVisibility(View.GONE);
                });
    }

    private PendingUser documentToPendingUser(QueryDocumentSnapshot document) {
        PendingUser user = new PendingUser();
        user.setUserId(document.getId());
        user.setName(document.getString("name"));
        user.setEmail(document.getString("email"));
        user.setPhone(document.getString("phone"));
        user.setUserType(document.getString("userType"));
        user.setStatus(document.getString("approvalStatus"));
        user.setProfileImageBase64(document.getString("profileImageBase64"));
        user.setDocumentImageBase64(document.getString("documentImageBase64"));
        user.setDocumentType(document.getString("documentType"));
        
        // Tutor specific fields
        user.setUniversityName(document.getString("universityName"));
        user.setDepartment(document.getString("department"));
        user.setExperience(document.getString("experience"));
        
        Long timestamp = document.getLong("registrationTimestamp");
        if (timestamp != null) {
            user.setRegistrationTimestamp(timestamp);
        }
        
        return user;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTutors(currentFilter);
    }
}
