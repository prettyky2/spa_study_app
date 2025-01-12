package com.example.myapp;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

public class AppTTSPlayer {

    private static final String TAG = "AppTTSPlayer";
    private static AppTTSPlayer instance;
    private TextToSpeechClient textToSpeechClient;
    private Context context;

    // Singleton private constructor
    private AppTTSPlayer(Context context) {
        this.context = context.getApplicationContext();
        initializeTextToSpeech();
    }

    // Singleton instance getter
    public static synchronized AppTTSPlayer getInstance(Context context) {
        if (instance == null) {
            instance = new AppTTSPlayer(context);
        }
        return instance;
    }
    private String getCellData(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        return cell.toString();
    } //getCellData(Row row, int cellIndex)



    private void initializeTextToSpeech() {
        try {
            InputStream credentialsStream = context.getResources().openRawResource(R.raw.spastudyproject_key);
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));

            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            if (textToSpeechClient != null) {
                textToSpeechClient.close();
            }

            textToSpeechClient = TextToSpeechClient.create(settings);
            Log.d(TAG, "TextToSpeechClient initialized successfully.");

        } catch (IOException e) {
            Log.e(TAG, "Failed to initialize Text-to-Speech client: " + e.getMessage());
            textToSpeechClient = null;
        }
    }

    public void release() {
        if (textToSpeechClient != null) {
            textToSpeechClient.close();
            textToSpeechClient = null;
            Log.d(TAG, "TextToSpeechClient released.");
        }
    }

    public void speak(String text) {
        if (textToSpeechClient == null) {
            Log.e(TAG, "TextToSpeechClient is null. Reinitializing...");
            initializeTextToSpeech();
            if (textToSpeechClient == null) {
                Log.e(TAG, "Failed to reinitialize TextToSpeechClient. Cannot synthesize speech.");
                return;
            }
        }

        try {
            // 텍스트 입력
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            // 목소리 선택
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("en-US")  // 언어 코드
                    .setName("en-US-Neural2-H") // 목소리 이름
                    .setSsmlGender(SsmlVoiceGender.FEMALE) // 성별
                    .build();

            // 오디오 설정
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.LINEAR16) // PCM 인코딩
                    .setSpeakingRate(1.0)  // 말하는 속도 (조정 가능)
                    .setPitch(0.0)         // 음높이
                    .build();

            // 음성 합성 요청
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            Log.d(TAG, "Requested Voice Name: " + voice.getName());
            Log.d(TAG, "Synthesized Voice: " + response.toString());

            // 오디오 데이터 추출
            byte[] audioContents = response.getAudioContent().toByteArray();

            // 오디오 데이터 재생
            playAudio(audioContents);

        } catch (Exception e) {
            Log.e(TAG, "Error while synthesizing speech: " + e.getMessage());
        }
    }


    private void playAudio(byte[] audioData) {
        try {
            AudioTrack audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    24000, // 생성된 데이터와 동일한 샘플 레이트로 수정
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    audioData.length,
                    AudioTrack.MODE_STATIC
            );

            // 실제 오디오 데이터를 작성
            audioTrack.write(audioData, 0, audioData.length);
            // 오디오 재생
            audioTrack.play();

        } catch (Exception e) {
            Log.e(TAG, "Error playing audio: " + e.getMessage());
        }
    }


    private void listAvailableVoices() {
//        try {
//            // res/raw에서 JSON 키 파일 로드
//            InputStream credentialsStream = getResources().openRawResource(R.raw.spastudyproject_key);
//            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
//                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
//
//            // 인증 정보를 사용해 TTS 클라이언트 생성
//            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
//                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
//                    .setEndpoint("us-central1-texttospeech.googleapis.com:443")
//                    .build();
//            if (textToSpeechClient != null) {
//                textToSpeechClient.close();
//                textToSpeechClient = null;
//            }
//            TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings);
//
//            // 목소리 리스트 가져오기
//            ListVoicesRequest request = ListVoicesRequest.newBuilder().build();
//            ListVoicesResponse response = textToSpeechClient.listVoices(request);
//
//            // 결과 출력
//            for (Voice voice : response.getVoicesList()) {
//                Log.d(TAG, "Voice Name: " + voice.getName());
//                Log.d(TAG, "Supported Languages: " + voice.getLanguageCodesList());
//                Log.d(TAG, "Gender: " + voice.getSsmlGender());
//                Log.d(TAG, "Natural Sample Rate Hertz: " + voice.getNaturalSampleRateHertz());
//            }
//
//            textToSpeechClient.close();
//        } catch (Exception e) {
//            Log.e(TAG, "Error listing voices: " + e.getMessage());
//        }
    }



}