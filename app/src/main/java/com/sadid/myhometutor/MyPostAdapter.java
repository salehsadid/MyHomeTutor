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

        holder.tvSubjectClass.setText(String.format("%s • %s", post.getSubject(), post.getGrade()));
        holder.tvStatusDate.setText(String.format("Status: %s", post.getStatus()));

        holder.tvGroup.setText(post.getGroup());
        holder.tvGender.setText("Any"); // Placeholder
        holder.tvType.setText(post.getTuitionType());
        holder.tvDays.setText(post.getDaysPerWeek());
        holder.tvTiming.setText(post.getPreferredTiming());
        holder.tvSalary.setText(post.getSalary() + " BDT");
        holder.tvLocation.setText(String.format("%s, %s, %s", post.getDetailedAddress(), post.getArea(), post.getDistrict()));

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
        TextView tvSubjectClass, tvStatusDate, tvGroup, tvGender, tvType, tvDays, tvTiming, tvSalary, tvLocation;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubjectClass = itemView.findViewById(R.id.tvSubjectClass);
            tvStatusDate = itemView.findViewById(R.id.tvStatusDate);
            tvGroup = itemView.findViewById(R.id.tvGroup);
            tvGender = itemView.findViewById(R.id.tvGender);
            tvType = itemView.findViewById(R.id.tvType);
            tvDays = itemView.findViewById(R.id.tvDays);
            tvTiming = itemView.findViewById(R.id.tvTiming);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
