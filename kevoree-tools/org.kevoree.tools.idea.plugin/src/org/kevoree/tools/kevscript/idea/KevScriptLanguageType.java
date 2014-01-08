package org.kevoree.tools.kevscript.idea;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by duke on 04/12/2013.
 */
public class KevScriptLanguageType extends LanguageFileType {

    public static final KevScriptLanguageType INSTANCE = new KevScriptLanguageType();

    protected KevScriptLanguageType() {
        super(KevScriptLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "KevScript";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "KevScript is a DSL dedicated to manipulate efficiency Model@Run.time";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "kevs";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
    }
}
