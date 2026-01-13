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
import com.sadid.myhometutor.repository.ApplicationRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ApplicationAdapter - Displays tutor applications
 * 
 * Used in:
 * - ViewApplicationsActivity (student reviews applications)
 * - AdminApplicationsActivity (admin reviews student-approved applications)
 * 
 * Status display:
 * - pending: Waiting for student review
 * - student_approved: Student accepted, waiting for admin
 * - approved: Admin approved, connection established
 * - rejected: Student rejected
 * - admin_rejected: Admin rejected
 */
public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private List<TuitionApplication> applicationList;
    private OnApplicationActionListener listener;
    private boolean showStudentActions; // true for student, false for admin
    private FirebaseFirestore db;

    public interface OnApplicationActionListener {
        void onAcceptClick(TuitionApplication application);
        void onRejectClick(TuitionApplication application);
        void onViewProfileClick(TuitionApplication application);
    }

    public ApplicationAdapter(List<TuitionApplication> applicationList, 
                              OnApplicationActionListener listener, 
                              boolean showStudentActions) {
        this.applicationList = applicationList;
        this.listener = listener;
        this.showStudentActions = showStudentActions;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TuitionApplication application = applicationList.get(position);
        
        // Load tutor name
        String tutorId = application.getTutorId();
        if (tutorId != null) {
            holder.tvTutorName.setText("Loading...");
            db.collection("users").document(tutorId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name == null) name = doc.getString("fullName");
                        holder.tvTutorName.setText(name != null ? name : "Unknown Tutor");
                        
                        // Also set other tutor info
                        String university = doc.getString("universityName");
                        String department = doc.getString("department");
                        if (university != null && department != null) {
                            holder.tvTutorInfo.setText(String.format("%s, %s", department, university));
                        } else if (university != null) {
                            holder.tvTutorInfo.setText(university);
                        } else {
                            holder.tvTutorInfo.setText("Info not available");
                        }
                    } else {
                        holder.tvTutorName.setText("Unknown Tutor");
                        holder.tvTutorInfo.setText("Info not available");
                    }
                })
                .addOnFailureListener(e -> {
                    holder.tvTutorName.setText("Unknown Tutor");
                    holder.tvTutorInfo.setText("Info not available");
                });
        }
        
        // Set status with color coding
        String status = application.getStatus();
        holder.tvStatus.setText(getStatusDisplay(status));
        holder.tvStatus.setBackgroundResource(getStatusBackground(status));
        
        // Set applied date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        if (application.getTimestamp() != null) {
            holder.tvAppliedDate.setText("Applied: " + sdf.format(application.getTimestamp()));
        } else {
            holder.tvAppliedDate.setText("Applied: N/A");
        }
        
        // Control button visibility based on status and user type
        setupButtons(holder, application, status);
    }
    
    private String getStatusDisplay(String status) {
        if (status == null) return "UNKNOWN";
        switch (status) {
            case ApplicationRepository.STATUS_PENDING:
                return "PENDING";
            case ApplicationRepository.STATUS_STUDENT_APPROVED:
                return "AWAITING ADMIN";
            case ApplicationRepository.STATUS_APPROVED:
                return "CONNECTED";
            case ApplicationRepository.STATUS_REJECTED:
                return "REJECTED";
            case ApplicationRepository.STATUS_ADMIN_REJECTED:
                return "ADMIN REJECTED";
            default:
                return status.toUpperCase();
        }
    }
    
    private int getStatusBackground(String status) {
        if (status == null) return R.drawable.badge_gray;
        switch (status) {
            case ApplicationRepository.STATUS_PENDING:
                return R.drawable.badge_orange;
            case ApplicationRepository.STATUS_STUDENT_APPROVED:
                return R.drawable.badge_blue;
            case ApplicationRepository.STATUS_APPROVED:
                return R.drawable.badge_green;
            case ApplicationRepository.STATUS_REJECTED:
            case ApplicationRepository.STATUS_ADMIN_REJECTED:
                return R.drawable.badge_red;
            default:
                return R.drawable.badge_gray;
        }
    }
    
    private void setupButtons(ViewHolder holder, TuitionApplication application, String status) {
        // View Profile is always visible
        holder.btnViewProfile.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewProfileClick(application);
            }
        });
        
        // For students: show accept/reject only for pending applications
        // For admin: show accept/reject only for student_approved applications
        if (showStudentActions) {
            // Student view
            if (ApplicationRepository.STATUS_PENDING.equals(status)) {
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
            } else {
                holder.btnAccept.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.GONE);
            }
        } else {
            // Admin view
            if (ApplicationRepository.STATUS_STUDENT_APPROVED.equals(status)) {
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnAccept.setText("Approve");
            } else {
                holder.btnAccept.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.GONE);
            }
        }
        
        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAcceptClick(application);
            }
        });
        
        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRejectClick(application);
            }
        });
    }

    @Override
    public int getItemCount() {
        return applicationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTutorName, tvTutorInfo, tvStatus, tvAppliedDate;
        Button btnAccept, btnReject, btnViewProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTutorName = itemView.findViewById(R.id.tvTutorName);
            tvTutorInfo = itemView.findViewById(R.id.tvTutorInfo);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAppliedDate = itemView.findViewById(R.id.tvAppliedDate);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnViewProfile = itemView.findViewById(R.id.btnViewProfile);
        }
    }
}
