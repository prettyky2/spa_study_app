package com.example.myapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.Image;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.util.Log;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.FileInputStream;

import android.Manifest;

import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.media.MediaPlayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.auth.oauth2.GoogleCredentials;

import com.google.protobuf.ByteString;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.api.gax.rpc.StreamController;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.BidiStreamingCallable;
import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.RecognitionConfig;

import io.grpc.stub.StreamObserver;

import com.google.api.gax.rpc.BidiStreamingCallable;
import com.google.api.gax.rpc.ApiStreamObserver;
import io.grpc.stub.StreamObserver;
import com.google.cloud.speech.v1.*;
import android.media.AudioRecord;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.AudioManager;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class SpaExaminationHall extends AppApplication implements View.OnClickListener {

    private static final String TAG = "spaExaminationHall";
    private GoogleCredentials googleCredentials; // 🔹 전역 변수로 선언
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private TextView questionNumber = null;
    private LinearProgressIndicator progressBar;
    private Button playPause = null;
    private Button playNext = null;
    private ImageView viewImage = null;
    private MediaPlayer mediaPlayer;
    private CountDownTimer timer;
    private TextView userAnswer = null;
    private int testNum = 0;
    private int currentIndex = 1;
    private int remainingTime = 120;
    private boolean isPaused = false;
    private boolean isMediaPlaying = false;
    private boolean isMediaPlayNow = false;
    private MediaRecorder recorder;
    private SpeechClient speechClient;
    private boolean isListening = false;
    private ScheduledExecutorService executorService;
    private ApiStreamObserver<StreamingRecognizeRequest> requestObserver;
    private String answer_1 = null;
    private String answer_2 = null;
    private String answer_3 = null;
    private String answer_4 = null;
    private String lastRecognizedText = ""; // 🔹 마지막으로 인식된 문장을 저장
    private ConstraintLayout dailySpaExaminationHallUserAnswer;
    private ByteArrayOutputStream byteArrayOutputStream;
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private String recordedAudioFilePath_1;
    private String recordedAudioFilePath_2;
    private String recordedAudioFilePath_3;
    private String recordedAudioFilePath_4;


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
        userAnswer = findViewById(R.id.daily_spa_examination_hall_user_answer);

        playPause.setOnClickListener(this);
        playNext.setOnClickListener(this);
        viewImage.setOnClickListener(this);
        userAnswer.setOnClickListener(this);

        updateQuestionTextImage();
        viewImage.setVisibility(View.GONE);

        // ✅ GoogleCredentials을 한 번만 로드하여 저장
        try {
            InputStream credentialsStream = getResources().openRawResource(R.raw.spastudyproject_key);
            googleCredentials = GoogleCredentials.fromStream(credentialsStream);
            Log.d(TAG, "GoogleCredentials loaded successfully.");
        } catch (Exception e) {
            googleCredentials = null; // 🔹 예외 발생 시 null로 설정
            Log.e(TAG, "Error loading GoogleCredentials", e);
        }

        // ✅ 마이크 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // 권한 요청만 하고, 승인 후 실행
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Microphone permission granted. Starting recording & speech recognition...");
            } else {
                Log.e(TAG, "Microphone permission denied.");
                Toast.makeText(this, "마이크 권한이 필요합니다. 설정에서 권한을 활성화하세요.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startPlayback() {
        if (testNum == -1 || currentIndex > 4) return;

        updateUILayout(); // 🔹 UI 변경 반영

        if(currentIndex ==  4) {
            viewImage.setVisibility(View.VISIBLE);
            // 🔹 동적으로 이미지 리소스 가져오기
            String resourceName = "spa_test_" + testNum;
            int resId = getResources().getIdentifier(resourceName, "drawable", getPackageName());

            if (resId != 0) {
                viewImage.setImageResource(resId);
            } else {
                Log.e(TAG, "Invalid resource: " + resourceName);
            }
        }

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
        startSpeechRecognition(); // 음성 인식 시작
        startRecording();

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
                Log.d(TAG, "Countdown finished. Stopping speech recognition.");

                stopSpeechRecognition(); // ✅ 음성 인식 종료
                saveUserAnswer(); // ✅ 저장
                runOnUiThread(() -> userAnswer.setText("")); // ✅ userAnswer 초기화

                stopRecording();

                if (currentIndex == 4) {
                    Intent intent = new Intent(SpaExaminationHall.this, SpaExaminationResult.class);
                    intent.putExtra("test_num", testNum);
                    intent.putExtra("answer_1", answer_1);
                    intent.putExtra("answer_2", answer_2);
                    intent.putExtra("answer_3", answer_3);
                    intent.putExtra("answer_4", answer_4);
                    // ✅ 문제별 녹음 파일 경로를 Intent에 추가
                    if (recordedAudioFilePath_1 != null) intent.putExtra("audio_file_1", recordedAudioFilePath_1);
                    if (recordedAudioFilePath_2 != null) intent.putExtra("audio_file_2", recordedAudioFilePath_2);
                    if (recordedAudioFilePath_3 != null) intent.putExtra("audio_file_3", recordedAudioFilePath_3);
                    if (recordedAudioFilePath_4 != null) intent.putExtra("audio_file_4", recordedAudioFilePath_4);

                    startActivity(intent);
                    finish();
                    //new android.os.Handler().postDelayed(() -> finish(), 500);
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

    private void startSpeechRecognition() {
        if (googleCredentials == null) {
            Log.e(TAG, "GoogleCredentials is null! Speech recognition cannot start.");
            Toast.makeText(this, "음성 인식 API 인증 정보가 없습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Log.d(TAG, "Starting speech recognition...");

            // ✅ GoogleCredentials 재사용
            SpeechSettings speechSettings = SpeechSettings.newBuilder()
                    .setCredentialsProvider(() -> googleCredentials)
                    .build();
            speechClient = SpeechClient.create(speechSettings);

            // ✅ responseObserver를 올바르게 구현 (ApiStreamObserver 사용)
            ApiStreamObserver<StreamingRecognizeResponse> responseObserver = new ApiStreamObserver<StreamingRecognizeResponse>() {
                @Override
                public void onNext(StreamingRecognizeResponse response) {
                    for (StreamingRecognitionResult result : response.getResultsList()) {
                        String transcript = result.getAlternatives(0).getTranscript();
                        Log.d(TAG, "Recognized speech: " + transcript);

                        runOnUiThread(() -> {
                            if (!result.getIsFinal()) {
                                // 🔹 중간 결과도 누적해서 표시
                                userAnswer.setText(lastRecognizedText + " " + transcript);
                            } else {
                                // 🔹 최종 결과 확정 시 누적 갱신
                                lastRecognizedText += " " + transcript;
                                lastRecognizedText = lastRecognizedText.trim();
                                userAnswer.setText(lastRecognizedText);
                            }
                        });


                    }
                }

                @Override
                public void onError(Throwable t) {
                    Log.e(TAG, "Speech recognition error: " + t.getMessage(), t);
                }

                @Override
                public void onCompleted() {
                    Log.d(TAG, "Speech recognition completed.");
                }
            };

            // ✅ `bidiStreamingCall()`을 올바르게 호출
            BidiStreamingCallable<StreamingRecognizeRequest, StreamingRecognizeResponse> callable =
                    speechClient.streamingRecognizeCallable();

            // ✅ requestObserver의 타입을 올바르게 설정 (강제 캐스팅 제거)
            requestObserver = callable.bidiStreamingCall(responseObserver);

            // ✅ 음성 인식 설정 구성
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setSampleRateHertz(16000)
                    .setLanguageCode("en-US")
                    .build();

            StreamingRecognitionConfig streamingConfig = StreamingRecognitionConfig.newBuilder()
                    .setConfig(config)
                    .setInterimResults(true)
                    .build();

            StreamingRecognizeRequest request = StreamingRecognizeRequest.newBuilder()
                    .setStreamingConfig(streamingConfig)
                    .build();

            // ✅ 요청을 전송
            requestObserver.onNext(request);
            Log.d(TAG, "Streaming recognition request sent.");

            isListening = true;

            // ✅ 음성 입력을 스트리밍으로 변환하는 스레드 실행
            executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(this::streamAudio, 0, 100, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            Log.e(TAG, "Error starting speech recognition", e);
        }
    }

    private void streamAudio() {
        if (!isListening) return; // ✅ 음성 인식이 중단된 경우 실행하지 않음

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Microphone permission is not granted!");
            return;
        }

        if (requestObserver == null) {
            Log.e(TAG, "Speech recognition request observer is null!");
            return;
        }

        try {
            AudioRecord audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    16000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            );

            audioRecord.startRecording();
            Log.d(TAG, "Audio recording started.");

            byte[] buffer = new byte[4096];

            while (isListening) {
                int bytesRead = audioRecord.read(buffer, 0, buffer.length);
                if (bytesRead > 0 && requestObserver != null) {
                    try {
                        StreamingRecognizeRequest request = StreamingRecognizeRequest.newBuilder()
                                .setAudioContent(ByteString.copyFrom(buffer, 0, bytesRead))
                                .build();
                        requestObserver.onNext(request);
                    } catch (Exception e) {
                        Log.e(TAG, "Error sending audio data", e);
                        break; // ✅ 예외 발생 시 루프 종료
                    }
                }
            }

            audioRecord.stop();
            audioRecord.release();
            Log.d(TAG, "Audio recording stopped.");

        } catch (Exception e) {
            Log.e(TAG, "Error in streamAudio", e);
        }
    }

    private void stopSpeechRecognition() {
        Log.d(TAG, "Stopping speech recognition...");

        isListening = false; // ✅ 음성 인식 중지 상태로 변경

        if (requestObserver != null) {
            try {
                requestObserver.onCompleted();
                Log.d(TAG, "Request observer completed.");
            } catch (Exception e) {
                Log.e(TAG, "Error completing request observer", e);
            }
            requestObserver = null; // ✅ 요청 옵저버를 null로 설정하여 이후 데이터 전송 방지
        }
        if (speechClient != null) {
            try {
                speechClient.close();
                speechClient = null;
                Log.d(TAG, "Speech client closed.");
            } catch (Exception e) {
                Log.e(TAG, "Error closing speech client", e);
            }
        }
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
            Log.d(TAG, "Executor service shut down.");
        }
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
            Intent intent = new Intent(SpaExaminationHall.this, SpaExaminationResult.class);
            intent.putExtra("test_num", testNum);
            intent.putExtra("answer_1", answer_1);
            intent.putExtra("answer_2", answer_2);
            intent.putExtra("answer_3", answer_3);
            intent.putExtra("answer_4", answer_4);
            // ✅ 문제별 녹음 파일 경로를 Intent에 추가
            if (recordedAudioFilePath_1 != null) intent.putExtra("audio_file_1", recordedAudioFilePath_1);
            if (recordedAudioFilePath_2 != null) intent.putExtra("audio_file_2", recordedAudioFilePath_2);
            if (recordedAudioFilePath_3 != null) intent.putExtra("audio_file_3", recordedAudioFilePath_3);
            if (recordedAudioFilePath_4 != null) intent.putExtra("audio_file_4", recordedAudioFilePath_4);

            startActivity(intent);
            finish();
            //new android.os.Handler().postDelayed(() -> finish(), 500);
            return;
        }

        // 음성 인식 종료 및 텍스트 저장
        stopSpeechRecognition(); // ✅ 음성 인식 종료
        saveUserAnswer(); // ✅ 저장
        runOnUiThread(() -> userAnswer.setText("")); // ✅ userAnswer 초기화

        stopRecording();

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

    // 사용자의 음성 인식 결과 저장
    private void saveUserAnswer() {
        String answer = lastRecognizedText.trim(); // 🔹 최종 인식된 문장을 저장
        Log.d(TAG, "Saving user answer: " + answer);

        if (!answer.isEmpty()) {
            switch (currentIndex) {
                case 1:
                    answer_1 = answer;
                    break;
                case 2:
                    answer_2 = answer;
                    break;
                case 3:
                    answer_3 = answer;
                    break;
                case 4:
                    answer_4 = answer;
                    break;
                default:
                    Log.e(TAG, "Invalid currentIndex: " + currentIndex);
                    return;
            }

            // ✅ 저장된 내용을 로그로 출력
            Log.d(TAG, "Answer saved -> answer_1: " + answer_1
                    + ", answer_2: " + answer_2
                    + ", answer_3: " + answer_3
                    + ", answer_4: " + answer_4);
        }

        lastRecognizedText = "";
    }

    private void updateQuestionTextImage() {
        int questionResId = getResources().getIdentifier("spa_test_question_" + currentIndex, "string", getPackageName());
        if (questionResId != 0) {
            questionNumber.setText(questionResId);
        }
        if(currentIndex == 4) {

        }
    }

    private void updateUILayout() {
        ConstraintLayout.LayoutParams userAnswerParams =
                (ConstraintLayout.LayoutParams) userAnswer.getLayoutParams();
        ConstraintLayout.LayoutParams imageParams =
                (ConstraintLayout.LayoutParams) viewImage.getLayoutParams();

        if (currentIndex == 4) {
            // 🔹 daily_spa_examination_hall_user_answer 높이 50dp 설정
            userAnswerParams.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());

            // 🔹 기존 bottom constraint 해제
            userAnswerParams.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;

            // 🔹 viewImage를 daily_spa_examination_hall_user_answer 아래로 배치
            imageParams.topToBottom = userAnswer.getId();
        } else {
            // 🔹 기존 상태로 복구
            userAnswerParams.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
            userAnswerParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            imageParams.topToBottom = ConstraintLayout.LayoutParams.UNSET;
        }

        // 🔹 변경 사항 적용
        userAnswer.setLayoutParams(userAnswerParams);
        viewImage.setLayoutParams(imageParams);
    }

    private void startRecording() {
        if (isRecording) {
            Log.w(TAG, "Recording is already in progress. Skipping duplicate call.");
            return;
        }

        // ✅ 마이크 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Microphone permission not granted. Cannot start recording.");
            return;
        }

        int bufferSize = AudioRecord.getMinBufferSize(
                16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        );

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                16000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
        );

        byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[bufferSize];

        isRecording = true;
        audioRecord.startRecording();

        // 백그라운드 스레드에서 녹음 데이터를 저장
        new Thread(() -> {
            while (isRecording) {
                int bytesRead = audioRecord.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
            }
        }).start();

        Log.d(TAG, "Recording started for question " + currentIndex);
    }

    private void stopRecording() {
        if (audioRecord == null || !isRecording) {
            Log.w(TAG, "stopRecording() called but recording is not active.");
            return;
        }

        isRecording = false;
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;

        if (byteArrayOutputStream != null) {
            byte[] audioData = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream = null; // 🔹 메모리 해제

            // ✅ 음성 데이터를 파일로 저장하고 경로를 저장
            String audioFilePath = saveAudioToFile(audioData, currentIndex);
            if (audioFilePath != null) {
                switch (currentIndex) {
                    case 1:
                        recordedAudioFilePath_1 = audioFilePath;
                        break;
                    case 2:
                        recordedAudioFilePath_2 = audioFilePath;
                        break;
                    case 3:
                        recordedAudioFilePath_3 = audioFilePath;
                        break;
                    case 4:
                        recordedAudioFilePath_4 = audioFilePath;
                        break;
                }
            }

            Log.d(TAG, "Recording stopped for question " + currentIndex + ", Saved at: " + audioFilePath);
        }
    }

    // 녹음 데이터 저장 메서드
    private String saveAudioToFile(byte[] audioData, int questionIndex) {
        File audioFile = new File(getFilesDir(), "audio_question_" + questionIndex + ".pcm");
        try (FileOutputStream fos = new FileOutputStream(audioFile)) {
            fos.write(audioData);
            Log.d(TAG, "Audio file saved: " + audioFile.getAbsolutePath());
            return audioFile.getAbsolutePath(); // 파일 경로 반환
        } catch (IOException e) {
            Log.e(TAG, "Failed to save audio file", e);
            return null;
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

        stopSpeechRecognition();
        Log.d(TAG, "onPause: Stopping all media and speech recognition.");

        // 액티비티 종료
        finish();
        //new android.os.Handler().postDelayed(() -> finish(), 500);
    }

}
