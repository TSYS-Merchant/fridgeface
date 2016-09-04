
package com.fridgeface.utils;

import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.fridgeface.R;

public class SpeechHelper implements TextToSpeech.OnInitListener {
    private static final int MAX_TTS_CHARS = 140;

    private Context mContext;
    private TextToSpeech mTts;
    private HashMap<String, String> mSpeechParams;
    private HashMap<String, String> mSpeechPaddingParams;

    public enum Language {
        US_ENGLISH(Locale.US),
        UK_ENGLISH(Locale.UK),
        FRENCH(Locale.FRANCE),
        GERMAN(Locale.GERMANY),
        ITALIAN(Locale.ITALY),
        MEXICAN_SPANISH(new Locale("es", "MX"));

        public Locale key;

        private Language(Locale key) {
            this.key = key;
        }
    }

    public enum Pitch {
        HIGHEST(2.0f),
        HIGHER(1.6f),
        HIGH(1.3f),
        NORMAL(1.0f),
        LOW(0.7f),
        LOWER(0.5f),
        LOWEST(0.3f);

        public float key;

        private Pitch(float key) {
            this.key = key;
        }
    }

    public enum Rate {
        FASTEST(1.6f),
        FASTER(1.4f),
        FAST(1.2f),
        NORMAL(1.0f),
        SLOW(0.85f),
        SLOWER(0.7f),
        SLOWEST(0.1f);

        public float key;

        private Rate(float key) {
            this.key = key;
        }
    }

    public SpeechHelper(Context context, UtteranceProgressListener listener) {
        mContext = context;
        mTts = new TextToSpeech(context, this);
        mTts.setOnUtteranceProgressListener(listener);

        mSpeechParams = new HashMap<String, String>();
        mSpeechParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "fridgeface");
        mSpeechParams.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                String.valueOf(AudioManager.STREAM_MUSIC));

        mSpeechPaddingParams = new HashMap<String, String>();
        mSpeechPaddingParams.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "0");
        mSpeechPaddingParams.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                String.valueOf(AudioManager.STREAM_MUSIC));
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = mTts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                LogHelper.print("This Language is not supported");
            } else {
                say(mContext.getResources().getString(R.string.tts_startup));
            }
        } else {
            LogHelper.print("Initilization Failed!");
        }
    }

    public void setLanguage(Language language) {
        mTts.setLanguage(language.key);
    }

    public void setPitch(Pitch pitch) {
        mTts.setPitch(pitch.key);
    }

    public void setRate(Rate rate) {
        mTts.setSpeechRate(rate.key);
    }

    public void say(String text) {
        if (text.length() > MAX_TTS_CHARS) {
            text = text.substring(0, MAX_TTS_CHARS);
        }
        mTts.speak("blah blah blah blah", TextToSpeech.QUEUE_FLUSH, mSpeechPaddingParams);
        mTts.speak(text, TextToSpeech.QUEUE_ADD, mSpeechParams);
    }

    public void shutup() {
        mTts.stop();
    }

    public void shutDown() {
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
    }
}
