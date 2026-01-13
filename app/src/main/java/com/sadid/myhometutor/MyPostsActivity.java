package com.sadid.myhometutor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * MyPostsActivity - Shows student's tuition posts
 * 
 * Students can:
 * - View all their posts
 * - View applications for each post (to accept/reject tutors)
 * - Delete posts
 */
public class MyPostsActivity extends AppCompatActivity {

    private RecyclerView rvMyPosts;
    private MyPostAdapter adapter;
    private List<TuitionPost> postList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Button btnRefresh;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupRecyclerView();
        setupSearch();
        setupListeners();
        loadMyPosts();
    }

    private void initializeViews() {
        rvMyPosts = findViewById(R.id.rvMyPosts);
        btnRefresh = findViewById(R.id.btnRefresh);
        searchView = findViewById(R.id.searchView);
    }
    
    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (adapter != null) {
                    adapter.getFilter().filter(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }

    private void setupRecyclerView() {
        rvMyPosts.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        adapter = new MyPostAdapter(postList, this::deletePost, this::viewApplications);
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
                    adapter.updateList(postList); // Update original list copy for filtering
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
    
    private void viewApplications(TuitionPost post) {
        Intent intent = new Intent(this, ViewApplicationsActivity.class);
        intent.putExtra("postId", post.getId());
        intent.putExtra("postSubject", post.getSubject());
        intent.putExtra("postGrade", post.getGrade());
        startActivity(intent);
    }
}
