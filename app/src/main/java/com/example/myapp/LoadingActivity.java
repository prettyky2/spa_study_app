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
    private static final int REQUEST_MICROPHONE_PERMISSION = 1002;


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
        requestMicrophonePermission(); // 🔹 마이크 권한 요청 추가
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
     * 🔹 마이크 권한 요청 함수 (추가됨)
     */
    private void requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_MICROPHONE_PERMISSION);
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
        } else if (requestCode == REQUEST_MICROPHONE_PERMISSION) { // 🔹 마이크 권한 처리 추가
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Microphone permission granted!");
            } else {
                Log.e(TAG, "Microphone permission denied!");
                Toast.makeText(this, "음성 인식을 사용하려면 마이크 권한을 허용해야 합니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

}