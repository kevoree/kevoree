package org.kevoree.tools.kevscript.idea.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.kevoree.tools.kevscript.idea.KevScriptLanguage;

/**
 * Created by duke on 17/01/2014.
 */
public class KevScriptElementType extends IElementType {

    public KevScriptElementType(@NotNull @NonNls String debugName) {
        super(debugName, KevScriptLanguage.INSTANCE);
    }



}
