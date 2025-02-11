package com.example.myapp;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;

public class SpaExaminationHall extends AppApplication implements View.OnClickListener {

    private static final String TAG = "spaExaminationHall";
    private TextView questionNumber = null;
    private LinearProgressIndicator progressBar;
    private Button playPause = null;
    private Button playNext = null;
    ImageView viewImage = null;
    private MediaPlayer mediaPlayer;
    private CountDownTimer timer;
    private int testNum = 0;
    private int currentIndex = 1;
    private int remainingTime = 120;
    private boolean isPaused = false;
    private boolean isMediaPlaying = false;
    private boolean isMediaPlayNow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spa_examination_hall);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.spa_examination_hall_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeClass();
        startPlayback();

    } //onCreate()

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_play_pause) {
            togglePause();
        } else if (v.getId() == R.id.btn_play_next) {
            skipToNext();
        } else if (v.getId() == R.id.image_view) {
            viewImage.setVisibility(View.VISIBLE);
        }

    } //onClick();

    private void initializeClass() {
        // Intent에서 값 가져오기 (기본값 -1 설정: 값이 없을 경우 대비)
        testNum = getIntent().getIntExtra("test_num", -1);

        questionNumber = findViewById(R.id.daily_spa_examination_question_number);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setProgress(0);
        playPause = findViewById(R.id.btn_play_pause);
        playNext = findViewById(R.id.btn_play_next);
        viewImage = findViewById(R.id.image_view);

        playPause.setOnClickListener(this);
        playNext.setOnClickListener(this);
        viewImage.setOnClickListener(this);

        updateQuestionTextImage();
        viewImage.setVisibility(View.GONE);

    }

    private void startPlayback() {
        if (testNum == -1 || currentIndex > 4) return;

        String fileName = "spa_test_" + testNum + "_" + currentIndex;
        int resId = getResources().getIdentifier(fileName, "raw", getPackageName());

        if (resId == 0) return;

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, resId);
        mediaPlayer.setOnCompletionListener(mp -> {
            isMediaPlaying = false;
            isMediaPlayNow = false;
            startCountdown();
        });
        mediaPlayer.start();
        isMediaPlaying = true;
        isMediaPlayNow = true;
    }

    private void startCountdown() {
        if (timer != null) {
            timer.cancel();
        }

        progressBar.setProgress(0); // 다음 문제로 넘어갈 때 프로그래스바 초기화

        timer = new CountDownTimer(remainingTime * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = (int) (millisUntilFinished / 1000);
                progressBar.setProgress(100 - (int) ((remainingTime / 120.0) * 100)); // 왼쪽부터 감소
                if (remainingTime <= 10) {
                    playPause.setEnabled(false);
                    playNext.setEnabled(false);
                }
            }

            @Override
            public void onFinish() {
                if (currentIndex == 4) {
                    finish(); // 마지막 파일 재생 후 액티비티 종료
                } else {
                    currentIndex++;
                    remainingTime = 120;
                    playPause.setEnabled(true);
                    playNext.setEnabled(true);
                    updateQuestionTextImage();
                    progressBar.setProgress(0); // 다음 문제로 넘어갈 때 프로그래스바 초기화
                    startPlayback();
                }
            }
        };
        timer.start();
    }

    private void togglePause() {
        if (isMediaPlayNow) {
            if (isMediaPlaying) {
                mediaPlayer.pause();
                isMediaPlaying = false;
            } else {
                mediaPlayer.start();
                isMediaPlaying = true;
            }
        } else if (timer != null) {
            if (isPaused) {
                startCountdown();
            } else {
                timer.cancel();
            }
            isPaused = !isPaused;
        }
    }

    private void skipToNext() {
        if (currentIndex == 4) {
            finish();
            return;
        }

        // 미디어가 재생 중이면 정지
        if (mediaPlayer != null && isMediaPlaying) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isMediaPlaying = false;
        }

        // 타이머가 존재하면 정지
        if (timer != null) {
            timer.cancel();
        }

        // 남은 시간을 초기화
        remainingTime = 120;

        // 다음 질문으로 이동
        currentIndex++;
        updateQuestionTextImage();

        // 프로그래스바 초기화
        progressBar.setProgress(0);

        // 다음 질문의 미디어를 재생
        startPlayback();


    }

    private void updateQuestionTextImage() {
        int questionResId = getResources().getIdentifier("spa_test_question_" + currentIndex, "string", getPackageName());
        if (questionResId != 0) {
            questionNumber.setText(questionResId);
        }
        if(currentIndex == 4) {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 미디어가 재생 중이면 정지 및 해제
        if (mediaPlayer != null) {
            if (isMediaPlaying) {
                mediaPlayer.stop();
                isMediaPlaying = false;
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // 타이머가 실행 중이면 정지
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        // 액티비티 종료
        finish();
    }

}
