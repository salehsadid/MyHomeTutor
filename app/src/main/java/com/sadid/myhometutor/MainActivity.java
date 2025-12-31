package com.sadid.myhometutor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStudent = findViewById(R.id.btnStudent);
        Button btnTutor = findViewById(R.id.btnTutor);

        btnStudent.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra("USER_TYPE", "Student");
            startActivity(intent);
        });

        btnTutor.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra("USER_TYPE", "Tutor");
            startActivity(intent);
        });
    }
}
