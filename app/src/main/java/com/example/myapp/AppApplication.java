package com.example.myapp;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.content.Intent;

public class AppApplication extends AppCompatActivity {
    private AudioManager audioManager;
    private MediaSessionCompat mediaSession;
    private static Activity currentActivity;

    // 현재 실행 중인 Activity 반환
    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

            @Override
            public void onActivityStarted(Activity activity) {}

            @Override
            public void onActivityResumed(Activity activity) {
                currentActivity = activity; // 현재 실행 중인 Activity 저장
            }

            @Override
            public void onActivityPaused(Activity activity) {
                currentActivity = null; // Activity가 일시 중지되면 null 처리
            }

            @Override
            public void onActivityStopped(Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

            @Override
            public void onActivityDestroyed(Activity activity) {}
        });


        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // 🔹 MediaSessionCompat 설정 (미디어 버튼 감지)
        mediaSession = new MediaSessionCompat(this, "MediaSession");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMediaButtonReceiver(null);

// 🔹 미디어 버튼 감지 리스너 설정
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                KeyEvent event = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                    int keyCode = event.getKeyCode();
                    handleMediaButtonPress(keyCode);
                    return true;
                }
                return super.onMediaButtonEvent(mediaButtonIntent);
            }
        });

        mediaSession.setActive(true);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaSession != null) {
            mediaSession.release();
        }
    }

    private void handleMediaButtonPress(int keyCode) {
        Activity activity = AppApplication.getCurrentActivity(); // 현재 실행 중인 Activity 가져오기
        if (activity instanceof DailyStudyPractice) { // 🔹 DailyStudyPractice인지 확인
            DailyStudyPractice practiceActivity = (DailyStudyPractice) activity;
            if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
                Log.d("AppApplication", "Next button pressed in DailyStudyPractice");
                practiceActivity.onNextPressed();
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
                Log.d("AppApplication", "Previous button pressed in DailyStudyPractice");
                practiceActivity.onPreviousPressed();
            }
        } else if (activity instanceof DailyWordPractice) { // 🔹 DailyWordPractice인지 확인
            DailyWordPractice wordPracticeActivity = (DailyWordPractice) activity;
            if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
                Log.d("AppApplication", "Next button pressed in DailyWordPractice");
                wordPracticeActivity.onNextPressed();
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
                Log.d("AppApplication", "Previous button pressed in DailyWordPractice");
                wordPracticeActivity.onPreviousPressed();
            }
        } else {
            Log.e("AppApplication", "handleMediaButtonPress() 호출됨, 하지만 현재 Activity가 DailyStudyPractice나 DailyWordPractice가 아님");
        }
    }

    // 🔹 다음 버튼(⏭️) 눌렀을 때 실행할 기능
    protected void onNextPressed() {
        // 이 메서드는 BaseActivity를 상속하는 Activity에서 오버라이드해서 구현 가능!
    }

    // 🔹 이전 버튼(⏮️) 눌렀을 때 실행할 기능
    protected void onPreviousPressed() {
        // 이 메서드는 BaseActivity를 상속하는 Activity에서 오버라이드해서 구현 가능!
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE,
                    AudioManager.FLAG_SHOW_UI
            );
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_SHOW_UI
            );
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }



}