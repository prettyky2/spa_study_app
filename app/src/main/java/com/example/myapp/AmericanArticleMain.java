package com.example.myapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.media.MediaPlayer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.util.Log;
import android.view.ActionMode;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.json.JSONObject;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import android.app.Dialog;
import android.widget.Toast;

public class AmericanArticleMain extends AppApplication implements View.OnClickListener {

    public static final String TAG = "AmericanArticleMain";
    private TextView ArticleTitle;
    private TextView ArticleText;
    private ImageView ArticleImage;
    private Button ArticleTranslateButton;
    private Button AudioStopButton;
    private Button AudioPlayButton;
    private Button NewsStudyButton;
    private MediaPlayer mediaPlayer;


    private String scriptFilePath;
    private String scriptTranslateFilePath;
    private String mp3FilePath;
    private String imageFilePath;
    private int articleNumber = 0;

    String scriptEnglishContent;
    String scriptKoreaContent;


    private boolean isTranslated = false; // 🔹 현재 번역 상태 (false: 원문, true: 번역본)
    private boolean isPlaying = false;
    private int lastPlaybackPosition = 0; // 🔹 마지막 재생 위치 저장

    // 🔹 각 기사에 대해 문장 단위로 저장할 배열 (최대 10개의 기사 저장)
    private String[] scriptSentences  = new String[0];
    private String[] translatedSentences = new String[0];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_america_article_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.america_article_main_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeClass();

    } //onCreate()



    private void initializeClass() {
        Intent intent = getIntent();
        articleNumber = intent.getIntExtra("article_number", -1);

        //initialize object
        ArticleTitle = findViewById(R.id.main_page_title);
        ArticleImage = findViewById(R.id.article_image);
        ArticleText = findViewById(R.id.headline_text);
        AudioPlayButton = findViewById(R.id.audio_play);
        AudioStopButton = findViewById(R.id.audio_stop);
        ArticleTranslateButton = findViewById(R.id.article_translate);
        NewsStudyButton = findViewById(R.id.news_study);

        // 버튼 클릭 리스너 설정
        AudioPlayButton.setOnClickListener(this);
        ArticleTranslateButton.setOnClickListener(this);
        AudioStopButton.setOnClickListener(this);
        NewsStudyButton.setOnClickListener(this);

        enableTextSelection(ArticleTitle);
        enableTextSelection(ArticleText);

        // 🔹 기사 정보 로드
        String[] headlines = AppAmericaArticleApplication.getInstance().getHeadlines();

        if (articleNumber >= 0 && articleNumber < headlines.length) {
            ArticleTitle.setText(headlines[articleNumber]);
            mp3FilePath = getFilesDir() + "/article_" + articleNumber + ".mp3"; // 🔹 MP3 파일 경로 저장
            scriptFilePath = getFilesDir() + "/article_" + articleNumber + ".txt"; // 🔹 스크립트 파일 경로 저장
            imageFilePath = getFilesDir() + "/article_" + articleNumber + ".jpg"; // 🔹 이미지 파일 경로 저장
            scriptTranslateFilePath = getFilesDir() + "/article_" + articleNumber + "_translated.txt"; // 🔹 번역된 스크립트 파일 경로 저장

            scriptEnglishContent = loadScriptFromFile(scriptFilePath);
            scriptKoreaContent = loadScriptFromFile(scriptTranslateFilePath);

            ArticleText.setText(scriptEnglishContent);

            loadScriptAndTranslateSentences(articleNumber);

            // 🔹 이미지 로드
            File imageFile = new File(getFilesDir(), "article_" + articleNumber + ".jpg");
            if (imageFile.exists()) {
                ArticleImage.setImageURI(android.net.Uri.fromFile(imageFile)); // 내부 저장소 이미지 표시
            } else {
                ArticleImage.setVisibility(View.GONE); // 이미지 없으면 숨김
            }

        } else {
            ArticleTitle.setText("Invalid Article");
            ArticleText.setText("No content available.");
            ArticleImage.setVisibility(View.GONE);
        }

    } //initializeClass()


    //🔹 내부 저장소에서 스크립트(.txt 파일) 로드
    private String loadScriptFromFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            Log.e(TAG, "❌ 스크립트 파일 없음: " + filePath);
            return "Transcript not available.";
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.e(TAG, "❌ 스크립트 파일 읽기 실패: " + filePath, e);
            return "Transcript not available.";
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.audio_play) {
            toggleAudioPlayback();
        } else if(v.getId() == R.id.audio_stop) {
            stopAudioPlayback();
        }
        else if(v.getId() == R.id.article_translate) {
            if(isTranslated) {
                ArticleText.setText(scriptEnglishContent);
                isTranslated = false;
                ArticleTranslateButton.setText(R.string.article_translate_korea);
                ArticleTranslateButton.setTextSize(25);
            } else {
                ArticleText.setText(scriptKoreaContent);
                isTranslated = true;
                ArticleTranslateButton.setText(R.string.article_translate_english);
                ArticleTranslateButton.setTextSize(20);
            }
        }
        else if(v.getId() == R.id.news_study) {
            if (scriptSentences == null || scriptSentences.length == 0 || translatedSentences == null || translatedSentences.length == 0) {
                Log.e(TAG, "❌ 문장 데이터가 없음, PracticeActivity 시작 불가");
                return;
            }
            Intent intent = new Intent(this, AmericanArticlePractice.class);
            intent.putExtra("article_number",articleNumber);
            // 🔹 문장 배열을 전달
            intent.putExtra("scriptSentences", scriptSentences);
            intent.putExtra("translatedSentences", translatedSentences);
            startActivity(intent);
        }
    } //onClick();

    private void toggleAudioPlayback() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(mp3FilePath);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                AudioPlayButton.setText("Error");
                return;
            }
        }

        if (isPlaying) {
            lastPlaybackPosition = mediaPlayer.getCurrentPosition(); // 🔹 현재 재생 위치 저장
            mediaPlayer.pause();
            isPlaying = false;
            AudioPlayButton.setText("Play");
        } else {
            // 🔹 속도 값을 SharedPreferences에서 가져와 설정
            SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
            float playbackSpeed = prefs.getFloat("tts_speed", 1.0f); // 기본값 1.0

            // 🔹 API 23 이상에서만 속도 조절 적용
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(playbackSpeed));
            }

            mediaPlayer.seekTo(lastPlaybackPosition); // 🔹 저장된 위치부터 재생
            mediaPlayer.start();
            isPlaying = true;
            AudioPlayButton.setText("Pause");

            // 🔹 재생이 끝나면 버튼 초기화
            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                lastPlaybackPosition = 0; // 🔹 재생 끝나면 위치 초기화
                AudioPlayButton.setText("Play");
            });
        }
    }

    private void stopAudioPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            lastPlaybackPosition = 0; // 🔹 위치 초기화
            AudioPlayButton.setText("Play");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void enableTextSelection(TextView textView) {
        textView.setTextIsSelectable(true);
        textView.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                menu.clear(); // 기본 컨텍스트 메뉴 제거

                // "번역" 버튼 추가
                menu.add(0, 1, 0, "번역").setOnMenuItemClickListener(item -> {
                    int startSelection = textView.getSelectionStart();
                    int endSelection = textView.getSelectionEnd();

                    if (startSelection >= 0 && endSelection > startSelection) {
                        String selectedText = textView.getText().subSequence(startSelection, endSelection).toString().trim();
                        Log.d(TAG, "Selected text: " + selectedText);

                        // 🔹 단어 길이 검사 (20자 이상이면 번역 거부)
                        if (selectedText.length() > 20) {
                            runOnUiThread(() ->
                                    Toast.makeText(textView.getContext(), "15자 이상 번역 불가", Toast.LENGTH_SHORT).show()
                            );
                            mode.finish(); // 액션 모드 종료
                            return true;
                        }
                        fetchTranslatedWord(selectedText); // 번역 실행
                    } else {
                        Log.e(TAG, "No word selected.");
                    }
                    mode.finish(); // 액션 모드 종료
                    return true;
                });

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
    }

    private void fetchTranslatedWord(String word) {
        new Thread(() -> {
            try {
                InputStream credentialsStream = getResources().openRawResource(R.raw.spastudyproject_key);
                GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                        .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));

                HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(
                        request -> request.setParser(new JsonObjectParser(new GsonFactory()))
                );

                GenericUrl url = new GenericUrl("https://translation.googleapis.com/language/translate/v2");
                url.put("q", word);
                url.put("source", "en");
                url.put("target", "ko");
                url.put("format", "text");

                HttpRequest request = requestFactory.buildPostRequest(url, null);
                request.getHeaders().setAuthorization("Bearer " + credentials.refreshAccessToken().getTokenValue());

                HttpResponse response = request.execute();
                String responseBody = response.parseAsString();

                JSONObject jsonResponse = new JSONObject(responseBody);
                String translatedText = jsonResponse.getJSONObject("data")
                        .getJSONArray("translations")
                        .getJSONObject(0)
                        .getString("translatedText");

                runOnUiThread(() -> showWordDefinition(word, translatedText));

            } catch (Exception e) {
                Log.e(TAG, "번역 오류: " + e.getMessage(), e);
                runOnUiThread(() -> showWordDefinition(word, "번역 오류 발생"));
            }
        }).start();
    }

    private void showWordDefinition(String word, String definition) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_app_translate_word_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView englishWordTextView = dialog.findViewById(R.id.dialog_english_word);
        TextView koreanWordTextView = dialog.findViewById(R.id.dialog_korean_word);
        Button btnSave = dialog.findViewById(R.id.btn_study);
        Button btnOk = dialog.findViewById(R.id.btn_test);
        btnSave.setVisibility(View.GONE);

        englishWordTextView.setText(word);
        koreanWordTextView.setText(definition);

        btnSave.setOnClickListener(v -> dialog.dismiss());
        btnOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // 🔹 문장을 배열로 변환하는 메서드 (문장 구분자: .!?)
    private String[] splitIntoSentences(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new String[0]; // 빈 배열 반환
        }
        // 🔹 약어 리스트 (여기에 필요한 약어를 추가할 수 있음)
        String[] abbreviations = {
                "U.S.", "Dr.", "Mr.", "Mrs.", "Ms.", "Prof.", "Inc.", "Ltd.",
                "Jr.", "Sr.", "e.g.", "i.e.", "U.N.", "St.", "vs.", "U.S.A.", "wasn't.", "isn't.", "aren't.", "won't.", "wouldn't.", "didn't.", "doesn't.", "don't.", "can't.", "couldn't.", "shouldn't.", "won't."
        };

        // 🔹 약어 내 마침표를 특수 문자 `{DOT}`로 변환
        for (String abbr : abbreviations) {
            text = text.replace(abbr, abbr.replace(".", "{DOT}"));
        }

        // 🔹 문장 분리 (마침표, 느낌표, 물음표 기준)
        String[] sentences = text.split("(?<=[.!?])\\s+");

        // 🔹 다시 `{DOT}`를 `.`으로 변경하여 원래 약어 복원
        for (int i = 0; i < sentences.length; i++) {
            sentences[i] = sentences[i].replace("{DOT}", ".");
        }

        return sentences;
    }

    // 🔹 내부 저장소에서 파일 불러온 후 문장 단위로 저장하는 기능 추가
    public void loadScriptAndTranslateSentences(int articleIndex) {
        // 🔹 원문 스크립트 로드
        String scriptFilePath = getFilesDir() + "/article_" + articleIndex + ".txt";
        String scriptContent = loadScriptFromFile(scriptFilePath);
        scriptSentences = splitIntoSentences(scriptContent);

        // 🔹 번역본 로드
        String translateFilePath = getFilesDir() + "/article_" + articleIndex + "_translated.txt";
        String translatedContent = loadScriptFromFile(translateFilePath);
        translatedSentences = splitIntoSentences(translatedContent);
    }

    // 🔹 문장 단위로 저장된 원문과 번역본을 가져오는 메서드
    public String[] getScriptSentences() {
        return scriptSentences;
    }

    public String[] getTranslatedSentences() {
        return translatedSentences;
    }

}
