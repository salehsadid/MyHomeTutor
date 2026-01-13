package com.sadid.myhometutor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyApplicationAdapter extends RecyclerView.Adapter<MyApplicationAdapter.ViewHolder> implements Filterable {

    private List<TuitionApplication> applicationList;
    private List<TuitionApplication> applicationListFull;
    private OnViewProfileClickListener onViewProfileClickListener;

    public interface OnViewProfileClickListener {
        void onViewProfileClick(TuitionApplication application);
    }

    public MyApplicationAdapter(List<TuitionApplication> applicationList, OnViewProfileClickListener onViewProfileClickListener) {
        this.applicationList = applicationList;
        this.applicationListFull = new ArrayList<>(applicationList);
        this.onViewProfileClickListener = onViewProfileClickListener;
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
                        TuitionPost post = item.getTuitionPost();
                        boolean matchesPost = false;
                        if (post != null) {
                            matchesPost = (post.getSubject() != null && post.getSubject().toLowerCase().contains(filterPattern)) ||
                                          (post.getGrade() != null && post.getGrade().toLowerCase().contains(filterPattern)) ||
                                          (post.getDistrict() != null && post.getDistrict().toLowerCase().contains(filterPattern)) ||
                                          (post.getArea() != null && post.getArea().toLowerCase().contains(filterPattern));
                        }
                        boolean matchesStatus = item.getStatus() != null && item.getStatus().toLowerCase().contains(filterPattern);
                        
                        if (matchesPost || matchesStatus) {
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
                if (results.values != null) {
                    applicationList.addAll((List) results.values);
                }
                notifyDataSetChanged();
            }
        };
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
            holder.tvSubject.setText(post.getSubject() != null ? post.getSubject() : "N/A");
            holder.tvClass.setText(post.getGrade() != null ? post.getGrade() : "N/A");
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
        } else {
            holder.tvSubject.setText("Loading...");
        }

        holder.tvStatus.setText(application.getStatus() != null ? application.getStatus().toUpperCase() : "UNKNOWN");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        if (application.getTimestamp() != null) {
            holder.tvAppliedOn.setText(sdf.format(application.getTimestamp()));
        } else {
            holder.tvAppliedOn.setText("N/A");
        }

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
        TextView tvSubject, tvStatus, tvClass, tvGroup, tvGender, tvMedium, tvType, tvDays, tvTiming, tvSalary, tvLocation, tvAppliedOn;
        Button btnViewProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvClass = itemView.findViewById(R.id.tvClass);
            tvGroup = itemView.findViewById(R.id.tvGroup);
            tvGender = itemView.findViewById(R.id.tvGender);
            tvMedium = itemView.findViewById(R.id.tvMedium);
            tvType = itemView.findViewById(R.id.tvType);
            tvDays = itemView.findViewById(R.id.tvDays);
            tvTiming = itemView.findViewById(R.id.tvTiming);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvAppliedOn = itemView.findViewById(R.id.tvAppliedOn);
            btnViewProfile = itemView.findViewById(R.id.btnViewProfile);
        }
    }
}
