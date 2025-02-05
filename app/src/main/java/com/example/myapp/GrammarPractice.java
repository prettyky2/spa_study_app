package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GrammarPractice  extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "GrammarPractice";
    int study_number = 0;
    int study_mode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_grammar_practice);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.grammar_practice_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeClass();

    } //onCreate()

    @Override
    public void onClick(View v) {

    } //onClick();

    private void initializeClass() {

        Intent intent = getIntent();
        study_number = intent.getIntExtra("study_number", -1);
        study_mode = intent.getIntExtra("study_mode", -1);

        Log.d(TAG, "study_number: " + study_number);
        Log.d(TAG, "study_mode: " + study_mode);



    }



}
