package com.sadid.myhometutor;

import android.content.Intent;
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

public class AdminTuitionPostsActivity extends AppCompatActivity {

    private RecyclerView rvTuitionPosts;
    private TextView tvEmptyState;
    private ImageView btnBack;
    private TuitionPostsAdapter adapter;
    private List<TuitionPostItem> postsList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_tuition_posts);

        db = FirebaseFirestore.getInstance();
        postsList = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
        loadTuitionPosts();
    }

    private void initializeViews() {
        rvTuitionPosts = findViewById(R.id.rvTuitionPosts);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new TuitionPostsAdapter(postsList, post -> {
            // View post details
            Intent intent = new Intent(AdminTuitionPostsActivity.this, AdminViewTuitionPostActivity.class);
            intent.putExtra("postId", post.getId());
            startActivity(intent);
        });

        rvTuitionPosts.setLayoutManager(new LinearLayoutManager(this));
        rvTuitionPosts.setAdapter(adapter);
    }

    private void loadTuitionPosts() {
        db.collection("tuition_posts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postsList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        TuitionPostItem post = new TuitionPostItem();
                        post.setId(document.getId());
                        post.setSubject(document.getString("subject"));
                        post.setStatus(document.getString("status"));
                        post.setStudentId(document.getString("studentId"));

                        // Load student name
                        loadStudentName(post);

                        postsList.add(post);
                    }

                    if (postsList.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        rvTuitionPosts.setVisibility(View.GONE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                        rvTuitionPosts.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading tuition posts", Toast.LENGTH_SHORT).show();
                    tvEmptyState.setVisibility(View.VISIBLE);
                    rvTuitionPosts.setVisibility(View.GONE);
                });
    }

    private void loadStudentName(TuitionPostItem post) {
        if (post.getStudentId() != null) {
            db.collection("users").document(post.getStudentId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            post.setStudentName(documentSnapshot.getString("fullName"));
                            adapter.notifyDataSetChanged();
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTuitionPosts();
    }
}
