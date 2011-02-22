
package org.kevoree.library.text2speech.android;

import android.speech.tts.TextToSpeech;
import java.util.Locale;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.android.framework.service.KevoreeAndroidService;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.osgi.framework.Bundle;

/**
 *
 * @author cdiehlwa
 */
@Library(name = "EnTiMid-android")
@Provides({
    @ProvidedPort(name = "text", type = PortType.MESSAGE)
})
@DictionaryType({
    @DictionaryAttribute(name = "lang", defaultValue = "fr", optional = true)
})
@ComponentType
public class androidtts extends AbstractComponentType implements TextToSpeech.OnInitListener {

    KevoreeAndroidService uiService = null;
    Object bundle;
    private TextToSpeech mTts;
    private Boolean ttsReady = false;
    private Locale lang = getLocaleFromString("fr");

    @Start
    public void start() {

        updateFromDictionnary();

        bundle = this.getDictionary().get("osgi.bundle");
        uiService = UIServiceHandler.getUIService((Bundle) bundle);

        //create the TTS instance
        // The OnInitListener (second argument) is called after initialization completes.
        mTts = new TextToSpeech(uiService.getRootActivity(), this);
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
    }

    @Port(name = "text")
    public void triggerText(final Object textMsg) {
        if (ttsReady) {
            mTts.speak(textMsg.toString(),
                    TextToSpeech.QUEUE_FLUSH, // Drop all pending entries in the playback queue.
                    null);
            System.out.println("Saying " + textMsg.toString());
        }
        // if tts is not ready drop the message
    }

    // Implements TextToSpeech.OnInitListener.
    public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            ttsReady = true;
            setTtsLanguage(lang);
        } else {
            ttsReady = false;
            System.out.println(">>>>>>>>>>>>>>>>> Could not initialize TextToSpeech..<<<<<<<<<<<<<<<<<");
        }
    }

    private void setTtsLanguage(Locale lang) {
    // no return code : don't care if lang is not an available language
        int result = mTts.setLanguage(lang);
        //int result = mTts.setLanguage(Locale.US);
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            System.out.println(">>>>>>>>>>>>>> Language data not available.<<<<<<<<<<<<<<<<<<<<<");
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
