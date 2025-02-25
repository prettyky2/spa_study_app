package com.example.myapp;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.app.Application;
import android.util.Log;
import java.io.File;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.View;

import android.util.Log;

import android.widget.Button;

import android.widget.TextView;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.airbnb.lottie.LottieAnimationView;


public class AppAmericaHeadlineApplication extends Application {

    private static final String TAG = "AppAmericaHeadlineApplication";

    public int AmericaHeadlineCrawlingDone = 0;
    private String politicsTitle, technologyTitle;
    private String politicsContent, technologyContent;
    private String politicsImageUrl, technologyImageUrl;
    private AppAmericanHeadlineTTSPlayer ttsPlayer;

    private static AppAmericaHeadlineApplication instance;

    // 🔹 Getter 메서드 추가 (데이터를 읽기만 가능하게)
    public String getPoliticsTitle() { return politicsTitle; }
    public String getPoliticsContent() { return politicsContent; }
    public String getPoliticsImageUrl() { return politicsImageUrl; }

    public String getTechnologyTitle() { return technologyTitle; }
    public String getTechnologyContent() { return technologyContent; }
    public String getTechnologyImageUrl() { return technologyImageUrl; }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        ttsPlayer = AppAmericanHeadlineTTSPlayer.getInstance(this);
        if (ttsPlayer == null) {
            Log.e(TAG, "❌ TTS 플레이어 초기화 실패");
        } else {
            Log.d(TAG, "✅ TTS 플레이어 초기화 완료");
        }

