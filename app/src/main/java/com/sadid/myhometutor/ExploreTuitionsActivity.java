package com.sadid.myhometutor;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExploreTuitionsActivity extends AppCompatActivity {

    private RecyclerView rvTuitionPosts;
    private TuitionPostAdapter adapter;
    private List<TuitionPost> postList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private Spinner spFilterClass, spFilterSubject, spFilterLocation, spFilterSalary, spFilterGender, spFilterType;
    private Button btnApplyFilters, btnClearFilters, btnBack, btnRefresh;
    private Switch switchToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_tuitions);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupSpinners();
        setupRecyclerView();
        setupListeners();
        loadTuitionPosts();
    }

    private void initializeViews() {
        rvTuitionPosts = findViewById(R.id.rvTuitionPosts);
        spFilterClass = findViewById(R.id.spFilterClass);
        spFilterSubject = findViewById(R.id.spFilterSubject);
        spFilterLocation = findViewById(R.id.spFilterLocation);
        spFilterSalary = findViewById(R.id.spFilterSalary);
        spFilterGender = findViewById(R.id.spFilterGender);
        spFilterType = findViewById(R.id.spFilterType);
        btnApplyFilters = findViewById(R.id.btnApplyFilters);
        btnClearFilters = findViewById(R.id.btnClearFilters);
        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnRefresh);

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
        adapter = new TuitionPostAdapter(postList, this::applyForTuition);
        rvTuitionPosts.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnRefresh.setOnClickListener(v -> loadTuitionPosts());
        btnApplyFilters.setOnClickListener(v -> applyFilters());
        btnClearFilters.setOnClickListener(v -> clearFilters());
    }

    private void loadTuitionPosts() {
        db.collection("tuition_posts")
                .whereEqualTo("status", "open")
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
                    Toast.makeText(ExploreTuitionsActivity.this, "Error loading posts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void applyFilters() {
        // Basic filtering logic (client-side for simplicity, can be moved to Firestore queries)
        String selectedClass = spFilterClass.getSelectedItem().toString();
        String selectedSubject = spFilterSubject.getSelectedItem().toString();
        String selectedLocation = spFilterLocation.getSelectedItem().toString();
        String selectedType = spFilterType.getSelectedItem().toString();

        List<TuitionPost> filteredList = new ArrayList<>();
        for (TuitionPost post : postList) {
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
