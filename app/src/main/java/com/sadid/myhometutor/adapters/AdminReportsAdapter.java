package com.sadid.myhometutor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
public class AdminReportsAdapter extends RecyclerView.Adapter<AdminReportsAdapter.ReportViewHolder> {
    
    private final Context context;
    private List<Report> reports;
    private OnReportActionListener listener;
    
    public interface OnReportActionListener {
        void onResolveClick(Report report);
        void onDeleteClick(Report report);
        void onViewReportedUserClick(String userId);
    }
    
    public AdminReportsAdapter(Context context) {
        this.context = context;
        this.reports = new ArrayList<>();
    }
    
    public void setReports(List<Report> reports) {
        this.reports = reports;
        notifyDataSetChanged();
    }
    
    public void setOnReportActionListener(OnReportActionListener listener) {
        this.listener = listener;
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
        
        // Report details
        holder.tvReporterName.setText("Reporter: " + report.getReporterName());
        holder.tvReportedUser.setText("Reported: " + report.getReportedUserName());
        holder.tvReportType.setText("Type: " + report.getReportType());
        holder.tvReason.setText("Reason: " + report.getReason());
        
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
