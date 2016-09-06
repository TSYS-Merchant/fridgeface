
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
import android.speech.tts.UtteranceProgressListener;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.fridgeface.utils.LogHelper;
import com.fridgeface.utils.SpeechHelper;

public class FaceView extends View {
    private static final String TAG = FaceView.class.getName();
    private Handler mHandler;
    private Runnable mMoodRunnable;
    private Runnable mSpeakingRunnable;

    private float mMood = 0f;
    private boolean mSpeaking = false;
    private boolean mMouthOpen = false;
    private boolean mBlink = false;
    private boolean mLeftEyePoked = false;
    private boolean mRightEyePoked = false;

    private float mLeftEyeCenterX;
    private float mLeftEyeCenterY;
    private float mRightEyeCenterX;
    private float mRightEyeCenterY;
    private float mEyeRadius;

    private Paint mPaintEyes;
    private Paint mPaintMouth;
    private RectF mOval;

    private SpeechHelper mSpeechHelper;

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
        mSpeechHelper = new SpeechHelper(getContext(), new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                setSpeaking(true);
            }

            @Override
            public void onDone(String utteranceId) {
                setSpeaking(false);
            }

            @Override
            public void onError(String utteranceId) {
                setSpeaking(false);
            }
        });
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
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getRawX();
            float touchY = event.getRawY();
            if (touchY >= (mLeftEyeCenterY - mEyeRadius) && touchY <= (mLeftEyeCenterY + mEyeRadius)) {
                if (touchX >= (mLeftEyeCenterX - mEyeRadius) && touchX <= (mLeftEyeCenterX + mEyeRadius)) {
                    LogHelper.d(TAG, "You poked Fridgeface in the left eye. How rude!");
                    mLeftEyePoked = true;
                    setMood(-1f);
                    mSpeechHelper.say("Ouch");
                }
            }

            if (touchY >= (mRightEyeCenterY - mEyeRadius) && touchY <= (mRightEyeCenterY + mEyeRadius)) {
                if (touchX >= (mRightEyeCenterX - mEyeRadius) && touchX <= (mRightEyeCenterX + mEyeRadius)) {
                    LogHelper.d(TAG, "You poked Fridgeface in the right eye. How rude!");
                    mRightEyePoked = true;
                    setMood(-1f);
                    mSpeechHelper.say("Ouch");
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mLeftEyePoked) {
                mLeftEyePoked = false;
                setMood(0f);
            }

            if (mRightEyePoked) {
                mRightEyePoked = false;
                setMood(0f);
            }
        }
        invalidate();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int h = canvas.getHeight();
        int w = canvas.getWidth();
        int smallestDim = Math.min(w, h);

        if (!mBlink) {
            mEyeRadius = smallestDim / 6;
            if (mLeftEyePoked) {
                drawLeftEyeClosed(canvas, h, w, smallestDim);
        } else {
                drawLeftEyeOpen(canvas, h, w);
            }

            if (mRightEyePoked) {
                drawRightEyeClosed(canvas, h, w, smallestDim);
            } else {
                drawRightEyeOpen(canvas, h, w);
            }
        } else {
            drawLeftEyeClosed(canvas, h, w, smallestDim);
            drawRightEyeClosed(canvas, h, w, smallestDim);
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

    private void drawLeftEyeClosed(Canvas canvas, int h, int w, int smallestDim) {
        float leftEyeClosedCenterX = w / 4 - smallestDim / 6;
        float leftEyeClosedCenterY = h / 4;
        float leftEyeClosedStopX = w / 4 + smallestDim / 6;
        float leftEyeClosedStopY = h / 4;
        canvas.drawLine(leftEyeClosedCenterX, leftEyeClosedCenterY, leftEyeClosedStopX, leftEyeClosedStopY,
                mPaintEyes);
    }

    private void drawRightEyeClosed(Canvas canvas, int h, int w, int smallestDim) {
        float rightEyeClosedCenterX = w / 4 * 3 - smallestDim / 6;
        float rightEyeClosedCenterY = h / 4;
        float rightEyeClosedStopX = w / 4 * 3 + smallestDim / 6;
        float rightEyeClosedStopY = h / 4;
        canvas.drawLine(rightEyeClosedCenterX, rightEyeClosedCenterY, rightEyeClosedStopX, rightEyeClosedStopY,
                mPaintEyes);
    }

    private void drawRightEyeOpen(Canvas canvas, int h, int w) {
        mRightEyeCenterX = w / 4 * 3;
        mRightEyeCenterY = h / 4;
        canvas.drawCircle(mRightEyeCenterX, mRightEyeCenterY, mEyeRadius, mPaintEyes);
    }

    private void drawLeftEyeOpen(Canvas canvas, int h, int w) {
        mLeftEyeCenterX = w / 4;
        mLeftEyeCenterY = h / 4;
        canvas.drawCircle(mLeftEyeCenterX, mLeftEyeCenterY, mEyeRadius, mPaintEyes);
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
