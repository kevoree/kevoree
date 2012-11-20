package org.daum.library.sensors;

import android.content.pm.ActivityInfo;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.android.framework.service.KevoreeAndroidService;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 01/10/12
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */
@Library(name = "Android")
@Provides({
        @ProvidedPort(name = "text", type = PortType.MESSAGE)
})
@DictionaryType({
        @DictionaryAttribute(name = "lang", defaultValue = "en", optional = true)
})
@ComponentType
public class Androidtts extends AbstractComponentType {

    KevoreeAndroidService uiService = null;
    private TextToSpeech mTts;
    private Boolean ttsReady = false;
    private Locale lang = getLocaleFromString("en");

    @Start
    public void start() {
        UIServiceHandler.getUIService().getRootActivity().setRequestedOrientation ( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        updateFromDictionnary();

        // The OnInitListener (second argument) is called after initialization completes.
        mTts = new TextToSpeech(UIServiceHandler.getUIService().getRootActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    ttsReady = true;
                    setTtsLanguage(lang);
                } else {
                    ttsReady = false;

                    Log.e("TTS", "Could not initialize TextToSpeech." + status);
                }
            }
        });
    }

    @Stop
    public void stop() {
        ttsReady = false;
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
    }

    @Update
    public void update() {
        updateFromDictionnary();
        setTtsLanguage(lang);
        mTts.shutdown();
    }

    @Port(name = "text")
    public void triggerText(final Object textMsg) {
        try {
            mTts.speak(textMsg.toString(),
                    TextToSpeech.QUEUE_FLUSH, // Drop all pending entries in the playback queue.
                    null);
        }   catch (Exception e){
            e.printStackTrace();

        }
    }

    private void setTtsLanguage(Locale lang) {
        // no return code : don't care if lang is not an available language
        if (mTts.isLanguageAvailable(lang) == TextToSpeech.LANG_AVAILABLE) {
            int result = mTts.setLanguage(lang);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                System.out.println("Language data not available.");
            }
        }

    }

    private void updateFromDictionnary() {
        Object o;
        if ((o = getDictionary().get("lang")) != null) {
            lang = getLocaleFromString(o.toString());
        }
    }

    /**
     * Convert a string based locale into a Locale Object.
     * Assumes the string has form "{language}_{country}_{variant}".
     * Examples: "en", "de_DE", "_GB", "en_US_WIN", "de__POSIX", "fr_MAC"
     *
     * @param localeString The String
     * @return the Locale
     */
    private static Locale getLocaleFromString(String localeString) {
        if (localeString == null) {
            return null;
        }
        localeString = localeString.trim();
        if (localeString.toLowerCase().equals("default")) {
            return Locale.getDefault();
        }

        // Extract language
        int languageIndex = localeString.indexOf('_');
        String language = null;
        if (languageIndex == -1) {
            // No further "_" so is "{language}" only
            return new Locale(localeString, "");
        } else {
            language = localeString.substring(0, languageIndex);
        }

        // Extract country
        int countryIndex = localeString.indexOf('_', languageIndex + 1);
        String country = null;
        if (countryIndex == -1) {
            // No further "_" so is "{language}_{country}"
            country = localeString.substring(languageIndex + 1);
            return new Locale(language, country);
        } else {
            // Assume all remaining is the variant so is "{language}_{country}_{variant}"
            country = localeString.substring(languageIndex + 1, countryIndex);
            String variant = localeString.substring(countryIndex + 1);
            return new Locale(language, country, variant);
        }
    }
}
