package com.sadid.myhometutor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ConnectionsAdapter extends RecyclerView.Adapter<ConnectionsAdapter.ConnectionViewHolder> {

    private List<Connection> connections;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public ConnectionsAdapter(List<Connection> connections) {
        this.connections = connections;
    }

    @NonNull
    @Override
    public ConnectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_connection, parent, false);
        return new ConnectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectionViewHolder holder, int position) {
        Connection connection = connections.get(position);
        
        holder.tvConnectionTitle.setText("Connection #" + (position + 1));
        holder.tvStudentName.setText(connection.getStudentName() != null ? connection.getStudentName() : "Loading...");
        holder.tvTutorName.setText(connection.getTutorName() != null ? connection.getTutorName() : "Loading...");
        holder.tvSubject.setText(connection.getSubject() != null ? connection.getSubject() : "Loading...");
        holder.tvDate.setText(connection.getDate() != null ? dateFormat.format(connection.getDate()) : "N/A");
    }

    @Override
    public int getItemCount() {
        return connections.size();
    }

    static class ConnectionViewHolder extends RecyclerView.ViewHolder {
        TextView tvConnectionTitle, tvStudentName, tvTutorName, tvSubject, tvDate;

        public ConnectionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvConnectionTitle = itemView.findViewById(R.id.tvConnectionTitle);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvTutorName = itemView.findViewById(R.id.tvTutorName);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
