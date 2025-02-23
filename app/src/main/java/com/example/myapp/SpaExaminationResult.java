package com.example.myapp;

import android.app.Dialog;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import com.arthenica.ffmpegkit.*;
import android.os.Handler;

public class SpaExaminationResult extends AppApplication implements View.OnClickListener {

    private static final String TAG = "spaExaminationResult";
    int getIntentTestNum;
    ImageView answerSheetImage_1 = null;
    String recordedAudioFilePath_1;
    String recordedAudioFilePath_2;
    String recordedAudioFilePath_3;
    String recordedAudioFilePath_4;
    String mp3FilePath_1;
    String mp3FilePath_2;
    String mp3FilePath_3;
    String mp3FilePath_4;
    String mergedMp3FilePath;

    private String combinedQA1;
    private String combinedQA2;
    private String combinedQA3;
    private String combinedQA4;
    private String combinedQAAll; // Q1+A1+Q2+A2+Q3+A3+Q4+A4 전체 저장


    File questionMp3;
    File answerMp3;
    File outputMp3;
    int totalAnswers = 0; // 답변 개수 카운트
    File internalDir;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spa_examination_result);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.spa_examination_hall_result_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initializeClass();
    } //onCreate()

    private void initializeClass() {
        internalDir = getFilesDir();

        Intent intent = getIntent();
        getIntentTestNum = intent.getIntExtra("test_num", -1);
        recordedAudioFilePath_1 = getIntent().getStringExtra("audio_file_1");
        recordedAudioFilePath_2 = getIntent().getStringExtra("audio_file_2");
        recordedAudioFilePath_3 = getIntent().getStringExtra("audio_file_3");
        recordedAudioFilePath_4 = getIntent().getStringExtra("audio_file_4");

        answerSheetImage_1 = findViewById(R.id.answer_sheet_image_1);
        Button answerSheetShareAnswer = findViewById(R.id.answer_sheet_share_answer);
        Button answerSheetRecord1 = findViewById(R.id.answer_sheet_record_1);
        Button answerSheetRecord2 = findViewById(R.id.answer_sheet_record_2);
        Button answerSheetRecord3 = findViewById(R.id.answer_sheet_record_3);
        Button answerSheetRecord4 = findViewById(R.id.answer_sheet_record_4);

        answerSheetShareAnswer.setOnClickListener(this);
        answerSheetRecord1.setOnClickListener(this);
        answerSheetRecord2.setOnClickListener(this);
        answerSheetRecord3.setOnClickListener(this);
        answerSheetRecord4.setOnClickListener(this);

        setQuestionAndAnswer(getIntentTestNum);
        convertPCMtoMP3Auto(getIntentTestNum);
    }

    private void convertPCMtoMP3Auto(int testNum) {
        if (recordedAudioFilePath_1 != null) totalAnswers++;
        if (recordedAudioFilePath_2 != null) totalAnswers++;
        if (recordedAudioFilePath_3 != null) totalAnswers++;
        if (recordedAudioFilePath_4 != null) totalAnswers++;

        AtomicInteger completedAnswers = new AtomicInteger(0); // 🔥 변환 완료된 답변 수 추적

        if (recordedAudioFilePath_1 != null) {
            mp3FilePath_1 = new File(internalDir, "answer_" + testNum + "_1.mp3").getAbsolutePath();
            Log.d(TAG, "mp3FilePath_1: " + mp3FilePath_1);
            convertPCMtoMP3(new File(recordedAudioFilePath_1), new File(mp3FilePath_1), (success, outputMp3File) -> {
                if (success && outputMp3File != null) {
                    mergeQuestionAndAnswer(testNum, 1, outputMp3File.getAbsolutePath(), completedAnswers);
                }
            });
        }
        if (recordedAudioFilePath_2 != null) {
            mp3FilePath_2 = new File(internalDir, "answer_" + testNum + "_2.mp3").getAbsolutePath();
            Log.d(TAG, "mp3FilePath_2: " + mp3FilePath_2);
            convertPCMtoMP3(new File(recordedAudioFilePath_2), new File(mp3FilePath_2), (success, outputMp3File) -> {
                if (success && outputMp3File != null) {
                    mergeQuestionAndAnswer(testNum, 2, outputMp3File.getAbsolutePath(), completedAnswers);
                }
            });
        }
        if (recordedAudioFilePath_3 != null) {
            mp3FilePath_3 = new File(internalDir, "answer_" + testNum + "_3.mp3").getAbsolutePath();
            Log.d(TAG, "mp3FilePath_3: " + mp3FilePath_3);
            convertPCMtoMP3(new File(recordedAudioFilePath_3), new File(mp3FilePath_3), (success, outputMp3File) -> {
                if (success && outputMp3File != null) {
                    mergeQuestionAndAnswer(testNum, 3, outputMp3File.getAbsolutePath(), completedAnswers);
                }
            });
        }
        if (recordedAudioFilePath_4 != null) {
            mp3FilePath_4 = new File(internalDir, "answer_" + testNum + "_4.mp3").getAbsolutePath();
            Log.d(TAG, "mp3FilePath_4: " + mp3FilePath_4);
            convertPCMtoMP3(new File(recordedAudioFilePath_4), new File(mp3FilePath_4), (success, outputMp3File) -> {
                if (success && outputMp3File != null) {
                    mergeQuestionAndAnswer(testNum, 4, outputMp3File.getAbsolutePath(), completedAnswers);
                }
            });
        }

    }

    private void convertPCMtoMP3(File pcmFile, File mp3File, ConversionCallback callback) {
        checkPCMFormat(pcmFile);

        String command = String.format(
                "-y -f s16le -ar 16000 -ac 1 -i \"%s\" -c:a libmp3lame -b:a 192k -ar 44100 -ac 1 \"%s\"",
                pcmFile.getAbsolutePath(),
                mp3File.getAbsolutePath()
        );

        FFmpegKit.executeAsync(command, session -> {
            boolean success = ReturnCode.isSuccess(session.getReturnCode());
            runOnUiThread(() -> {
                if (success) {
                    Log.d(TAG, "PCM -> MP3 변환 성공: " + mp3File.getAbsolutePath());
                    checkMP3Format(mp3File);
                    callback.onConversionComplete(true, mp3File);
                } else {
                    Log.e(TAG, "PCM -> MP3 변환 실패: " + session.getFailStackTrace());
                    callback.onConversionComplete(false, null);
                }
            });
        });

    }

    private File copyRawResourceToFile(int rawResId, String outputFileName) {
        File outputFile = new File(getFilesDir(), outputFileName);

        // 파일이 이미 존재하면 다시 복사하지 않음
        if (outputFile.exists()) {
            Log.d(TAG, "파일 이미 존재: " + outputFile.getAbsolutePath());
            return outputFile;
        }

        Log.d(TAG, "파일 복사 시작 - 리소스 ID: " + rawResId + ", 대상 파일명: " + outputFileName);

        try (InputStream in = getResources().openRawResource(rawResId);
             FileOutputStream out = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[1024];
            int length;
            int totalBytes = 0;

            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
                totalBytes += length;
            }

            Log.d(TAG, "파일 복사 완료: " + outputFile.getAbsolutePath() + " (총 " + totalBytes + " 바이트)");

        } catch (IOException e) {
            Log.e(TAG, "파일 복사 실패: " + e.getMessage(), e);
            return null;
        }

        return outputFile;
    }

    private void mergeQuestionAndAnswer(int testNum, int questionNumber, String answerMp3Path, AtomicInteger completedAnswers) {
        String searchQuestionMp3FileName = "spa_test_" + testNum + "_" + questionNumber; //ex "spa_test_1_1"
        int resId = getResources().getIdentifier(searchQuestionMp3FileName, "raw", getPackageName());
        if (resId == 0) {
            Log.e(TAG, "해당하는 질문 MP3 리소스를 찾을 수 없습니다: " + searchQuestionMp3FileName);
            return;
        }
        String questionMp3FileName = searchQuestionMp3FileName + ".mp3"; //ex "spa_test_1_1.mp3"

        File questionMp3File = copyRawResourceToFile(resId, questionMp3FileName);
        if (questionMp3File == null) {
            Log.e(TAG, "질문 MP3 파일을 내부 저장소로 복사하는 데 실패했습니다.");
            return;
        }
        String questionMp3Path = questionMp3File.getAbsolutePath();

        //questionMp3 = questionMp3File; //원본 객체 자체를 참조(원본 객체_questionMp3File가 변경되면 questionMp3도 변경됨)
        questionMp3 = new File(questionMp3Path); //새로운 questionMp3 객체를 생성해서 questionMp3File의 경로(questionMp3Path)만 참조하게 함. 향후 원본이 변경되도 영향 없음.
        answerMp3 = new File(answerMp3Path);


        mergedMp3FilePath = new File(internalDir, "merged_" + testNum + "_" + questionNumber + ".mp3").getAbsolutePath();
        Log.d(TAG, "mergedMp3FilePath: " + mergedMp3FilePath);
        outputMp3 = new File(mergedMp3FilePath); //새로운 outputMp3 객체를 생성해서 mergedMp3FilePath 경로를 참조하는 객체를 참조하게 함. 향후 원본이 변경되도 영향 없음.

        if (!questionMp3.exists() || !answerMp3.exists()) {
            Log.e(TAG, "병합할 MP3 파일이 존재하지 않습니다: " + questionMp3Path + " 또는 " + answerMp3Path);
            return;
        }

        String command = String.format(
                //"-y -i \"%s\" -i \"%s\" -filter_complex \"[0:0][1:0]concat=n=2:v=0:a=1[out]\" -map \"[out]\" \"%s\"",
                "-y -i \"%s\" -i \"%s\" -filter_complex \"[0:a]volume=0.5[a0];[1:a]volume=1.0[a1];[a0][a1]concat=n=2:v=0:a=1[out]\" -map \"[out]\" \"%s\"",
                questionMp3.getAbsolutePath(),
                answerMp3.getAbsolutePath(),
                outputMp3.getAbsolutePath()
        );

        FFmpegKit.executeAsync(command, session -> {
            boolean success = ReturnCode.isSuccess(session.getReturnCode());
            runOnUiThread(() -> {
                if (success) {
                    Log.d(TAG, "질문과 답변 MP3 병합 성공: " + outputMp3.getAbsolutePath());
                    completedAnswers.incrementAndGet();  // ✅ 병합이 끝난 후 증가
                    checkAndMergeAllQA(testNum, completedAnswers);
                    checkMergedMP3Format(outputMp3);
                } else {
                    Log.e(TAG, "질문과 답변 MP3 병합 실패: " + session.getFailStackTrace());
                }
            });
        });
    }

    private void checkAndMergeAllQA(int testNum, AtomicInteger completedAnswers) {
        if (completedAnswers.get() >= totalAnswers) { // 모든 변환 완료된 후 실행
            Log.d(TAG, "✅ 모든 변환 완료, 최종 병합 실행!");
            mergeAllQA(testNum);
        }
    }

    private void mergeAllQA(int testNum) {
        File[] files = internalDir.listFiles();  // 내부 저장소의 모든 파일 가져오기
        ArrayList<File> mp3FilesToMerge = new ArrayList<>();

        // 🔹 "merged_X_Y.mp3" 패턴을 가진 파일을 찾아 리스트에 추가
        for (File file : files) {
            if (file.getName().matches("merged_" + testNum + "_\\d+.mp3")) {
                if (file.exists() && file.length() > 0) {  // ✅ 파일이 존재하고 크기가 0보다 클 때만 추가
                    mp3FilesToMerge.add(file);
                    Log.d(TAG, "🔹 병합할 MP3 파일 추가됨: " + file.getAbsolutePath());
                } else {
                    Log.e(TAG, "⚠️ 병합할 파일이 존재하지 않거나 크기가 0임: " + file.getAbsolutePath());
                }
            }
        }

        // 🔹 병합할 파일이 2개 이상이어야 진행
        if (mp3FilesToMerge.size() < 2) {
            Log.e(TAG, "병합할 MP3 파일이 2개 미만입니다.");
            return;
        }

        // 🔹 파일 이름 기준 오름차순 정렬 (merged_1_1.mp3 → merged_1_2.mp3 → ...)
        mp3FilesToMerge.sort((f1, f2) -> f1.getName().compareTo(f2.getName()));

        // 🔹 FFmpeg concat용 파일 목록 생성
        File concatFile = new File(internalDir, "mp3_concat_list.txt");
        try (FileWriter writer = new FileWriter(concatFile)) {
            for (File mp3File : mp3FilesToMerge) {
                writer.write("file '" + mp3File.getAbsolutePath() + "'\n");
            }
            writer.flush();
            Log.d(TAG, "✅ FFmpeg 병합 리스트 생성 완료: " + concatFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "FFmpeg 병합 리스트 파일 생성 실패: " + e.getMessage());
            return;
        }

        // 🔹 최종 병합된 MP3 파일 경로 설정
        String mergedAllMp3Path = new File(internalDir, "merged_all_QA_" + testNum + ".mp3").getAbsolutePath();
        Log.d(TAG, "최종 병합 파일 경로: " + mergedAllMp3Path);

        // 🔹 FFmpeg 병합 명령 실행
        String command = String.format(
                "-y -f concat -safe 0 -i \"%s\" -c copy \"%s\"",
                concatFile.getAbsolutePath(),
                mergedAllMp3Path
        );

        FFmpegKit.executeAsync(command, session -> {
            boolean success = ReturnCode.isSuccess(session.getReturnCode());
            runOnUiThread(() -> {
                if (success) {
                    Log.d(TAG, "모든 질문+답변 MP3 병합 성공: " + mergedAllMp3Path);
                    checkMergedMP3Format(new File(mergedAllMp3Path));
                } else {
                    Log.e(TAG, "모든 질문+답변 MP3 병합 실패: " + session.getFailStackTrace());
                }
            });
        });
    }




    private void shareMergedMP3(int testNum, int questionNumber) {
        File mergedMp3File = null;

        if(questionNumber == 5) {
            mergedMp3File = new File(internalDir, "merged_all_QA_" + testNum + ".mp3");
        } else {
            mergedMp3File = new File(internalDir, "merged_" + testNum + "_" + questionNumber + ".mp3");
        }

        if (!mergedMp3File.exists()) {
            Toast.makeText(this, "MP3 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "MP3 파일 없음: " + mergedMp3File.getAbsolutePath());
            return;
        }

        Uri fileUri = FileProvider.getUriForFile(this, "com.example.myapp.fileprovider", mergedMp3File);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("audio/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "MP3 공유"));
    }

//-----click & dialog-------------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
        String playAudioFilePath = null;

        if (v.getId() == R.id.answer_sheet_share_answer) {
            showShareDialog();
            Log.d(TAG, "Share dialog is being displayed.");
        }
        if (v.getId() == R.id.answer_sheet_record_1) {
            playAudioFilePath = new File(internalDir,"merged_" + getIntentTestNum + "_1.mp3").getAbsolutePath();
        } else if (v.getId() == R.id.answer_sheet_record_2) {
            playAudioFilePath = new File(internalDir,"merged_" + getIntentTestNum + "_2.mp3").getAbsolutePath();
        } else if (v.getId() == R.id.answer_sheet_record_3) {
            playAudioFilePath = new File(internalDir,"merged_" + getIntentTestNum + "_3.mp3").getAbsolutePath();
        } else if (v.getId() == R.id.answer_sheet_record_4) {
            playAudioFilePath = new File(internalDir,"merged_" + getIntentTestNum + "_4.mp3").getAbsolutePath();
        }
        if (playAudioFilePath == null) {
            //Toast.makeText(this, "결합된 MP3 파일이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        playAudio(playAudioFilePath); // ✅ MP3 재생 함수 호출
    } //onClick();

    private void showShareDialog() {
        // 다이얼로그 생성
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_app_share_dialog);

        // 다이얼로그 닫기 불가능하도록 설정 (선택 사항)
        dialog.setCancelable(true);
        dialog.setOnCancelListener(dialogInterface -> dialog.dismiss());

        // 다이얼로그 뷰 초기화
        Button btnQuestion1Text = dialog.findViewById(R.id.question_1_text_button);
        Button btnQuestion2Text = dialog.findViewById(R.id.question_2_text_button);
        Button btnQuestion3Text = dialog.findViewById(R.id.question_3_text_button);
        Button btnQuestion4Text = dialog.findViewById(R.id.question_4_text_button);
        Button btnQuestionAllText = dialog.findViewById(R.id.question_all_text_button);
        Button btnQuestion1Mp3 = dialog.findViewById(R.id.question_1_mp3_button);
        Button btnQuestion2Mp3 = dialog.findViewById(R.id.question_2_mp3_button);
        Button btnQuestion3Mp3 = dialog.findViewById(R.id.question_3_mp3_button);
        Button btnQuestion4Mp3 = dialog.findViewById(R.id.question_4_mp3_button);
        Button btnQuestionAllMp3 = dialog.findViewById(R.id.question_all_mp3_button);

        // 🔹 텍스트 공유 버튼 이벤트 설정
        btnQuestion1Text.setOnClickListener(v -> shareText(combinedQA1));
        btnQuestion2Text.setOnClickListener(v -> shareText(combinedQA2));
        btnQuestion3Text.setOnClickListener(v -> shareText(combinedQA3));
        btnQuestion4Text.setOnClickListener(v -> shareText(combinedQA4));
        btnQuestionAllText.setOnClickListener(v -> shareText(combinedQAAll));

        // 🔹 MP3 공유 버튼 이벤트 설정
        btnQuestion1Mp3.setOnClickListener(v -> shareMergedMP3(getIntentTestNum, 1));
        btnQuestion2Mp3.setOnClickListener(v -> shareMergedMP3(getIntentTestNum, 2));
        btnQuestion3Mp3.setOnClickListener(v -> shareMergedMP3(getIntentTestNum, 3));
        btnQuestion4Mp3.setOnClickListener(v -> shareMergedMP3(getIntentTestNum, 4));
        btnQuestionAllMp3.setOnClickListener(v -> shareMergedMP3(getIntentTestNum, 5));

        // 버튼 이벤트 설정
        if(recordedAudioFilePath_1 == null) {
            btnQuestion1Text.setAlpha(0.5f);
            btnQuestion1Text.setEnabled(false);
            btnQuestion1Text.setOnClickListener(null);
            btnQuestion1Mp3.setAlpha(0.5f);
            btnQuestion1Mp3.setEnabled(false);
            btnQuestion1Mp3.setOnClickListener(null);
        }
        if(recordedAudioFilePath_2 == null) {
            btnQuestion2Text.setAlpha(0.5f);
            btnQuestion2Text.setEnabled(false);
            btnQuestion2Text.setOnClickListener(null);
            btnQuestion2Mp3.setAlpha(0.5f);
            btnQuestion2Mp3.setEnabled(false);
            btnQuestion2Mp3.setOnClickListener(null);
        }
        if(recordedAudioFilePath_3 == null) {
            btnQuestion3Text.setAlpha(0.5f);
            btnQuestion3Text.setEnabled(false);
            btnQuestion3Text.setOnClickListener(null);
            btnQuestion3Mp3.setAlpha(0.5f);
            btnQuestion3Mp3.setEnabled(false);
            btnQuestion3Mp3.setOnClickListener(null);
        }
        if(recordedAudioFilePath_4 == null) {
            btnQuestion4Text.setAlpha(0.5f);
            btnQuestion4Text.setEnabled(false);
            btnQuestion4Text.setOnClickListener(null);
            btnQuestion4Mp3.setAlpha(0.5f);
            btnQuestion4Mp3.setEnabled(false);
            btnQuestion4Mp3.setOnClickListener(null);
        }
        if(totalAnswers < 2) {
            btnQuestionAllText.setAlpha(0.5f);
            btnQuestionAllText.setEnabled(false);
            btnQuestionAllText.setOnClickListener(null);
            btnQuestionAllMp3.setAlpha(0.5f);
            btnQuestionAllMp3.setEnabled(false);
            btnQuestionAllMp3.setOnClickListener(null);
        }

        // 다이얼로그 표시
        dialog.show();
    }



//-----Play MP3 Audio-------------------------------------------------------------------------------
    private void playAudio(String filePath) {
        if (filePath == null) {
            Log.e(TAG, "Audio file path is null, cannot play.");
            return;
        }

        Log.d(TAG, "🔹 playAudio 실행됨. filePath: " + filePath);

        File file = new File(filePath);
        if (!file.exists()) {
            Log.e(TAG, "MP3 파일을 찾을 수 없습니다: " + filePath);
            return;
        }

        Uri fileUri = FileProvider.getUriForFile(this, "com.example.myapp.fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "audio/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

//-----Text Share-----------------------------------------------------------------------------------
    private void shareText(String text) {
        if (text == null || text.isEmpty()) {
            Toast.makeText(this, "공유할 내용이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(shareIntent, "공유 항목을 선택하세요"));
    }

    private void setQuestionAndAnswer(int examNum) {
        StringBuilder qaBuilderAll = new StringBuilder(); // 전체 Q&A 저장용

        for (int i = 1; i <= 4; i++) {
            // 🔹 동적으로 리소스 ID 가져오기
            String questionResourceName = "spa_examination_" + examNum + "_question_" + i;
            int questionResId = getResources().getIdentifier(questionResourceName, "string", getPackageName());

            // 🔹 XML에서 정의된 ID 이름과 맞춤
            int textViewResId = getResources().getIdentifier("answer_sheet_question_" + i, "id", getPackageName());
            int answerViewResId = getResources().getIdentifier("answer_sheet_answer_" + i, "id", getPackageName());

            TextView questionView = findViewById(textViewResId);
            TextView answerView = findViewById(answerViewResId);

            // 🔹 질문 설정
            if (questionView != null && questionResId != 0) {
                questionView.setText(getResources().getString(questionResId));
            } else {
                Log.e(TAG, "Invalid resource for question " + i + " (resourceName=" + questionResourceName + ")");
            }

            // 🔹 답변 설정 (인텐트에서 올바른 이름으로 가져옴)
            String intentAnswer = getIntent().getStringExtra("answer_" + i);
            if (answerView != null) {
                answerView.setText(intentAnswer != null ? intentAnswer : "No Answer");
            }
        }

        for (int i = 1; i <= 4; i++) {
            // 🔹 질문 가져오기
            String questionResourceName = "spa_examination_" + examNum + "_question_" + i;
            int questionResId = getResources().getIdentifier(questionResourceName, "string", getPackageName());
            String questionText = (questionResId != 0) ? getResources().getString(questionResId) : "Question not found";

            // 🔹 답변 가져오기
            String answerText = getIntent().getStringExtra("answer_" + i);
            if (answerText == null) {
                answerText = "No Answer"; // 답변이 없는 경우 기본값 설정
            }

            // 🔹 TextView에 설정
            int textViewResId = getResources().getIdentifier("answer_sheet_question_" + i, "id", getPackageName());
            int answerViewResId = getResources().getIdentifier("answer_sheet_answer_" + i, "id", getPackageName());
            TextView questionView = findViewById(textViewResId);
            TextView answerView = findViewById(answerViewResId);

            if (questionView != null) questionView.setText(questionText);
            if (answerView != null) answerView.setText(answerText);

            // 🔹 개별 Q&A 저장
            String combinedQA = "Q" + i + ": " + questionText + "\n" +
                    "A" + i + ": " + answerText + "\n\n";

            switch (i) {
                case 1:
                    combinedQA1 = combinedQA;
                    break;
                case 2:
                    combinedQA2 = combinedQA;
                    break;
                case 3:
                    combinedQA3 = combinedQA;
                    break;
                case 4:
                    combinedQA4 = combinedQA;
                    break;
            }

            // 🔹 전체 Q&A 문자열에 추가
            qaBuilderAll.append(combinedQA);
        }

        // 🔹 최종적으로 전체 Q&A 저장
        combinedQAAll = qaBuilderAll.toString();
        Log.d(TAG, "Combined Q&A (All): \n" + combinedQAAll);

        // 🔹 이미지 설정
        int imageResId = getResources().getIdentifier("spa_test_" + examNum, "drawable", getPackageName());
        if (answerSheetImage_1 != null) {
            if (imageResId != 0) {
                answerSheetImage_1.setImageResource(imageResId);
            } else {
                Log.e(TAG, "Invalid image resource for spa_test_" + examNum);
            }
        }
    }

//-----No Important---------------------------------------------------------------------------------
    private void checkPCMFormat(File pcmFile) {
        String command = String.format("-i \"%s\"", pcmFile.getAbsolutePath());

        FFmpegKit.executeAsync(command, session -> {
            Log.d("FFMPEG_PCM", "PCM 파일 포맷 정보: " + session.getAllLogsAsString());
        });
    }

    private void checkMP3Format(File mp3File) {
        String command = String.format("-i \"%s\"", mp3File.getAbsolutePath());

        FFmpegKit.executeAsync(command, session -> {
            String logs = session.getOutput();
            Log.d(TAG, "MP3 파일 정보: " + logs);
        });
    }

    private void checkMergedMP3Format(File mergedMP3File) {
        if (mergedMP3File == null || !mergedMP3File.exists()) {
            Log.e(TAG, "병합된 MP3 파일이 존재하지 않습니다.");
            return;
        }

        // ffprobe 명령어로 MP3 파일 정보 조회
        String command = String.format("-i \"%s\" -print_format json -show_format -show_streams", mergedMP3File.getAbsolutePath());

        FFprobeKit.executeAsync(command, session -> {
            String logs = session.getAllLogsAsString();
            Log.d(TAG, "병합된 MP3 파일 정보:\n" + logs);
        });
    }

    private interface ConversionCallback {
        void onConversionComplete(boolean success, File outputMp3File);
    }

//-----No Use---------------------------------------------------------------------------------------
    private void playAudioPCM(String filePath) {
//        if (filePath == null) {
//            Log.e(TAG, "Audio file path is null, cannot play.");
//            return;
//        }
//
//        try {
//            File file = new File(filePath);
//            FileInputStream fis = new FileInputStream(file);
//            byte[] buffer = new byte[(int) file.length()];
//            fis.read(buffer);
//            fis.close();
//
//            int sampleRate = 16000; // 녹음할 때 사용한 샘플레이트
//            AudioTrack audioTrack = new AudioTrack(
//                    AudioManager.STREAM_MUSIC,
//                    sampleRate,
//                    AudioFormat.CHANNEL_OUT_MONO,
//                    AudioFormat.ENCODING_PCM_16BIT,
//                    buffer.length,
//                    AudioTrack.MODE_STATIC
//            );
//
//            audioTrack.write(buffer, 0, buffer.length);
//            audioTrack.play();
//
//            Log.d(TAG, "Playing PCM audio from: " + filePath);
//        } catch (IOException e) {
//            Log.e(TAG, "Failed to play PCM audio file", e);
//        }
    }
}
