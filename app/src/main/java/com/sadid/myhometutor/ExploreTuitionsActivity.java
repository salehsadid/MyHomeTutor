package com.sadid.myhometutor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExploreTuitionsActivity extends AppCompatActivity {

    private RecyclerView rvTuitionPosts;
    private TuitionPostAdapter adapter;
    private List<TuitionPost> postList;
    private List<TuitionPost> allPostList; // Store all posts for filtering
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private Button btnFilter, btnRefresh;
    private BottomSheetDialog filterDialog;

    // Filter spinners (from dialog)
    private Spinner spFilterClass, spFilterSubject, spFilterLocation, spFilterSalary, spFilterGender, spFilterType;
    
    // Store post IDs that have approved connections
    private List<String> connectedPostIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_tuitions);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        connectedPostIds = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
        setupListeners();
        setupFilterDialog();
        loadTuitionPosts();
    }

    private void initializeViews() {
        rvTuitionPosts = findViewById(R.id.rvTuitionPosts);
        btnFilter = findViewById(R.id.btnFilter);
        btnRefresh = findViewById(R.id.btnRefresh);
    }

    private void setupFilterDialog() {
        filterDialog = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_filter_bottom_sheet, null);
        filterDialog.setContentView(dialogView);

        // Initialize spinners from dialog
        spFilterClass = dialogView.findViewById(R.id.spFilterClass);
        spFilterSubject = dialogView.findViewById(R.id.spFilterSubject);
        spFilterLocation = dialogView.findViewById(R.id.spFilterLocation);
        spFilterSalary = dialogView.findViewById(R.id.spFilterSalary);
        spFilterGender = dialogView.findViewById(R.id.spFilterGender);
        spFilterType = dialogView.findViewById(R.id.spFilterType);

        Button btnApplyFilters = dialogView.findViewById(R.id.btnApplyFilters);
        Button btnClearFilters = dialogView.findViewById(R.id.btnClearFilters);

        // Setup spinners
        setupSpinners();

        // Apply filters button
        btnApplyFilters.setOnClickListener(v -> {
            applyFilters();
            filterDialog.dismiss();
        });

        // Clear filters button
        btnClearFilters.setOnClickListener(v -> {
            clearFilters();
        });
    }

    private void showFilterDialog() {
        if (filterDialog != null) {
            filterDialog.show();
        }
    }

    private void setupSpinners() {
        setupSpinner(spFilterClass, new String[]{"Class", "Class 1", "Class 2", "Class 3", "Class 4", "Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "HSC"});
        setupSpinner(spFilterSubject, new String[]{"Subject", "Bangla", "English", "Math", "Physics", "Chemistry", "Biology", "All Subjects"});
        setupSpinner(spFilterLocation, new String[]{"Location", "Dhaka", "Chittagong", "Sylhet", "Khulna", "Rajshahi", "Barisal", "Rangpur", "Mymensingh"});
        setupSpinner(spFilterSalary, new String[]{"Salary Range", "0-2000", "2000-5000", "5000-10000", "10000+"});
        setupSpinner(spFilterGender, new String[]{"Gender", "Male", "Female", "Any"});
        setupSpinner(spFilterType, new String[]{"Tuition Type", "Offline", "Online"});
    }

    private void setupSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        rvTuitionPosts.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        allPostList = new ArrayList<>();
        adapter = new TuitionPostAdapter(this, postList, this::applyForTuition);
        rvTuitionPosts.setAdapter(adapter);
    }

    private void setupListeners() {
        btnRefresh.setOnClickListener(v -> loadTuitionPosts());
        btnFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void loadTuitionPosts() {
        // First, get all post IDs that have approved connections
        db.collection("applications")
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(connectionSnapshot -> {
                    connectedPostIds.clear();
                    for (DocumentSnapshot doc : connectionSnapshot.getDocuments()) {
                        String postId = doc.getString("postId");
                        if (postId != null && !connectedPostIds.contains(postId)) {
                            connectedPostIds.add(postId);
                        }
                    }
                    
                    // Now load all approved tuition posts
                    db.collection("tuition_posts")
                            .whereIn("status", Arrays.asList("approved", "active"))
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                postList.clear();
                                allPostList.clear();
                                for (DocumentSnapshot document : queryDocumentSnapshots) {
                                    TuitionPost post = document.toObject(TuitionPost.class);
                                    if (post != null) {
                                        post.setId(document.getId());
                                        // Only add posts that don't have approved connections
                                        if (!connectedPostIds.contains(document.getId())) {
                                            postList.add(post);
                                            allPostList.add(post);
                                        }
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ExploreTuitionsActivity.this, "Error loading posts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ExploreTuitionsActivity.this, "Error checking connections: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void applyFilters() {
        // Basic filtering logic (client-side for simplicity, can be moved to Firestore queries)
        String selectedClass = spFilterClass.getSelectedItem().toString();
        String selectedSubject = spFilterSubject.getSelectedItem().toString();
        String selectedLocation = spFilterLocation.getSelectedItem().toString();
        String selectedType = spFilterType.getSelectedItem().toString();

        List<TuitionPost> filteredList = new ArrayList<>();
        for (TuitionPost post : allPostList) {
            boolean matches = true;
            if (!selectedClass.equals("Class") && !post.getGrade().equals(selectedClass)) matches = false;
            if (!selectedSubject.equals("Subject") && !post.getSubject().equals(selectedSubject)) matches = false;
            if (!selectedLocation.equals("Location") && !post.getDivision().equals(selectedLocation)) matches = false;
            if (!selectedType.equals("Tuition Type") && !post.getTuitionType().equals(selectedType)) matches = false;

            if (matches) {
                filteredList.add(post);
            }
        }
        adapter.updateList(filteredList);
    }

    private void clearFilters() {
        spFilterClass.setSelection(0);
        spFilterSubject.setSelection(0);
        spFilterLocation.setSelection(0);
        spFilterSalary.setSelection(0);
        spFilterGender.setSelection(0);
        spFilterType.setSelection(0);
        loadTuitionPosts();
    }

    private void applyForTuition(TuitionPost post) {
        String tutorId = mAuth.getCurrentUser().getUid();
        String postId = post.getId();

        Map<String, Object> application = new HashMap<>();
        application.put("tutorId", tutorId);
        application.put("postId", postId);
        application.put("studentId", post.getStudentId());
        application.put("status", "pending");
        application.put("timestamp", System.currentTimeMillis());

        db.collection("applications")
                .add(application)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ExploreTuitionsActivity.this, "Applied successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ExploreTuitionsActivity.this, "Error applying: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
