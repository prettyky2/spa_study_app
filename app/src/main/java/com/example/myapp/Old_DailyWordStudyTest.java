package com.example.myapp;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;


public class Old_DailyWordStudyTest extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DailyWordStudyTest";
    int chunkIndex = 0;
    int mode = 0;
    int buttonNumber = 0;
    private List<ExcelRow> selectedChunk;
    private int currentRowIndex = 0;
    private GestureDetector gestureDetector;
    TextView wordKorean = null;
    TextView wordEnglish  = null;
    TextView exampleInterpretation  = null;
    TextView exampleSentence  = null;
    String jsonData = null;
    int wordsPerDay = AppConstants.WORDS_PER_DAY;
    private ProgressBar progressBar; // ProgressBar 추가
    ImageView onClickImgage = null;
    private TextToSpeechClient textToSpeechClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_daily_word_study_test);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.daily_word_study_test_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeClass();
        initializeTextToSpeech();
        listAvailableVoices();

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (selectedChunk == null || selectedChunk.isEmpty()) {
                    Log.e(TAG, "selectedChunk is null or empty. Cannot handle swipe.");
                    return false;
                }

                if (e1.getX() - e2.getX() > 100) {
                    // 왼쪽으로 스와이프 (다음 단어)
                    if (currentRowIndex < selectedChunk.size() - 1) {
                        currentRowIndex++;
                        updateWordView(currentRowIndex);
                        updateProgressBar(currentRowIndex);
                    }
                } else if (e2.getX() - e1.getX() > 100) {
                    // 오른쪽으로 스와이프 (이전 단어)
                    if (currentRowIndex > 0) {
                        currentRowIndex--;
                        updateWordView(currentRowIndex);
                        updateProgressBar(currentRowIndex);
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });

        String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        Log.d(TAG, "Environment Variable Path: " + credentialsPath);

    } //onCreate()

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.on_click_image) {
            wordEnglish.setVisibility(View.VISIBLE);
            exampleSentence.setVisibility(View.VISIBLE);
            Log.e(TAG,"onclick");
        } else if (v.getId() == R.id.example_sentence) {
            // 클릭된 문장 읽기
            String textToRead = exampleSentence.getText().toString();
            if (textToRead != null && !textToRead.isEmpty()) {
                readWordAloud(textToRead);
            } else {
                Log.e(TAG, "exampleSentence is empty or null");
            }
        }
    } //onClick();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    } //onTouchEvent()

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeechClient != null) {
            textToSpeechClient.close();
            textToSpeechClient = null;
            Log.d(TAG, "TextToSpeechClient closed.");
        }
    }

    private void initializeClass() {
        //initialize object

        //get intent extra data
        chunkIndex = getIntent().getIntExtra("chunkIndex", -1);
        mode = getIntent().getIntExtra("mode", -1);
        buttonNumber = getIntent().getIntExtra("buttonNumber", -1); // 버튼 번호 추가
        jsonData = getIntent().getStringExtra("selectedChunk");

        //initialize title
        TextView titleTextView = findViewById(R.id.daily_word_study_test_title);
        String modeText = (mode == 0) ? "Study" : "Test";   // mode에 따라 Study 또는 Test 설정
        titleTextView.setText("Day" + buttonNumber + " " + modeText);

        // initialize TextViews
        wordKorean = findViewById(R.id.word_korean);
        wordEnglish = findViewById(R.id.word_english);
        exampleInterpretation = findViewById(R.id.example_interpretation);
        exampleSentence = findViewById(R.id.example_sentence);
        exampleSentence.setOnClickListener(this);
        progressBar = findViewById(R.id.progress_bar);
        onClickImgage = findViewById(R.id.on_click_image);
        onClickImgage.setOnClickListener(this);

        if (jsonData != null) {
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<List<ExcelRow>>() {}.getType();
                selectedChunk = gson.fromJson(jsonData, type); // JSON 복원
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse JSON data: " + e.getMessage());
                selectedChunk = null;
            }
        } else {
            Log.e(TAG, "No data received in Intent");
            selectedChunk = null;
        }

        // selectedChunk 유효성 검사
        if (selectedChunk == null || selectedChunk.isEmpty()) {
            Log.e(TAG, "selectedChunk is null or empty.");
            return; // 초기화 실패 처리
        }

        if(mode==0) {
            onClickImgage.setVisibility(View.GONE);
        }

        if (mode == 1) { // Test 모드
            wordEnglish.setVisibility(View.GONE);
            exampleSentence.setVisibility(View.GONE);
        }

        // 첫 번째 단어 데이터 표시
        updateWordView(0);

        // ProgressBar 초기값 설정
        updateProgressBar(0);

    } //initializeClass()

    private void updateProgressBar(int currentIndex) {
        if (progressBar != null && selectedChunk != null) {
            int progress = (int) (((float) (currentIndex + 1) / wordsPerDay) * 100);
            progressBar.setProgress(progress); // 진행도 업데이트
            Log.d(TAG, "ProgressBar updated: " + progress + "%");
        }
    }

    private void updateWordView(int rowIndex) {
        if (selectedChunk == null || rowIndex < 0 || rowIndex >= selectedChunk.size()) {
            Log.e(TAG, "Invalid row index or no data in selectedChunk");
            return;
        }

        ExcelRow row = (ExcelRow) selectedChunk.get(rowIndex);

        // TextView 찾기
        wordKorean = findViewById(R.id.word_korean);
        wordEnglish = findViewById(R.id.word_english);
        exampleInterpretation = findViewById(R.id.example_interpretation);
        exampleSentence = findViewById(R.id.example_sentence);

        // 셀 데이터 가져오기
        wordKorean.setText(row.getColumn1());
        wordEnglish.setText(row.getColumn2());
        exampleInterpretation.setText(row.getColumn3());
        exampleSentence.setText(row.getColumn4());

        // Test 모드일 경우 단어 변경 시 숨김
        if (mode == 1) { // Test 모드
            wordEnglish.setVisibility(View.GONE);
            exampleSentence.setVisibility(View.GONE);
        }

    } //updateWordView(int rowIndex)

    private String getCellData(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        return cell.toString();
    } //getCellData(Row row, int cellIndex)

    private void initializeTextToSpeech() {
        try {
            // res/raw 디렉토리에서 JSON 키 파일 읽기
            InputStream credentialsStream = getResources().openRawResource(R.raw.spastudyproject_key);
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();
            if (textToSpeechClient != null) {
                textToSpeechClient.close();
                textToSpeechClient = null;
            }
            textToSpeechClient = TextToSpeechClient.create(settings);
            Log.d(TAG, "TextToSpeechClient initialized successfully.");
        } catch (IOException e) {
            Log.e(TAG, "Failed to initialize Text-to-Speech client: " + e.getMessage());
            textToSpeechClient = null;
        }
    }

    private void readWordAloud(String text) {
//        if (textToSpeechClient == null) {
//            Log.e(TAG, "TextToSpeechClient is null. Reinitializing...");
//            initializeTextToSpeech();
//            if (textToSpeechClient == null) {
//                Log.e(TAG, "Failed to reinitialize TextToSpeechClient. Cannot synthesize speech.");
//                return;
//            }
//        }
        try {
            // 텍스트 입력
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            // 목소리 선택
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("en-US")  // 언어 코드
                    .setName("en-US-Neural2-H") // 원하는 목소리 이름
                    .setSsmlGender(SsmlVoiceGender.FEMALE) // 성별
                    .build();

            // 오디오 설정
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.LINEAR16) // PCM 인코딩
                    .setSpeakingRate(AppConstants.SENTENCE_PLAY_SPEED)  // 말하는 속도
                    .setPitch(0.0)         // 음높이
                    .build();

            // 음성 합성
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            Log.d(TAG, "Requested Voice Name: " + voice.getName());
            Log.d(TAG, "Synthesized Voice: " + response.toString());

            byte[] audioContents = response.getAudioContent().toByteArray();

            // 오디오 재생
            playAudio(audioContents);

        } catch (Exception e) {
            Log.e(TAG, "Error while synthesizing speech: " + e.getMessage());
        }
    }

    private void playAudio(byte[] audioData) {
        try {
            AudioTrack audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    24000, // 생성된 데이터와 동일한 샘플 레이트로 수정
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    audioData.length,
                    AudioTrack.MODE_STATIC
            );

            // 실제 오디오 데이터를 작성
            audioTrack.write(audioData, 0, audioData.length);
            // 오디오 재생
            audioTrack.play();

        } catch (Exception e) {
            Log.e(TAG, "Error playing audio: " + e.getMessage());
        }
    }

    private void listAvailableVoices() {
//        try {
//            // res/raw에서 JSON 키 파일 로드
//            InputStream credentialsStream = getResources().openRawResource(R.raw.spastudyproject_key);
//            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
//                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
//
//            // 인증 정보를 사용해 TTS 클라이언트 생성
//            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
//                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
//                    .setEndpoint("us-central1-texttospeech.googleapis.com:443")
//                    .build();
//            if (textToSpeechClient != null) {
//                textToSpeechClient.close();
//                textToSpeechClient = null;
//            }
//            TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings);
//
//            // 목소리 리스트 가져오기
//            ListVoicesRequest request = ListVoicesRequest.newBuilder().build();
//            ListVoicesResponse response = textToSpeechClient.listVoices(request);
//
//            // 결과 출력
//            for (Voice voice : response.getVoicesList()) {
//                Log.d(TAG, "Voice Name: " + voice.getName());
//                Log.d(TAG, "Supported Languages: " + voice.getLanguageCodesList());
//                Log.d(TAG, "Gender: " + voice.getSsmlGender());
//                Log.d(TAG, "Natural Sample Rate Hertz: " + voice.getNaturalSampleRateHertz());
//            }
//
//            textToSpeechClient.close();
//        } catch (Exception e) {
//            Log.e(TAG, "Error listing voices: " + e.getMessage());
//        }
    }


}
