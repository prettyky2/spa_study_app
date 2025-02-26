package com.example.myapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;

import java.io.File;

/*
public class OldAmericanHeadline extends AppApplication implements View.OnClickListener {

    public static final String TAG = "AmericanHeadline";
    private TextView HeadlineTitle;
    private TextView HeadlineText;
    private ImageView HeadlineImage;
    private Button PolicyButton;
    private Button TechnologyButton;
    private Button AudioPlayButton;
    private Button NewsStudyButton;


    // 🔹 현재 표시되는 뉴스 타입 (true = 정치, false = 기술)
    private boolean isPoliticsDisplayed = true;
    // 🔹 TTS 재생 상태 변수
    private boolean isTTSPlaying = false;
    private boolean isTTSPaused = false;
    // 🔹 TTS 플레이어 객체
    private AppAmericanHeadlineTTSPlayer ttsPlayer;
    private LottieAnimationView loadingProgress;
    private View loading_background;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_america_headline_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.america_headline_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeClass();

    } //onCreate()

    // 🔹 BroadcastReceiver 정의
    private final BroadcastReceiver ttsCompletionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("TTSBroadcastReceiver", "📥 TTS 완료 브로드캐스트 수신됨!");

            if ("com.example.myapp.TTS_COMPLETED".equals(intent.getAction())) {
                loading_background.setVisibility(View.GONE);
                loadingProgress.setVisibility(View.GONE);
                updateNewsDisplay();
                Log.d("TTSBroadcastReceiver", "✅ 로딩 숨김 완료 & 뉴스 업데이트");
            }
        }
    };

    private void initializeClass() {

        // 🔹 BroadcastReceiver 등록 (TTS 완료 시 로딩 종료)
        IntentFilter filter = new IntentFilter("com.example.myapp.TTS_COMPLETED");
        registerReceiver(ttsCompletionReceiver, filter, Context.RECEIVER_EXPORTED);


        //initialize object
        HeadlineTitle = findViewById(R.id.headline_title);
        HeadlineImage = findViewById(R.id.headline_image);
        HeadlineText = findViewById(R.id.headline_text);
        PolicyButton = findViewById(R.id.policy_news);
        TechnologyButton = findViewById(R.id.technology_news);
        AudioPlayButton = findViewById(R.id.audio_play);
        NewsStudyButton = findViewById(R.id.news_study);
        loadingProgress = findViewById(R.id.loading_progress);
        loading_background = findViewById(R.id.loading_background);

        // 버튼 클릭 리스너 설정
        PolicyButton.setOnClickListener(this);
        TechnologyButton.setOnClickListener(this);
        AudioPlayButton.setOnClickListener(this);
        NewsStudyButton.setOnClickListener(this);

        // 🔹 TTS 객체 초기화
        ttsPlayer = AppAmericanHeadlineTTSPlayer.getInstance(this);

        if(AppAmericaHeadlineApplication.getInstance().AmericaHeadlineCrawlingDone == 1) {
            loading_background.setVisibility(View.GONE);
            loadingProgress.setVisibility(View.GONE);
            updateNewsDisplay();
        }

    } //initializeClass()


    @Override
    public void onClick(View v) {
        ScrollView scrollView = findViewById(R.id.scrollable_section);
        if(v.getId() == R.id.policy_news) {
            isPoliticsDisplayed = true;
            // 🔹 기존 음성이 재생 중이거나 일시정지 중이면 중지
            if (isTTSPlaying || isTTSPaused) {
                ttsPlayer.stop();
                isTTSPlaying = false;
                isTTSPaused = false;  // 일시정지 상태도 해제
            }
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_UP));
            updateNewsDisplay();
        } else if(v.getId() == R.id.technology_news) {
            isPoliticsDisplayed = false;
            // 🔹 기존 음성이 재생 중이거나 일시정지 중이면 중지
            if (isTTSPlaying || isTTSPaused) {
                ttsPlayer.stop();
                isTTSPlaying = false;
                isTTSPaused = false;  // 일시정지 상태도 해제
            }
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_UP));
            updateNewsDisplay();
        } else if(v.getId() == R.id.audio_play) {
            toggleTTSPlayback();
        } else if(v.getId() == R.id.news_study) {

        }




    } //onClick();

    // 🔹 화면에 뉴스 업데이트
    private void updateNewsDisplay() {
        if (isPoliticsDisplayed) {
            HeadlineTitle.setText(AppAmericaHeadlineApplication.getInstance().getPoliticsTitle());
            Log.d(TAG, "Updating politics news: " + AppAmericaHeadlineApplication.getInstance().getPoliticsTitle());
            HeadlineText.setText(AppAmericaHeadlineApplication.getInstance().getPoliticsContent());
            Log.d(TAG, "Updating politics news: " + AppAmericaHeadlineApplication.getInstance().getPoliticsContent());
            Glide.with(this).load(AppAmericaHeadlineApplication.getInstance().getPoliticsImageUrl()).into(HeadlineImage);
        } else {
            HeadlineTitle.setText(AppAmericaHeadlineApplication.getInstance().getTechnologyTitle());
            HeadlineText.setText(AppAmericaHeadlineApplication.getInstance().getTechnologyContent());
            Glide.with(this).load(AppAmericaHeadlineApplication.getInstance().getTechnologyImageUrl()).into(HeadlineImage);
        }
    }

    // 🔹 TTS 재생/일시정지 토글
    private void toggleTTSPlayback() {
        File internalDir = getFilesDir();
        File audioFile = new File(internalDir, isPoliticsDisplayed ? "politics_audio.wav" : "tech_audio.wav");

        if (!audioFile.exists()) {
            Log.e(TAG, "TTS 재생 실패: " + audioFile.getAbsolutePath() + " 파일을 찾을 수 없음.");
            return;
        }

        if (isTTSPlaying) {
            ttsPlayer.pause();
            isTTSPlaying = false;
            isTTSPaused = true;
            AudioPlayButton.setText("Play");
        } else {
            if (isTTSPaused) {
                ttsPlayer.play(audioFile);  // 🔹 일시정지된 상태라면 다시 시작
            } else {
                ttsPlayer.play(audioFile);  // 🔹 새롭게 재생
            }
            isTTSPlaying = true;
            isTTSPaused = false;
            AudioPlayButton.setText("Pause");
        }
    }

    // 🔹 액티비티가 포커스를 잃으면 TTS 종료
    @Override
    protected void onPause() {
        super.onPause();
        if (isTTSPlaying) {
            ttsPlayer.stop();  // 🔹 TTS 중지
            isTTSPlaying = false;
            AudioPlayButton.setText("Play");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(ttsCompletionReceiver);
    }



}
*/