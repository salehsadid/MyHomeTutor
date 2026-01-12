package com.sadid.myhometutor;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.sadid.myhometutor.adapters.AdminApplicationAdapter;
import com.sadid.myhometutor.repository.ApplicationRepository;
import com.sadid.myhometutor.repository.ConnectionRepository;
import com.sadid.myhometutor.repository.EmailNotificationService;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminApplicationsActivity - Admin reviews student-approved applications
 * 
 * Workflow:
 * 1. Student accepts tutor application (status: student_approved)
 * 2. Admin reviews the application here
 * 3. Admin can Approve (status -> approved, connection created) or Reject (status -> admin_rejected)
 * 
 * When admin approves:
 * - Application status changes to "approved"
 * - A connection is counted (student and tutor can now see each other's contact info)
 * - Email notification is sent to both parties
 */
public class AdminApplicationsActivity extends AppCompatActivity implements AdminApplicationAdapter.OnAdminApplicationActionListener {

    private static final String TAG = "AdminApplicationsActivity";

    private RecyclerView rvApplications;
    private TextView tvEmptyState, tvTitle;
    private ImageView btnBack;
    
    private AdminApplicationAdapter adapter;
    private List<TuitionApplication> applicationList;
    private FirebaseFirestore db;
    private ApplicationRepository applicationRepo;
    private ConnectionRepository connectionRepo;
    private EmailNotificationService emailService;
    private ListenerRegistration applicationsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_applications);

        db = FirebaseFirestore.getInstance();
        applicationRepo = new ApplicationRepository();
        connectionRepo = new ConnectionRepository();
        emailService = new EmailNotificationService();

        initializeViews();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeListener();
    }

    private void initializeViews() {
        rvApplications = findViewById(R.id.rvApplications);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.btnBack);

        tvTitle.setText("Pending Approvals");
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        applicationList = new ArrayList<>();
        adapter = new AdminApplicationAdapter(applicationList, this);
        rvApplications.setLayoutManager(new LinearLayoutManager(this));
        rvApplications.setAdapter(adapter);
    }

    private void attachListener() {
        Log.d(TAG, "Attaching listener for student_approved applications");
        
        applicationsListener = applicationRepo.listenToStudentApprovedApplications(
            new ApplicationRepository.ApplicationsListener() {
                @Override
                public void onApplicationsUpdated(com.google.firebase.firestore.QuerySnapshot snapshot) {
                    Log.d(TAG, "Student-approved applications updated: " + snapshot.size());
                    applicationList.clear();
                    
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        TuitionApplication application = document.toObject(TuitionApplication.class);
                        if (application != null) {
                            application.setApplicationId(document.getId());
                            applicationList.add(application);
                        }
                    }
                    
                    updateUI();
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error loading applications", e);
                    Toast.makeText(AdminApplicationsActivity.this, 
                        "Error loading applications: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void removeListener() {
        if (applicationsListener != null) {
            applicationsListener.remove();
            applicationsListener = null;
        }
    }

    private void updateUI() {
        if (applicationList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No pending approvals");
            rvApplications.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvApplications.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onApproveClick(TuitionApplication application) {
        new AlertDialog.Builder(this)
            .setTitle("Approve Connection")
            .setMessage("Approve this student-tutor connection? Both parties will be able to see each other's contact information.")
            .setPositiveButton("Approve", (dialog, which) -> {
                approveApplication(application);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void approveApplication(TuitionApplication application) {
        // First, update application status to approved
        applicationRepo.adminApproveApplication(application.getApplicationId())
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Application approved: " + application.getApplicationId());
                
                // Create connection record
                connectionRepo.createConnection(
                    application.getStudentId(),
                    application.getTutorId(),
                    application.getPostId(),
                    application.getApplicationId()
                ).addOnSuccessListener(connectionId -> {
                    Log.d(TAG, "Connection created: " + connectionId);
                    Toast.makeText(this, "Connection approved! Both parties can now see contact info.", 
                        Toast.LENGTH_SHORT).show();
                    
                    // Send email notifications to both student and tutor
                    sendApprovalNotifications(application);
                    
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create connection record", e);
                    // Application is already approved, just log the error
                    Toast.makeText(this, "Approved, but failed to create connection record.", 
                        Toast.LENGTH_SHORT).show();
                });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    /**
     * Send email notifications when connection is approved
     */
    private void sendApprovalNotifications(TuitionApplication application) {
        // Get tutor and student details to send personalized emails
        db.collection("users").document(application.getTutorId()).get()
            .addOnSuccessListener(tutorDoc -> {
                String tutorEmail = tutorDoc.getString("email");
                String tutorName = tutorDoc.getString("name");
                
                db.collection("users").document(application.getStudentId()).get()
                    .addOnSuccessListener(studentDoc -> {
                        String studentEmail = studentDoc.getString("email");
                        String studentName = studentDoc.getString("name");
                        
                        // Get post details for subject
                        if (application.getTuitionPost() != null) {
                            String subject = application.getTuitionPost().getSubject();
                            
                            // Send email to tutor
                            if (tutorEmail != null && !tutorEmail.isEmpty()) {
                                emailService.sendConnectionApprovedToTutor(
                                    tutorEmail, tutorName, studentName, subject
                                );
                                Log.d(TAG, "Sent approval email to tutor: " + tutorEmail);
                            }
                            
                            // Send email to student (reuse the same method or create specific one)
                            if (studentEmail != null && !studentEmail.isEmpty()) {
                                emailService.sendConnectionApprovedToTutor(
                                    studentEmail, studentName, tutorName, subject
                                );
                                Log.d(TAG, "Sent approval email to student: " + studentEmail);
                            }
                        }
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to fetch user details for email notification", e);
            });
    }

    @Override
    public void onRejectClick(TuitionApplication application) {
        new AlertDialog.Builder(this)
            .setTitle("Reject Application")
            .setMessage("Reject this application? The student and tutor will be notified.")
            .setPositiveButton("Reject", (dialog, which) -> {
                applicationRepo.adminRejectApplication(application.getApplicationId())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Application rejected", Toast.LENGTH_SHORT).show();
                        // TODO: Send rejection notification
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onViewStudentClick(TuitionApplication application) {
        android.content.Intent intent = new android.content.Intent(this, AdminViewUserActivity.class);
        intent.putExtra("userId", application.getStudentId());
        intent.putExtra("userType", "Student");
        startActivity(intent);
    }

    @Override
    public void onViewTutorClick(TuitionApplication application) {
        android.content.Intent intent = new android.content.Intent(this, AdminViewUserActivity.class);
        intent.putExtra("userId", application.getTutorId());
        intent.putExtra("userType", "Tutor");
        startActivity(intent);
    }

    @Override
    public void onViewPostClick(TuitionApplication application) {
        android.content.Intent intent = new android.content.Intent(this, AdminViewTuitionPostActivity.class);
        intent.putExtra("postId", application.getPostId());
        startActivity(intent);
    }
}
