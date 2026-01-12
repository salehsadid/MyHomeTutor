package com.sadid.myhometutor.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.sadid.myhometutor.R;
import com.sadid.myhometutor.TuitionApplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * AdminApplicationAdapter - For admin to review student-approved applications
 * 
 * Shows:
 * - Student info (name)
 * - Tutor info (name, university)
 * - Post info (subject, class)
 * - Applied date
 * - Actions: View Student, View Tutor, View Post, Approve, Reject
 */
public class AdminApplicationAdapter extends RecyclerView.Adapter<AdminApplicationAdapter.ViewHolder> {

    private List<TuitionApplication> applicationList;
    private OnAdminApplicationActionListener listener;
    private FirebaseFirestore db;

    public interface OnAdminApplicationActionListener {
        void onApproveClick(TuitionApplication application);
        void onRejectClick(TuitionApplication application);
        void onViewStudentClick(TuitionApplication application);
        void onViewTutorClick(TuitionApplication application);
        void onViewPostClick(TuitionApplication application);
    }

    public AdminApplicationAdapter(List<TuitionApplication> applicationList, 
                                   OnAdminApplicationActionListener listener) {
        this.applicationList = applicationList;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TuitionApplication application = applicationList.get(position);
        
        // Load student name
        loadUserName(application.getStudentId(), holder.tvStudentName, "Student");
        
        // Load tutor name and info
        loadTutorInfo(application.getTutorId(), holder.tvTutorName, holder.tvTutorInfo);
        
        // Load post info
        loadPostInfo(application.getPostId(), holder.tvPostInfo);
        
        // Set applied date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        holder.tvAppliedDate.setText("Applied: " + sdf.format(new Date(application.getTimestamp())));
        
        // Status badge
        holder.tvStatus.setText("AWAITING APPROVAL");
        holder.tvStatus.setBackgroundResource(R.drawable.badge_blue);
        
        // Setup click listeners
        holder.btnViewStudent.setOnClickListener(v -> {
            if (listener != null) listener.onViewStudentClick(application);
        });
        
        holder.btnViewTutor.setOnClickListener(v -> {
            if (listener != null) listener.onViewTutorClick(application);
        });
        
        holder.btnViewPost.setOnClickListener(v -> {
            if (listener != null) listener.onViewPostClick(application);
        });
        
        holder.btnApprove.setOnClickListener(v -> {
            if (listener != null) listener.onApproveClick(application);
        });
        
        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onRejectClick(application);
        });
    }
    
    private void loadUserName(String userId, TextView textView, String defaultText) {
        if (userId == null) {
            textView.setText(defaultText);
            return;
        }
        
        textView.setText("Loading...");
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("name");
                    if (name == null) name = doc.getString("fullName");
                    textView.setText(name != null ? name : "Unknown");
                } else {
                    textView.setText("Unknown");
                }
            })
            .addOnFailureListener(e -> textView.setText("Unknown"));
    }
    
    private void loadTutorInfo(String tutorId, TextView nameView, TextView infoView) {
        if (tutorId == null) {
            nameView.setText("Unknown Tutor");
            infoView.setText("Info not available");
            return;
        }
        
        nameView.setText("Loading...");
        db.collection("users").document(tutorId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("name");
                    if (name == null) name = doc.getString("fullName");
                    nameView.setText(name != null ? name : "Unknown Tutor");
                    
                    String university = doc.getString("universityName");
                    String department = doc.getString("department");
                    if (university != null && department != null) {
                        infoView.setText(String.format("%s, %s", department, university));
                    } else if (university != null) {
                        infoView.setText(university);
                    } else {
                        infoView.setText("Education info not available");
                    }
                } else {
                    nameView.setText("Unknown Tutor");
                    infoView.setText("Info not available");
                }
            })
            .addOnFailureListener(e -> {
                nameView.setText("Unknown Tutor");
                infoView.setText("Info not available");
            });
    }
    
    private void loadPostInfo(String postId, TextView textView) {
        if (postId == null) {
            textView.setText("Post not found");
            return;
        }
        
        textView.setText("Loading...");
        db.collection("tuition_posts").document(postId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String subject = doc.getString("subject");
                    String grade = doc.getString("grade");
                    if (grade == null) grade = doc.getString("class");
                    
                    if (subject != null && grade != null) {
                        textView.setText(String.format("%s • %s", subject, grade));
                    } else if (subject != null) {
                        textView.setText(subject);
                    } else {
                        textView.setText("Post details unavailable");
                    }
                } else {
                    textView.setText("Post not found");
                }
            })
            .addOnFailureListener(e -> textView.setText("Post not found"));
    }

    @Override
    public int getItemCount() {
        return applicationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvTutorName, tvTutorInfo, tvPostInfo, tvStatus, tvAppliedDate;
        Button btnViewStudent, btnViewTutor, btnViewPost, btnApprove, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvTutorName = itemView.findViewById(R.id.tvTutorName);
            tvTutorInfo = itemView.findViewById(R.id.tvTutorInfo);
            tvPostInfo = itemView.findViewById(R.id.tvPostInfo);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAppliedDate = itemView.findViewById(R.id.tvAppliedDate);
            btnViewStudent = itemView.findViewById(R.id.btnViewStudent);
            btnViewTutor = itemView.findViewById(R.id.btnViewTutor);
            btnViewPost = itemView.findViewById(R.id.btnViewPost);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
