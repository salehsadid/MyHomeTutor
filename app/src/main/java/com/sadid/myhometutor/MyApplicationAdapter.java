package com.sadid.myhometutor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyApplicationAdapter extends RecyclerView.Adapter<MyApplicationAdapter.ViewHolder> {

    private List<TuitionApplication> applicationList;
    private OnViewProfileClickListener onViewProfileClickListener;

    public interface OnViewProfileClickListener {
        void onViewProfileClick(TuitionApplication application);
    }

    public MyApplicationAdapter(List<TuitionApplication> applicationList, OnViewProfileClickListener onViewProfileClickListener) {
        this.applicationList = applicationList;
        this.onViewProfileClickListener = onViewProfileClickListener;
    }

    public void updateList(List<TuitionApplication> newList) {
        this.applicationList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TuitionApplication application = applicationList.get(position);
        TuitionPost post = application.getTuitionPost();

        if (post != null) {
            holder.tvSubject.setText(post.getSubject());
            holder.tvClass.setText(post.getGrade());
            holder.tvGroup.setText(post.getGroup());
            holder.tvType.setText(post.getTuitionType());
            holder.tvDays.setText(post.getDaysPerWeek());
            holder.tvSalary.setText(post.getSalary() + " BDT");
            holder.tvLocation.setText(post.getLocation()); // Or construct from address
        } else {
            holder.tvSubject.setText("Loading...");
        }

        holder.tvStatus.setText(application.getStatus().toUpperCase());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        holder.tvAppliedOn.setText(sdf.format(new Date(application.getTimestamp())));

        holder.btnViewProfile.setOnClickListener(v -> {
            if (onViewProfileClickListener != null) {
                onViewProfileClickListener.onViewProfileClick(application);
            }
        });
    }

    @Override
    public int getItemCount() {
        return applicationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubject, tvStatus, tvClass, tvGroup, tvType, tvDays, tvSalary, tvLocation, tvAppliedOn;
        Button btnViewProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvClass = itemView.findViewById(R.id.tvClass);
            tvGroup = itemView.findViewById(R.id.tvGroup);
            tvType = itemView.findViewById(R.id.tvType);
            tvDays = itemView.findViewById(R.id.tvDays);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvAppliedOn = itemView.findViewById(R.id.tvAppliedOn);
            btnViewProfile = itemView.findViewById(R.id.btnViewProfile);
        }
    }
}