        // 🔹 크롤링 실행 (앱 시작 시 한 번만)
        new AppAmericaHeadlineApplication.FetchHeadlineTask().execute(
                "https://www.npr.org/sections/politics/",
                "https://www.npr.org/sections/technology/"
        );
    }

    // 🔹 싱글턴 인스턴스를 반환하는 메서드 추가
    public static AppAmericaHeadlineApplication getInstance() {
        return instance;
    }

    // 🔹 NPR 웹사이트에서 헤드라인 + 본문 + 이미지 가져오는 AsyncTask
    private class FetchHeadlineTask extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... urls) {
            String[] newsData = new String[6]; // [정치 제목, 정치 본문, 정치 이미지, 기술 제목, 기술 본문, 기술 이미지]

            for (int i = 0; i < urls.length; i++) {
                try {
                    // 1️⃣ 정치 & 기술 섹션 페이지에 접속
                    Document doc = Jsoup.connect(urls[i]).get();

                    // 2️⃣ <article> 태그 내에서 첫 번째 뉴스 기사 찾기
                    Element articleElement = doc.selectFirst("article.item.has-image h2 a");
                    Element imageElement = doc.selectFirst("article.item.has-image img");

                    if (articleElement != null) {
                        String headline = articleElement.text(); // 기사 제목
                        String articleUrl = articleElement.absUrl("href"); // 기사 URL

                        // 3️⃣ 개별 기사 페이지에 접속해서 본문 가져오기
                        Document articleDoc = Jsoup.connect(articleUrl).get();
                        Element storyContent = articleDoc.selectFirst("div.storytext");

                        String articleText = (storyContent != null) ? storyContent.text() : "본문을 찾을 수 없습니다.";
                        String imageUrl = (imageElement != null) ? imageElement.absUrl("src") : null;

                        // 4️⃣ 결과 저장
                        newsData[i * 3] = headline;
                        newsData[i * 3 + 1] = articleText;
                        newsData[i * 3 + 2] = imageUrl;
                    } else {
                        newsData[i * 3] = "헤드라인을 찾을 수 없습니다.";
                        newsData[i * 3 + 1] = "본문을 찾을 수 없습니다.";
                        newsData[i * 3 + 2] = null;
                    }

                } catch (IOException e) {
                    Log.e(TAG, "Error fetching headline", e);
                    newsData[i * 3] = "헤드라인을 가져오지 못했습니다.";
                    newsData[i * 3 + 1] = "본문을 가져오지 못했습니다.";
                    newsData[i * 3 + 2] = null;
                }
            }
            return newsData;
        }


        @Override
        protected void onPostExecute(String[] newsData) {
            // 크롤링한 데이터를 저장 (앱 내에서 유지)
            politicsTitle = newsData[0];
            politicsContent = newsData[1];
            politicsImageUrl = newsData[2];

            technologyTitle = newsData[3];
            technologyContent = newsData[4];
            technologyImageUrl = newsData[5];
            Log.d(TAG, "string save done");
            // 🔹 TTS 음원 미리 생성
            prepareTTSAsync();
        }
    }

    private void prepareTTSAsync() {
        Log.d(TAG, "prepareTTSAsync");
        new TTSAsyncTask(this).execute();
    }

    // 🔹 `AsyncTask`를 `static` 클래스로 변경 (메모리 누수 방지)
    private static class TTSAsyncTask extends AsyncTask<Void, Void, Void> {
        private final WeakReference<AppAmericaHeadlineApplication> appRef;

        TTSAsyncTask(AppAmericaHeadlineApplication app) {
            Log.d(TAG, "TTSAsyncTask");
            this.appRef = new WeakReference<>(app);
        }


        private boolean isFileContentSame(File file, String newText) {
            try {
                String fileContent = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                return fileContent.equals(newText);
            } catch (IOException e) {
                return false;
            }
        }


        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground");
            AppAmericaHeadlineApplication app = appRef.get();
            if (app == null) return null; // 앱이 종료되었으면 작업 중단

            // 🔹 ttsPlayer가 null인지 확인
            if (app.ttsPlayer == null) {
                Log.e(TAG, "❌ TTS 플레이어가 초기화되지 않음!");
                return null;
            }

            File internalDir = app.getFilesDir();

            File politicsAudioFile = new File(internalDir, "politics_audio.wav");
            String newPoliticsText = app.politicsTitle + ". " + app.politicsContent;

            if (!politicsAudioFile.exists() || !isFileContentSame(politicsAudioFile, newPoliticsText)) {
                if (politicsAudioFile.exists()) {
                    politicsAudioFile.delete();
                }
                try {
                    app.ttsPlayer.prepareToFile(newPoliticsText, politicsAudioFile);
                    Log.d(TAG, "✅ 정치 뉴스 TTS 파일이 정상적으로 생성 요청됨");
                } catch (Exception e) {
                    Log.e(TAG, "❌ TTS 파일 생성 중 예외 발생: " + e.getMessage());
                }

                if (politicsAudioFile.exists()) {
                    Log.d(TAG, "✅ 정치 뉴스 TTS 파일 생성 확인: " + politicsAudioFile.getAbsolutePath());
                    Log.d(TAG, "✅ 정치 뉴스 TTS 파일 업데이트됨");
                } else {
                    Log.e(TAG, "❌ 정치 뉴스 TTS 파일이 생성되지 않음!");
                }
            } else {
                Log.d(TAG, "🔹 기존 정치 뉴스 TTS 파일과 동일하여 업데이트하지 않음");
            }

            File techAudioFile = new File(internalDir, "tech_audio.wav");
            String newTechText = app.technologyTitle + ". " + app.technologyContent;

            if (!techAudioFile.exists() || !isFileContentSame(techAudioFile, newTechText)) {
                if (techAudioFile.exists()) {
                    techAudioFile.delete();
                }
                try {
                    app.ttsPlayer.prepareToFile(newTechText, techAudioFile);
                    Log.d(TAG, "✅ 기술 뉴스 TTS 파일이 정상적으로 생성 요청됨");
                } catch (Exception e) {
                    Log.e(TAG, "❌ TTS 파일 생성 중 예외 발생: " + e.getMessage());
                }

                if (techAudioFile.exists()) {
                    Log.d(TAG, "✅ 기술 뉴스 TTS 파일 생성 확인: " + techAudioFile.getAbsolutePath());
                    Log.d(TAG, "✅ 기술 뉴스 TTS 파일 업데이트됨");
                } else {
                    Log.e(TAG, "❌ 기술 뉴스 TTS 파일이 생성되지 않음!");
                }
            } else {
                Log.d(TAG, "🔹 기존 기술 뉴스 TTS 파일과 동일하여 업데이트하지 않음");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            AppAmericaHeadlineApplication app = appRef.get();
            if (app != null) {
                app.AmericaHeadlineCrawlingDone = 1;
                Log.d(TAG, "✅ TTS 파일 생성 완료, AmericaHeadlineCrawlingDone = 1");

                File internalDir = app.getFilesDir();
                File politicsAudioFile = new File(internalDir, "politics_audio.wav");
                File techAudioFile = new File(internalDir, "tech_audio.wav");

                // 🔹 최종적으로 TTS 파일이 존재하는지 확인
                if (!politicsAudioFile.exists()) {
                    Log.e(TAG, "❌ onPostExecute: 정치 뉴스 TTS 파일이 없음!");
                }
                if (!techAudioFile.exists()) {
                    Log.e(TAG, "❌ onPostExecute: 기술 뉴스 TTS 파일이 없음!");
                }


                // 📢 브로드캐스트 발송
                Intent intent = new Intent("com.example.myapp.TTS_COMPLETED");
                app.sendBroadcast(intent);
                Log.d(TAG, "📢 Broadcast 전송됨: com.example.myapp.TTS_COMPLETED");
            } else {
                Log.e(TAG, "❌ onPostExecute에서 app 객체 null");
            }
        }


    }


}