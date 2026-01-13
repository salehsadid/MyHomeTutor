package com.sadid.myhometutor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sadid.myhometutor.R;
import com.sadid.myhometutor.models.Report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying admin reports list
 */
public class AdminReportsAdapter extends RecyclerView.Adapter<AdminReportsAdapter.ReportViewHolder> implements Filterable {
    
    private final Context context;
    private List<Report> reports;
    private List<Report> reportsFull;
    private OnReportActionListener listener;
    
    public interface OnReportActionListener {
        void onResolveClick(Report report);
        void onDeleteClick(Report report);
        void onViewReportedUserClick(String userId);
    }
    
    public AdminReportsAdapter(Context context) {
        this.context = context;
        this.reports = new ArrayList<>();
        this.reportsFull = new ArrayList<>();
    }
    
    public void updateList(List<Report> reports) {
        this.reports = new ArrayList<>(reports);
        this.reportsFull = new ArrayList<>(reports);
        notifyDataSetChanged();
    }

    public void setReports(List<Report> reports) {
        updateList(reports);
    }
    
    public void setOnReportActionListener(OnReportActionListener listener) {
        this.listener = listener;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Report> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(reportsFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Report item : reportsFull) {
                        if (contains(item.getReporterName(), filterPattern) ||
                            contains(item.getReportedUserName(), filterPattern) ||
                            contains(item.getReportType(), filterPattern) ||
                            contains(item.getReportMessage(), filterPattern) ||
                            contains(item.getReason(), filterPattern)) {
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
                reports.clear();
                reports.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };
    }
    
    private boolean contains(String text, String pattern) {
        return text != null && text.toLowerCase().contains(pattern);
    }
    
    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reports.get(position);
        
        // Report details with user types
        String reporterInfo = "Reporter: " + report.getReporterName();
        if (report.getReporterType() != null && !report.getReporterType().isEmpty()) {
            reporterInfo += " (" + report.getReporterType() + ")";
        }
        holder.tvReporterName.setText(reporterInfo);
        
        String reportedInfo = "Reported: " + report.getReportedUserName();
        if (report.getReportedUserType() != null && !report.getReportedUserType().isEmpty()) {
            reportedInfo += " (" + report.getReportedUserType() + ")";
        }
        holder.tvReportedUser.setText(reportedInfo);
        
        holder.tvReportType.setText("Type: " + report.getReportType());
        
        // Show report message if available, otherwise show reason
        String reasonText = "Reason: ";
        if (report.getReportMessage() != null && !report.getReportMessage().isEmpty()) {
            reasonText += report.getReportMessage();
        } else if (report.getReason() != null) {
            reasonText += report.getReason();
        } else {
            reasonText += "No details provided";
        }
        holder.tvReason.setText(reasonText);
        
        // Format timestamp
        if (report.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            holder.tvTimestamp.setText(sdf.format(report.getTimestamp().toDate()));
        }
        
        // Status badge
        holder.tvStatus.setText(report.getStatus().toUpperCase());
        if ("pending".equals(report.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.badge_pending);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.badge_resolved);
        }
        
        // Action buttons visibility
        if ("pending".equals(report.getStatus())) {
            holder.btnResolve.setVisibility(View.VISIBLE);
        } else {
            holder.btnResolve.setVisibility(View.GONE);
        }
        
        // Click listeners
        holder.btnResolve.setOnClickListener(v -> {
            if (listener != null) {
                listener.onResolveClick(report);
            }
        });
        
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(report);
            }
        });
        
        holder.btnViewUser.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewReportedUserClick(report.getReportedUserId());
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return reports.size();
    }
    
    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvReporterName, tvReportedUser, tvReportType, tvReason, tvTimestamp, tvStatus;
        Button btnResolve, btnDelete, btnViewUser;
        
        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReporterName = itemView.findViewById(R.id.tvReporterName);
            tvReportedUser = itemView.findViewById(R.id.tvReportedUser);
            tvReportType = itemView.findViewById(R.id.tvReportType);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnResolve = itemView.findViewById(R.id.btnResolve);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnViewUser = itemView.findViewById(R.id.btnViewUser);
        }
    }
}
