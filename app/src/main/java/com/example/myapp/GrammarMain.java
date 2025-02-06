package com.example.myapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import org.apache.poi.ss.usermodel.Row;

import java.util.ArrayList;
import java.util.List;

public class GrammarMain extends AppApplication implements View.OnClickListener {

    private static final String TAG = "GrammarMain";
    ImageView grammar_3 = null;
    ImageView grammar_4 = null;
    ImageView grammar_5 = null;
    ImageView grammar_6 = null;
    ImageView grammar_7 = null;
    ImageView grammar_8 = null;
    ImageView grammar_9 = null;
    ImageView grammar_10 = null;
    ImageView grammar_11 = null;
    ImageView grammar_12 = null;
    String study_number = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_grammar_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.grammar_test_page_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeClass();

    } //onCreate()

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.grammar_3) {
            study_number = "grammar_study_03";
        } else if(v.getId() == R.id.grammar_4) {
            study_number = "grammar_study_04";
        } else if(v.getId() == R.id.grammar_5) {
            study_number = "grammar_study_05";
        } else if(v.getId() == R.id.grammar_6) {
            study_number = "grammar_study_06";
        } else if(v.getId() == R.id.grammar_7) {
            study_number = "grammar_study_07";
        } else if(v.getId() == R.id.grammar_8) {
            study_number = "grammar_study_08";
        } else if(v.getId() == R.id.grammar_9) {
            study_number = "grammar_study_09";
        } else if(v.getId() == R.id.grammar_10) {
            study_number = "grammar_study_10";
        } else if(v.getId() == R.id.grammar_11) {
            study_number = "grammar_study_11";
        } else if(v.getId() == R.id.grammar_12) {
            study_number = "grammar_study_12";
        }
        showCustomDialog();
    } //onClick();

    private void initializeClass() {
        grammar_3 = findViewById(R.id.grammar_3);
        grammar_4 = findViewById(R.id.grammar_4);
        grammar_5 = findViewById(R.id.grammar_5);
        grammar_6 = findViewById(R.id.grammar_6);
        grammar_7 = findViewById(R.id.grammar_7);
        grammar_8 = findViewById(R.id.grammar_8);
        grammar_9 = findViewById(R.id.grammar_9);
        grammar_10 = findViewById(R.id.grammar_10);
        grammar_11 = findViewById(R.id.grammar_11);
        grammar_12 = findViewById(R.id.grammar_12);

        grammar_3.setOnClickListener(this);
        grammar_4.setOnClickListener(this);
        grammar_5.setOnClickListener(this);
        grammar_6.setOnClickListener(this);
        grammar_7.setOnClickListener(this);
        grammar_8.setOnClickListener(this);
        grammar_9.setOnClickListener(this);
        grammar_10.setOnClickListener(this);
        grammar_11.setOnClickListener(this);
        grammar_12.setOnClickListener(this);
    }

    private void showCustomDialog() {
        // 다이얼로그 생성
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_app_dialog);

        // 다이얼로그 외부 터치로 닫히지 않게 설정 (선택 사항)
        dialog.setCancelable(true);
        dialog.setOnCancelListener(dialogInterface -> dialog.dismiss());

        // 다이얼로그 뷰 초기화
        Button btnStudy = dialog.findViewById(R.id.btn_study);
        Button btnExam = dialog.findViewById(R.id.btn_test);

        // 버튼 이벤트 설정
        btnStudy.setOnClickListener(v -> {
            navigateToNextActivity(0); // 학습 모드로 이동
            dialog.dismiss();
        });

        btnExam.setOnClickListener(v -> {
            navigateToNextActivity(1); // 시험 모드로 이동
            dialog.dismiss();
        });

        // 다이얼로그 표시
        dialog.show();
    }

    private void navigateToNextActivity(int mode) {
        Intent intent = new Intent(this, GrammarPractice.class); // NextActivity는 대상 액티비티

        intent.putExtra("study_number", study_number); // 선택한 묶음의 인덱스
        intent.putExtra("study_mode", mode); // 0: 학습, 1: 시험

        startActivity(intent);
    }

}
