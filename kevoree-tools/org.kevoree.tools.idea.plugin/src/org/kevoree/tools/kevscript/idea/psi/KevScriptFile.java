package org.kevoree.tools.kevscript.idea.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;
import org.kevoree.tools.kevscript.idea.KevScriptLanguage;
import org.kevoree.tools.kevscript.idea.KevScriptLanguageType;

import javax.swing.*;

/**
 * Created by duke on 17/01/2014.
 */
public class KevScriptFile extends PsiFileBase {

    public KevScriptFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, KevScriptLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return KevScriptLanguageType.INSTANCE;
    }

    @Override
    public String toString() {
        return "KevScript File";
    }

    @Override
    public Icon getIcon(int flags) {
        return super.getIcon(flags);
    }

}
