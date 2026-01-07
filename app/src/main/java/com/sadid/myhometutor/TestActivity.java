package com.sadid.myhometutor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a simple layout programmatically to avoid XML issues
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        TextView tv = new TextView(this);
        tv.setText("MyHomeTutor - Test Screen\n\nIf you see this, the app is working!");
        tv.setTextSize(18);
        tv.setPadding(0, 0, 0, 50);
        layout.addView(tv);

        Button btnGoToLogin = new Button(this);
        btnGoToLogin.setText("Go to Login");
        btnGoToLogin.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, LoginActivity.class));
            } catch (Exception e) {
                tv.setText("Error loading Login: " + e.getMessage());
            }
        });
        layout.addView(btnGoToLogin);

        setContentView(layout);
    }
}

