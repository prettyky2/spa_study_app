package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

public class DailyStudyPractice extends AppApplication implements View.OnClickListener {

    private static final String TAG = "DailyStudyPractice";
    private String studyTopic;
    private String imagePopUp;
    private List<String> sentences;
    private int currentSentenceIndex = 0;

    private TextView exampleInterpretation;
    private TextView exampleSentence;
    private ImageView practiceImage;
    private LinearProgressIndicator progressBar;
    private GestureDetector gestureDetector;
    int practice_mode;
    TextView practiceTitle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_daily_study_practice);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.daily_study_page_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeClass();

        // 스와이프 감지 리스너 추가
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (sentences == null || sentences.isEmpty()) {
                    Log.e(TAG, "No sentences available.");
                    return false;
                }

                if (e1.getX() - e2.getX() > 100) {
                    // 왼쪽으로 스와이프 (다음 문장)
                    if (currentSentenceIndex < sentences.size() - 1) {
                        currentSentenceIndex++;
                        updateSentenceView();
                        if (practice_mode == 1) {
                            exampleSentence.setVisibility(View.GONE);
                        }
                    }
                } else if (e2.getX() - e1.getX() > 100) {
                    // 오른쪽으로 스와이프 (이전 문장)
                    if (currentSentenceIndex > 0) {
                        currentSentenceIndex--;
                        updateSentenceView();
                        if (practice_mode == 1) {
                            exampleSentence.setVisibility(View.GONE);
                        }
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    }

    private void initializeClass() {
        Intent intent = getIntent();
        studyTopic = intent.getStringExtra("study_topic");
        imagePopUp = intent.getStringExtra("image_popup");
        sentences = intent.getStringArrayListExtra("sentences");
        practice_mode = intent.getIntExtra("mode", -1); // 기본값 -1

        Log.d(TAG, "studyTopic: " + studyTopic);
        Log.d(TAG, "imagePopUp: " + imagePopUp);
        Log.d(TAG, "Selected Mode: " + practice_mode);

        practiceTitle = findViewById(R.id.daily_study_practice_title);
        exampleInterpretation = findViewById(R.id.example_interpretation);
        exampleSentence = findViewById(R.id.example_sentence);
        practiceImage = findViewById(R.id.daily_study_practice_image);
        progressBar = findViewById(R.id.progress_bar);

        practiceTitle.setOnClickListener(this);
        exampleSentence.setOnClickListener(this);

        // 4단원 일 경우의 이미지 설정
        if ("no".equals(imagePopUp)) {
            practiceImage.setVisibility(View.GONE);
        } else {
            int imageResId = getResources().getIdentifier(imagePopUp, "drawable", getPackageName());
            if (imageResId != 0) {
                practiceImage.setImageResource(imageResId);
                practiceImage.setVisibility(View.VISIBLE);
            } else {
                Log.e(TAG, "Image not found for: " + imagePopUp);
                practiceImage.setVisibility(View.GONE);
            }
        }

        // 첫 번째 문장 표시
        if (sentences != null && !sentences.isEmpty()) {
            currentSentenceIndex = 0;
            updateSentenceView();
        } else {
            exampleInterpretation.setText("No sentences available.");
            exampleSentence.setText("");
        }

        if (practice_mode == 1) {
            exampleSentence.setVisibility(View.GONE);
        }

    }

    private void updateSentenceView() {
        if (sentences != null && currentSentenceIndex >= 0 && currentSentenceIndex < sentences.size()) {
            String[] sentenceParts = sentences.get(currentSentenceIndex).split(" \\| ");
            if (sentenceParts.length == 2) {
                exampleInterpretation.setText(sentenceParts[0]); // 한글 문장
                exampleSentence.setText(sentenceParts[1]); // 영어 문장
            } else {
                exampleInterpretation.setText(sentences.get(currentSentenceIndex));
                exampleSentence.setText("");
            }

            // ProgressBar 업데이트
            updateProgressBar();
        }
    }

    private void updateProgressBar() {
        if (progressBar != null && sentences != null && !sentences.isEmpty()) {
            int progress = (int) (((float) (currentSentenceIndex + 1) / sentences.size()) * 100);
            progressBar.setProgress(progress);
            Log.d(TAG, "ProgressBar updated: " + progress + "%");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.example_sentence) {
            String textToRead = exampleSentence.getText().toString();
            if (!textToRead.isEmpty()) {
                AppTTSPlayer.getInstance(this).speak(textToRead);
            } else {
                Log.e(TAG, "No text to read.");
            }
        } else if(v.getId() == R.id.daily_study_practice_title) {
            exampleSentence.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onNextPressed() {
        if (sentences != null && currentSentenceIndex < sentences.size() - 1) {
            currentSentenceIndex++;
            updateSentenceView();
            if (practice_mode == 1) {
                exampleSentence.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onPreviousPressed() {
        if (sentences != null && currentSentenceIndex > 0) {
            currentSentenceIndex--;
            updateSentenceView();
            if (practice_mode == 1) {
                exampleSentence.setVisibility(View.GONE);
            }
        }
    }

}