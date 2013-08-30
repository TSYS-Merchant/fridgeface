
package com.fridgeface.utils;

import java.util.HashMap;

public class CensorHelper {
    private HashMap<String, String> mReplacementMap;

    public CensorHelper() {
        mReplacementMap = new HashMap<String, String>();

        // Doin' it Moronie style
        mReplacementMap.put("ball", "bell");
        mReplacementMap.put("ass", "ice");
        mReplacementMap.put("fuck", "farg");
    }

    public String cleanUp(String phrase) {
        for (String key : mReplacementMap.keySet()) {
            phrase = phrase.replaceAll(key, mReplacementMap.get(key));
        }

        return phrase;
    }
}
