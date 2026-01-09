package com.sadid.myhometutor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TuitionPostsAdapter extends RecyclerView.Adapter<TuitionPostsAdapter.TuitionPostViewHolder> {

    private List<TuitionPostItem> posts;
    private OnPostClickListener listener;

    public interface OnPostClickListener {
        void onPostClick(TuitionPostItem post);
    }

    public TuitionPostsAdapter(List<TuitionPostItem> posts, OnPostClickListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TuitionPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tuition_post, parent, false);
        return new TuitionPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TuitionPostViewHolder holder, int position) {
        TuitionPostItem post = posts.get(position);
        
        holder.tvSubject.setText(post.getSubject() != null ? post.getSubject() : "N/A");
        holder.tvStudentName.setText("Posted by: " + (post.getStudentName() != null ? post.getStudentName() : "Loading..."));
        
        String status = post.getStatus();
        holder.tvStatus.setText(status != null ? status.toUpperCase() : "UNKNOWN");
        
        // Set status color
        if (status != null) {
            switch (status.toLowerCase()) {
                case "pending":
                    holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_light));
                    break;
                case "active":
                case "approved":
                    holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_light));
                    break;
                case "rejected":
                    holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_light));
                    break;
            }
        }

        holder.btnView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPostClick(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class TuitionPostViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubject, tvStudentName, tvStatus;
        Button btnView;

        public TuitionPostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnView = itemView.findViewById(R.id.btnView);
        }
    }
}
