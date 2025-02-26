package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.media.MediaPlayer;
import java.io.IOException;

public class AmericanArticleMain extends AppApplication implements View.OnClickListener {

    public static final String TAG = "AmericanArticleMain";
    private TextView HeadlineTitle;
    private TextView HeadlineText;
    private ImageView HeadlineImage;
    private Button PolicyButton;
    private Button AudioStopButton;
    private Button AudioPlayButton;
    private Button NewsStudyButton;
    private MediaPlayer mediaPlayer;
    private String mp3FilePath;
    private boolean isPlaying = false;
    private int lastPlaybackPosition = 0; // 🔹 마지막 재생 위치 저장
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_america_article_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.america_article_main_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeClass();

    } //onCreate()



    private void initializeClass() {
        Intent intent = getIntent();
        int articleNumber = intent.getIntExtra("article_number", -1);

        //initialize object
        HeadlineTitle = findViewById(R.id.main_page_title);
        //HeadlineImage = findViewById(R.id.headline_image);
        HeadlineText = findViewById(R.id.headline_text);
        AudioPlayButton = findViewById(R.id.audio_play);
        AudioStopButton = findViewById(R.id.audio_stop);
        //AudioPlayButton = findViewById(R.id.audio_play);
        NewsStudyButton = findViewById(R.id.news_study);

        // 버튼 클릭 리스너 설정
        AudioPlayButton.setOnClickListener(this);
       // TechnologyButton.setOnClickListener(this);
        AudioStopButton.setOnClickListener(this);
        NewsStudyButton.setOnClickListener(this);

        // 🔹 기사 정보 로드
        String[] headlines = AppAmericaArticleApplication.getInstance().getHeadlines();
        String[] transcripts = AppAmericaArticleApplication.getInstance().getTranscripts();
        String[] mp3Urls = AppAmericaArticleApplication.getInstance().getMp3Urls();

        if (articleNumber >= 0 && articleNumber < headlines.length) {
            HeadlineTitle.setText(headlines[articleNumber]);
            HeadlineText.setText(transcripts[articleNumber] != null ? transcripts[articleNumber] : "Transcript not available.");
            mp3FilePath = getFilesDir() + "/article_" + articleNumber + ".mp3"; // 🔹 MP3 파일 경로 저장
        } else {
            HeadlineTitle.setText("Invalid Article");
            HeadlineText.setText("No content available.");
        }

    } //initializeClass()


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.audio_play) {
            toggleAudioPlayback();
        } else if(v.getId() == R.id.audio_stop) {
            stopAudioPlayback();
        }
//        else if(v.getId() == R.id.audio_play) {
//            toggleAudioPlayback();
//        }
        else if(v.getId() == R.id.news_study) {

        }
    } //onClick();

    private void toggleAudioPlayback() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(mp3FilePath);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                AudioPlayButton.setText("Error");
                return;
            }
        }

        if (isPlaying) {
            lastPlaybackPosition = mediaPlayer.getCurrentPosition(); // 🔹 현재 재생 위치 저장
            mediaPlayer.pause();
            isPlaying = false;
            AudioPlayButton.setText("Play");
        } else {
            mediaPlayer.seekTo(lastPlaybackPosition); // 🔹 저장된 위치부터 재생
            mediaPlayer.start();
            isPlaying = true;
            AudioPlayButton.setText("Pause");

            // 🔹 재생이 끝나면 버튼 초기화
            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                lastPlaybackPosition = 0; // 🔹 재생 끝나면 위치 초기화
                AudioPlayButton.setText("Play");
            });
        }
    }

    private void stopAudioPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            lastPlaybackPosition = 0; // 🔹 위치 초기화
            AudioPlayButton.setText("Play");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}
