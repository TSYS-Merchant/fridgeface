
package com.fridgeface.utils;

import java.util.HashMap;
import java.util.Locale;

public class CensorHelper {
    private HashMap<String, String> mReplacementMap;

    public CensorHelper() {
        mReplacementMap = new HashMap<>();

        // Doin' it Moronie style
        mReplacementMap.put("shit", "shtie");
        mReplacementMap.put("bastard", "bastage");
        mReplacementMap.put("bitch", "batch");
        mReplacementMap.put("ball", "bell");
        mReplacementMap.put("ass", "ice");
        mReplacementMap.put("fuck", "farg");
    }

    public String cleanUp(String phrase) {
        phrase = phrase.toLowerCase(Locale.US);
        for (String key : mReplacementMap.keySet()) {
            phrase = phrase.replaceAll(key, mReplacementMap.get(key));
        }

        return phrase;
    }
}
