
package com.fridgeface.views;

import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

public class FaceView extends View {
    private Handler mHandler;
    private Runnable mMoodRunnable;

    private float mMood = 0f;
    private boolean mBlink = false;

    private Paint mPaintFilled;
    private Paint mPaintStroke;
    private RectF mOval;

    public FaceView(Context context) {
        super(context);
        init();
    }

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                blink();
                mHandler.postDelayed(this, new Random().nextInt(2000) + 5000);
            }
        }, 5000);

        mPaintFilled = new Paint();
        mPaintFilled.setColor(Color.BLACK);
        mPaintFilled.setStyle(Style.FILL);

        mPaintStroke = new Paint();
        mPaintStroke.setColor(Color.BLACK);
        mPaintStroke.setStyle(Style.STROKE);
        mPaintStroke.setStrokeWidth(30);
        mPaintStroke.setStrokeCap(Paint.Cap.ROUND);
        mOval = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int h = canvas.getHeight();
        int w = canvas.getWidth();
        int smallestDim = Math.min(w, h);

        if (!mBlink) {
            canvas.drawCircle(w / 4, h / 4, smallestDim / 6, mPaintFilled);
            canvas.drawCircle(w / 4 * 3, h / 4, smallestDim / 6, mPaintFilled);
        } else {
            canvas.drawLine(w / 4 - smallestDim / 6, h / 4, w / 4 + smallestDim / 6, h / 4,
                    mPaintStroke);
            canvas.drawLine(w / 4 * 3 - smallestDim / 6, h / 4, w / 4 * 3 + smallestDim / 6, h / 4,
                    mPaintStroke);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBlink = false;
                    invalidate();
                }
            }, 100);
        }

        float mouthHeight = h / 5;
        final float startAngle, mouthTop;
        if (mMood >= .5) {
            startAngle = 0;
            mouthTop = (1f - mMood) * mouthHeight;
        } else {
            startAngle = 180;
            mouthTop = mMood * mouthHeight;
        }
        mOval.set(w / 5, mouthHeight * 3 + mouthTop, w / 5 * 4, mouthHeight * 4 - mouthTop);

        canvas.drawArc(mOval, startAngle, 180, false, mPaintStroke);
    }

    public void setMood(final float mood) {
        float difference = Math.abs(mMood - mood);
        float change = .04f;
        if (difference <= change) {
            mMood = mood;
        } else {
            if (mMood > mood) {
                change *= -1;
            }
            mMood += change;
            if (mMoodRunnable != null) {
                mHandler.removeCallbacks(mMoodRunnable, null);
            }
            mMoodRunnable = new Runnable() {
                @Override
                public void run() {
                    setMood(mood);
                }
            };
            mHandler.postDelayed(mMoodRunnable, 20); // 20 ms == 50 fps
        }

        invalidate();
    }

    public float getMood() {
        return mMood;
    }

    public void blink() {
        mBlink = true;
        invalidate();
    }
}
