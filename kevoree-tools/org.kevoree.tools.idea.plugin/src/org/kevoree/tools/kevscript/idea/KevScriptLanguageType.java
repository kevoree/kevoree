package org.kevoree.tools.kevscript.idea;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
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

    @NonNls
    public static final String DEFAULT_EXTENSION = "kevs";

    @NotNull
    @Override
    public String getDescription() {
        return "KevScript is a DSL dedicated to manipulate efficiency Model@Run.time";
    }

    @NotNull
    @NonNls
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return KevIcons.KEVS_ICON_16x16;
    }

    @Override
    public String getCharset(@NotNull VirtualFile file, byte[] content) {
        return CharsetToolkit.UTF8;
    }

    public boolean isJVMDebuggingSupported() {
        return false;
    }

}
