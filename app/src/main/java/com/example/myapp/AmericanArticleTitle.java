package com.example.myapp;

import android.os.Bundle;
import android.view.View;

import android.util.Log;

import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.example.myapp.AmericanArticleMain;

public class AmericanArticleTitle extends AppApplication implements View.OnClickListener {

    public static final String TAG = "AmericanArticleTitle";
    private TextView article_1, article_2, article_3, article_4, article_5, article_6, article_7, article_8, article_9, article_10;


    // 🔹 현재 표시되는 뉴스 타입 (true = 정치, false = 기술)

    private LottieAnimationView loadingProgress;
    private View loading_background;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_america_article_title);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.america_article_title_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeClass();

    } //onCreate()

    // 🔹 BroadcastReceiver 정의
    private final BroadcastReceiver mp3DownloadCompletionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Mp3DownloadBroadcastReceiver", "📥 TTS 완료 브로드캐스트 수신됨!");

            if ("com.example.myapp.MP3_DOWNLOAD_COMPLETED".equals(intent.getAction())) {
                loading_background.setVisibility(View.GONE);
                loadingProgress.setVisibility(View.GONE);
                updateArticlesDisplay();
            }
        }
    };

    private void initializeClass() {
        IntentFilter filter = new IntentFilter("com.example.myapp.MP3_DOWNLOAD_COMPLETED");
        registerReceiver(mp3DownloadCompletionReceiver, filter, Context.RECEIVER_EXPORTED);

        //initialize object
        article_1 = findViewById(R.id.article_1);
        article_2 = findViewById(R.id.article_2);
        article_3 = findViewById(R.id.article_3);
        article_4 = findViewById(R.id.article_4);
        article_5 = findViewById(R.id.article_5);
        article_6 = findViewById(R.id.article_6);
        article_7 = findViewById(R.id.article_7);
        article_8 = findViewById(R.id.article_8);
        article_9 = findViewById(R.id.article_9);
        article_10 = findViewById(R.id.article_10);
        loadingProgress = findViewById(R.id.loading_progress);
        loading_background = findViewById(R.id.loading_background);

        // 버튼 클릭 리스너 설정
        article_1.setOnClickListener(this);
        article_2.setOnClickListener(this);
        article_3.setOnClickListener(this);
        article_4.setOnClickListener(this);
        article_5.setOnClickListener(this);
        article_6.setOnClickListener(this);
        article_7.setOnClickListener(this);
        article_8.setOnClickListener(this);
        article_9.setOnClickListener(this);
        article_10.setOnClickListener(this);



        if(AppAmericaArticleApplication.getInstance().AmericaHeadlineCrawlingDone == 1) {
            loading_background.setVisibility(View.GONE);
            loadingProgress.setVisibility(View.GONE);
            updateArticlesDisplay();
        }
    } //initializeClass()


    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, AmericanArticleMain.class);
        int article_number = 0;
        if(v.getId() == R.id.article_1) {
            article_number = 1;
        } else if(v.getId() == R.id.article_2) {
            article_number = 2;
        } else if(v.getId() == R.id.article_3) {
            article_number = 3;
        } else if(v.getId() == R.id.article_4) {
            article_number = 4;
        } else if(v.getId() == R.id.article_5) {
            article_number = 5;
        } else if(v.getId() == R.id.article_6) {
            article_number = 6;
        } else if(v.getId() == R.id.article_7) {
            article_number = 7;
        } else if(v.getId() == R.id.article_8) {
            article_number = 8;
        } else if(v.getId() == R.id.article_9) {
            article_number = 9;
        } else if(v.getId() == R.id.article_10) {
            article_number = 10;
        }
        intent.putExtra("article_number", article_number); // 선택한 묶음의 인덱스
        startActivity(intent);
    } //onClick();

    private void updateArticlesDisplay() {
        String[] headlines = AppAmericaArticleApplication.getInstance().getHeadlines();
        if (headlines == null) return;

        article_1.setText(headlines.length > 0 ? "▶ " + headlines[0] : "");
        article_2.setText(headlines.length > 1 ? "▶ " + headlines[1] : "");
        article_3.setText(headlines.length > 2 ? "▶ " + headlines[2] : "");
        article_4.setText(headlines.length > 3 ? "▶ " + headlines[3] : "");
        article_5.setText(headlines.length > 4 ? "▶ " + headlines[4] : "");
        article_6.setText(headlines.length > 5 ? "▶ " + headlines[5] : "");
        article_7.setText(headlines.length > 6 ? "▶ " + headlines[6] : "");
        article_8.setText(headlines.length > 7 ? "▶ " + headlines[7] : "");
        article_9.setText(headlines.length > 8 ? "▶ " + headlines[8] : "");
        article_10.setText(headlines.length > 9 ? "▶ " + headlines[9] : "");
    }



}
