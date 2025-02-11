package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SpaTestMain extends AppApplication implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spa_test_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.spa_test_page_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeClass();

    } //onCreate()


    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, SpaExaminationHall.class);
        int test_num = 0;
        if (v.getId() == R.id.spa_test_1) {
            test_num = 1;
        } else if (v.getId() == R.id.spa_test_2) {
            test_num = 2;
        } else if (v.getId() == R.id.spa_test_3) {
            test_num = 3;
        } else if (v.getId() == R.id.spa_test_4) {
            test_num = 4;
        } else if (v.getId() == R.id.spa_test_5) {
            test_num = 5;
        } else if (v.getId() == R.id.spa_test_6) {
            test_num = 6;
        } else if (v.getId() == R.id.spa_test_7) {
            test_num = 7;
        } else if (v.getId() == R.id.spa_test_8) {
            test_num = 8;
        } else if (v.getId() == R.id.spa_test_9) {
            test_num = 9;
        } else if (v.getId() == R.id.spa_test_10) {
            test_num = 10;
        }
        intent.putExtra("test_num", test_num); // 선택한 묶음의 인덱스
        startActivity(intent);
    } //onClick();


    private void initializeClass() {
        Button spa_test1 = findViewById(R.id.spa_test_1);
        Button spa_test2 = findViewById(R.id.spa_test_2);
        Button spa_test3 = findViewById(R.id.spa_test_3);
        Button spa_test4 = findViewById(R.id.spa_test_4);
        Button spa_test5 = findViewById(R.id.spa_test_5);
        Button spa_test6 = findViewById(R.id.spa_test_6);
        Button spa_test7 = findViewById(R.id.spa_test_7);
        Button spa_test8 = findViewById(R.id.spa_test_8);
        Button spa_test9 = findViewById(R.id.spa_test_9);
        Button spa_test10 = findViewById(R.id.spa_test_10);

        spa_test1.setOnClickListener(this);
        spa_test2.setOnClickListener(this);
        spa_test3.setOnClickListener(this);
        spa_test4.setOnClickListener(this);
        spa_test5.setOnClickListener(this);
        spa_test6.setOnClickListener(this);
        spa_test7.setOnClickListener(this);
        spa_test8.setOnClickListener(this);
        spa_test9.setOnClickListener(this);
        spa_test10.setOnClickListener(this);
    }

}
