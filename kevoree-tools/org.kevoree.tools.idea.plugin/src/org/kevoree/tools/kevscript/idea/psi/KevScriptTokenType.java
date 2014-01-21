package org.kevoree.tools.kevscript.idea.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.kevoree.tools.kevscript.idea.KevScriptLanguage;

/**
 * Created by duke on 17/01/2014.
 */
public class KevScriptTokenType extends IElementType {

    public KevScriptTokenType(@NotNull @NonNls String debugName) {
        super(debugName, KevScriptLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "KevScriptTokenType." + super.toString();
    }

}
