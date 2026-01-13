package com.sadid.myhometutor.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sadid.myhometutor.R;
import com.sadid.myhometutor.model.Notification;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notificationList;
    private OnNotificationClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notificationList, OnNotificationClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        
        if (notification.getTimestamp() != null) {
            holder.tvDate.setText(dateFormat.format(notification.getTimestamp().toDate()));
        } else {
             holder.tvDate.setText("Just now");
        }

        holder.viewUnreadDot.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
        
        holder.itemView.setOnClickListener(v -> listener.onNotificationClick(notification));
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvDate;
        View viewUnreadDot;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvDate = itemView.findViewById(R.id.tvDate);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
        }
    }
}
