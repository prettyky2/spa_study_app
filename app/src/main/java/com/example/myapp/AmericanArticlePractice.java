package com.example.myapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.auth.oauth2.GoogleCredentials;

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
import retrofit2.http.GET;
import retrofit2.http.Query;

public class AmericanArticlePractice extends AppApplication implements View.OnClickListener {

    private static final String TAG = "AmericanArticlePractice";

    private String[] scriptSentences; // 🔹 원문 문장 배열
    private String[] translatedSentences; // 🔹 번역 문장 배열

    private int currentSentenceIndex = 0;

    private TextView articleTitle;
    private TextView exampleInterpretation;
    private TextView exampleSentence;
    private LinearProgressIndicator progressBar;
    private GestureDetector gestureDetector;
    private int articleNumber = 0;
    TextView practiceTitle = null;
    private Context context;
    String activeSentence = null;
    String activeTranslate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_america_article_practice);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.america_article_practice_activity), (v, insets) -> {
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
        articleNumber = intent.getIntExtra("article_number", -1);
        scriptSentences = intent.getStringArrayExtra("scriptSentences");
        translatedSentences = intent.getStringArrayExtra("translatedSentences");

        if (scriptSentences == null || scriptSentences.length == 0 || translatedSentences == null || translatedSentences.length == 0) {
            Log.e(TAG, "❌ 받은 문장 데이터가 없음");
            exampleInterpretation.setText("No sentences available.");
            exampleSentence.setText("");
            return;
        }

        articleTitle = findViewById(R.id.america_article_practice_title);
        exampleInterpretation = findViewById(R.id.america_article_example_interpretation);
        exampleSentence = findViewById(R.id.america_article_example_sentence);
        progressBar = findViewById(R.id.america_article_progress_bar);

        exampleSentence.setOnClickListener(this);
        exampleInterpretation.setOnClickListener(this);

        String[] headlines = AppAmericaArticleApplication.getInstance().getHeadlines();
        articleTitle.setText(headlines[articleNumber]);

        // 🔹 첫 번째 문장 표시
        if (scriptSentences != null && translatedSentences != null && scriptSentences.length > 0) {
            currentSentenceIndex = 0;
            updateSentenceView();
        } else {
            exampleInterpretation.setText("No sentences available.");
            exampleSentence.setText("");
        }

        exampleSentence.setTextIsSelectable(true);
        enableTextSelection();

    }

    private void updateSentenceView() {
        Log.d(TAG, "updateSentenceView() 실행됨, 현재 문장 index: " + currentSentenceIndex);
        if (scriptSentences.length > currentSentenceIndex && translatedSentences.length > currentSentenceIndex) {
            exampleInterpretation.setText(translatedSentences[currentSentenceIndex]); // 한글 번역
            exampleSentence.setText(scriptSentences[currentSentenceIndex]); // 영어 원문
        }
            // ProgressBar 업데이트
            updateProgressBar();

    }

    private void updateProgressBar() {
        if (progressBar != null && scriptSentences.length > 0) {
            int progress = (int) (((float) (currentSentenceIndex + 1) / scriptSentences.length) * 100);
            progressBar.setProgress(progress);
            Log.d(TAG, "ProgressBar updated: " + progress + "%");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.america_article_example_interpretation) {
            Log.e(TAG, "TTS Touch");
            String textToRead = exampleSentence.getText().toString();
            if (!textToRead.isEmpty()) {
                AppTTSPlayer.getInstance(this).speak(textToRead);
            } else {
                Log.e(TAG, "No text to read.");
            }
        }
    }

    private void swipeListener() {
        // 스와이프 감지 리스너 추가
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (scriptSentences == null || scriptSentences.length == 0) {
                    Log.e(TAG, "No sentences available.");
                    return false;
                }

                if (e1.getX() - e2.getX() > 100) {
                    // 왼쪽으로 스와이프 (다음 문장)
                    if (currentSentenceIndex < scriptSentences.length - 1) {
                        currentSentenceIndex++;
                        updateSentenceView();
                    }
                } else if (e2.getX() - e1.getX() > 100) {
                    // 오른쪽으로 스와이프 (이전 문장)
                    if (currentSentenceIndex > 0) {
                        currentSentenceIndex--;
                        updateSentenceView();
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
        Button btnOk = dialog.findViewById(R.id.btn_test);
        Button btnSave = dialog.findViewById(R.id.btn_study);
        btnSave.setVisibility(View.GONE);

        // 단어 데이터 설정
        englishWordTextView.setText(word);
        koreanWordTextView.setText(definition);

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
                    int startSelection = exampleSentence.getSelectionStart();
                    int endSelection = exampleSentence.getSelectionEnd();

                    Log.d(TAG, "startSelection (onDestroyActionMode): " + startSelection + ", endSelection: " + endSelection);

                    if (startSelection >= 0 && endSelection > startSelection) {
                        String selectedText = exampleSentence.getText().subSequence(startSelection, endSelection).toString().trim();
                        Log.d(TAG, "Selected text (onDestroyActionMode): " + selectedText);
                        // 🔹 단어 길이 검사 (15자 이상이면 번역 거부)
                        if (selectedText.length() > 15) {
                            runOnUiThread(() ->
                                    Toast.makeText(exampleSentence.getContext(), "15자 이상 번역 불가", Toast.LENGTH_SHORT).show()
                            );
                            mode.finish(); // 액션 모드 종료
                            return true;
                        }
                        fetchTranslatedWord(selectedText); // 번역 실행


                        fetchTranslatedWord(selectedText); // 기존 fetchWordDefinition 대신 번역 함수 호출
                    } else {
                        Log.e(TAG, "No word selected (onDestroyActionMode).");
                    }
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
        if (scriptSentences != null && currentSentenceIndex < scriptSentences.length - 1) {
            currentSentenceIndex++;
            updateSentenceView();
        }
    }

    @Override
    protected void onPreviousPressed() {
        Log.d(TAG, "onPreviousPressed() 실행됨, currentSentenceIndex: " + currentSentenceIndex);
        if (scriptSentences != null && currentSentenceIndex > 0) {
            currentSentenceIndex--;
            updateSentenceView();
        }
    }

}