package com.example.myapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.AlertDialog;


public class AppSettingMain extends AppApplication implements View.OnClickListener {

    private static final String TAG = "DailyStudyPractice";
    private double tts_speed = 1.0;
    TextView tts_speed_view = null;
    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_TTS_SPEED = "tts_speed";


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_app_setting_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.setting_main_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        }); //setContentView

        initializeClass();



    } //onCreate();

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_tts_speed_down) {
            if (tts_speed > 0.5) {
                tts_speed = Math.max(0.5, Math.round((tts_speed - 0.1) * 10) / 10.0);
            }
        } else if (v.getId() == R.id.btn_tts_speed_up) {
            if (tts_speed < 1.5) {
                tts_speed = Math.min(1.5, Math.round((tts_speed + 0.1) * 10) / 10.0);
            }
        } else if (v.getId() == R.id.btn_backup) {
            backupExcelFile();
        } else if (v.getId() == R.id.btn_reload) {
            restoreExcelFile();
        } else if (v.getId() == R.id.btn_delete) {
            showCustomDialog();
        }

        // UI 업데이트
        tts_speed_view.setText(String.format("%.1f", tts_speed));

        // 변경된 속도 저장
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(KEY_TTS_SPEED, (float) tts_speed);
        editor.apply(); // 비동기 저장

        // 변경된 속도를 TTSPlayer에 적용
        AppTTSPlayer.getInstance(this).setTTSAudioSpeed(tts_speed);
    } //onClick();

    private void showCustomDialog() {
        // 다이얼로그 생성
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_app_dialog);

        // 다이얼로그 외부 터치로 닫히지 않게 설정 (선택 사항)
        dialog.setCancelable(true);
        dialog.setOnCancelListener(dialogInterface -> dialog.dismiss());

        // 다이얼로그 뷰 초기화
        TextView warning_title = dialog.findViewById(R.id.dialog_title);
        Button btnWord = dialog.findViewById(R.id.btn_word);
        Button btnStudy = dialog.findViewById(R.id.btn_study);
        Button btnExam = dialog.findViewById(R.id.btn_test);
        warning_title.setText(R.string.setting_user_word_delete_warning);
        btnWord.setVisibility(View.GONE);
        btnStudy.setVisibility(View.GONE);
        btnExam.setText(R.string.setting_user_word_delete);

        btnExam.setOnClickListener(v -> {
            deleteUserWordFile();
            dialog.dismiss();
        });

        // 다이얼로그 표시
        dialog.show();
    }

    private void initializeClass() {

        tts_speed_view = findViewById(R.id.setting_tts_speed);
        Button tts_speed_down = findViewById(R.id.btn_tts_speed_down);
        Button tts_speed_up = findViewById(R.id.btn_tts_speed_up);
        Button backupButton = findViewById(R.id.btn_backup);
        Button reloadButton = findViewById(R.id.btn_reload);
        Button deleteButton = findViewById(R.id.btn_delete);

        tts_speed_down.setOnClickListener(this);
        tts_speed_up.setOnClickListener(this);
        backupButton.setOnClickListener(this);
        reloadButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);

        // SharedPreferences에서 저장된 속도 불러오기
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        tts_speed = prefs.getFloat(KEY_TTS_SPEED, 1.0f); // 기본값 1.0

        // UI 업데이트
        tts_speed_view.setText(String.format("%.1f", tts_speed));

        // TTSPlayer에 적용
        AppTTSPlayer.getInstance(this).setTTSAudioSpeed(tts_speed);
    }

    private void backupExcelFile() {
        File sourceFile = new File(getFilesDir(), "user_word.xlsx"); // 내부 저장소에 있는 원본 파일
        File destFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "user_word.xlsx"); // Download 폴더로 복사

        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(destFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            if (destFile.exists()) {
                showAlertDialog("백업 완료", "백업된 파일 경로:\n" + destFile.getAbsolutePath());
            } else {
                showAlertDialog("백업 실패", "파일이 정상적으로 저장되지 않았습니다.");
            }

        } catch (IOException e) {
            Log.e(TAG, "백업 오류", e);
            showAlertDialog("백업 오류", "파일을 저장하는 중 오류가 발생했습니다.");
        }
    }

    private void restoreExcelFile() {
        File sourceFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "user_word.xlsx"); // 백업된 파일
        File destFile = new File(getFilesDir(), "user_word.xlsx"); // 앱 내부 저장소로 복사

        if (!sourceFile.exists()) {
            showAlertDialog("복원 실패", "복원할 파일이 없습니다!");
            return;
        }

        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(destFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            showAlertDialogWithRestart("복원 완료", "단어장이 복구되었습니다. 앱을 다시 시작합니다.");

        } catch (IOException e) {
            Log.e(TAG, "복원 오류", e);
            showAlertDialog("복원 실패", "파일을 복원하는 중 오류가 발생했습니다.");
        }
    }

    private void deleteUserWordFile() {
        File internalFile = new File(getFilesDir(), "user_word.xlsx"); // 내부 저장소 파일
        File externalFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "user_word.xlsx"); // 다운로드 폴더의 백업 파일

        boolean internalDeleted = deleteFileSafely(internalFile);
        boolean externalDeleted = deleteFileSafely(externalFile);

        if (internalDeleted && externalDeleted) {
            showAlertDialog("삭제 완료", "내부 저장소 및 다운로드 폴더의 user_word.xlsx 파일이 삭제되었습니다.");
        } else if (internalDeleted) {
            showAlertDialog("삭제 완료", "내부 저장소의 user_word.xlsx 파일만 삭제되었습니다.");
        } else if (externalDeleted) {
            showAlertDialog("삭제 완료", "다운로드 폴더의 user_word.xlsx 파일만 삭제되었습니다.");
        } else {
            showAlertDialog("삭제 실패", "삭제할 파일이 없습니다.");
        }
    }

    // 안전하게 파일 삭제하는 함수
    private boolean deleteFileSafely(File file) {
        if (file.exists()) {
            if (file.delete()) {
                return true;
            } else {
                try {
                    file.getCanonicalFile().delete();
                    if (file.exists()) {
                        getApplicationContext().deleteFile(file.getName());
                    }
                    return !file.exists();
                } catch (IOException e) {
                    Log.e(TAG, "파일 삭제 오류: " + e.getMessage());
                }
            }
        }
        return false;
    }

    private void showAlertDialogWithRestart(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> restartApp())
                .show();
    }

    private void restartApp() {
        Intent intent = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        Runtime.getRuntime().exit(0); // 앱 완전 종료 후 재시작
    }

    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }






}
