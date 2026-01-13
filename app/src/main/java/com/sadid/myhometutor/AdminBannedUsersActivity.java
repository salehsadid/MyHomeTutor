package com.sadid.myhometutor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sadid.myhometutor.adapters.AdminUserAdapter;
import com.sadid.myhometutor.models.PendingUser;

import java.util.ArrayList;
import java.util.List;

public class AdminBannedUsersActivity extends AppCompatActivity {

    private RecyclerView rvBannedUsers;
    private TextView tvEmptyState;
    private ImageView btnBack, btnSearch;
    private CardView searchContainer;
    private SearchView searchView;
    private AdminUserAdapter adapter;
    private FirebaseFirestore db;
    private List<PendingUser> bannedUsersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_banned_users);

        db = FirebaseFirestore.getInstance();
        bannedUsersList = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
        // loadBannedUsers(); // Removed to prevent duplicate loading onResume
    }

    private void initializeViews() {
        rvBannedUsers = findViewById(R.id.rvBannedUsers);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnBack = findViewById(R.id.btnBack);
        
        btnSearch = findViewById(R.id.btnSearch);
        searchContainer = findViewById(R.id.searchContainer);
        searchView = findViewById(R.id.searchView);

        btnBack.setOnClickListener(v -> finish());
        
        btnSearch.setOnClickListener(v -> {
            if (searchContainer.getVisibility() == View.VISIBLE) {
                searchContainer.setVisibility(View.GONE);
                searchView.setQuery("", false);
            } else {
                searchContainer.setVisibility(View.VISIBLE);
                searchView.setIconified(false);
            }
        });
        
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return true;
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new AdminUserAdapter(this, bannedUsersList, user -> {
            Intent intent = new Intent(AdminBannedUsersActivity.this, AdminViewUserActivity.class);
            intent.putExtra("userId", user.getUserId());
            intent.putExtra("userType", user.getUserType());
            startActivity(intent);
        });

        rvBannedUsers.setLayoutManager(new LinearLayoutManager(this));
        rvBannedUsers.setAdapter(adapter);
    }

    private void loadBannedUsers() {
        // bannedUsersList.clear(); // Moved inside onSuccess to prevent race conditions
        
        db.collection("users")
                .whereEqualTo("isBanned", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bannedUsersList.clear(); // Clear list before adding new data
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        PendingUser user = documentToPendingUser(document);
                        if (user != null) {
                            bannedUsersList.add(user);
                        }
                    }

                    if (bannedUsersList.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        rvBannedUsers.setVisibility(View.GONE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                        rvBannedUsers.setVisibility(View.VISIBLE);
                    }
                    
                    adapter.updateList(new ArrayList<>(bannedUsersList));
                    
                    if (searchView != null && !searchView.getQuery().toString().isEmpty()) {
                        adapter.getFilter().filter(searchView.getQuery());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading banned users", Toast.LENGTH_SHORT).show();
                    tvEmptyState.setVisibility(View.VISIBLE);
                    rvBannedUsers.setVisibility(View.GONE);
                });
    }

    private PendingUser documentToPendingUser(QueryDocumentSnapshot document) {
        PendingUser user = new PendingUser();
        user.setUserId(document.getId());
        user.setName(document.getString("name"));
        user.setEmail(document.getString("email"));
        user.setPhone(document.getString("phone"));
        user.setUserType(document.getString("userType"));
        user.setStatus("BANNED");
        user.setProfileImageBase64(document.getString("profileImageBase64"));
        user.setDocumentImageBase64(document.getString("documentImageBase64"));
        user.setDocumentType(document.getString("documentType"));
        
        Long timestamp = document.getLong("registrationTimestamp");
        if (timestamp != null) {
            user.setRegistrationTimestamp(timestamp);
        }
        
        return user;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBannedUsers();
    }
}
