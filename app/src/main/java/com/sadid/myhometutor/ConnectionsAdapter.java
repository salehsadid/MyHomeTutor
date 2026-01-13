package com.sadid.myhometutor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConnectionsAdapter extends RecyclerView.Adapter<ConnectionsAdapter.ConnectionViewHolder> implements Filterable {

    private static final String TAG = "ConnectionsAdapter";
    private List<Connection> connections;
    private List<Connection> connectionsFull;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    
    // Interface for button clicks (can be implemented later)
    public interface OnConnectionActionListener {
        void onViewStudent(Connection connection);
        void onViewTutor(Connection connection);
        void onViewPost(Connection connection);
    }
    
    private OnConnectionActionListener listener;

    public ConnectionsAdapter(List<Connection> connections) {
        this.connections = connections;
        this.connectionsFull = new ArrayList<>(connections);
    }
    
    public void updateList(List<Connection> newList) {
        this.connections = newList;
        this.connectionsFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }
    
    public void setOnConnectionActionListener(OnConnectionActionListener listener) {
        this.listener = listener;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Connection> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(connectionsFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Connection item : connectionsFull) {
                        if ((item.getStudentName() != null && item.getStudentName().toLowerCase().contains(filterPattern)) ||
                            (item.getTutorName() != null && item.getTutorName().toLowerCase().contains(filterPattern)) ||
                            (item.getSubject() != null && item.getSubject().toLowerCase().contains(filterPattern))) {
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
                connections.clear();
                if (results.values != null) {
                    connections.addAll((List) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public ConnectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Updated to use the new premium card layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_connection, parent, false);
        return new ConnectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectionViewHolder holder, int position) {
        Connection connection = connections.get(position);
        Log.d(TAG, "Binding connection at position " + position + ": " + connection.getId());

        holder.tvStudentName.setText(connection.getStudentName() != null ? connection.getStudentName() : "Loading...");
        holder.tvTutorName.setText(connection.getTutorName() != null ? connection.getTutorName() : "Loading...");
        holder.tvSubject.setText(connection.getSubject() != null ? connection.getSubject() : "Loading...");
        
        // Format date
        String dateStr = connection.getDate() != null ? dateFormat.format(connection.getDate()) : "N/A";
        holder.tvDate.setText("Connected: " + dateStr);
        
        // Status is hardcoded in layout but we can update it if needed
        holder.tvStatus.setText("CONNECTED");
        holder.tvStatus.setBackgroundResource(R.drawable.badge_green);
        
        holder.btnViewStudent.setOnClickListener(v -> {
            if (listener != null) listener.onViewStudent(connection);
        });
        
        holder.btnViewTutor.setOnClickListener(v -> {
            if (listener != null) listener.onViewTutor(connection);
        });
        
        holder.btnViewPost.setOnClickListener(v -> {
            if (listener != null) listener.onViewPost(connection);
        });
    }

    @Override
    public int getItemCount() {
        return connections.size();
    }

    static class ConnectionViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvTutorName, tvSubject, tvDate, tvStatus;
        Button btnViewStudent, btnViewTutor, btnViewPost;

        public ConnectionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvTutorName = itemView.findViewById(R.id.tvTutorName);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            
            btnViewStudent = itemView.findViewById(R.id.btnViewStudent);
            btnViewTutor = itemView.findViewById(R.id.btnViewTutor);
            btnViewPost = itemView.findViewById(R.id.btnViewPost);
        }
    }
}
