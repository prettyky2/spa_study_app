package com.example.myapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.ClipboardManager;
import android.content.Context;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.auth.oauth2.GoogleCredentials;

import android.content.Context;
import android.widget.Toast;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

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
    private Context context;
    String activeSentence = null;
    String activeTranslate = null;

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

        context = this;

        initializeClass();
        swipeListener();

    } //onCreate();


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

        exampleSentence.setTextIsSelectable(true);
        enableTextSelection();

    }

    private void updateSentenceView() {
        Log.d(TAG, "updateSentenceView() 실행됨, 현재 문장 index: " + currentSentenceIndex);
        if (sentences != null && currentSentenceIndex >= 0 && currentSentenceIndex < sentences.size()) {
            String[] sentenceParts = sentences.get(currentSentenceIndex).split(" \\| ");
            if (sentenceParts.length == 2) {
                activeTranslate = sentenceParts[0];
                activeSentence = sentenceParts[1];
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

    private void swipeListener() {
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

    private void fetchTranslatedWord(String word) {
        new Thread(() -> {
            try {
                // 🔹 기존 TTS에서 사용한 서비스 계정 JSON 키 로드
                InputStream credentialsStream = context.getResources().openRawResource(R.raw.spastudyproject_key);
                GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                        .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));

                HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(
                        request -> request.setParser(new JsonObjectParser(new GsonFactory()))
                );

                GenericUrl url = new GenericUrl("https://translation.googleapis.com/language/translate/v2");
                url.put("q", word);
                url.put("source", "en");
                url.put("target", "ko");
                url.put("format", "text");

                HttpRequest request = requestFactory.buildPostRequest(url, null);
                request.getHeaders().setAuthorization("Bearer " + credentials.refreshAccessToken().getTokenValue());

                HttpResponse response = request.execute();
                String responseBody = response.parseAsString();

                JSONObject jsonResponse = new JSONObject(responseBody);
                String translatedText = jsonResponse.getJSONObject("data")
                        .getJSONArray("translations")
                        .getJSONObject(0)
                        .getString("translatedText");

                runOnUiThread(() -> showWordDefinition(word, translatedText));

            } catch (Exception e) {
                Log.e(TAG, "번역 오류: " + e.getMessage(), e);
                runOnUiThread(() -> showWordDefinition(word, "번역 오류 발생"));
            }
        }).start();
    }

    // Retrofit 인터페이스
    public interface TranslationApi {
        @GET("language/translate/v2")
        Call<TranslationResponse> getTranslation(
                @Query("key") String apiKey,
                @Query("q") String text,
                @Query("source") String sourceLang,
                @Query("target") String targetLang,
                @Query("format") String format
        );
    }

    // 번역 응답 데이터 클래스
    public class TranslationResponse {
        private Data data;

        public String getTranslatedText() {
            if (data != null && data.translations != null && !data.translations.isEmpty()) {
                return data.translations.get(0).translatedText;
            }
            return "번역 결과 없음.";
        }

        class Data {
            List<Translation> translations;
        }

        class Translation {
            String translatedText;
        }
    }

    private void showWordDefinition(String word, String definition) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_app_translate_word_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true); // 다이얼로그 바깥을 터치하면 닫히도록 설정

        // 다이얼로그 내부 뷰 찾기
        TextView englishWordTextView = dialog.findViewById(R.id.dialog_english_word);
        TextView koreanWordTextView = dialog.findViewById(R.id.dialog_korean_word);
        Button btnSave = dialog.findViewById(R.id.btn_study); // Save 버튼
        Button btnOk = dialog.findViewById(R.id.btn_test);

        // 단어 데이터 설정
        englishWordTextView.setText(word);
        koreanWordTextView.setText(definition);

        btnSave.setOnClickListener(v -> {
            saveWordToExcel(studyTopic, definition, word, activeTranslate, activeSentence);
            dialog.dismiss();
        });

        // "OK" 버튼 클릭 시 다이얼로그 닫기
        btnOk.setOnClickListener(v -> dialog.dismiss());

        // 다이얼로그 표시
        dialog.show();
    }

    private void saveWordToExcel(String studyTopic, String koreanWord, String englishWord, String translate, String sentence) {
        File file = new File(getFilesDir(), "user_word.xlsx"); // 내부 저장소 엑셀 파일 경로

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트 가져오기
            int lastRowNum = sheet.getLastRowNum();
            Row newRow = sheet.createRow(lastRowNum + 1); // 새로운 행 추가

            // 문장을 | 기준으로 분리
            String[] sentenceParts = sentence.split(" \\| ");
            String part1 = (sentenceParts.length > 0) ? sentenceParts[0] : "";
            String part2 = (sentenceParts.length > 1) ? sentenceParts[1] : "";

            // 각 셀에 값 입력
            newRow.createCell(0).setCellValue(studyTopic);  // 1열
            newRow.createCell(1).setCellValue(koreanWord);  // 2열
            newRow.createCell(2).setCellValue(englishWord); // 3열
            newRow.createCell(3).setCellValue(translate);   // 4열
            newRow.createCell(4).setCellValue(sentence);    // 5열

            // 파일 저장
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            Log.d(TAG, "단어 저장 완료: " + studyTopic + " | " + koreanWord + " | " + englishWord + " | " + translate + " | " + sentence);
            Toast.makeText(this, "단어가 저장되었습니다!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e(TAG, "엑셀 파일 저장 오류", e);
            Toast.makeText(this, "저장 중 오류 발생", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableTextSelection() {
        exampleSentence.setTextIsSelectable(true);
        exampleSentence.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                menu.clear(); // 복사/공유 팝업 메뉴 삭제

                // "번역" 버튼 추가 (실제 동작은 팝업 닫기)
                menu.add(0, 1, 0, "번역").setOnMenuItemClickListener(item -> {
                    mode.finish(); // 팝업 닫기
                    return true;
                });

                return true;  // 기본 컨텍스트 메뉴 활성화
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                int startSelection = exampleSentence.getSelectionStart();
                int endSelection = exampleSentence.getSelectionEnd();

                Log.d(TAG, "startSelection (onDestroyActionMode): " + startSelection + ", endSelection: " + endSelection);

                if (startSelection >= 0 && endSelection > startSelection) {
                    String selectedText = exampleSentence.getText().subSequence(startSelection, endSelection).toString().trim();
                    Log.d(TAG, "Selected text (onDestroyActionMode): " + selectedText);
                    fetchTranslatedWord(selectedText); // 기존 fetchWordDefinition 대신 번역 함수 호출
                } else {
                    Log.e(TAG, "No word selected (onDestroyActionMode).");
                }
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onNextPressed() {
        Log.d(TAG, "onNextPressed() 실행됨, currentSentenceIndex: " + currentSentenceIndex);
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
        Log.d(TAG, "onPreviousPressed() 실행됨, currentSentenceIndex: " + currentSentenceIndex);
        if (sentences != null && currentSentenceIndex > 0) {
            currentSentenceIndex--;
            updateSentenceView();
            if (practice_mode == 1) {
                exampleSentence.setVisibility(View.GONE);
            }
        }
    }

}