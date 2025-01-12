package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuAdapter;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DailyStudyPage extends AppCompatActivity implements View.OnClickListener{

    TextView daily_study_personal_question = null;
    TextView daily_study_summarize_passage = null;
    TextView daily_study_tell_your_opinion = null;
    TextView daily_study_describe_graph_and_photo = null;
    private RecyclerView recyclerView;
    private AppMenuAdapter menuAdapter;

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

    } //onCreate();


    @Override
    public void onClick(View v) {
//        if (v.getId() == R.id.daily_study_personal_question) {
//            Intent intent = new Intent(this, DailyWordPage.class);
//            startActivity(intent);
//        } else if (v.getId() == R.id.daily_study_summarize_passage) {
//            Intent intent = new Intent(this, DailyStudyPage.class);
//            startActivity(intent);
//        } else if (v.getId() == R.id.daily_study_tell_your_opinion) {
//            Intent intent = new Intent(this, DailyTestPage.class);
//            startActivity(intent);
//        } else if (v.getId() == R.id.daily_study_describe_graph_and_photo) {
//            Intent intent = new Intent(this, GrammarTestPage.class);
//            startActivity(intent);
//        }
    } //onClick();


    private void initializeClass() {
        //initialize object

        // RecyclerView 초기화
        recyclerView = findViewById(R.id.recycler_view_menu);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 데이터 생성
        List<AppMenuItem> menuItems = new ArrayList<>();
        menuItems.add(new AppMenuItem(getString(R.string.daily_study_personal_question), "일상 루틴"));
        menuItems.add(new AppMenuItem(getString(R.string.daily_study_summarize_passage), "좋아하는 것"));
        menuItems.add(new AppMenuItem(getString(R.string.daily_study_tell_your_opinion), "하는 일"));
        menuItems.add(new AppMenuItem(getString(R.string.daily_study_describe_graph_and_photo), "과거의 일"));

        // Adapter 설정
        menuAdapter = new AppMenuAdapter(this, menuItems);
        recyclerView.setAdapter(menuAdapter);

        daily_study_personal_question.setOnClickListener(this);
        daily_study_summarize_passage.setOnClickListener(this);
        daily_study_tell_your_opinion.setOnClickListener(this);
        daily_study_describe_graph_and_photo.setOnClickListener(this);





    } //initializeClass();









}
