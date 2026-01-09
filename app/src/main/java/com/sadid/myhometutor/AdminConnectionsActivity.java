package com.sadid.myhometutor;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminConnectionsActivity extends AppCompatActivity {

    private RecyclerView rvConnections;
    private TextView tvEmptyState;
    private ImageView btnBack;
    private ConnectionsAdapter adapter;
    private List<Connection> connectionsList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_connections);

        db = FirebaseFirestore.getInstance();
        
        initializeViews();
        setupRecyclerView();
        loadConnections();
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

    private void loadConnections() {
        db.collection("applications")
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    connectionsList.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Connection connection = new Connection();
                        connection.setId(document.getId());
                        connection.setStudentId(document.getString("studentId"));
                        connection.setTutorId(document.getString("tutorId"));
                        connection.setTuitionId(document.getString("tuitionId"));
                        connection.setDate(document.getDate("acceptedAt"));
                        
                        // Load student and tutor names
                        loadUserDetails(connection);
                        
                        connectionsList.add(connection);
                    }

                    if (connectionsList.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        rvConnections.setVisibility(View.GONE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                        rvConnections.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading connections", Toast.LENGTH_SHORT).show();
                    tvEmptyState.setVisibility(View.VISIBLE);
                    rvConnections.setVisibility(View.GONE);
                });
    }

    private void loadUserDetails(Connection connection) {
        // Load student name
        if (connection.getStudentId() != null) {
            db.collection("users").document(connection.getStudentId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            connection.setStudentName(documentSnapshot.getString("fullName"));
                            adapter.notifyDataSetChanged();
                        }
                    });
        }

        // Load tutor name
        if (connection.getTutorId() != null) {
            db.collection("users").document(connection.getTutorId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            connection.setTutorName(documentSnapshot.getString("fullName"));
                            adapter.notifyDataSetChanged();
                        }
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
                    });
        }
    }
}
