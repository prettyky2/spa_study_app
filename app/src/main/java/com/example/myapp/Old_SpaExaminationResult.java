package com.example.myapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
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

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFprobeKit;
import com.arthenica.ffmpegkit.ReturnCode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/*
public class Old_SpaExaminationResult extends AppApplication implements View.OnClickListener {

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
    private String combinedQA1;
    private String combinedQA2;
    private String combinedQA3;
    private String combinedQA4;
    private String combinedQAAll; // Q1+A1+Q2+A2+Q3+A3+Q4+A4 전체 저장

    File questionFile ;
    File answerFile ;
    File convertedQuestionFile ;
    File outputFile;
    String pcmFilePath ;
    String mp3FilePath ;
    String mergedMp3FilePath ;

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
        if (recordedAudioFilePath_1 != null) {
            mp3FilePath_1 = getExternalFilesDir(null) + "/answer_" + testNum + "_1.mp3";
            Log.d(TAG, "mp3FilePath_1: " + mp3FilePath_1);
            convertPCMtoMP3(new File(recordedAudioFilePath_1), new File(mp3FilePath_1),
                    (success, outputMp3File) -> {
                        if (success && outputMp3File != null) {
                            mergeQuestionAndAnswer(testNum, 1, outputMp3File.getAbsolutePath());
                        }
                    });
        }
        if (recordedAudioFilePath_2 != null) {
            mp3FilePath_2 = getExternalFilesDir(null) + "/answer_" + testNum + "_2.mp3";
            Log.d(TAG, "mp3FilePath_2: " + mp3FilePath_2);
            convertPCMtoMP3(new File(recordedAudioFilePath_2), new File(mp3FilePath_2),
                    (success, outputMp3File) -> {
                        if (success && outputMp3File != null) {
                            mergeQuestionAndAnswer(testNum, 2, outputMp3File.getAbsolutePath());
                        }
                    });
        }
        if (recordedAudioFilePath_3 != null) {
            mp3FilePath_3 = getExternalFilesDir(null) + "/answer_" + testNum + "_3.mp3";
            Log.d(TAG, "mp3FilePath_3: " + mp3FilePath_3);
            convertPCMtoMP3(new File(recordedAudioFilePath_3), new File(mp3FilePath_3),
                    (success, outputMp3File) -> {
                        if (success && outputMp3File != null) {
                            mergeQuestionAndAnswer(testNum, 3, outputMp3File.getAbsolutePath());
                        }
                    });
        }
        if (recordedAudioFilePath_4 != null) {
            mp3FilePath_4 = getExternalFilesDir(null) + "/answer_" + testNum + "_4.mp3";
            Log.d(TAG, "mp3FilePath_4: " + mp3FilePath_4);
            convertPCMtoMP3(new File(recordedAudioFilePath_4), new File(mp3FilePath_4),
                    (success, outputMp3File) -> {
                        if (success && outputMp3File != null) {
                            mergeQuestionAndAnswer(testNum, 4, outputMp3File.getAbsolutePath());
                        }
                    });
        }
    }

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


    // 🔹 Raw 리소스 파일을 내부 저장소로 복사하는 함수
    private File copyRawResourceToFile(int rawResId, String outputFileName) {
        File outputFile = new File(getFilesDir(), outputFileName);

        // 파일이 이미 존재하는 경우
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

        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "파일 복사 실패 - 리소스를 찾을 수 없음: " + rawResId, e);
            return null;
        } catch (IOException e) {
            Log.e(TAG, "파일 복사 실패: " + e.getMessage(), e);
            return null;
        }

        // 최종 확인
        if (outputFile.exists()) {
            Log.d(TAG, "파일 복사 성공 - 최종 확인: " + outputFile.getAbsolutePath());
        } else {
            Log.e(TAG, "파일 복사 후에도 존재하지 않음: " + outputFile.getAbsolutePath());
        }

        return outputFile;
    }

    private void mergeQuestionAndAnswer(int questionNum, int answerNum, String answerFilePath) {
        Log.d(TAG, "mergeQuestionAndAnswer() 호출됨 - questionNum: " + questionNum + ", answerNum: " + answerNum + ", answerFilePath: " + answerFilePath);

        questionFile = copyRawResourceToFile(
                getResources().getIdentifier("spa_test_" + questionNum + "_" + answerNum, "raw", getPackageName()),
                "question_" + questionNum + "_" + answerNum + ".mp3"
        );
        answerFile = new File(answerFilePath);
        convertedQuestionFile = new File(getFilesDir(), "converted_question_" + questionNum + "_" + answerNum + ".mp3");
//        File convertedAnswerFile = new File(getFilesDir(), "converted_answer_" + questionNum + "_" + answerNum + ".mp3");
        outputFile = new File(getFilesDir(), "merged_" + questionNum + "_" + answerNum + ".mp3");

        if (questionFile == null || !questionFile.exists()) {
            Log.e(TAG, "오류: 질문 MP3 파일이 존재하지 않습니다.");
            return;
        }
        if (!answerFile.exists()) {
            Log.e(TAG, "오류: 답변 MP3 파일이 존재하지 않습니다.");
            return;
        }

        // 🔹 MP3 파일을 192kbps, 44.1kHz, 스테레오로 변환 (질문)
        String convertQuestionCommand = String.format(
                "-y -i \"%s\" -b:a 192k -ar 44100 -ac 1 \"%s\"",
                questionFile.getAbsolutePath(),
                convertedQuestionFile.getAbsolutePath()
        );

        // 🔹 MP3 파일을 192kbps, 44.1kHz, 모노 변환
//        String convertAnswerCommand = String.format(
//                "-y -i \"%s\" -b:a 192k -ar 44100 -ac 1 \"%s\"",
//                answerFile.getAbsolutePath(),
//                convertedAnswerFile.getAbsolutePath()
//        );

        // FFmpeg 실행: 질문 MP3 변환
        FFmpegKit.executeAsync(convertQuestionCommand, session1 -> {
            if (ReturnCode.isSuccess(session1.getReturnCode())) {
//                Log.d(TAG, "질문 MP3 변환 성공: " + convertedQuestionFile.getAbsolutePath());

                // FFmpeg 실행: 답변 MP3 변환
//                FFmpegKit.executeAsync(convertAnswerCommand, session2 -> {
//                    if (ReturnCode.isSuccess(session2.getReturnCode())) {
//                        Log.d(TAG, "답변 MP3 변환 성공: " + convertedAnswerFile.getAbsolutePath());

                        // 🔹 변환된 두 파일을 결합
                        String mergeCommand = String.format(
                                //"-y -i \"%s\" -i \"%s\" -filter_complex \"[0:a] asetpts=PTS-STARTPTS [a0]; [1:a] asetpts=PTS-STARTPTS [a1]; [a0][a1] concat=n=2:v=0:a=1[out]\" -map \"[out]\" -c:a libmp3lame -b:a 192k -ar 44100 -ac 1 \"%s\"",
                                "-y -i \"%s\" -i \"%s\" -filter_complex \"[0:a] asetpts=PTS-STARTPTS,aresample=44100,volume=1.0 [a0]; " +
                                        "[1:a] asetpts=PTS-STARTPTS,aresample=44100,volume=1.0 [a1]; " +
                                        "[a0][a1] concat=n=2:v=0:a=1[out]\" -map \"[out]\" -c:a libmp3lame -b:a 192k \"%s\"",
                                convertedQuestionFile.getAbsolutePath(),
//                                convertedAnswerFile.getAbsolutePath(),
                                convertedQuestionFile.getAbsolutePath(),
                                //answerFile.getAbsolutePath(),
                                outputFile.getAbsolutePath()
                        );

                        FFmpegKit.executeAsync(mergeCommand, session3 -> {
                            if (ReturnCode.isSuccess(session3.getReturnCode())) {
                                Log.d(TAG, "MP3 결합 성공: " + outputFile.getAbsolutePath());
                            } else {
                                Log.e(TAG, "MP3 결합 실패!");
                                Log.e(TAG, "FFmpeg 실행 로그:\n" + session3.getAllLogsAsString());
                            }
                        });

//                    } else {
//                        Log.e(TAG, "답변 MP3 변환 실패: " + session2.getAllLogsAsString());
//                    }
//                });

            } else {
                Log.e(TAG, "질문 MP3 변환 실패: " + session1.getAllLogsAsString());
            }
        });
        checkMergedMP3Format(outputFile);
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


    private void convertAndShareMP3(String pcmFilePath, String mp3FileName) {
        if (pcmFilePath == null || pcmFilePath.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(this, "녹음된 PCM 파일이 없습니다.", Toast.LENGTH_SHORT).show());
            return;
        }

        File pcmFile = new File(pcmFilePath);
        if (!pcmFile.exists()) {
            runOnUiThread(() -> Toast.makeText(this, "PCM 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show());
            return;
        }

        File mp3File = new File(getExternalFilesDir(null), mp3FileName);

        // 🔹 콜백에서 두 개의 파라미터를 받도록 수정
        convertPCMtoMP3(pcmFile, mp3File, (success, outputMp3File) -> {
            if (success && outputMp3File != null) {
                runOnUiThread(() -> {
                    // 🔹 변환 성공 후 MP3 공유
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("audio/*");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(outputMp3File));
                    startActivity(Intent.createChooser(shareIntent, "MP3 공유"));
                });
            } else {
                runOnUiThread(() -> Toast.makeText(this, "MP3 변환 후 공유 실패", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // 🔹 FFmpeg을 사용한 PCM -> MP3 변환
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
                    Log.d(TAG, "MP3 변환 성공: " + mp3File.getAbsolutePath());
                    checkMP3Format(mp3File);
                    callback.onConversionComplete(true, mp3File);
                } else {
                    Log.e(TAG, "MP3 변환 실패: " + session.getFailStackTrace());
                    callback.onConversionComplete(false, null);
                }
            });
        });

    }

    // 🔹 여러 개의 PCM 파일을 변환하여 공유
    private void convertAndShareAllMP3() {
        ArrayList<String> pcmFiles = new ArrayList<>();

        if (recordedAudioFilePath_1 != null) pcmFiles.add(recordedAudioFilePath_1);
        if (recordedAudioFilePath_2 != null) pcmFiles.add(recordedAudioFilePath_2);
        if (recordedAudioFilePath_3 != null) pcmFiles.add(recordedAudioFilePath_3);
        if (recordedAudioFilePath_4 != null) pcmFiles.add(recordedAudioFilePath_4);

        if (pcmFiles.isEmpty()) {
            Toast.makeText(this, "공유할 MP3 파일이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < pcmFiles.size(); i++) {
            String pcmFilePath = pcmFiles.get(i);
            String mp3FileName = "audio_" + (i + 1) + ".mp3";
            convertAndShareMP3(pcmFilePath, mp3FileName);
        }
    }

    // 🔹 변환 완료 후 실행될 콜백 인터페이스
    private interface ConversionCallback {
        void onConversionComplete(boolean success, File outputMp3File);
    }


//-----Play MP3 Audio-------------------------------------------------------------------------------
private void playAudio(String filePath) {
    if (filePath == null) {
        Log.e(TAG, "Audio file path is null, cannot play.");
        return;
    }

    File file = new File(filePath);
    if (!file.exists()) {
        Log.e(TAG, "MP3 파일을 찾을 수 없습니다: " + filePath);
        Toast.makeText(this, "MP3 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        return;
    }

    Uri fileUri = FileProvider.getUriForFile(this, "com.example.myapp.fileprovider", file);

    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setDataAndType(fileUri, "audio/*");
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 🔹 권한 부여
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

//-----click & dialog-------------------------------------------------------------------------------
    @Override
    public void onClick(View v) {


        pcmFilePath = recordedAudioFilePath_1; // 기본적으로 1번 파일 사용
        mp3FilePath = getExternalFilesDir(null) + "/answer_" + getIntentTestNum + "_1.mp3"; // 변환된 MP3 저장 위치
        mergedMp3FilePath = getExternalFilesDir(null) + "/merged_" + getIntentTestNum + "_1.mp3"; // 결합된 MP3

        if (v.getId() == R.id.answer_sheet_record_1) {
            // 🔹 PCM 직접 재생
            playAudioPCM(pcmFilePath);
        } else if (v.getId() == R.id.answer_sheet_record_2) {
            // 🔹 PCM을 MP3로 변환 후 재생
            convertPCMtoMP3(new File(pcmFilePath), new File(mp3FilePath), (success, outputMp3File) -> {
                if (success && outputMp3File != null) {
                    playAudio(outputMp3File.getAbsolutePath());
                } else {
                    Log.e(TAG, "MP3 변환 실패");
                }
            });
        } else if (v.getId() == R.id.answer_sheet_record_3) {
            // 🔹 PCM을 MP3로 변환 후 Question과 결합한 뒤 재생
            convertPCMtoMP3(new File(pcmFilePath), new File(mp3FilePath), (success, outputMp3File) -> {
                if (success && outputMp3File != null) {
                    mergeQuestionAndAnswer(getIntentTestNum, 1, outputMp3File.getAbsolutePath());
                    playAudio(mergedMp3FilePath);
                } else {
                    Log.e(TAG, "MP3 변환 실패");
                }
            });
        } else if (v.getId() == R.id.answer_sheet_record_4) {
            // 🔹 기존 결합된 MP3 재생
            playAudio(mergedMp3FilePath);
        }


//        String filePath = null;
//
//        if (v.getId() == R.id.answer_sheet_share_answer) {
//            showShareDialog();
//            Log.d(TAG, "Share dialog is being displayed.");
//        }
//        if (v.getId() == R.id.answer_sheet_record_1) {
//            filePath = getExternalFilesDir(null) + "/merged_" + getIntentTestNum + "_1.mp3"; // ✅ 결합된 MP3 경로
//        } else if (v.getId() == R.id.answer_sheet_record_2) {
//            filePath = getExternalFilesDir(null) + "/merged_" + getIntentTestNum + "_2.mp3"; // ✅ 결합된 MP3 경로
//        } else if (v.getId() == R.id.answer_sheet_record_3) {
//            filePath = getExternalFilesDir(null) + "/merged_" + getIntentTestNum + "_3.mp3"; // ✅ 결합된 MP3 경로
//        } else if (v.getId() == R.id.answer_sheet_record_4) {
//            filePath = getExternalFilesDir(null) + "/merged_" + getIntentTestNum + "_4.mp3"; // ✅ 결합된 MP3 경로
//        }
//        if (filePath == null) {
//            Toast.makeText(this, "결합된 MP3 파일이 없습니다.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        playAudio(filePath); // ✅ MP3 재생 함수 호출



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

        // 버튼 이벤트 설정
        // 🔹 텍스트 공유 버튼 이벤트 설정
        btnQuestion1Text.setOnClickListener(v -> shareText(combinedQA1));
        btnQuestion2Text.setOnClickListener(v -> shareText(combinedQA2));
        btnQuestion3Text.setOnClickListener(v -> shareText(combinedQA3));
        btnQuestion4Text.setOnClickListener(v -> shareText(combinedQA4));
        btnQuestionAllText.setOnClickListener(v -> shareText(combinedQAAll));

        // 🔹 MP3 공유 버튼 이벤트 설정
        btnQuestion1Mp3.setOnClickListener(v -> convertAndShareMP3(recordedAudioFilePath_1, "audio_question_1.mp3"));
        btnQuestion2Mp3.setOnClickListener(v -> convertAndShareMP3(recordedAudioFilePath_2, "audio_question_2.mp3"));
        btnQuestion3Mp3.setOnClickListener(v -> convertAndShareMP3(recordedAudioFilePath_3, "audio_question_3.mp3"));
        btnQuestion4Mp3.setOnClickListener(v -> convertAndShareMP3(recordedAudioFilePath_4, "audio_question_4.mp3"));
        btnQuestionAllMp3.setOnClickListener(v -> convertAndShareAllMP3());

        // 다이얼로그 표시
        dialog.show();
    }

//-----No Use---------------------------------------------------------------------------------------
    private void playAudioPCM(String filePath) {
        if (filePath == null) {
            Log.e(TAG, "Audio file path is null, cannot play.");
            return;
        }

        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            fis.close();

            int sampleRate = 16000; // 녹음할 때 사용한 샘플레이트
            AudioTrack audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    buffer.length,
                    AudioTrack.MODE_STATIC
            );

            audioTrack.write(buffer, 0, buffer.length);
            audioTrack.play();

            Log.d(TAG, "Playing PCM audio from: " + filePath);
        } catch (IOException e) {
            Log.e(TAG, "Failed to play PCM audio file", e);
        }
    }
}
*/