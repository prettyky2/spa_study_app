package com.example.myapp;

import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;

import androidx.appcompat.app.AppCompatActivity;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;
import android.view.View;
import android.content.Intent;

public class AppApplication extends AppCompatActivity {
    private AudioManager audioManager;
    private MediaSessionCompat mediaSession;

    @Override
    protected void onStart() {
        super.onStart();
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
        if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
            onNextPressed();
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
            onPreviousPressed();
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