package com.example.myapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Handler;
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

        long delay = isBluetoothConnected ? 500 : 0;

        new Handler().postDelayed(() -> {
            try {
                SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
                VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                        .setLanguageCode("en-US")
                        .setName("en-US-Neural2-H")
                        .setSsmlGender(SsmlVoiceGender.FEMALE)
                        .build();

                AudioConfig audioConfig = AudioConfig.newBuilder()
                        .setAudioEncoding(AudioEncoding.LINEAR16)
                        .setSpeakingRate(1.0)
                        .setPitch(0.0)
                        .build();

                SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
                byte[] audioContents = response.getAudioContent().toByteArray();
                playAudio(audioContents);
            } catch (Exception e) {
                Log.e(TAG, "Error while synthesizing speech: " + e.getMessage());
            }
        }, delay);
    }

    private void playAudio(byte[] audioData) {
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
                        }
                    })
                    .build();

            int focusResult = audioManager.requestAudioFocus(audioFocusRequest);
            if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.e(TAG, "Failed to gain audio focus");
                return;
            }

            // 🔹 블루투스 A2DP 연결 확인 (전화용 SCO 사용 안 함)
            boolean isBluetoothConnected = false;
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                try {
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

            if (isBluetoothConnected) {
                Log.d(TAG, "Bluetooth is connected, forcing audio to Bluetooth A2DP");
                audioManager.setMode(AudioManager.MODE_NORMAL);
                audioManager.setSpeakerphoneOn(false);
                audioManager.setBluetoothScoOn(false); // 🔹 A2DP를 위해 SCO OFF
            } else {
                Log.d(TAG, "Bluetooth not connected, playing through phone speaker");
            }

            // 🔹 AudioTrack 설정 변경 (MODE_STREAM으로 변경)
            AudioTrack audioTrack = new AudioTrack(
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

}

