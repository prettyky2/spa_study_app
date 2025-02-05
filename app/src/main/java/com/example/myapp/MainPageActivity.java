package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainPageActivity extends AppCompatActivity implements View.OnClickListener{

    TextView mainPageSubTitleDailyWord = null;
    TextView mainPageSubTitleDailyStudy = null;
    TextView mainPageSubTitleGrammarTest = null;
    TextView mainPageSubTitleSpaTest = null;
    TextView mainPageSubTitleTopGun = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_page_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        }); //setContentView

        initializeClass(); // set button, text, progress bar, onClickListener etc

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity(); // 앱 종료
            }
        });

    } //onCreate();

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.main_page_sub_title_daily_word) {
            Intent intent = new Intent(this, DailyWordMain.class);
            startActivity(intent);
        } else if (v.getId() == R.id.main_page_sub_title_daily_study) {
            Intent intent = new Intent(this, DailyStudyMain.class);
            startActivity(intent);
        } else if (v.getId() == R.id.main_page_sub_title_grammar_test) {
            Intent intent = new Intent(this, GrammarMain.class);
            startActivity(intent);
        } else if (v.getId() == R.id.main_page_sub_title_spa_test) {
            Intent intent = new Intent(this, SpaTestPage.class);
            startActivity(intent);
        } else if (v.getId() == R.id.main_page_sub_title_topgun) {
            Intent intent = new Intent(this, TopGunPage.class);
            startActivity(intent);
        }
    } //onClick();

    private void initializeClass() {
        //initialize object
        mainPageSubTitleDailyWord = findViewById(R.id.main_page_sub_title_daily_word);
        mainPageSubTitleDailyStudy = findViewById(R.id.main_page_sub_title_daily_study);
        mainPageSubTitleGrammarTest = findViewById(R.id.main_page_sub_title_grammar_test);
        mainPageSubTitleSpaTest = findViewById(R.id.main_page_sub_title_spa_test);
        mainPageSubTitleTopGun = findViewById(R.id.main_page_sub_title_topgun);

        mainPageSubTitleDailyWord.setOnClickListener(this);
        mainPageSubTitleDailyStudy.setOnClickListener(this);
        mainPageSubTitleGrammarTest.setOnClickListener(this);
        mainPageSubTitleSpaTest.setOnClickListener(this);
        mainPageSubTitleTopGun.setOnClickListener(this);
    } //initializeClass();


}
