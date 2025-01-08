package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainPageActivity extends AppCompatActivity implements View.OnClickListener{

    TextView mainPageSubTitleDailyWord = null;
    TextView mainPageSubTitleDailyStudy = null;
    TextView mainPageSubTitleDailyTest = null;
    TextView mainPageSubTitleGrammarTest = null;
    TextView mainPageSubTitleSpaTest = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_page_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //initialize object
        mainPageSubTitleDailyWord = findViewById(R.id.main_page_sub_title_daily_word);
        mainPageSubTitleDailyStudy = findViewById(R.id.main_page_sub_title_daily_study);
        mainPageSubTitleDailyTest = findViewById(R.id.main_page_sub_title_daily_test);
        mainPageSubTitleGrammarTest = findViewById(R.id.main_page_sub_title_grammar_test);
        mainPageSubTitleSpaTest = findViewById(R.id.main_page_sub_title_spa_test);

        mainPageSubTitleDailyWord.setOnClickListener(this);
        mainPageSubTitleDailyStudy.setOnClickListener(this);
        mainPageSubTitleDailyTest.setOnClickListener(this);
        mainPageSubTitleGrammarTest.setOnClickListener(this);
        mainPageSubTitleSpaTest.setOnClickListener(this);



    } //onCreate();

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.main_page_sub_title_daily_word) {
            Intent intent = new Intent(this, DailyWordPage.class);
            startActivity(intent);
        } else if (v.getId() == R.id.main_page_sub_title_daily_study) {
            Intent intent = new Intent(this, MainPageActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.main_page_sub_title_daily_test) {
            Intent intent = new Intent(this, MainPageActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.main_page_sub_title_grammar_test) {
            Intent intent = new Intent(this, MainPageActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.main_page_sub_title_spa_test) {
            Intent intent = new Intent(this, MainPageActivity.class);
            startActivity(intent);
        }
    } //onClick();




}
