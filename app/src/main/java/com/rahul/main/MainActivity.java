package com.rahul.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.rahul.customcameraview.CustomCameraLayout;

public class MainActivity extends AppCompatActivity implements CustomCameraLayout.CaptureCallback {

    private CustomCameraLayout customCameraLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        customCameraLayout = findViewById(R.id.customCameraLayout);

        customCameraLayout.init(this, this, new OverlayView(this));
        customCameraLayout.getCaptureButton().setText("set capture text");


    }

    @Override
    public void onFullScreenBitmap(Bitmap bitmap) {
        Log.e("TAG", "onFullScreenBitmap: " + bitmap.toString());
    }


    public class OverlayView extends View {
        private Paint paint;
        private float rectTop;
        private float rectBottom;
        private float rectLeft;
        private float rectRight;

        public OverlayView(Context context) {
            super(context);
            init();
        }

        private void init() {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5f);

            float rectHeight = 200f;
            rectBottom = rectHeight;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            float centerY = h / 2f;
            rectTop = centerY - (rectBottom / 2);
            rectBottom = centerY + (rectBottom / 2);
            rectRight = w - rectLeft;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint);
        }
    }
}