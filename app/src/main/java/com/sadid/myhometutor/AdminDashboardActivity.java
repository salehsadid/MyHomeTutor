package com.sadid.myhometutor;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvWelcome = findViewById(R.id.tvWelcome);

        loadUserData();
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String name = document.getString("name");
                            tvWelcome.setText("Welcome, Admin " + (name != null ? name : ""));
                        }
                    })
                    .addOnFailureListener(e -> 
                        Toast.makeText(this, "Error loading data: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show()
                    );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}
