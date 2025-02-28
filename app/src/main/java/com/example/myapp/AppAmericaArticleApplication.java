package com.example.myapp;

import android.app.Application;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import android.content.SharedPreferences;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class AppAmericaArticleApplication extends Application {

    private static final String TAG = "AppAmericaArticleApplication";

    public int AmericaHeadlineCrawlingDone = 0;

    private static AppAmericaArticleApplication instance;


    // 🔹 headlines 저장 배열 추가
    private String[] headlines = new String[10];
    private String[] articleUrls = new String[10];  // 기사 상세 URL 저장
    private String[] transcripts = new String[10];  // 스크립트 텍스트 저장
    private String[] mp3Urls = new String[10];      // MP3 다운로드 URL 저장
    private String[] imageUrls = new String[10]; // 이미지 URL 저장 배열
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
        boolean downloadCompleted = prefs.getBoolean("download_completed", false);
        boolean scriptDownloadCompleted = prefs.getBoolean("script_download_completed", false);

        new FetchArticlesTask(savedFirstTitle, downloadCompleted, scriptDownloadCompleted).execute();
    }

    // 🔹 새로운 메서드: 이미지 다운로드 후 내부 저장소에 저장
    private void downloadImage(String imageUrl, String fileName) {
        File imageFile = new File(getFilesDir(), fileName);

        if (imageFile.exists()) {
            return; // 이미 존재하면 다운로드 생략
        }

        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Accept", "image/*");

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "❌ 이미지 다운로드 실패: " + connection.getResponseCode());
                return;
            }

            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream out = new FileOutputStream(imageFile)) {

                byte[] buffer = new byte[8192]; // 8KB 버퍼
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            Log.d(TAG, "✅ 이미지 다운로드 성공: " + fileName);

        } catch (IOException e) {
            Log.e(TAG, "❌ 이미지 다운로드 중 오류 발생", e);
        }
    }


    private class FetchArticlesTask extends AsyncTask<Void, Void, Void> {
        private String savedFirstTitle; // 🔹 기존 저장된 첫 번째 기사 제목
        private boolean downloadCompleted;
        private boolean scriptDownloadCompleted;

        public FetchArticlesTask(String savedFirstTitle, boolean downloadCompleted, boolean scriptDownloadCompleted) {
            this.savedFirstTitle = savedFirstTitle;
            this.downloadCompleted = downloadCompleted;
            this.scriptDownloadCompleted = scriptDownloadCompleted;
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

                if (newFirstTitle.equals(savedFirstTitle) && downloadCompleted && allFilesExist()) {
                    Log.d(TAG, "✅ 기사 제목이 변경되지 않았으며, 모든 파일이 정상적으로 존재 → 다운로드 생략");
                    AmericaHeadlineCrawlingDone = 1;

                    Intent intent = new Intent("com.example.myapp.MP3_DOWNLOAD_COMPLETED");
                    sendBroadcast(intent);
                } else {
                    Log.d(TAG, "⚠ 기사 제목 변경됨 또는 이전 다운로드 미완료 → MP3 & 스크립트 새로 다운로드");

                    deleteOldFiles(); // ✅ 기존 파일 삭제 후 다시 다운로드

                    // ✅ 다운로드 시작 전에 `download_completed` 초기화
                    SharedPreferences.Editor editor = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit();
                    editor.putBoolean("download_completed", false);
                    editor.putBoolean("script_download_completed", false);
                    editor.apply();


                    for (int i = 0; i < count; i++) {
                        fetchArticleDetails(i); // ✅ 새로운 데이터 다운로드
                    }

                    // 🔹 새로운 기사 제목 저장 (MP3 완료 여부는 `sendMp3DownloadCompleteBroadcast()`에서 업데이트)
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


    //🔹 모든 MP3 & 스크립트 파일이 정상적으로 존재하는지 확인
    private boolean allFilesExist() {
        File dir = getFilesDir();
        for (int i = 0; i < 10; i++) {
            File textFile = new File(dir, "article_" + i + ".txt");
            File mp3File = new File(dir, "article_" + i + ".mp3");

            if (!textFile.exists() || textFile.length() == 0) {
                Log.d(TAG, "❌ 스크립트 파일이 없거나 손상됨: " + textFile.getName());
                return false; // 🔹 파일이 없거나 크기가 0이면 다운로드 필요
            }
            if (!mp3File.exists() || mp3File.length() < 1024) { // 🔹 1KB 미만이면 손상된 파일로 간주
                Log.d(TAG, "❌ MP3 파일이 없거나 손상됨: " + mp3File.getName());
                return false;
            }
        }
        return true;
    }

     // 🔹 기존 MP3 & 스크립트 파일 삭제
    private void deleteOldFiles() {
        File dir = getFilesDir();
        for (int i = 0; i < 10; i++) {
            File textFile = new File(dir, "article_" + i + ".txt");
            File translatedTextFile = new File(dir, "article_" + i + "_translated.txt");
            File mp3File = new File(dir, "article_" + i + ".mp3");
            File imageFile = new File(dir, "article_" + i + ".jpg");

            if (textFile.exists()) textFile.delete();
            if (translatedTextFile.exists()) translatedTextFile.delete();
            if (mp3File.exists()) mp3File.delete();
            if (imageFile.exists()) imageFile.delete();
        }
        Log.d(TAG, "✅ 기존 기사 파일 삭제 완료");
    }



     //🔹 개별 기사 상세 페이지에서 스크립트 & MP3 URL 가져오기

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

            // 3️⃣ 🔹 Google Translate 웹사이트를 이용해 번역 후 저장
            if (transcripts[index] != null && !transcripts[index].equals("Transcript not available.")) {
                String translatedText = translateUsingGoogle(transcripts[index]);
                saveToFile("article_" + index + "_translated.txt", translatedText);
            }

            // 3️⃣ MP3 가져오기
            Element mp3Element = articleDoc.selectFirst("li.audio-tool.audio-tool-download a");
            if (mp3Element != null) {
                mp3Urls[index] = mp3Element.absUrl("href");
                downloadMp3(mp3Urls[index], "article_" + index + ".mp3");
            }

            // 4️⃣ 이미지 가져오기
            Element imageElement = articleDoc.selectFirst("div.imagewrap img");
            if (imageElement != null) {
                imageUrls[index] = imageElement.absUrl("src"); // 이미지 URL 저장
                downloadImage(imageUrls[index], "article_" + index + ".jpg");
            } else {
                imageUrls[index] = null; // 이미지 없음
            }


        } catch (IOException e) {
            Log.e(TAG, "❌ Error fetching article details", e);
        }
    }


    //🔹 Google Translate 웹사이트를 이용해 문장을 번역
    private String translateUsingGoogle(String text) {
        try {
            // 🔹 줄바꿈(`\n`)을 `{NEWLINE}`으로 변환하여 Google Translate에서 제거되지 않도록 처리
            text = text.replace("\n\n", "<br>");

            // 🔹 Google Translate의 최대 입력 제한을 고려하여 4000자 단위로 나누기
            List<String> textChunks = splitTextIntoChunks(text, 4000);
            List<String> translatedChunks = new ArrayList<>();

            for (String chunk : textChunks) {
                String encodedText = URLEncoder.encode(chunk, "UTF-8");

                // 🔹 Google Translate 웹사이트 URL
                String googleTranslateUrl = "https://translate.google.com/m?sl=en&tl=ko&q=" + encodedText;

                // 🔹 Google Translate 웹사이트에 GET 요청
                Connection.Response response = Jsoup.connect(googleTranslateUrl)
                        .userAgent("Mozilla/5.0")
                        .timeout(10000)
                        .method(Connection.Method.GET)
                        .execute();

                Document doc = response.parse();

                // 🔹 번역된 문장 찾기
                Element translatedElement = doc.selectFirst(".result-container");

                if (translatedElement != null) {
                    String translatedText = translatedElement.text();

                    // 🔹 `{NEWLINE}`을 다시 `\n`으로 복원하여 줄바꿈 적용
                    translatedText = translatedText.replace("<br>", "\n\n");

                    translatedChunks.add(translatedText);
                } else {
                    Log.e(TAG, "❌ 번역된 텍스트를 찾을 수 없음.");
                    translatedChunks.add("[번역 실패]");
                }

                // 🔹 요청 간 딜레이 추가 (Google 차단 방지)
                Thread.sleep(100);
            }

            // 🔹 번역된 조각들을 하나의 텍스트로 합쳐서 반환
            return String.join(" ", translatedChunks);

        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "❌ Google Translate 요청 중 오류 발생", e);
            return "Translation failed.";
        }
    }

    private List<String> splitTextIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += chunkSize) {
            chunks.add(text.substring(i, Math.min(length, i + chunkSize)));
        }
        return chunks;
    }

    // 🔹 스크립트 텍스트 가져오기
    private String fetchTranscriptText(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Element transcriptBody = doc.selectFirst("div.transcript, div.transcript-container");

            if (transcriptBody != null) {
                return transcriptBody.html() // 🔹 원본 HTML을 가져오기
                        .replaceAll("(?i)<b[^>]*>.*?</b>", "") // 🔹 `<b>` 태그 및 그 내부 내용 제거
                        .replaceAll("(?i)<p>\\s*</p>", "") // 🔹 빈 `<p>` 태그 제거
                        .replaceAll("(?i)<p>", "\n\n") // ✅ `<p>` 태그를 문단 구분 (`\n\n`)으로 변환
                        .replaceAll("(?i)<[^>]+>", "") // 🔹 모든 남은 HTML 태그 제거
                        .replace("&amp;", "&") // 🔹 특수 문자 변환 (예: &amp; → &)
                        .replaceAll("\n{3,}", "\n\n") // ✅ 3개 이상의 줄바꿈을 2개로 변환
                        .replaceAll("\n{2,}", "\n\n") // ✅ 2개 이상의 연속된 줄바꿈은 2개로 유지
                        .trim(); // 🔹 앞뒤 불필요한 공백 제거

            } else {
                Log.e(TAG, "❌ Transcript not found at: " + url);
                return "Transcript not available.";
            }
        } catch (IOException e) {
            Log.e(TAG, "❌ Error fetching transcript", e);
            return "Transcript not available.";
        }
    }

    // 🔹 파일을 내부 저장소에 저장
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
        File mp3File = new File(getFilesDir(), fileName);

        // ✅ 기존에 존재하는 잘못된 파일 삭제 후 새로 다운로드
        if (mp3File.exists() && mp3File.length() < 1024 * 10) { // 10KB 미만이면 손상된 파일로 간주
            mp3File.delete();
        }

        try {
            URL url = new URL(mp3Url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // ✅ HTTP 요청 헤더 설정
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Connection", "keep-alive");

            // ✅ 리디렉션이 발생하면 최종 URL로 다시 연결
            connection.setInstanceFollowRedirects(true);
            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "❌ MP3 다운로드 실패: 서버 응답 코드 " + responseCode);
                return;
            }

            // ✅ MP3 파일을 안정적으로 다운로드
            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream out = new FileOutputStream(mp3File)) {

                byte[] buffer = new byte[8192]; // 8KB 버퍼
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            // ✅ 파일 크기 검사 (정상 다운로드 여부 확인)
            if (mp3File.length() < 1024 * 10) { // 10KB 미만이면 파일이 손상된 것으로 간주
                Log.e(TAG, "❌ MP3 다운로드가 완료되지 않음: " + fileName);
                mp3File.delete(); // 손상된 파일 삭제
            } else {
                Log.d(TAG, "✅ MP3 다운로드 성공: " + fileName);
            }

            mp3DownloadCount++; // ✅ 성공한 MP3 개수 증가

            // 🔹 모든 MP3 다운로드 완료 시 `sendMp3DownloadCompleteBroadcast()` 호출
            if (mp3DownloadCount >= 10) {
                sendMp3DownloadCompleteBroadcast();
            }

        } catch (IOException e) {
            Log.e(TAG, "❌ MP3 다운로드 중 오류 발생: " + fileName, e);
        }
    }

    /**
     * 🔹 MP3 다운로드가 모두 완료되었을 때 브로드캐스트 전송
     */
    private void sendMp3DownloadCompleteBroadcast() {
        Log.d(TAG, "📢 모든 MP3 다운로드 완료, Broadcast 전송!");
        AmericaHeadlineCrawlingDone = 1;

        // ✅ 다운로드 완료 상태 저장
        SharedPreferences.Editor editor = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit();
        editor.putBoolean("download_completed", true);
        editor.putBoolean("script_download_completed", true);
        editor.apply();

        Intent intent = new Intent("com.example.myapp.MP3_DOWNLOAD_COMPLETED");
        sendBroadcast(intent);
    }














}