package org.kevoree.tools.kevscript.idea;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * Created by duke on 04/12/2013.
 */
public class KevScriptSyntaxHighlighter extends SyntaxHighlighterBase {
    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return null;
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType iElementType) {
        return new TextAttributesKey[0];
    }
}
