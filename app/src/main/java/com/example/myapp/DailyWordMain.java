package com.example.myapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson; // Gson 라이브러리 import


public class DailyWordMain extends AppApplication implements View.OnClickListener {

    private static final String TAG = "DailyWordPageActivity";
    ProgressBar progressBar;
    private List<List<Row>> chunkedRows; // 20개씩 나눠진 행 데이터 저장
    private List<Row> allRows = new ArrayList<>(); // 선언과 동시에 초기화; // 필드 변수로 선언
    int wordsPerDay = AppConstants.WORDS_PER_DAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_daily_word_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.daily_word_page_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initializeClass();
        initializeExcelFile(); //initialize excel file
        loadExcelFile(); //load excel file
        createButtons();

    } //onCreate();


    @Override
    public void onClick(View v) {

    } //onClick();

    private void initializeClass() {
        //initialize object
        progressBar = findViewById(R.id.loading_progressBar);
        progressBar.setVisibility(View.GONE);
    } //initializeClass()

    private void initializeExcelFile() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            // 앱 전용 디렉토리 경로
            File file = new File(getExternalFilesDir(null), "daily_word.xlsx");

            // 기존 파일 삭제
            if (file.exists()) {
                Log.d(TAG, "Existing file found. Deleting...");
                boolean deleted = file.delete();
                if (deleted) {
                    Log.d(TAG, "Existing file deleted successfully.");
                } else {
                    Log.e(TAG, "Failed to delete existing file.");
                }
            }

            InputStream inputStream = getAssets().open("daily_word.xlsx"); // assets에서 파일 읽기
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();

            Log.d(TAG, "New file copied successfully. File size: " + file.length());
            Toast.makeText(this, "Excel File Initialized Successfully", Toast.LENGTH_SHORT).show();

            progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Excel File Initialize Fail", Toast.LENGTH_SHORT).show();
        }
    } //initializeExcelFile()

    private void loadExcelFile() {
        progressBar.setVisibility(View.VISIBLE);
        try {
            File file = new File(getExternalFilesDir(null), "daily_word.xlsx");
            if (!file.exists()) {
                //Log.e(TAG, "File not found: " + file.getAbsolutePath());
                Toast.makeText(this, "daily_word is not exist", Toast.LENGTH_SHORT).show();
                return;
            }
            //Log.d(TAG, "daily_word exists with size: " + file.length());
            InputStream inputStream = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                allRows.add(row);
            }
            chunkedRows = chunkRows(allRows, wordsPerDay); // n개씩 묶기 n=상수값
            workbook.close();
            inputStream.close();
            Log.d(TAG, "총 행 수: " + allRows.size());
            Log.d(TAG, "총 묶음 수: " + chunkedRows.size());

            Toast.makeText(this, "daily_word load and grouping complete", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e(TAG, "Error while loading Excel file", e);
            Toast.makeText(this, "daily_word load fail : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    } //loadExcelFile()

    private List<List<Row>> chunkRows(List<Row> rows, int chunkSize) {
        List<List<Row>> chunks = new ArrayList<>();
        for (int i = 0; i < rows.size(); i += chunkSize) {
            chunks.add(rows.subList(i, Math.min(i + chunkSize, rows.size())));
        }
        return chunks;
    } //chunkRows

    private void createButtons() {
        GridLayout gridLayout = findViewById(R.id.button_grid);

        if (chunkedRows != null && !chunkedRows.isEmpty()) {
            for (int i = 0; i < chunkedRows.size(); i++) {
                Button button = new Button(this);
                button.setText(String.valueOf(i + 1)); // 1부터 시작
                button.setId(View.generateViewId());

                // 버튼 클릭 이벤트
                int chunkIndex = i; // final로 사용해야 하므로 복사
                button.setOnClickListener(v -> {
                    showCustomDialog(chunkIndex); // 선택된 묶음 처리
                });

                // 버튼 크기 및 레이아웃 설정
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = GridLayout.LayoutParams.WRAP_CONTENT; //GridLayout.LayoutParams.WRAP_CONTENT
                params.height = 200; //GridLayout.LayoutParams.WRAP_CONTENT
                params.setMargins(10, 10, 10, 10);
                button.setLayoutParams(params);

                button.setTextSize(50); // 텍스트 크기
                button.setPadding(8, 8, 8, 8); // 버튼 내부 여백
                button.setBackgroundResource(R.drawable.ripple_effect);

                // GridLayout에 버튼 추가
                gridLayout.addView(button);
            }
        }
    } //createButtons()

    private void displayChunk(int chunkIndex) {
        List<Row> selectedChunk = chunkedRows.get(chunkIndex);
        for (Row row : selectedChunk) {
            Cell wordCell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell meaningCell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

            String word = wordCell.toString();
            String meaning = meaningCell.toString();

            Log.d("ChunkReader", "Word: " + word + ", Meaning: " + meaning);
        }
    }

    private void showCustomDialog(int chunkIndex) {
        // 다이얼로그 생성
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_app_dialog);

        // 다이얼로그 외부 터치로 닫히지 않게 설정 (선택 사항)
        dialog.setCancelable(true);
        dialog.setOnCancelListener(dialogInterface -> dialog.dismiss());

        // 다이얼로그 뷰 초기화
        Button btnStudy = dialog.findViewById(R.id.btn_study);
        Button btnExam = dialog.findViewById(R.id.btn_test);

        // 버튼 이벤트 설정
        btnStudy.setOnClickListener(v -> {
            navigateToNextActivity(chunkIndex, 0); // 학습 모드로 이동
            dialog.dismiss();
        });

        btnExam.setOnClickListener(v -> {
            navigateToNextActivity(chunkIndex, 1); // 시험 모드로 이동
            dialog.dismiss();
        });

        // 다이얼로그 표시
        dialog.show();
    }

    private void navigateToNextActivity(int chunkIndex, int mode) {
        Intent intent = new Intent(this, DailyWordPractice.class); // NextActivity는 대상 액티비티

        // 선택된 chunk 데이터 가져오기
        List<Row> selectedChunk = chunkedRows.get(chunkIndex);

        // Row 데이터를 ExcelRow DTO로 변환
        List<AppExcelRow> dtoList = new ArrayList<>();
        for (Row row : selectedChunk) {
            String column1 = getCellData(row, 0);
            String column2 = getCellData(row, 1);
            String column3 = getCellData(row, 2);
            String column4 = getCellData(row, 3);
            dtoList.add(new AppExcelRow(column1, column2, column3, column4));
        }

        // Row 데이터를 JSON 문자열로 변환
        Gson gson = new Gson();
        String jsonData = gson.toJson(dtoList);

        intent.putExtra("chunkIndex", chunkIndex); // 선택한 묶음의 인덱스
        intent.putExtra("mode", mode); // 0: 학습, 1: 시험
        intent.putExtra("buttonNumber", chunkIndex + 1); // 버튼 번호 (1부터 시작)
        intent.putExtra("selectedChunk", jsonData); // JSON 문자열 추가
        startActivity(intent);
    }

    private String getCellData(Row row, int cellIndex) {
        if (row == null) {
            return ""; // Row가 null인 경우 빈 문자열 반환
        }
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK); // 셀 가져오기
        return cell.toString(); // 셀 값을 문자열로 반환
    } //getCellData(Row row, int cellIndex)

}
