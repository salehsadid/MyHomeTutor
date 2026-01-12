package com.sadid.myhometutor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sadid.myhometutor.adapters.AdminUserAdapter;
import com.sadid.myhometutor.models.PendingUser;
import com.sadid.myhometutor.repository.UserFilterRepository;

import java.util.ArrayList;
import java.util.List;

public class AdminTutorsActivity extends AppCompatActivity {

    private static final String TAG = "AdminTutorsActivity";
    
    private RecyclerView rvTutors;
    private TextView tvEmptyState;
    private ImageView btnBack;
    private Spinner spinnerFilter;
    private AdminUserAdapter adapter;
    private FirebaseFirestore db;
    private UserFilterRepository userFilterRepo;
    private List<PendingUser> tutorsList;
    private String currentFilter = "All";
    
    private ListenerRegistration tutorsListener;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_tutors);

        db = FirebaseFirestore.getInstance();
        userFilterRepo = new UserFilterRepository();
        tutorsList = new ArrayList<>();

        initializeViews();
        setupSpinner();
        setupRecyclerView();
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
                String selectedFilter = filters[position];
                if (!selectedFilter.equals(currentFilter)) {
                    currentFilter = selectedFilter;
                    removeListener();
                    attachListener();
                }
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

    private void attachListener() {
        Log.d(TAG, "Attaching listener for filter: " + currentFilter);
        
        UserFilterRepository.UsersListener listener = new UserFilterRepository.UsersListener() {
            @Override
            public void onUsersUpdated(com.google.firebase.firestore.QuerySnapshot snapshot) {
                Log.d(TAG, "Tutors data updated, count: " + snapshot.size());
                retryCount = 0; // Reset retry count on success
                tutorsList.clear();
                
                for (com.google.firebase.firestore.DocumentSnapshot document : snapshot.getDocuments()) {
                    try {
                        PendingUser user = documentToPendingUser(document);
                        if (user != null) {
                            tutorsList.add(user);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing user document: " + document.getId(), e);
                    }
                }

                updateUI();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading tutors", e);
                
                if (retryCount < MAX_RETRIES) {
                    retryCount++;
                    Toast.makeText(AdminTutorsActivity.this, 
                        "Connection issue. Retrying... (" + retryCount + "/" + MAX_RETRIES + ")", 
                        Toast.LENGTH_SHORT).show();
                    
                    // Retry after 2 seconds
                    rvTutors.postDelayed(() -> {
                        removeListener();
                        attachListener();
                    }, 2000);
                } else {
                    Toast.makeText(AdminTutorsActivity.this, 
                        "Unable to load tutors. Please check your connection.", 
                        Toast.LENGTH_LONG).show();
                    showEmptyState();
                }
            }
        };
        
        if (currentFilter.equals("All")) {
            tutorsListener = userFilterRepo.listenToAllTutors(listener);
        } else {
            String status = currentFilter.toLowerCase();
            tutorsListener = userFilterRepo.listenToTutorsByStatus(status, listener);
        }
    }

    private void removeListener() {
        if (tutorsListener != null) {
            Log.d(TAG, "Removing tutors listener");
            tutorsListener.remove();
            tutorsListener = null;
        }
    }

    private void updateUI() {
        if (tutorsList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No tutors found");
            rvTutors.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvTutors.setVisibility(View.VISIBLE);
        }
        
        adapter.notifyDataSetChanged();
    }

    private void showEmptyState() {
        tutorsList.clear();
        tvEmptyState.setVisibility(View.VISIBLE);
        tvEmptyState.setText("Unable to load data");
        rvTutors.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }

    private PendingUser documentToPendingUser(com.google.firebase.firestore.DocumentSnapshot document) {
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
    protected void onStart() {
        super.onStart();
        attachListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeListener();
    }
}
