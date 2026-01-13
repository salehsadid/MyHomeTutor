package com.sadid.myhometutor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sadid.myhometutor.R;
import com.sadid.myhometutor.models.PendingUser;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> implements Filterable {

    private Context context;
    private List<PendingUser> usersList;
    private List<PendingUser> usersListFull;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(PendingUser user);
    }

    public AdminUserAdapter(Context context, List<PendingUser> usersList, OnUserClickListener listener) {
        this.context = context;
        this.usersList = usersList;
        this.usersListFull = new ArrayList<>(usersList);
        this.listener = listener;
    }

    public void updateList(List<PendingUser> newList) {
        this.usersList = newList;
        this.usersListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<PendingUser> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(usersListFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (PendingUser item : usersListFull) {
                        if ((item.getName() != null && item.getName().toLowerCase().contains(filterPattern)) ||
                            (item.getEmail() != null && item.getEmail().toLowerCase().contains(filterPattern)) ||
                            (item.getPhone() != null && item.getPhone().toLowerCase().contains(filterPattern)) ||
                            (item.getStatus() != null && item.getStatus().toLowerCase().contains(filterPattern))) {
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
                usersList.clear();
                if (results.values != null) {
                    usersList.addAll((List) results.values);
                }
                notifyDataSetChanged();
            }
        };
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
