package com.sadid.myhometutor;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sadid.myhometutor.repository.ConnectionRepository;

import java.util.ArrayList;
import java.util.List;

public class AdminConnectionsActivity extends AppCompatActivity {

    private static final String TAG = "AdminConnectionsActivity";
    
    private RecyclerView rvConnections;
    private TextView tvEmptyState;
    private ImageView btnBack;
    private ConnectionsAdapter adapter;
    private List<Connection> connectionsList;
    private FirebaseFirestore db;
    private ConnectionRepository connectionRepo;
    
    private ListenerRegistration connectionsListener;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_connections);

        db = FirebaseFirestore.getInstance();
        connectionRepo = new ConnectionRepository();
        
        initializeViews();
        setupRecyclerView();
    }

    private void initializeViews() {
        rvConnections = findViewById(R.id.rvConnections);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        connectionsList = new ArrayList<>();
        adapter = new ConnectionsAdapter(connectionsList);
        rvConnections.setLayoutManager(new LinearLayoutManager(this));
        rvConnections.setAdapter(adapter);
    }

    private void attachListener() {
        Log.d(TAG, "Attaching connections listener");
        
        ConnectionRepository.ConnectionsListener listener = new ConnectionRepository.ConnectionsListener() {
            @Override
            public void onConnectionsUpdated(com.google.firebase.firestore.QuerySnapshot snapshot) {
                Log.d(TAG, "Connections data updated, count: " + snapshot.size());
                retryCount = 0; // Reset retry count on success
                connectionsList.clear();
                
                for (com.google.firebase.firestore.DocumentSnapshot document : snapshot.getDocuments()) {
                    try {
                        Connection connection = new Connection();
                        connection.setId(document.getId());
                        connection.setStudentId(document.getString("studentId"));
                        connection.setTutorId(document.getString("tutorId"));
                        connection.setTuitionId(document.getString("postId")); // Using postId from applications
                        connection.setDate(document.getTimestamp("timestamp") != null ? 
                            document.getTimestamp("timestamp").toDate() : null);
                        
                        // Load user details asynchronously
                        loadUserDetails(connection);
                        
                        connectionsList.add(connection);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing connection document: " + document.getId(), e);
                    }
                }

                updateUI();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading connections", e);
                
                if (retryCount < MAX_RETRIES) {
                    retryCount++;
                    Toast.makeText(AdminConnectionsActivity.this, 
                        "Connection issue. Retrying... (" + retryCount + "/" + MAX_RETRIES + ")", 
                        Toast.LENGTH_SHORT).show();
                    
                    // Retry after 2 seconds
                    rvConnections.postDelayed(() -> {
                        removeListener();
                        attachListener();
                    }, 2000);
                } else {
                    Toast.makeText(AdminConnectionsActivity.this, 
                        "Unable to load connections. Please check your connection.", 
                        Toast.LENGTH_LONG).show();
                    showEmptyState();
                }
            }
        };
        
        connectionsListener = connectionRepo.listenToAllConnections(listener);
    }

    private void removeListener() {
        if (connectionsListener != null) {
            Log.d(TAG, "Removing connections listener");
            connectionsListener.remove();
            connectionsListener = null;
        }
    }

    private void updateUI() {
        if (connectionsList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No connections found");
            rvConnections.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvConnections.setVisibility(View.VISIBLE);
        }
        
        adapter.notifyDataSetChanged();
    }

    private void showEmptyState() {
        connectionsList.clear();
        tvEmptyState.setVisibility(View.VISIBLE);
        tvEmptyState.setText("Unable to load data");
        rvConnections.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }

    private void loadUserDetails(Connection connection) {
        // Load student name
        if (connection.getStudentId() != null) {
            db.collection("users").document(connection.getStudentId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name == null) {
                                name = documentSnapshot.getString("fullName");
                            }
                            connection.setStudentName(name);
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to load student details", e);
                    });
        }

        // Load tutor name
        if (connection.getTutorId() != null) {
            db.collection("users").document(connection.getTutorId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name == null) {
                                name = documentSnapshot.getString("fullName");
                            }
                            connection.setTutorName(name);
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to load tutor details", e);
                    });
        }

        // Load tuition details (for subject)
        if (connection.getTuitionId() != null) {
            db.collection("tuition_posts").document(connection.getTuitionId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            connection.setSubject(documentSnapshot.getString("subject"));
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to load tuition details", e);
                    });
        }
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
