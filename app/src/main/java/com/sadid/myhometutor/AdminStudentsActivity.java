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

public class AdminStudentsActivity extends AppCompatActivity {

    private RecyclerView rvStudents;
    private Button btnAllStudents, btnPendingStudents;
    private AdminUserAdapter adapter;
    private FirebaseFirestore db;
    private List<PendingUser> studentsList;
    private boolean showingPending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_students);

        db = FirebaseFirestore.getInstance();
        studentsList = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
        setupListeners();
        loadAllStudents();
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvStudents = findViewById(R.id.rvStudents);
        btnAllStudents = findViewById(R.id.btnAllStudents);
        btnPendingStudents = findViewById(R.id.btnPendingStudents);
    }

    private void setupRecyclerView() {
        adapter = new AdminUserAdapter(this, studentsList, user -> {
            // On item click, open detail view
            Intent intent = new Intent(AdminStudentsActivity.this, AdminViewUserActivity.class);
            intent.putExtra("userId", user.getUserId());
            intent.putExtra("userType", "Student");
            startActivity(intent);
        });

        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        rvStudents.setAdapter(adapter);
    }

    private void setupListeners() {
        btnAllStudents.setOnClickListener(v -> {
            showingPending = false;
            updateButtonStyles();
            loadAllStudents();
        });

        btnPendingStudents.setOnClickListener(v -> {
            showingPending = true;
            updateButtonStyles();
            loadPendingStudents();
        });
    }

    private void updateButtonStyles() {
        if (showingPending) {
            btnPendingStudents.setBackgroundColor(getResources().getColor(R.color.teal_button));
            btnAllStudents.setBackgroundResource(R.drawable.bg_button_outline);
        } else {
            btnAllStudents.setBackgroundColor(getResources().getColor(R.color.teal_button));
            btnPendingStudents.setBackgroundResource(R.drawable.bg_button_outline);
        }
    }

    private void loadAllStudents() {
        studentsList.clear();
        db.collection("users")
                .whereEqualTo("userType", "Student")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        PendingUser user = documentToPendingUser(document);
                        if (user != null) {
                            studentsList.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error loading students", Toast.LENGTH_SHORT).show()
                );
    }

    private void loadPendingStudents() {
        studentsList.clear();
        db.collection("users")
                .whereEqualTo("userType", "Student")
                .whereEqualTo("approvalStatus", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        PendingUser user = documentToPendingUser(document);
                        if (user != null) {
                            studentsList.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    
                    if (studentsList.isEmpty()) {
                        Toast.makeText(this, "No pending students", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error loading pending students", Toast.LENGTH_SHORT).show()
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
        
        // Student specific fields
        user.setInstitute(document.getString("institute"));
        user.setStudentClass(document.getString("class"));
        user.setGroup(document.getString("group"));
        
        Long timestamp = document.getLong("registrationTimestamp");
        if (timestamp != null) {
            user.setRegistrationTimestamp(timestamp);
        }
        
        return user;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from detail view
        if (showingPending) {
            loadPendingStudents();
        } else {
            loadAllStudents();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
