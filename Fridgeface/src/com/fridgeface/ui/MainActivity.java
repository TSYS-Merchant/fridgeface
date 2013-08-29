
package com.fridgeface.ui;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.fridgeface.R;
import com.fridgeface.constants.IntentExtras;
import com.fridgeface.utils.LogHelper;
import com.fridgeface.utils.SpeechHelper;
import com.fridgeface.utils.SystemUiHider;
import com.fridgeface.views.FaceView;
import com.fridgeface.webserver.ServerRequestHandler;
import com.fridgeface.webserver.SocketListenerService;

/**
 * An example full-screen activity that shows and hides the system UI (i.e. status bar and
 * navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainActivity extends Activity {
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final boolean TOGGLE_ON_CLICK = true;
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
    private static final int RESULT_TTS_CHECK = 1;

    private Intent mServerIntent;
    private SystemUiHider mSystemUiHider;

    private FaceView mFaceView;
    private BroadcastReceiver mReceiver;

    private SpeechHelper mSpeechHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        mServerIntent = new Intent(this, SocketListenerService.class)
                .putExtra(SocketListenerService.EXTRA_REQUEST_HANDLER, ServerRequestHandler.class)
                .putExtra(SocketListenerService.EXTRA_PORT, 1234)
                .putExtra(SocketListenerService.EXTRA_NOTIFICATION,
                        new Notification.Builder(this)
                                .setContentTitle("Webserver running")
                                .setSmallIcon(R.drawable.ic_launcher)
                                .getNotification()
                );

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        mFaceView = (FaceView)findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, mFaceView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    private int mControlsHeight;
                    private int mShortAnimTime;

                    @Override
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        mFaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra(IntentExtras.EXTRA_MOOD)) {
                    float mood = intent.getFloatExtra(IntentExtras.EXTRA_MOOD, 0);
                    mFaceView.setMood(Math.min(1f, Math.max(-1f, mood)));
                }
            }
        };

        // setup TTS
        checkTts();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(mServerIntent.putExtra(SocketListenerService.EXTRA_COMMAND,
                SocketListenerService.COMMAND_START_SERVER));
        registerReceiver(mReceiver, new IntentFilter(IntentExtras.ACTION_POKE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        startService(mServerIntent.putExtra(SocketListenerService.EXTRA_COMMAND,
                SocketListenerService.COMMAND_STOP_SERVER));
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroy() {
        mSpeechHelper.shutDown();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                mFaceView.setMood(Math.min(mFaceView.getMood() + .1f, 1f));
                return true;

            case KeyEvent.KEYCODE_VOLUME_DOWN:
                mFaceView.setMood(Math.max(mFaceView.getMood() - .1f, -1f));
                return true;

            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the system UI. This is to prevent
     * the jarring behavior of controls going away while interacting with activity UI.
     */
    private View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private Handler mHideHandler = new Handler();
    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Launch intent to query TTS engine for voice availability
     */
    private void checkTts() {
        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, RESULT_TTS_CHECK);
    }

    /**
     * Receive result from TTS engine query and init engine if voices installed, otherwise launch play
     * store to download
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_TTS_CHECK) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                initTts();
            } else {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(intent);
                finish();
            }
        }
    }

    /**
     * Init TTS engine and setup utterance callback
     */
    private void initTts() {
        mSpeechHelper = new SpeechHelper(this, new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                LogHelper.print("Utterance onStart");
                // START speech animation
            }

            @Override
            public void onError(String utteranceId) {
                LogHelper.print("Utterance onError");
                // STOP speech animation
            }

            @Override
            public void onDone(String utteranceId) {
                LogHelper.print("Utterance onDone");
                // STOP speech animation
            }
        });
    }
}
