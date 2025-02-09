package com.example.myapp;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import com.google.cloud.texttospeech.v1.*;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;


public class DailyWordPractice extends AppApplication implements View.OnClickListener {

    private static final String TAG = "DailyWordStudyTest";
    String study_topic = null;
    int mode = 0;
    int totalRows = 0;
    int matchedRows = 0;

    TextView titleTextView = null;
    TextView wordKorean = null;
    TextView wordEnglish  = null;
    TextView emptyWordEnglish  = null;
    TextView exampleInterpretation  = null;
    TextView exampleSentence  = null;
    TextView emptyExampleSentence  = null;

    int chunkIndex = 0;
    int buttonNumber = 0;
    private List<AppExcelRow> selectedChunk;
    private int currentRowIndex = 0;
    private GestureDetector gestureDetector;

    String jsonData = null;
    int wordsPerDay = AppConstants.WORDS_PER_DAY;
    private ProgressBar progressBar; // ProgressBar 추가
//    ImageView onClickImage = null;
    private TextToSpeechClient textToSpeechClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_daily_word_practice);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.daily_word_study_test_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        showCustomDialog();
        initializeClass();

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

    private void initializeClass() {
        //initialize object

        //get intent extra data
        study_topic = getIntent().getStringExtra("study_topic");

        //initialize title
        titleTextView = findViewById(R.id.daily_word_study_test_title);
        titleTextView.setText(R.string.main_page_sub_title_daily_word);
        //String modeText = (mode == 0) ? "Study" : "Test";   // mode에 따라 Study 또는 Test 설정
        titleTextView.setOnClickListener(this);

        // initialize TextViews
        wordKorean = findViewById(R.id.word_korean);
        wordEnglish = findViewById(R.id.word_english);
        emptyWordEnglish = findViewById(R.id.word_english_empty_box);
        exampleInterpretation = findViewById(R.id.example_interpretation);
        exampleSentence = findViewById(R.id.example_sentence);
        exampleSentence.setOnClickListener(this);
        emptyExampleSentence = findViewById(R.id.example_sentence_empty_box);
        progressBar = findViewById(R.id.progress_bar);

        if (study_topic != null) {
            Log.d(TAG, "Received study_topic: " + study_topic);
            loadExcelData();  // 엑셀 데이터 로드 추가
        } else {
            Log.e(TAG, "study_topic is null!");
        }

        if (mode == 1) { // Test 모드
            emptyWordEnglish.setVisibility(View.VISIBLE);
            emptyExampleSentence.setVisibility(View.VISIBLE);
            wordEnglish.setVisibility(View.GONE);
            exampleSentence.setVisibility(View.GONE);
        }

        // 첫 번째 단어 데이터 표시
        updateWordView(0);

        // ProgressBar 초기값 설정
        updateProgressBar(0);

    } //initializeClass()

    private void showCustomDialog() {
        // 다이얼로그 생성
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_app_dialog);

        dialog.setCancelable(true);
        dialog.setOnCancelListener(dialogInterface -> dialog.dismiss());

        // 다이얼로그 뷰 초기화
        Button btnWord = dialog.findViewById(R.id.btn_word);
        Button btnStudy = dialog.findViewById(R.id.btn_study);
        Button btnExam = dialog.findViewById(R.id.btn_test);

        btnWord.setText(R.string.dialog_daily_word_study);
        btnStudy.setVisibility(View.GONE);

        // 버튼 이벤트 설정
        btnWord.setOnClickListener(v -> {
            mode = 0;
            dialog.dismiss();
        });

        btnExam.setOnClickListener(v -> {
            mode = 1;
            emptyWordEnglish.setVisibility(View.VISIBLE);
            emptyExampleSentence.setVisibility(View.VISIBLE);
            wordEnglish.setVisibility(View.GONE);
            exampleSentence.setVisibility(View.GONE);
            dialog.dismiss();
        });

        // 다이얼로그 표시
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.daily_word_study_test_title) {
            wordEnglish.setVisibility(View.VISIBLE);
            emptyWordEnglish.setVisibility(View.GONE);
            exampleSentence.setVisibility(View.VISIBLE);
            emptyExampleSentence.setVisibility(View.GONE);
            Log.e(TAG,"onclick");
        } else if (v.getId() == R.id.example_sentence) {
            // 클릭된 문장 읽기
            String textToRead = exampleSentence.getText().toString();
            if (textToRead != null && !textToRead.isEmpty()) {
                AppTTSPlayer.getInstance(this).speak(textToRead);
            } else {
                Log.e(TAG, "exampleSentence is empty or null");
            }
        }
    } //onClick();



    private void updateProgressBar(int currentIndex) {
        if (progressBar != null && selectedChunk != null) {
            int totalWords = selectedChunk.size(); // 불러온 단어 개수
            if (totalWords == 0) {
                progressBar.setProgress(0);
                Log.d(TAG, "ProgressBar updated: 0% (No words available)");
                return;
            }

            int progress = (int) (((float) (currentIndex + 1) / totalWords) * 100);
            progressBar.setProgress(progress); // 진행도 업데이트
            Log.d(TAG, "ProgressBar updated: " + progress + "% (Current Index: " + currentIndex + " / Total: " + totalWords + ")");
        }
    } //updateProgressBar(int currentIndex)

    private void updateWordView(int rowIndex) {
        if (selectedChunk == null || selectedChunk.isEmpty()) {
            Log.e(TAG, "Error: selectedChunk is null or empty");
            return;
        }

        if (rowIndex < 0 || rowIndex >= selectedChunk.size()) {
            Log.e(TAG, "Invalid row index: " + rowIndex);
            return;
        }

        AppExcelRow row = selectedChunk.get(rowIndex);

        wordKorean.setText(row.getColumn1());
        wordEnglish.setText(row.getColumn2());
        exampleInterpretation.setText(row.getColumn3());
        exampleSentence.setText(row.getColumn4());

        // Test 모드일 경우 단어 숨김
        if (mode == 1) {
            emptyWordEnglish.setVisibility(View.VISIBLE);
            emptyExampleSentence.setVisibility(View.VISIBLE);
            wordEnglish.setVisibility(View.GONE);
            exampleSentence.setVisibility(View.GONE);
        }
    } //updateWordView(int rowIndex)

    private void loadExcelData() {
        totalRows = 0;  // 전체 행 개수
        matchedRows = 0; // study_topic과 일치하는 행 개수
        selectedChunk = new ArrayList<>(); // 데이터를 저장할 리스트 초기화

        try {
            // user_word.xlsx 파일을 앱 내부 저장소에서 로드
            File file = new File(getFilesDir(), "user_word.xlsx");
            if (!file.exists()) {
                Log.e(TAG, "Excel file not found: " + file.getAbsolutePath());
                return;
            }

            InputStream inputStream = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트

            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                totalRows++; // 전체 행 개수 증가

                Cell firstCell = row.getCell(0); // 1열 (인덱스 0)
                if (firstCell != null && firstCell.getCellType() == CellType.STRING) {
                    String cellValue = firstCell.getStringCellValue();

                    if (cellValue.equalsIgnoreCase(study_topic)) {
                        matchedRows++; // study_topic과 매칭된 행 개수 증가

                        // 데이터를 AppExcelRow 객체로 변환 후 리스트에 추가
                        AppExcelRow newRow = new AppExcelRow(
                                getCellValue(row.getCell(1)), // Korean
                                getCellValue(row.getCell(2)), // English
                                getCellValue(row.getCell(3)), // Example Interpretation
                                getCellValue(row.getCell(4))  // Example Sentence
                        );
                        selectedChunk.add(newRow);
                    }
                }
            }

            // 총 행 개수 및 매칭된 행 개수 로그 출력
            Log.d(TAG, "Total rows in sheet: " + totalRows);
            Log.d(TAG, "Total matched rows for study_topic '" + study_topic + "': " + matchedRows);

            workbook.close();
            inputStream.close();

            // 첫 번째 데이터 표시 (데이터가 있을 경우)
            if (!selectedChunk.isEmpty()) {
                updateWordView(0);
                updateProgressBar(0);
            } else {
                Log.e(TAG, "No matching rows found for study_topic: " + study_topic);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error reading Excel file", e);
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

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

    @Override
    protected void onNextPressed() {
        if (currentRowIndex < selectedChunk.size() - 1) {
            currentRowIndex++;
            updateWordView(currentRowIndex);
            updateProgressBar(currentRowIndex);
        }
    }

    @Override
    protected void onPreviousPressed() {
        if (currentRowIndex > 0) {
            currentRowIndex--;
            updateWordView(currentRowIndex);
            updateProgressBar(currentRowIndex);
        }
    }



} //DailyWordStudyTest
