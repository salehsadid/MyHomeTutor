package com.sadid.myhometutor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TuitionPostAdapter extends RecyclerView.Adapter<TuitionPostAdapter.ViewHolder> {

    private Context context;
    private List<TuitionPost> postList;
    private OnApplyClickListener onApplyClickListener;
    private FirebaseFirestore db;

    public interface OnApplyClickListener {
        void onApplyClick(TuitionPost post);
    }

    public TuitionPostAdapter(Context context, List<TuitionPost> postList, OnApplyClickListener onApplyClickListener) {
        this.context = context;
        this.postList = postList;
        this.onApplyClickListener = onApplyClickListener;
        this.db = FirebaseFirestore.getInstance();
    }

    public void updateList(List<TuitionPost> newList) {
        this.postList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tuition_post_explore, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TuitionPost post = postList.get(position);

        String subject = post.getSubject() != null ? post.getSubject() : "N/A";
        String grade = post.getGrade() != null ? post.getGrade() : "N/A";
        holder.tvSubjectClass.setText(String.format("%s • %s", subject, grade));
        holder.tvStatusDate.setText(String.format("Available | %s", "2025-12-31")); // Placeholder date, use timestamp

        holder.tvGroup.setText(post.getGroup() != null ? post.getGroup() : "N/A");
        holder.tvGender.setText(post.getPreferredGender() != null ? post.getPreferredGender() : "Any");
        holder.tvMedium.setText(post.getMedium() != null ? post.getMedium() : "N/A");
        holder.tvType.setText(post.getTuitionType() != null ? post.getTuitionType() : "N/A");
        holder.tvDays.setText(post.getDaysPerWeek() != null ? post.getDaysPerWeek() : "N/A");
        holder.tvTiming.setText(post.getPreferredTiming() != null ? post.getPreferredTiming() : "N/A");
        holder.tvSalary.setText((post.getSalary() != null ? post.getSalary() : "0") + " BDT");
        
        String address = post.getDetailedAddress() != null ? post.getDetailedAddress() : "N/A";
        String area = post.getArea() != null ? post.getArea() : "";
        String district = post.getDistrict() != null ? post.getDistrict() : "";
        
        if (area.isEmpty() && district.isEmpty()) {
            holder.tvLocation.setText(address);
        } else if (area.isEmpty()) {
            holder.tvLocation.setText(String.format("%s, %s", address, district));
        } else if (district.isEmpty()) {
            holder.tvLocation.setText(String.format("%s, %s", address, area));
        } else {
            holder.tvLocation.setText(String.format("%s, %s, %s", address, area, district));
        }

        // Load and display student name
        holder.tvPostedBy.setVisibility(View.VISIBLE);
        String studentId = post.getStudentId();
        if (studentId != null) {
            holder.tvPostedBy.setText("Loading...");
            db.collection("users").document(studentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String studentName = documentSnapshot.getString("fullName");
                        holder.tvPostedBy.setText(studentName != null ? studentName : "Unknown Student");
                    } else {
                        holder.tvPostedBy.setText("Unknown Student");
                    }
                })
                .addOnFailureListener(e -> holder.tvPostedBy.setText("Unknown Student"));
        } else {
            holder.tvPostedBy.setText("Unknown Student");
        }

        holder.btnApply.setOnClickListener(v -> {
            if (onApplyClickListener != null) {
                onApplyClickListener.onApplyClick(post);
            }
        });

        holder.btnViewProfile.setOnClickListener(v -> {
            if (studentId != null) {
                Intent intent = new Intent(context, AdminViewUserActivity.class);
                intent.putExtra("userId", studentId);
                intent.putExtra("userType", "Student");
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectClass, tvStatusDate, tvGroup, tvGender, tvMedium, tvType, tvDays, tvTiming, tvSalary, tvLocation, tvPostedBy;
        Button btnApply, btnViewProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubjectClass = itemView.findViewById(R.id.tvSubjectClass);
            tvStatusDate = itemView.findViewById(R.id.tvStatusDate);
            tvGroup = itemView.findViewById(R.id.tvGroup);
            tvGender = itemView.findViewById(R.id.tvGender);
            tvMedium = itemView.findViewById(R.id.tvMedium);
            tvType = itemView.findViewById(R.id.tvType);
            tvDays = itemView.findViewById(R.id.tvDays);
            tvTiming = itemView.findViewById(R.id.tvTiming);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPostedBy = itemView.findViewById(R.id.tvPostedBy);
            btnApply = itemView.findViewById(R.id.btnApply);
            btnViewProfile = itemView.findViewById(R.id.btnViewProfile);
        }
    }
}
