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
import com.sadid.myhometutor.models.PendingUser;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private Context context;
    private List<PendingUser> usersList;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(PendingUser user);
    }

    public AdminUserAdapter(Context context, List<PendingUser> usersList, OnUserClickListener listener) {
        this.context = context;
        this.usersList = usersList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        PendingUser user = usersList.get(position);

        holder.tvUserType.setText(user.getUserType() != null ? user.getUserType() : "Unknown");
        holder.tvUserName.setText(user.getName() != null ? user.getName() : "N/A");
        holder.tvUsername.setText(user.getEmail() != null ? user.getEmail() : "No email");
        
        String status = user.getStatus() != null ? user.getStatus() : "unknown";
        holder.tvStatus.setText(status);
        
        // Set status color
        if ("approved".equals(status)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.teal_button));
        } else if ("pending".equals(status)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_light));
        } else if ("rejected".equals(status)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
        }

        holder.btnView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserType, tvUserName, tvUsername, tvStatus;
        Button btnView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserType = itemView.findViewById(R.id.tvUserType);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnView = itemView.findViewById(R.id.btnView);
        }
    }
}
