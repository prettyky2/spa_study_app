package com.example.myapp;

import android.app.Application;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import android.content.SharedPreferences;


public class AppAmericaArticleApplication extends Application {

    private static final String TAG = "AppAmericaArticleApplication";

    public int AmericaHeadlineCrawlingDone = 0;

    private static AppAmericaArticleApplication instance;


    // 🔹 headlines 저장 배열 추가
    private String[] headlines = new String[10];
    private String[] articleUrls = new String[10];  // 기사 상세 URL 저장
    private String[] transcripts = new String[10];  // 스크립트 텍스트 저장
    private String[] mp3Urls = new String[10];      // MP3 다운로드 URL 저장
    private int mp3DownloadCount = 0;               // MP3 다운로드 완료 카운트

    public String[] getHeadlines() { return headlines; }
    public String[] getTranscripts() { return transcripts; }
    public String[] getMp3Urls() { return mp3Urls; }

    // 🔹 싱글턴 인스턴스를 반환하는 메서드 추가
    public static AppAmericaArticleApplication getInstance() {
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        // 🔹 기존 저장된 기사 제목 가져오기
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String savedFirstTitle = prefs.getString("first_article_title", "");

        new FetchArticlesTask(savedFirstTitle).execute();
    }

    private class FetchArticlesTask extends AsyncTask<Void, Void, Void> {
        private String savedFirstTitle; // 🔹 기존 저장된 첫 번째 기사 제목

        public FetchArticlesTask(String savedFirstTitle) {
            this.savedFirstTitle = savedFirstTitle;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // 1️⃣ NPR 메인 페이지에서 기사 리스트 가져오기
                Document doc = Jsoup.connect("https://www.npr.org/programs/morning-edition/").get();
                Elements articles = doc.select("h3.rundown-segment__title a");

                int count = 0;
                for (int i = 1; i < articles.size(); i++) {
                    if (count >= 10) break;
                    headlines[count] = articles.get(i).text();
                    articleUrls[count] = articles.get(i).absUrl("href"); // 기사 상세 URL 저장
                    count++;
                }

                // 🔹 첫 번째 기사 제목 비교
                String newFirstTitle = headlines[0];

                if (newFirstTitle.equals(savedFirstTitle)) {
                    Log.d(TAG, "✅ 기사 제목이 변경되지 않음 → MP3 다운로드 생략");
                } else {
                    Log.d(TAG, "⚠ 기사 제목 변경됨 → 기존 MP3 & 스크립트 삭제 후 새로 다운로드");
                    deleteOldFiles(); // ✅ 기존 파일 삭제
                    for (int i = 0; i < count; i++) {
                        fetchArticleDetails(i); // ✅ 새로운 데이터 다운로드
                    }

                    // 🔹 새로운 기사 제목 저장
                    SharedPreferences.Editor editor = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit();
                    editor.putString("first_article_title", newFirstTitle);
                    editor.apply();
                }

            } catch (IOException e) {
                Log.e(TAG, "❌ Error fetching article list", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // ✅ 크롤링 완료 후 UI 업데이트 요청
            Intent intent = new Intent("com.example.myapp.ARTICLES_UPDATED");
            sendBroadcast(intent);
        }
    }

    /**
     * 🔹 기존 MP3 & 스크립트 파일 삭제
     */
    private void deleteOldFiles() {
        File dir = getFilesDir();
        for (int i = 0; i < 10; i++) {
            File textFile = new File(dir, "article_" + i + ".txt");
            File mp3File = new File(dir, "article_" + i + ".mp3");

            if (textFile.exists()) textFile.delete();
            if (mp3File.exists()) mp3File.delete();
        }
        Log.d(TAG, "✅ 기존 기사 파일 삭제 완료");
    }

    /**
     * 🔹 개별 기사 상세 페이지에서 스크립트 & MP3 URL 가져오기
     */
    private void fetchArticleDetails(int index) {
        try {
            Document articleDoc = Jsoup.connect(articleUrls[index]).get();

            // 1️⃣ 스크립트 가져오기
            Element transcriptElement = articleDoc.selectFirst("a.audio-tool.audio-tool-transcript");
            if (transcriptElement != null) {
                String transcriptUrl = transcriptElement.absUrl("href");
                transcripts[index] = fetchTranscriptText(transcriptUrl);
            } else {
                transcripts[index] = "Transcript not available.";
            }

            // 2️⃣ 파일 저장 (null 체크 후 저장)
            if (transcripts[index] != null) {
                saveToFile("article_" + index + ".txt", transcripts[index]);
            }

            // 3️⃣ MP3 가져오기
            Element mp3Element = articleDoc.selectFirst("li.audio-tool.audio-tool-download a");
            if (mp3Element != null) {
                mp3Urls[index] = mp3Element.absUrl("href");
                downloadMp3(mp3Urls[index], "article_" + index + ".mp3");
            }
        } catch (IOException e) {
            Log.e(TAG, "❌ Error fetching article details", e);
        }
    }

    /**
     * 🔹 스크립트 텍스트 가져오기
     */
    private String fetchTranscriptText(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Element transcriptBody = doc.selectFirst("div.transcript, div.transcript-container");

            if (transcriptBody != null) {
                return transcriptBody.html() // 🔹 HTML을 그대로 가져옴
                        .replace("<p>", "\n")    // 🔹 `<p>` 태그를 줄바꿈으로 변환
                        .replace("</p>", "")     // 🔹 `</p>` 제거
                        .replace("<br>", "\n")   // 🔹 `<br>`을 줄바꿈으로 변환
                        .replace("<br/>", "\n"); // 🔹 `<br/>`도 변환
            } else {
                Log.e(TAG, "❌ Transcript not found at: " + url);
                return "Transcript not available.";
            }
        } catch (IOException e) {
            Log.e(TAG, "❌ Error fetching transcript", e);
            return "Transcript not available.";
        }
    }

    /**
     * 🔹 파일을 내부 저장소에 저장
     */
    private void saveToFile(String fileName, String content) {
        if (content == null) {
            Log.e(TAG, "❌ Attempted to save null content to " + fileName);
            return; // 🔹 `null` 데이터 저장 방지
        }

        File file = new File(getFilesDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            Log.d(TAG, "✅ 파일 저장 완료: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "❌ 파일 저장 실패", e);
        }
    }

    private void downloadMp3(String mp3Url, String fileName) {
        File file = new File(getFilesDir(), fileName);
        try {
            byte[] mp3Bytes = Jsoup.connect(mp3Url).ignoreContentType(true).execute().bodyAsBytes();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(mp3Bytes);
                Log.d(TAG, "✅ MP3 다운로드 완료: " + file.getAbsolutePath());
            }

            // 🔹 MP3 다운로드 완료 카운트 증가
            mp3DownloadCount++;

            // 🔹 모든 MP3가 다운로드되면 Intent 전송
            if (mp3DownloadCount == headlines.length) {
                sendMp3DownloadCompleteBroadcast();
            }

        } catch (IOException e) {
            Log.e(TAG, "❌ MP3 다운로드 실패", e);
        }
    }

    /**
     * 🔹 MP3 다운로드가 모두 완료되었을 때 브로드캐스트 전송
     */
    private void sendMp3DownloadCompleteBroadcast() {
        Log.d(TAG, "📢 모든 MP3 다운로드 완료, Broadcast 전송!");
        AmericaHeadlineCrawlingDone = 1;

        Intent intent = new Intent("com.example.myapp.MP3_DOWNLOAD_COMPLETED");
        sendBroadcast(intent);
    }














}