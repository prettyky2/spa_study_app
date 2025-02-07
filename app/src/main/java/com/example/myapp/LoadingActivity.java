package com.example.myapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import java.io.IOException;
import java.io.InputStream;

public class LoadingActivity extends AppApplication implements View.OnClickListener{

    private static final String TAG = "LoadingActivity";
    Button startButton = null;
    private static final int REQUEST_BLUETOOTH_CONNECT = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loading_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        }); //setContentView

        initializeClass(); //set button, text, progress bar, onClickListener etc

        // 🔹 블루투스 권한 요청 추가 (Android 12 이상)
        requestBluetoothPermission();
        copyExcelFileToInternalStorage();

    } //onCreate();

    private void copyExcelFileToInternalStorage() {
        String fileName = "user_word.xlsx"; // 엑셀 파일 이름
        File file = new File(getFilesDir(), fileName);

        if (!file.exists()) { // 파일이 없을 때만 복사
            try (InputStream inputStream = getAssets().open(fileName);
                 FileOutputStream outputStream = new FileOutputStream(file)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                Log.d(TAG, "Excel 파일이 내부 저장소로 복사됨: " + file.getAbsolutePath());
            } catch (IOException e) {
                Log.e(TAG, "Excel 파일 복사 실패", e);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.loading_page_start_button) {
            //Toast.makeText(this, "Start button clicked!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainPageActivity.class);
            startActivity(intent);
        }
    } //onClick();

    private void initializeClass() {
        //initialize object
        startButton = findViewById(R.id.loading_page_start_button);
        startButton.setOnClickListener(this);
    } //initializeClass()

    /**
     * 🔹 블루투스 권한 요청 함수
     */
    private void requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12(API 31) 이상
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_BLUETOOTH_CONNECT);
            }
        }
    }

    /**
     * 🔹 권한 요청 결과 처리
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_CONNECT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "BLUETOOTH_CONNECT permission granted!");
            } else {
                Log.e(TAG, "BLUETOOTH_CONNECT permission denied!");
                Toast.makeText(this, "블루투스 연결을 사용하려면 권한을 허용해야 합니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void backupExcelFile() {
        File sourceFile = new File(getFilesDir(), "user_word.xlsx");
        File destFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "user_word.xlsx");

        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(destFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            Toast.makeText(this, "백업 완료! 다운로드 폴더에서 확인하세요.", Toast.LENGTH_LONG).show();
            Log.d(TAG, "백업된 파일 경로: " + destFile.getAbsolutePath());

        } catch (IOException e) {
            Log.e(TAG, "백업 오류", e);
            Toast.makeText(this, "백업 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private void restoreExcelFile() {
        File sourceFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "user_word.xlsx");
        File destFile = new File(getFilesDir(), "user_word.xlsx");

        if (!sourceFile.exists()) {
            Toast.makeText(this, "복원할 파일이 없습니다!", Toast.LENGTH_SHORT).show();
            return;
        }

        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(destFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            Toast.makeText(this, "복원 완료! 단어장이 복구되었습니다.", Toast.LENGTH_LONG).show();
            Log.d(TAG, "복원된 파일 경로: " + destFile.getAbsolutePath());

        } catch (IOException e) {
            Log.e(TAG, "복원 오류", e);
            Toast.makeText(this, "복원 실패", Toast.LENGTH_SHORT).show();
        }
    }

}