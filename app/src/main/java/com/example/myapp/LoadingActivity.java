package com.example.myapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Build;
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

    } //onCreate();

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

}