package com.sadid.myhometutor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etAdminEmail, etAdminPassword;
    private Button btnAdminLogin;
    private TextView tvBackToLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        etAdminEmail = findViewById(R.id.etAdminEmail);
        etAdminPassword = findViewById(R.id.etAdminPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    private void setupListeners() {
        btnAdminLogin.setOnClickListener(v -> loginAdmin());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void loginAdmin() {
        String email = etAdminEmail.getText().toString().trim();
        String password = etAdminPassword.getText().toString();

        if (email.isEmpty()) {
            etAdminEmail.setError("Email is required");
            etAdminEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etAdminPassword.setError("Password is required");
            etAdminPassword.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Check if user is admin
                        String userId = mAuth.getCurrentUser().getUid();
                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String userType = documentSnapshot.getString("userType");
                                        if ("Admin".equals(userType)) {
                                            Toast.makeText(this, "Admin login successful", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(this, AdminDashboardActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(this, "You are not an admin", Toast.LENGTH_SHORT).show();
                                            mAuth.signOut();
                                        }
                                    } else {
                                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                                        mAuth.signOut();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error checking admin status: " + e.getMessage(), 
                                            Toast.LENGTH_SHORT).show();
                                    mAuth.signOut();
                                });
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
