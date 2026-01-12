package com.sadid.myhometutor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MyPostsActivity extends AppCompatActivity {

    private RecyclerView rvMyPosts;
    private MyPostAdapter adapter;
    private List<TuitionPost> postList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Button btnRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupRecyclerView();
        setupListeners();
        loadMyPosts();
    }

    private void initializeViews() {
        rvMyPosts = findViewById(R.id.rvMyPosts);
        btnRefresh = findViewById(R.id.btnRefresh);
    }

    private void setupRecyclerView() {
        rvMyPosts.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        adapter = new MyPostAdapter(postList, this::deletePost);
        rvMyPosts.setAdapter(adapter);
    }

    private void setupListeners() {
        btnRefresh.setOnClickListener(v -> loadMyPosts());
    }

    private void loadMyPosts() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("tuition_posts")
                .whereEqualTo("studentId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        TuitionPost post = document.toObject(TuitionPost.class);
                        if (post != null) {
                            post.setId(document.getId());
                            postList.add(post);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MyPostsActivity.this, "Error loading posts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deletePost(TuitionPost post) {
        db.collection("tuition_posts").document(post.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MyPostsActivity.this, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                    loadMyPosts();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MyPostsActivity.this, "Error deleting post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
