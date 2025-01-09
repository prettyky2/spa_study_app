package com.example.myapp;

import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.TextView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.poi.ss.usermodel.Row;
import java.lang.reflect.Type;

import com.example.myapp.ExcelRow;

public class DailyWordStudyTest extends AppCompatActivity implements View.OnClickListener {

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

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (selectedChunk == null || selectedChunk.isEmpty()) {
                    Log.e(TAG, "selectedChunk is null or empty. Cannot handle swipe.");
                    return false; // 스와이프 처리 중단
                }

                if (e1.getX() - e2.getX() > 100) {
                    // 왼쪽으로 스와이프 (다음 단어)
                    if (currentRowIndex < selectedChunk.size() - 1) {
                        currentRowIndex++;
                        updateWordView(currentRowIndex);
                    }
                } else if (e2.getX() - e1.getX() > 100) {
                    // 오른쪽으로 스와이프 (이전 단어)
                    if (currentRowIndex > 0) {
                        currentRowIndex--;
                        updateWordView(currentRowIndex);
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });


    } //onCreate()

    @Override
    public void onClick(View v) {

    } //onClick();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    } //onTouchEvent()

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

        // 첫 번째 단어 데이터 표시
        updateWordView(0);


    } //initializeClass()

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
    } //updateWordView(int rowIndex)

    private String getCellData(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        return cell.toString();
    } //getCellData(Row row, int cellIndex)
}
