package com.example.myapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.api.gax.rpc.BidiStreamingCallable;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SpaExaminationResult extends AppApplication implements View.OnClickListener {

    private static final String TAG = "spaExaminationResult";

    private TextView answerSheetQuestion_1 = null;
    private TextView answerSheetQuestion_2 = null;
    private TextView answerSheetQuestion_3 = null;
    private TextView answerSheetQuestion_4 = null;
    private TextView answerSheetAnswer_1 = null;
    private TextView answerSheetAnswer_2 = null;
    private TextView answerSheetAnswer_3 = null;
    private TextView answerSheetAnswer_4 = null;
    private ImageView answerSheetImage_1 = null;
    private int getIntentQuestionNum = -1;
    private String getIntentAnswer_1 = null;
    private String getIntentAnswer_2 = null;
    private String getIntentAnswer_3 = null;
    private String getIntentAnswer_4 = null;
    private Button answer_sheet_record_1 = null;
    private Button answer_sheet_record_2 = null;
    private Button answer_sheet_record_3 = null;
    private Button answer_sheet_record_4 = null;
    String recordedAudioFilePath_1;
    String recordedAudioFilePath_2;
    String recordedAudioFilePath_3;
    String recordedAudioFilePath_4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spa_examination_result);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.spa_examination_hall_result_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeClass();


    } //onCreate()

    @Override
    public void onClick(View v) {
        String filePath = null;

        if (v.getId() == R.id.answer_sheet_record_1) {
            filePath = recordedAudioFilePath_1;
        } else if (v.getId() == R.id.answer_sheet_record_2) {
            filePath = recordedAudioFilePath_2;
        } else if (v.getId() == R.id.answer_sheet_record_3) {
            filePath = recordedAudioFilePath_3;
        } else if (v.getId() == R.id.answer_sheet_record_4) {
            filePath = recordedAudioFilePath_4;
        }

        if (filePath == null) {
            Toast.makeText(this, "녹음된 파일이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        playAudioPCM(filePath);
    } //onClick();

    private void initializeClass() {
        Intent intent = getIntent();
        getIntentQuestionNum = intent.getIntExtra("test_num", -1);
        getIntentAnswer_1 = intent.getStringExtra("answer_1");
        getIntentAnswer_2 = intent.getStringExtra("answer_2");
        getIntentAnswer_3 = intent.getStringExtra("answer_3");
        getIntentAnswer_4 = intent.getStringExtra("answer_4");
        recordedAudioFilePath_1 = getIntent().getStringExtra("audio_file_1");
        recordedAudioFilePath_2 = getIntent().getStringExtra("audio_file_2");
        recordedAudioFilePath_3 = getIntent().getStringExtra("audio_file_3");
        recordedAudioFilePath_4 = getIntent().getStringExtra("audio_file_4");


        answerSheetQuestion_1 = findViewById(R.id.answer_sheet_question_1);
        answerSheetQuestion_2 = findViewById(R.id.answer_sheet_question_2);
        answerSheetQuestion_3 = findViewById(R.id.answer_sheet_question_3);
        answerSheetQuestion_4 = findViewById(R.id.answer_sheet_question_4);
        answerSheetAnswer_1 = findViewById(R.id.answer_sheet_answer_1);
        answerSheetAnswer_2 = findViewById(R.id.answer_sheet_answer_2);
        answerSheetAnswer_3 = findViewById(R.id.answer_sheet_answer_3);
        answerSheetAnswer_4 = findViewById(R.id.answer_sheet_answer_4);
        answerSheetImage_1 = findViewById(R.id.answer_sheet_image_1);
        answer_sheet_record_1 = findViewById(R.id.answer_sheet_record_1);
        answer_sheet_record_2 = findViewById(R.id.answer_sheet_record_2);
        answer_sheet_record_3 = findViewById(R.id.answer_sheet_record_3);
        answer_sheet_record_4 = findViewById(R.id.answer_sheet_record_4);

        answer_sheet_record_1.setOnClickListener(this);
        answer_sheet_record_2.setOnClickListener(this);
        answer_sheet_record_3.setOnClickListener(this);
        answer_sheet_record_4.setOnClickListener(this);


        setQuestionAndAnswer(getIntentQuestionNum);





    }

    private void setQuestionAndAnswer(int examNum) {
        for (int i = 1; i <= 4; i++) {
            // 🔹 동적으로 리소스 ID 가져오기
            String questionResourceName = "spa_examination_" + examNum + "_question_" + i;
            int questionResId = getResources().getIdentifier(questionResourceName, "string", getPackageName());

            // 🔹 XML에서 정의된 ID 이름과 맞춤
            int textViewResId = getResources().getIdentifier("answer_sheet_question_" + i, "id", getPackageName());
            int answerViewResId = getResources().getIdentifier("answer_sheet_answer_" + i, "id", getPackageName());

            TextView questionView = findViewById(textViewResId);
            TextView answerView = findViewById(answerViewResId);

            // 🔹 질문 설정
            if (questionView != null && questionResId != 0) {
                questionView.setText(getResources().getString(questionResId));
            } else {
                Log.e(TAG, "Invalid resource for question " + i + " (resourceName=" + questionResourceName + ")");
            }

            // 🔹 답변 설정 (인텐트에서 올바른 이름으로 가져옴)
            String intentAnswer = getIntent().getStringExtra("answer_" + i);
            if (answerView != null) {
                answerView.setText(intentAnswer != null ? intentAnswer : "No Answer");
            }
        }

        // 🔹 이미지 설정
        int imageResId = getResources().getIdentifier("spa_test_" + examNum, "drawable", getPackageName());
        if (answerSheetImage_1 != null) {
            if (imageResId != 0) {
                answerSheetImage_1.setImageResource(imageResId);
            } else {
                Log.e(TAG, "Invalid image resource for spa_test_" + examNum);
            }
        }
    }

    private void playAudioPCM(String filePath) {
        if (filePath == null) {
            Log.e(TAG, "Audio file path is null, cannot play.");
            return;
        }

        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            fis.close();

            int sampleRate = 16000; // 녹음할 때 사용한 샘플레이트
            AudioTrack audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    buffer.length,
                    AudioTrack.MODE_STATIC
            );

            audioTrack.write(buffer, 0, buffer.length);
            audioTrack.play();

            Log.d(TAG, "Playing PCM audio from: " + filePath);
        } catch (IOException e) {
            Log.e(TAG, "Failed to play PCM audio file", e);
        }
    }

}
