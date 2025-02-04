package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class DailyStudyPractice extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "DailyStudyPractice";
    String studyTopic = null;
    String imagePopUp = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_daily_study_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.daily_study_page_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeClass();
    }

    private void initializeClass() {
        // Initialize object
        Intent intent = getIntent();
        studyTopic = intent.getStringExtra("study_topic");
        imagePopUp = intent.getStringExtra("image_popup");
        List<String> sentences = intent.getStringArrayListExtra("sentences");

        Log.e(TAG, "studyTopic : " + studyTopic);
        Log.e(TAG, "imagePopUp : " + imagePopUp);

        if (sentences != null && !sentences.isEmpty()) {
            Log.d(TAG, "Received " + sentences.size() + " sentences.");
            for (int i = 0; i < sentences.size(); i++) {
                Log.d(TAG, "Sentence " + (i + 1) + ": " + sentences.get(i));
            }
        } else {
            Log.e(TAG, "No sentences received!");
        }
    }
 //initializeClass()





    @Override
    public void onClick(View v) {

    } //onClick();













}