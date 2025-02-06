package com.example.myapp;

import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GrammarPractice  extends AppApplication implements View.OnTouchListener {

    private static final String TAG = "GrammarPractice";
    TextView grammar_practice_title = null;
    private ImageView grammar_practice_image = null;
    private String study_number = null;
    private int study_mode = 0;
    private Matrix matrix = new Matrix(); // 이미지 변환을 위한 Matrix 객체
    private Matrix savedMatrix = new Matrix();
    private PointF start = new PointF();
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = 0;

    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;  // 기본 스케일 값
    private float minScale = 0.5f;     // 최소 스케일 값
    private float maxScale = 3.0f;     // 최대 스케일 값
    private float baseScale = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_grammar_practice);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.grammar_practice_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeClass();

    } //onCreate()


    private void initializeClass() {
        Intent intent = getIntent();
        study_number = intent.getStringExtra("study_number");
        study_mode = intent.getIntExtra("study_mode", -1);

        Log.d(TAG, "study_number: " + study_number);
        Log.d(TAG, "study_mode: " + study_mode);

        grammar_practice_title = findViewById(R.id.grammar_test_page_title);
        grammar_practice_image = findViewById(R.id.grammar_practice_image);

        if (study_mode == 0) {
            grammar_practice_image.setVisibility(View.VISIBLE);
        } else if (study_mode == 1) {
            grammar_practice_image.setVisibility(View.GONE);
        }

        int resId = getResources().getIdentifier(study_number, "drawable", getPackageName());
        if (resId != 0) {
            grammar_practice_image.setImageResource(resId);
        } else {
            Log.e(TAG, "해당 이미지 리소스를 찾을 수 없음: " + study_number);
        }

        // 이미지 크기를 초기 화면에 맞게 조정
        grammar_practice_image.post(this::fitImageToScreen);
        grammar_practice_image.setOnTouchListener(this);

        // 핀치 줌을 위한 ScaleGestureDetector 초기화
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
    }

    private void fitImageToScreen() {
        Drawable drawable = grammar_practice_image.getDrawable();
        if (drawable == null) {
            return;
        }
        int imageWidth = drawable.getIntrinsicWidth();
        int imageHeight = drawable.getIntrinsicHeight();
        int viewWidth = grammar_practice_image.getWidth();
        int viewHeight = grammar_practice_image.getHeight();
        float scaleX = (float) viewWidth / imageWidth;
        float scaleY = (float) viewHeight / imageHeight;
        float scale = Math.min(scaleX, scaleY); // 화면에 맞게 조정
        float offsetX = (viewWidth - (imageWidth * scale)) / 2;
        float offsetY = (viewHeight - (imageHeight * scale)) / 2;

        baseScale = scale;
        scaleFactor = scale;

        matrix.set(new Matrix()); // 빈 Matrix 객체를 활용하여 최소한의 초기화
        matrix.postScale(scale, scale);
        matrix.postTranslate(offsetX, offsetY);
        grammar_practice_image.setImageMatrix(matrix);

        savedMatrix.set(matrix); // 초기 상태를 저장
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        scaleGestureDetector.onTouchEvent(event);  // 핀치 줌 감지

        ImageView view = (ImageView) v;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                savedMatrix.set(matrix);
                mode = ZOOM;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG && event.getPointerCount() == 1) {
                    float dx = event.getX() - start.x;
                    float dy = event.getY() - start.y;
                    matrix.set(savedMatrix);
                    matrix.postTranslate(dx, dy);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = 0;
                break;
        }
        view.setImageMatrix(matrix);
        return true;
    }

    // 핀치 줌 이벤트 처리 클래스
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = detector.getScaleFactor();
            float tempScaleFactor = scaleFactor * scale;

            // ✅ 스케일 범위 제한
            if (tempScaleFactor < minScale * baseScale) {
                scale = (minScale * baseScale) / scaleFactor;
            } else if (tempScaleFactor > maxScale * baseScale) {
                scale = (maxScale * baseScale) / scaleFactor;
            }

            scaleFactor *= scale;

            // ✅ 기존 변환 상태를 덮어쓰지 않고 확대/축소를 적용
            matrix.postScale(scale, scale, detector.getFocusX(), detector.getFocusY());
            grammar_practice_image.setImageMatrix(matrix);

            return true;
        }
    }



}