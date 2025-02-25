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
import android.media.MediaPlayer;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class AppAmericanHeadlineTTSPlayer {

    private static final String TAG = "AppAmericanHeadlineTTSPlayer";
    private static AppAmericanHeadlineTTSPlayer instance;
    private TextToSpeechClient textToSpeechClient;
    private Context context;
    private double TTSAudioSpeed = 1.0; // 기본값 1.0
    private Map<String, String> cachedAudioTexts; // 🔹 미리 생성된 음성 텍스트 저장
    private MediaPlayer mediaPlayer;
    private boolean isPaused = false;
    private int pausePosition = 0;
    // Singleton private constructor
    private AppAmericanHeadlineTTSPlayer(Context context) {
        this.context = context.getApplicationContext();
        cachedAudioTexts = new HashMap<>();
        initializeTextToSpeech();
    }

    // Singleton instance getter
    public static synchronized AppAmericanHeadlineTTSPlayer getInstance(Context context) {
        if (instance == null) {
            instance = new AppAmericanHeadlineTTSPlayer(context);
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

    // 🔹 TTS 음원 미리 생성하여 저장하는 메서드 추가
    public void prepare(String text, String key) {
        if (text == null || text.isEmpty()) {
            Log.e(TAG, "TTS 준비 실패: 입력된 텍스트가 비어 있습니다.");
            return;
        }
        cachedAudioTexts.put(key, text);
        Log.d(TAG, "TTS 준비 완료: " + key);
    }

    // 🔹 기존 `speak()` 메서드 활용하여 재생하도록 수정
    public void play(String key) {
        if (!cachedAudioTexts.containsKey(key)) {
            Log.e(TAG, "TTS 재생 실패: " + key + " 키에 대한 준비된 음원이 없습니다.");
            return;
        }
        String textToSpeak = cachedAudioTexts.get(key);
        Log.d(TAG, "TTS 재생 시작: " + key);
        // 기존 `speak()` 메서드 호출 (음성 합성 및 재생)
        speak(textToSpeak);
    }


    public void prepareToFile(String text, File file) {
        if (text == null || text.isEmpty()) {
            Log.e(TAG, "TTS 준비 실패: 입력된 텍스트가 비어 있습니다.");
            return;
        }

        if (file.exists()) {
            Log.d(TAG, "TTS 음원 파일이 이미 존재함: " + file.getAbsolutePath());
            return;
        }

        new Thread(() -> {
            try {
                List<String> textChunks = splitTextByByteLimit(text, 2000);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                // 🔹 최대 2개의 요청을 동시에 실행하는 스레드 풀 생성
                ExecutorService executorService = Executors.newFixedThreadPool(6);

                for (String chunk : textChunks) {
                    executorService.execute(() -> {
                        try {
                            Log.d(TAG, "🔹 TTS 변환 요청 실행 중...");

                            SynthesisInput input = SynthesisInput.newBuilder().setText(chunk).build();
                            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                                    .setLanguageCode("en-US")
                                    .setName("en-US-Neural2-H")
                                    .setSsmlGender(SsmlVoiceGender.FEMALE)
                                    .build();

                            AudioConfig audioConfig = AudioConfig.newBuilder()
                                    .setAudioEncoding(AudioEncoding.LINEAR16)
                                    .setPitch(0.0)
                                    .build();

                            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
                            byte[] audioContents = response.getAudioContent().toByteArray();

                            synchronized (outputStream) {
                                outputStream.write(audioContents);
                            }

                            Log.d(TAG, "✅ TTS 변환 완료: " + chunk.length() + "자 처리됨.");

                        } catch (Exception e) {
                            Log.e(TAG, "❌ TTS 변환 중 오류 발생: " + e.getMessage());
                        }
                    });
                }

                // 🔹 모든 요청이 끝날 때까지 대기
                executorService.shutdown();
                while (!executorService.isTerminated()) {
                    Thread.sleep(100);
                }

                // 🔹 최종 음원 파일 저장
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(outputStream.toByteArray());
                fos.close();
                outputStream.close();

                Log.d(TAG, "✅ TTS 음원 저장 완료: " + file.getAbsolutePath());
            } catch (Exception e) {
                Log.e(TAG, "TTS 음원 저장 실패: " + e.getMessage());
            }
        }).start();
    }


    private List<String> splitTextByByteLimit(String text, int byteLimit) {
        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + byteLimit, text.length());
            chunks.add(text.substring(start, end));
            start = end;
        }

        return chunks;
    }

    public void playFile(File file) {
        new Thread(() -> {
            try {
                // 🔹 기존 재생 중이라면 중지
                stop();

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(file.getAbsolutePath());
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build());
                mediaPlayer.prepare();
                mediaPlayer.start();

                mediaPlayer.setOnCompletionListener(mp -> {
                    Log.d(TAG, "TTS 음원 재생 완료됨.");
                    stop(); // 🔹 자동으로 정리
                });

            } catch (IOException e) {
                Log.e(TAG, "TTS 음원 재생 실패: " + e.getMessage());
            }
        }).start();
    }

    // 🔹 TTS 재생 함수
    public void play(File audioFile) {
        if (!audioFile.exists()) {
            Log.e(TAG, "오디오 파일이 존재하지 않습니다: " + audioFile.getAbsolutePath());
            return;
        }

        if (mediaPlayer != null && isPaused) {
            mediaPlayer.seekTo(pausePosition);
            mediaPlayer.start();
            isPaused = false;
        } else {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(audioFile.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                Log.e(TAG, "오디오 재생 실패: " + e.getMessage());
            }
        }
    }

    // 🔹 TTS 일시정지 함수
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            pausePosition = mediaPlayer.getCurrentPosition();  // 🔹 현재 재생 위치 저장
            mediaPlayer.pause();
            isPaused = true;
        }
    }

    // 🔹 TTS 정지 함수 (완전 정지)
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPaused = false;
            pausePosition = 0;
        }
    }

    public void setTTSAudioSpeed(double speed) {
        TTSAudioSpeed = speed;
    }

}

