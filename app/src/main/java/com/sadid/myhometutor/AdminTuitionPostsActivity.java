package com.sadid.myhometutor;

import android.content.Intent;
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
import com.sadid.myhometutor.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;

public class AdminTuitionPostsActivity extends AppCompatActivity {

    private static final String TAG = "AdminTuitionPostsActivity";
    
    private RecyclerView rvTuitionPosts;
    private TextView tvEmptyState;
    private ImageView btnBack;
    private TuitionPostsAdapter adapter;
    private List<TuitionPostItem> postsList;
    private FirebaseFirestore db;
    private PostRepository postRepo;
    
    private ListenerRegistration postsListener;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_tuition_posts);

        db = FirebaseFirestore.getInstance();
        postRepo = new PostRepository();
        postsList = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
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

    private void attachListener() {
        Log.d(TAG, "Attaching tuition posts listener");
        
        PostRepository.PostsListener listener = new PostRepository.PostsListener() {
            @Override
            public void onPostsUpdated(com.google.firebase.firestore.QuerySnapshot snapshot) {
                Log.d(TAG, "Posts data updated, count: " + snapshot.size());
                retryCount = 0; // Reset retry count on success
                postsList.clear();

                for (com.google.firebase.firestore.DocumentSnapshot document : snapshot.getDocuments()) {
                    try {
                        TuitionPostItem post = new TuitionPostItem();
                        post.setId(document.getId());
                        post.setSubject(document.getString("subject"));
                        post.setStatus(document.getString("status"));
                        post.setStudentId(document.getString("studentId"));

                        // Load student name
                        loadStudentName(post);

                        postsList.add(post);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing post document: " + document.getId(), e);
                    }
                }

                updateUI();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading tuition posts", e);
                
                if (retryCount < MAX_RETRIES) {
                    retryCount++;
                    Toast.makeText(AdminTuitionPostsActivity.this, 
                        "Connection issue. Retrying... (" + retryCount + "/" + MAX_RETRIES + ")", 
                        Toast.LENGTH_SHORT).show();
                    
                    // Retry after 2 seconds
                    rvTuitionPosts.postDelayed(() -> {
                        removeListener();
                        attachListener();
                    }, 2000);
                } else {
                    Toast.makeText(AdminTuitionPostsActivity.this, 
                        "Unable to load posts. Please check your connection.", 
                        Toast.LENGTH_LONG).show();
                    showEmptyState();
                }
            }
        };
        
        postsListener = postRepo.listenToAllPosts(listener);
    }

    private void removeListener() {
        if (postsListener != null) {
            Log.d(TAG, "Removing posts listener");
            postsListener.remove();
            postsListener = null;
        }
    }

    private void updateUI() {
        if (postsList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No tuition posts found");
            rvTuitionPosts.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvTuitionPosts.setVisibility(View.VISIBLE);
        }
        
        adapter.notifyDataSetChanged();
    }

    private void showEmptyState() {
        postsList.clear();
        tvEmptyState.setVisibility(View.VISIBLE);
        tvEmptyState.setText("Unable to load data");
        rvTuitionPosts.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }

    private void loadStudentName(TuitionPostItem post) {
        if (post.getStudentId() != null) {
            db.collection("users").document(post.getStudentId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name == null) {
                                name = documentSnapshot.getString("fullName");
                            }
                            post.setStudentName(name);
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to load student name", e);
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
