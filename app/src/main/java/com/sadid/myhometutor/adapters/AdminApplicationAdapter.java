package com.sadid.myhometutor.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.sadid.myhometutor.R;
import com.sadid.myhometutor.TuitionApplication;
import com.sadid.myhometutor.TuitionPost;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminApplicationAdapter extends RecyclerView.Adapter<AdminApplicationAdapter.ViewHolder> implements Filterable {

    private List<TuitionApplication> applicationList;
    private List<TuitionApplication> applicationListFull;
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
        this.applicationListFull = new ArrayList<>(applicationList);
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
    }
    
    public void updateList(List<TuitionApplication> newList) {
        this.applicationList = newList;
        this.applicationListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<TuitionApplication> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(applicationListFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (TuitionApplication item : applicationListFull) {
                        // Match against Student Name, Tutor Name, Post Subject
                        if (contains(item.getStudentName(), filterPattern) ||
                            contains(item.getTutorName(), filterPattern) ||
                            (item.getTuitionPost() != null && contains(item.getTuitionPost().getSubject(), filterPattern)) ||
                            (item.getTuitionPost() != null && contains(item.getTuitionPost().getGrade(), filterPattern))) {
                            filteredList.add(item);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                applicationList.clear();
                applicationList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };
    }
    
    private boolean contains(String text, String pattern) {
        return text != null && text.toLowerCase().contains(pattern);
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
        if (application.getStudentName() != null) {
            holder.tvStudentName.setText(application.getStudentName());
        } else {
            loadUserName(application, holder.tvStudentName, "Student");
        }
        
        // Load tutor name and info
        if (application.getTutorName() != null) {
            holder.tvTutorName.setText(application.getTutorName());
            holder.tvTutorInfo.setText(application.getTutorUniversity() != null ? 
                    application.getTutorUniversity() : "Info not available");
        } else {
            loadTutorInfo(application, holder.tvTutorName, holder.tvTutorInfo);
        }
        
        // Load post info
        if (application.getTuitionPost() != null) {
             TuitionPost post = application.getTuitionPost();
             holder.tvPostInfo.setText(String.format("%s • %s", 
                     post.getSubject(), 
                     post.getGrade() != null ? post.getGrade() : "N/A"));
        } else {
            loadPostInfo(application, holder.tvPostInfo);
        }
        
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
    
    private void loadUserName(TuitionApplication application, TextView textView, String defaultText) {
        if (application.getStudentId() == null) {
            textView.setText(defaultText);
            return;
        }
        
        textView.setText("Loading...");
        db.collection("users").document(application.getStudentId())
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("name");
                    if (name == null) name = doc.getString("fullName");
                    String finalName = name != null ? name : "Unknown";
                    application.setStudentName(finalName);
                    // Update field directly to avoid full rebind flicker
                    textView.setText(finalName); 
                } else {
                    textView.setText("Unknown");
                }
            })
            .addOnFailureListener(e -> textView.setText("Unknown"));
    }
    
    private void loadTutorInfo(TuitionApplication application, TextView nameView, TextView infoView) {
        if (application.getTutorId() == null) {
            nameView.setText("Unknown Tutor");
            infoView.setText("Info not available");
            return;
        }
        
        nameView.setText("Loading...");
        db.collection("users").document(application.getTutorId())
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("name");
                    if (name == null) name = doc.getString("fullName");
                    String finalName = name != null ? name : "Unknown Tutor";
                    application.setTutorName(finalName);
                    nameView.setText(finalName);
                    
                    String university = doc.getString("universityName");
                    String department = doc.getString("department");
                    String infoText;
                    if (university != null && department != null) {
                        infoText = String.format("%s, %s", department, university);
                    } else if (university != null) {
                        infoText = university;
                    } else {
                        infoText = "Education info not available";
                    }
                    application.setTutorUniversity(infoText);
                    infoView.setText(infoText);
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
    
    private void loadPostInfo(TuitionApplication application, TextView textView) {
        if (application.getPostId() == null) {
            textView.setText("Post not found");
            return;
        }
        
        textView.setText("Loading...");
        db.collection("tuition_posts").document(application.getPostId())
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    TuitionPost post = doc.toObject(TuitionPost.class);
                    if (post != null) {
                        post.setId(doc.getId());
                        application.setTuitionPost(post);
                        
                        String subject = post.getSubject();
                        String grade = post.getGrade() != null ? post.getGrade() : "N/A";
                        
                        if (subject != null && grade != null) {
                            textView.setText(String.format("%s • %s", subject, grade));
                        } else if (subject != null) {
                            textView.setText(subject);
                        } else {
                            textView.setText("Post details unavailable");
                        }
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
