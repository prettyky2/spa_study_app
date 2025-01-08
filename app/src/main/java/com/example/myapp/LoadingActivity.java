package com.example.myapp;


import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.content.Intent;

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

public class LoadingActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "LoadingActivity";
    Button startButton = null;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loading_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //initialize object
        startButton = findViewById(R.id.loading_page_start_button);
        startButton.setOnClickListener(this);
        startButton.setVisibility(View.GONE);

        progressBar = findViewById(R.id.loading_progressBar);
        progressBar.setVisibility(View.VISIBLE);

        initializeExcelFile();
        loadExcelFile();


    } //onCreate();

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.loading_page_start_button) {
            Toast.makeText(this, "Start button clicked!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainPageActivity.class);
            startActivity(intent);
        }
    } //onClick();


    private void initializeExcelFile() {
        try {
            // 앱 전용 디렉토리 경로
            File file = new File(getExternalFilesDir(null), "daily_word.xlsx");

            // 파일이 이미 있으면 복사하지 않음
            if (!file.exists()) {
                InputStream inputStream = getAssets().open("daily_word.xlsx"); // assets에서 파일 읽기
                FileOutputStream outputStream = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                inputStream.close();
                outputStream.close();

                Toast.makeText(this, "엑셀 파일 초기화 완료", Toast.LENGTH_SHORT).show();
                Log.d("FileCheck", "Copied file size: " + file.length());
            } else {
                Toast.makeText(this, "엑셀 파일이 이미 존재합니다", Toast.LENGTH_SHORT).show();
                Log.d("FileCheck", "Existing file size: " + file.length());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "엑셀 파일 초기화 실패", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "엑셀 파일 초기화 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadExcelFile() {
        try {
            File file = new File(getExternalFilesDir(null), "daily_word.xlsx");
            if (!file.exists()) {
                Log.e("FileCheck", "File not found: " + file.getAbsolutePath());
                Toast.makeText(this, "파일이 존재하지 않습니다", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("FileCheck", "File exists with size: " + file.length());
            InputStream inputStream = new FileInputStream(file);

            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                Cell wordCell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                Cell meaningCell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

                String word = wordCell.toString(); // 안전하게 데이터 읽기
                String meaning = meaningCell.toString();

                Log.d("ExcelReader", "Word: " + word + ", Meaning: " + meaning);
            }

            workbook.close();
            inputStream.close();

            Toast.makeText(this, "엑셀 파일 로드 완료", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE); // 로드 완료 후 숨김
            startButton.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.e("ExcelReader", "Error while loading Excel file", e);
            Toast.makeText(this, "엑셀 파일 로드 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


}