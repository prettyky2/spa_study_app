package com.example.myapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
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
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


import android.os.Handler;

import com.github.adrielcafe.androidaudioconverter.AndroidAudioConverter;
import com.github.adrielcafe.androidaudioconverter.callback.IConvertCallback;
import com.github.adrielcafe.androidaudioconverter.model.AudioFormat;
import com.github.


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
    private Button answerSheetShareAnswer = null;
    private Button answerSheetRecord1 = null;
    private Button answerSheetRecord2 = null;
    private Button answerSheetRecord3 = null;
    private Button answerSheetRecord4 = null;
    String recordedAudioFilePath_1;
    String recordedAudioFilePath_2;
    String recordedAudioFilePath_3;
    String recordedAudioFilePath_4;
    private String combinedQA1;
    private String combinedQA2;
    private String combinedQA3;
    private String combinedQA4;
    private String combinedQAAll; // Q1+A1+Q2+A2+Q3+A3+Q4+A4 전체 저장

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

        if (v.getId() == R.id.answer_sheet_share_answer) {
            showShareDialog();

        }
    } //onClick();

    private void showShareDialog() {
        // 다이얼로그 생성
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_app_share_dialog);

        // 다이얼로그 닫기 불가능하도록 설정 (선택 사항)
        dialog.setCancelable(true);
        dialog.setOnCancelListener(dialogInterface -> dialog.dismiss());

        // 다이얼로그 뷰 초기화
        Button btnQuestion1Text = dialog.findViewById(R.id.question_1_text_button);
        Button btnQuestion2Text = dialog.findViewById(R.id.question_2_text_button);
        Button btnQuestion3Text = dialog.findViewById(R.id.question_3_text_button);
        Button btnQuestion4Text = dialog.findViewById(R.id.question_4_text_button);
        Button btnQuestionAllText = dialog.findViewById(R.id.question_all_text_button);
        Button btnQuestion1Mp3 = dialog.findViewById(R.id.question_1_mp3_button);
        Button btnQuestion2Mp3 = dialog.findViewById(R.id.question_2_mp3_button);
        Button btnQuestion3Mp3 = dialog.findViewById(R.id.question_3_mp3_button);
        Button btnQuestion4Mp3 = dialog.findViewById(R.id.question_4_mp3_button);
        Button btnQuestionAllMp3 = dialog.findViewById(R.id.question_all_mp3_button);



        // 버튼 이벤트 설정
        // 🔹 텍스트 공유 버튼 이벤트 설정
        btnQuestion1Text.setOnClickListener(v -> shareText(combinedQA1));
        btnQuestion2Text.setOnClickListener(v -> shareText(combinedQA2));
        btnQuestion3Text.setOnClickListener(v -> shareText(combinedQA3));
        btnQuestion4Text.setOnClickListener(v -> shareText(combinedQA4));
        btnQuestionAllText.setOnClickListener(v -> shareText(combinedQAAll));

        // 🔹 MP3 공유 버튼 이벤트 설정
        btnQuestion1Mp3.setOnClickListener(v -> convertAndShareMP3(recordedAudioFilePath_1, "audio_question_1.mp3"));
        btnQuestion2Mp3.setOnClickListener(v -> convertAndShareMP3(recordedAudioFilePath_2, "audio_question_2.mp3"));
        btnQuestion3Mp3.setOnClickListener(v -> convertAndShareMP3(recordedAudioFilePath_3, "audio_question_3.mp3"));
        btnQuestion4Mp3.setOnClickListener(v -> convertAndShareMP3(recordedAudioFilePath_4, "audio_question_4.mp3"));
        btnQuestionAllMp3.setOnClickListener(v -> convertAndShareAllMP3());


        // 다이얼로그 표시
        dialog.show();
    }

    private void convertAndShareMP3(String pcmFilePath, String mp3FileName) {
        if (pcmFilePath == null || pcmFilePath.isEmpty()) {
            Toast.makeText(this, "녹음된 PCM 파일이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        File pcmFile = new File(pcmFilePath);
        if (!pcmFile.exists()) {
            Toast.makeText(this, "PCM 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        File mp3File = new File(getFilesDir(), mp3FileName);

        // PCM -> MP3 변환
        AndroidAudioConverter.with(this)
                .setFile(pcmFile)
                .setFormat(AudioFormat.MP3)
                .setCallback(new IConvertCallback() {
                    @Override
                    public void onSuccess(File convertedFile) {
                        Log.d(TAG, "MP3 변환 성공: " + convertedFile.getAbsolutePath());
                        shareAudio(convertedFile.getAbsolutePath()); // 변환 성공 후 자동 공유
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Log.e(TAG, "MP3 변환 실패", error);
                        Toast.makeText(SpaExaminationResult.this, "MP3 변환 실패", Toast.LENGTH_SHORT).show();
                    }
                })
                .convert();
    }

    private void convertAndShareAllMP3() {
        ArrayList<String> pcmFiles = new ArrayList<>();
        ArrayList<String> mp3Files = new ArrayList<>();

        if (recordedAudioFilePath_1 != null) {
            pcmFiles.add(recordedAudioFilePath_1);
            mp3Files.add(getFilesDir() + "/audio_question_1.mp3");
        }
        if (recordedAudioFilePath_2 != null) {
            pcmFiles.add(recordedAudioFilePath_2);
            mp3Files.add(getFilesDir() + "/audio_question_2.mp3");
        }
        if (recordedAudioFilePath_3 != null) {
            pcmFiles.add(recordedAudioFilePath_3);
            mp3Files.add(getFilesDir() + "/audio_question_3.mp3");
        }
        if (recordedAudioFilePath_4 != null) {
            pcmFiles.add(recordedAudioFilePath_4);
            mp3Files.add(getFilesDir() + "/audio_question_4.mp3");
        }

        if (pcmFiles.isEmpty()) {
            Toast.makeText(this, "공유할 MP3 파일이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < pcmFiles.size(); i++) {
            String pcmFilePath = pcmFiles.get(i);
            String mp3FilePath = mp3Files.get(i);

            convertAndShareMP3(pcmFilePath, mp3FilePath);
        }
    }


    private void shareText(String text) {
        if (text == null || text.isEmpty()) {
            Toast.makeText(this, "공유할 내용이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(shareIntent, "공유 항목을 선택하세요"));
    }



    private void shareAudio(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            Toast.makeText(this, "MP3 파일이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(this, "MP3 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("audio/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        startActivity(Intent.createChooser(shareIntent, "MP3 공유"));
    }

    private void shareAllAudio() {
        ArrayList<Uri> audioUris = new ArrayList<>();

        if (recordedAudioFilePath_1 != null) audioUris.add(Uri.fromFile(new File(recordedAudioFilePath_1)));
        if (recordedAudioFilePath_2 != null) audioUris.add(Uri.fromFile(new File(recordedAudioFilePath_2)));
        if (recordedAudioFilePath_3 != null) audioUris.add(Uri.fromFile(new File(recordedAudioFilePath_3)));
        if (recordedAudioFilePath_4 != null) audioUris.add(Uri.fromFile(new File(recordedAudioFilePath_4)));

        if (audioUris.isEmpty()) {
            Toast.makeText(this, "공유할 MP3 파일이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.setType("audio/*");
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, audioUris);
        startActivity(Intent.createChooser(shareIntent, "MP3 파일 여러 개 공유"));
    }

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
        answerSheetShareAnswer = findViewById(R.id.answer_sheet_share_answer);
        answerSheetRecord1 = findViewById(R.id.answer_sheet_record_1);
        answerSheetRecord2 = findViewById(R.id.answer_sheet_record_2);
        answerSheetRecord3 = findViewById(R.id.answer_sheet_record_3);
        answerSheetRecord4 = findViewById(R.id.answer_sheet_record_4);

        answerSheetShareAnswer.setOnClickListener(this);
        answerSheetRecord1.setOnClickListener(this);
        answerSheetRecord2.setOnClickListener(this);
        answerSheetRecord3.setOnClickListener(this);
        answerSheetRecord4.setOnClickListener(this);


        setQuestionAndAnswer(getIntentQuestionNum);





    }

    private void setQuestionAndAnswer(int examNum) {
        StringBuilder qaBuilderAll = new StringBuilder(); // 전체 Q&A 저장용

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

        for (int i = 1; i <= 4; i++) {
            // 🔹 질문 가져오기
            String questionResourceName = "spa_examination_" + examNum + "_question_" + i;
            int questionResId = getResources().getIdentifier(questionResourceName, "string", getPackageName());
            String questionText = (questionResId != 0) ? getResources().getString(questionResId) : "Question not found";

            // 🔹 답변 가져오기
            String answerText = getIntent().getStringExtra("answer_" + i);
            if (answerText == null) {
                answerText = "No Answer"; // 답변이 없는 경우 기본값 설정
            }

            // 🔹 TextView에 설정
            int textViewResId = getResources().getIdentifier("answer_sheet_question_" + i, "id", getPackageName());
            int answerViewResId = getResources().getIdentifier("answer_sheet_answer_" + i, "id", getPackageName());
            TextView questionView = findViewById(textViewResId);
            TextView answerView = findViewById(answerViewResId);

            if (questionView != null) questionView.setText(questionText);
            if (answerView != null) answerView.setText(answerText);

            // 🔹 개별 Q&A 저장
            String combinedQA = "Q" + i + ": " + questionText + "\n" +
                    "A" + i + ": " + answerText + "\n\n";

            switch (i) {
                case 1:
                    combinedQA1 = combinedQA;
                    break;
                case 2:
                    combinedQA2 = combinedQA;
                    break;
                case 3:
                    combinedQA3 = combinedQA;
                    break;
                case 4:
                    combinedQA4 = combinedQA;
                    break;
            }

            // 🔹 전체 Q&A 문자열에 추가
            qaBuilderAll.append(combinedQA);
        }

        // 🔹 최종적으로 전체 Q&A 저장
        combinedQAAll = qaBuilderAll.toString();
        Log.d(TAG, "Combined Q&A (All): \n" + combinedQAAll);

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
