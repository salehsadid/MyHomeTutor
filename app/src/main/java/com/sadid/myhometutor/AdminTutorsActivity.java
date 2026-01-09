package com.sadid.myhometutor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
    private Button btnAllTutors, btnPendingTutors;
    private AdminUserAdapter adapter;
    private FirebaseFirestore db;
    private List<PendingUser> tutorsList;
    private boolean showingPending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_students);

        db = FirebaseFirestore.getInstance();
        tutorsList = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
        setupListeners();
        loadAllTutors();
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tutors Management");
        }

        rvTutors = findViewById(R.id.rvStudents);
        btnAllTutors = findViewById(R.id.btnAllStudents);
        btnPendingTutors = findViewById(R.id.btnPendingStudents);
        
        btnAllTutors.setText("All Tutors");
        btnPendingTutors.setText("Pending Tutors");
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

    private void setupListeners() {
        btnAllTutors.setOnClickListener(v -> {
            showingPending = false;
            updateButtonStyles();
            loadAllTutors();
        });

        btnPendingTutors.setOnClickListener(v -> {
            showingPending = true;
            updateButtonStyles();
            loadPendingTutors();
        });
    }

    private void updateButtonStyles() {
        if (showingPending) {
            btnPendingTutors.setBackgroundColor(getResources().getColor(R.color.teal_button));
            btnAllTutors.setBackgroundResource(R.drawable.bg_button_outline);
        } else {
            btnAllTutors.setBackgroundColor(getResources().getColor(R.color.teal_button));
            btnPendingTutors.setBackgroundResource(R.drawable.bg_button_outline);
        }
    }

    private void loadAllTutors() {
        tutorsList.clear();
        db.collection("users")
                .whereEqualTo("userType", "Tutor")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        PendingUser user = documentToPendingUser(document);
                        if (user != null) {
                            tutorsList.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error loading tutors", Toast.LENGTH_SHORT).show()
                );
    }

    private void loadPendingTutors() {
        tutorsList.clear();
        db.collection("users")
                .whereEqualTo("userType", "Tutor")
                .whereEqualTo("approvalStatus", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        PendingUser user = documentToPendingUser(document);
                        if (user != null) {
                            tutorsList.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    
                    if (tutorsList.isEmpty()) {
                        Toast.makeText(this, "No pending tutors", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error loading pending tutors", Toast.LENGTH_SHORT).show()
                );
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
        if (showingPending) {
            loadPendingTutors();
        } else {
            loadAllTutors();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
