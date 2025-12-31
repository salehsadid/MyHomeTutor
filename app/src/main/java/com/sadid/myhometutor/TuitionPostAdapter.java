package com.sadid.myhometutor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TuitionPostAdapter extends RecyclerView.Adapter<TuitionPostAdapter.ViewHolder> {

    private List<TuitionPost> postList;
    private OnApplyClickListener onApplyClickListener;

    public interface OnApplyClickListener {
        void onApplyClick(TuitionPost post);
    }

    public TuitionPostAdapter(List<TuitionPost> postList, OnApplyClickListener onApplyClickListener) {
        this.postList = postList;
        this.onApplyClickListener = onApplyClickListener;
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

        holder.tvSubjectClass.setText(String.format("%s • %s", post.getSubject(), post.getGrade()));
        holder.tvStatusDate.setText(String.format("Available | %s", "2025-12-31")); // Placeholder date, use timestamp

        holder.tvGroup.setText(post.getGroup());
        holder.tvGender.setText("Any"); // Placeholder, add gender to TuitionPost if needed
        holder.tvType.setText(post.getTuitionType());
        holder.tvDays.setText(post.getDaysPerWeek());
        holder.tvTiming.setText(post.getPreferredTiming());
        holder.tvSalary.setText(post.getSalary() + " BDT");
        holder.tvLocation.setText(String.format("%s, %s, %s", post.getDetailedAddress(), post.getArea(), post.getDistrict()));

        // Fetch poster name if needed, for now placeholder
        holder.tvPostedBy.setText("Posted by: Student");

        holder.btnApply.setOnClickListener(v -> {
            if (onApplyClickListener != null) {
                onApplyClickListener.onApplyClick(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectClass, tvStatusDate, tvGroup, tvGender, tvType, tvDays, tvTiming, tvSalary, tvLocation, tvPostedBy;
        Button btnApply;

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
            tvPostedBy = itemView.findViewById(R.id.tvPostedBy);
            btnApply = itemView.findViewById(R.id.btnApply);
        }
    }
}
