package org.kevoree.tools.kevscript.idea;

import com.intellij.lang.Language;

/**
 * Created by duke on 04/12/2013.
 */
public class KevScriptLanguage extends Language {

    public static final KevScriptLanguage INSTANCE = new KevScriptLanguage();

    private KevScriptLanguage() {
        super("KevScript");
    }
}
