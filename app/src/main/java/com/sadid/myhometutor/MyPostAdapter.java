package com.sadid.myhometutor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyPostAdapter extends RecyclerView.Adapter<MyPostAdapter.ViewHolder> {

    private List<TuitionPost> postList;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(TuitionPost post);
    }

    public MyPostAdapter(List<TuitionPost> postList, OnDeleteClickListener onDeleteClickListener) {
        this.postList = postList;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    public void updateList(List<TuitionPost> newList) {
        this.postList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TuitionPost post = postList.get(position);

        String subject = post.getSubject() != null ? post.getSubject() : "N/A";
        String grade = post.getGrade() != null ? post.getGrade() : "N/A";
        holder.tvSubjectClass.setText(String.format("%s • %s", subject, grade));
        holder.tvStatusDate.setText(String.format("Status: %s", post.getStatus() != null ? post.getStatus() : "N/A"));

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

        holder.btnDelete.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectClass, tvStatusDate, tvGroup, tvGender, tvMedium, tvType, tvDays, tvTiming, tvSalary, tvLocation;
        Button btnDelete;

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
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
