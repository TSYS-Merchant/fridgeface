
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
    private Runnable mSpeakingRunnable;

    private float mMood = 0f;
    private boolean mSpeaking = false;
    private boolean mMouthOpen = false;
    private boolean mBlink = false;

    private Paint mPaintEyes;
    private Paint mPaintMouth;
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

        mSpeakingRunnable = new Runnable() {
            @Override
            public void run() {
                mMouthOpen = (!mMouthOpen) && mSpeaking;
                invalidate();

                if (mSpeaking) {
                    mHandler.postDelayed(this, 80 + new Random().nextInt(150));
                } else {
                    mHandler.removeCallbacks(this);
                }
            }
        };

        mPaintEyes = new Paint();
        mPaintEyes.setColor(Color.BLACK);
        mPaintEyes.setStyle(Style.FILL_AND_STROKE);
        mPaintEyes.setStrokeWidth(30);
        mPaintEyes.setStrokeCap(Paint.Cap.ROUND);

        mPaintMouth = new Paint();
        mPaintMouth.setColor(Color.BLACK);
        mPaintMouth.setStyle(Style.STROKE);
        mPaintMouth.setStrokeWidth(30);
        mPaintMouth.setStrokeCap(Paint.Cap.ROUND);
        mOval = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int h = canvas.getHeight();
        int w = canvas.getWidth();
        int smallestDim = Math.min(w, h);

        if (!mBlink) {
            canvas.drawCircle(w / 4, h / 4, smallestDim / 6, mPaintEyes);
            canvas.drawCircle(w / 4 * 3, h / 4, smallestDim / 6, mPaintEyes);
        } else {
            canvas.drawLine(w / 4 - smallestDim / 6, h / 4, w / 4 + smallestDim / 6, h / 4,
                    mPaintEyes);
            canvas.drawLine(w / 4 * 3 - smallestDim / 6, h / 4, w / 4 * 3 + smallestDim / 6, h / 4,
                    mPaintEyes);
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
        if (mMouthOpen) {
            mPaintMouth.setStyle(Style.FILL_AND_STROKE);
        } else {
            mPaintMouth.setStyle(Style.STROKE);
        }

        canvas.drawArc(mOval, startAngle, 180, false, mPaintMouth);
    }

    public void setMood(final float mood) {
        float difference = Math.abs(mMood - mood);
        float change = .06f;
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

    public void setSpeaking(boolean speaking) {
        mSpeaking = speaking;
        mHandler.removeCallbacks(mSpeakingRunnable);
        mHandler.post(mSpeakingRunnable);
    }

    public boolean isSpeaking() {
        return mSpeaking;
    }

    public void blink() {
        mBlink = true;
        invalidate();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBlink = false;
                invalidate();
            }
        }, 100);
    }
}
