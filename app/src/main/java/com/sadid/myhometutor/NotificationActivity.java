package com.sadid.myhometutor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sadid.myhometutor.adapters.NotificationAdapter;
import com.sadid.myhometutor.model.Notification;
import com.sadid.myhometutor.repository.NotificationRepository;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private NotificationRepository notificationRepository;
    private TextView tvNoNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        notificationRepository = new NotificationRepository();

        initializeViews();
        setupRecyclerView();
        loadNotifications();
    }

    private void initializeViews() {
        rvNotifications = findViewById(R.id.rvNotifications);
        tvNoNotifications = findViewById(R.id.tvNoNotifications);
    }

    private void setupRecyclerView() {
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList, this::onNotificationClick);
        rvNotifications.setAdapter(adapter);
    }

    private void loadNotifications() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        notificationRepository.getNotificationsQuery(userId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notificationList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Notification notification = doc.toObject(Notification.class);
                        if (notification != null) {
                            notification.setId(doc.getId());
                            notificationList.add(notification);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    updateEmptyView();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                    updateEmptyView();
                });
    }

    private void updateEmptyView() {
        if (notificationList.isEmpty()) {
            tvNoNotifications.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            tvNoNotifications.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
        }
    }

    private void onNotificationClick(Notification notification) {
        // Mark read
        notificationRepository.markAsRead(notification.getId());
        notification.setRead(true);
        adapter.notifyDataSetChanged();

        Intent intent = null;
        String type = notification.getType();
        
        if (type == null) return;

        switch (type) {
            case NotificationRepository.TYPE_APPLY:
                intent = new Intent(this, ViewApplicationsActivity.class);
                intent.putExtra("postId", notification.getReferenceId());
                break;
            case NotificationRepository.TYPE_POST_APPROVED:
                intent = new Intent(this, MyPostsActivity.class); 
                break;
            case NotificationRepository.TYPE_POST_DELETED:
                intent = new Intent(this, MyPostsActivity.class);
                break;
            case NotificationRepository.TYPE_PASSWORD_CHANGED:
                intent = new Intent(this, ChangePasswordActivity.class);
                break;
            case NotificationRepository.TYPE_APPLICATION_ACCEPTED:
            case NotificationRepository.TYPE_CONNECTION_CREATED:
                intent = new Intent(this, MyApplicationsActivity.class);
                break;
        }

        if (intent != null) {
             startActivity(intent);
        }
    }
}
