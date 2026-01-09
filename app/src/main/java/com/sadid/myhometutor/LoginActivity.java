package com.sadid.myhometutor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleLogin;
    private TextView tvRegister, tvAdminLogin;
    private RadioGroup rgUserType;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvAdminLogin = findViewById(R.id.tvAdminLogin);
        rgUserType = findViewById(R.id.rgUserType);

        // Set up click listeners
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> loginUser());
        }

        if (btnGoogleLogin != null) {
            btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());
        }

        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                try {
                    String userType = getSelectedUserType();
                    Intent intent;
                    if ("Student".equals(userType)) {
                        intent = new Intent(LoginActivity.this, StudentRegistrationActivity.class);
                    } else {
                        intent = new Intent(LoginActivity.this, TutorRegistrationActivity.class);
                    }
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, "Error opening registration: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        }

        if (tvAdminLogin != null) {
            tvAdminLogin.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(LoginActivity.this, AdminLoginActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, "Error opening admin login: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        }

        // Configure Google Sign In
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        } catch (Exception e) {
            Toast.makeText(this, "Google Sign-In setup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithGoogle() {
        if (mGoogleSignInClient == null) {
            Toast.makeText(this, "Google Sign-In not available", Toast.LENGTH_SHORT).show();
            return;
        }
        // Sign out first to force account selection dialog
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkUserRoleAndRedirect();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getSelectedUserType() {
        int selectedId = rgUserType.getCheckedRadioButtonId();
        if (selectedId == R.id.rbStudent) {
            return "Student";
        } else {
            return "Tutor";
        }
    }

    private void loginUser() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkUserRoleAndRedirect();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRoleAndRedirect() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = user.getUid();
        String selectedUserType = getSelectedUserType(); // Get what user selected

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userType = documentSnapshot.getString("userType");
                        if (userType == null) {
                            Toast.makeText(LoginActivity.this, "User type not found. Please complete registration.", Toast.LENGTH_LONG).show();
                            mAuth.signOut(); // Sign out user
                            return;
                        }
                        
                        // Check approval status
                        String approvalStatus = documentSnapshot.getString("approvalStatus");
                        if ("pending".equals(approvalStatus)) {
                            mAuth.signOut();
                            Toast.makeText(LoginActivity.this, "Your account is pending admin approval. Please wait for approval email.", Toast.LENGTH_LONG).show();
                            return;
                        } else if ("rejected".equals(approvalStatus)) {
                            mAuth.signOut();
                            Toast.makeText(LoginActivity.this, "Your account has been rejected by admin. Please contact support.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        
                        // Check if selected user type matches actual user type
                        if (!selectedUserType.equals(userType)) {
                            mAuth.signOut(); // Sign out user
                            Toast.makeText(LoginActivity.this, "This account is registered as " + userType + ". Please select " + userType + " and try again, or use a different email.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent intent;
                        if ("Student".equals(userType)) {
                            intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, TutorDashboardActivity.class);
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // User doesn't exist in Firestore, create new user
                        createNewUser(user);
                    }
                })
                .addOnFailureListener(e -> {
                    String errorMessage = e.getMessage();
                    if (errorMessage != null && errorMessage.contains("offline")) {
                        Toast.makeText(LoginActivity.this, "No internet connection. Please check your network and try again.", Toast.LENGTH_LONG).show();
                    } else if (errorMessage != null && errorMessage.contains("PERMISSION_DENIED")) {
                        Toast.makeText(LoginActivity.this, "Database access denied. Please contact support.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Database error: " + errorMessage + "\n\nPlease check your internet connection.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createNewUser(FirebaseUser user) {
        String userType = getSelectedUserType();
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getDisplayName());
        userData.put("email", user.getEmail());
        userData.put("userType", userType);
        userData.put("approvalStatus", "pending");
        userData.put("registrationTimestamp", System.currentTimeMillis());
        // Add other default fields if necessary

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    mAuth.signOut();
                    Toast.makeText(LoginActivity.this, "Account Created Successfully! Please complete your profile via registration page.", Toast.LENGTH_LONG).show();
                    // Don't redirect, stay on login page
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Failed to create account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
