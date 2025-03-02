package com.example.myapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import androidx.core.content.ContextCompat;

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
    private double TTSAudioSpeed = 1.0; // 기본값 1.0
    private AudioTrack audioTrack; // 🔹 오디오 트랙 객체 추가


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
            // SharedPreferences에서 저장된 값 가져오기
            SharedPreferences prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
            TTSAudioSpeed = prefs.getFloat("tts_speed", 1.0f); // 기본값 1.0

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

        boolean isBluetoothConnected = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            try {
                // 🔹 블루투스 권한 체크 추가
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                        == PackageManager.PERMISSION_GRANTED) {
                    int profileConnectionState = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
                    isBluetoothConnected = (profileConnectionState == BluetoothProfile.STATE_CONNECTED);
                } else {
                    Log.w(TAG, "Bluetooth permission not granted. Skipping Bluetooth check.");
                }
            } catch (SecurityException e) {
                Log.e(TAG, "SecurityException while checking Bluetooth connection: " + e.getMessage());
            }
        }

            try {
                SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
                VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                        .setLanguageCode("en-US")
                        .setName("en-US-Neural2-H")
                        .setSsmlGender(SsmlVoiceGender.FEMALE)
                        .build();

                AudioConfig audioConfig = AudioConfig.newBuilder()
                        .setAudioEncoding(AudioEncoding.LINEAR16)
                        .setSpeakingRate(TTSAudioSpeed) // 사용자 설정 반영
                        .setPitch(0.0)
                        .build();

                SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
                byte[] audioContents = response.getAudioContent().toByteArray();
                playAudio(audioContents, isBluetoothConnected);
            } catch (Exception e) {
                Log.e(TAG, "Error while synthesizing speech: " + e.getMessage());
            }
    }

    private void playAudio(byte[] audioData, boolean isBluetoothConnected) {
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            // 🔹 오디오 포커스 요청
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            AudioFocusRequest audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(audioAttributes)
                    .setOnAudioFocusChangeListener(focusChange -> {
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            Log.d(TAG, "Audio Focus Lost - Stopping playback");
                            stop();
                        }
                    })
                    .build();

            int focusResult = audioManager.requestAudioFocus(audioFocusRequest);
            if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.e(TAG, "Failed to gain audio focus");
                return;
            }

            // 🔹 블루투스 연결 시에만 500ms 무음 추가
            if (isBluetoothConnected) {
                int silenceDurationMs = 500;
                int sampleRate = 24000;
                int numChannels = 1;
                int bytesPerSample = 2; // 16bit PCM = 2 bytes per sample

                int silenceByteLength = (sampleRate * numChannels * bytesPerSample * silenceDurationMs) / 1000;
                byte[] silenceData = new byte[silenceByteLength];

                // 🔹 16비트 PCM 무음 값 (리틀 엔디안 기준 0x0000) 명확히 설정
                for (int i = 0; i < silenceByteLength; i += 2) {
                    silenceData[i] = 0x00;
                    silenceData[i + 1] = 0x00;
                }

                // 🔹 기존 오디오 첫 50ms의 평균값을 구해서 무음과 부드럽게 연결
                int fadeInSamples = (sampleRate * numChannels * bytesPerSample * 50) / 1000; // 50ms 구간
                short firstAudioSample = 0;

                if (audioData.length > 2) {
                    firstAudioSample = (short) ((audioData[0] & 0xFF) | (audioData[1] << 8)); // 첫 샘플 값
                }

                for (int i = silenceByteLength - fadeInSamples; i < silenceByteLength; i += 2) {
                    float factor = (float) (i - (silenceByteLength - fadeInSamples)) / fadeInSamples;
                    short fadeSample = (short) (factor * firstAudioSample); // 첫 오디오 샘플 값으로 점진적 증가
                    silenceData[i] = (byte) (fadeSample & 0xFF);
                    silenceData[i + 1] = (byte) ((fadeSample >> 8) & 0xFF);
                }

                // 🔹 무음과 기존 오디오 데이터 결합
                byte[] finalAudioData = new byte[silenceData.length + audioData.length];
                System.arraycopy(silenceData, 0, finalAudioData, 0, silenceData.length);
                System.arraycopy(audioData, 0, finalAudioData, silenceData.length, audioData.length);

                audioData = finalAudioData; // 🔹 변경된 데이터 사용
            }

            // 🔹 기존 AudioTrack 정리 후 새로 생성
            if (audioTrack != null) {
                Log.d(TAG, "🔄 기존 audioTrack이 존재 → 먼저 중지");
                audioTrack.pause();
                audioTrack.flush();
                audioTrack.stop();
                audioTrack.release();
                audioTrack = null;
            }

            // 🔹 AudioTrack 설정 변경 (MODE_STREAM으로 변경)
             audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                24000, // 샘플 레이트
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                audioData.length,
                AudioTrack.MODE_STREAM // 🔹 MODE_STATIC → MODE_STREAM 변경
            );

            // 실제 오디오 데이터를 작성
            audioTrack.write(audioData, 0, audioData.length);
            // 오디오 재생
            audioTrack.play();

        } catch (Exception e) {
            Log.e(TAG, "Error playing audio: " + e.getMessage());
        }
    }

    public void setTTSAudioSpeed(double speed) {
        TTSAudioSpeed = speed;
    }

    public void stop() {
        if (audioTrack != null) {
            try {
                Log.d(TAG, "📢 TTS 강제 중지 시도");

                // 🔹 오디오 중지 및 리소스 해제
                audioTrack.pause();
                audioTrack.flush();  // 🔹 현재 버퍼 비우기
                audioTrack.stop();
                audioTrack.release();
                audioTrack = null;

                Log.d(TAG, "✅ 오디오 트랙 정리 완료");
            } catch (Exception e) {
                Log.e(TAG, "❌ TTS 중지 중 오류 발생: " + e.getMessage());
            }
        } else {
            Log.w(TAG, "⚠ audioTrack이 null, 이미 중지된 상태일 가능성");
        }

        if (textToSpeechClient != null) {
            try {
                Log.d(TAG, "📢 TTS 클라이언트 종료 시도");
                textToSpeechClient.close(); // 🔹 먼저 닫기
                textToSpeechClient.shutdown();  // 🔹 그 후 종료
                textToSpeechClient = null;
                Log.d(TAG, "✅ TTS 클라이언트 종료 완료");
            } catch (Exception e) {
                Log.e(TAG, "❌ TTS 클라이언트 종료 실패: " + e.getMessage());
            }
        }
    }


}

