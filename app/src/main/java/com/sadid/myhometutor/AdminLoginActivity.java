package com.sadid.myhometutor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etAdminEmail;
    private TextInputEditText etAdminPassword;
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

        // Hardcoded admin credentials
        if ("myhometutor.manager@gmail.com".equals(email) && "admin".equals(password)) {
            Toast.makeText(this, "Admin login successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AdminDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_SHORT).show();
        }
    }
}
