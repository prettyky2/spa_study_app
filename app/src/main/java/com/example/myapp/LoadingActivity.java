package com.example.myapp;


import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoadingActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "LoadingActivity";
    Button startButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loading_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //initialize object
        startButton = findViewById(R.id.loading_page_start_button);
        startButton.setOnClickListener(this);

    } //onCreate();

    @Override
    public void onClick(View v) {
        // 클릭된 뷰의 ID 확인
        if (v.getId() == R.id.loading_page_start_button) {
            Toast.makeText(this, "Start button clicked!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainPageActivity.class);
            startActivity(intent);
        }
    }


}