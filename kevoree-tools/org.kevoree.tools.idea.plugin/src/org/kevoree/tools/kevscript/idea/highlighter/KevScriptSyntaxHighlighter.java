package org.kevoree.tools.kevscript.idea.highlighter;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.codehaus.groovy.antlr.java.JavaTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.kevoree.tools.kevscript.idea.lexer.KevScriptLexer;
import org.kevoree.tools.kevscript.idea.psi.KevScriptTypes;
import java.awt.*;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/**
 * Created by duke on 18/01/2014.
 */
public class KevScriptSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey KEYWORD = createTextAttributesKey("KEVS_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey STRING = createTextAttributesKey("KEVS_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey SEPARATOR = createTextAttributesKey("KEVS_SEPARATOR", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey COMMENT = createTextAttributesKey("KEVS_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey IDENT = createTextAttributesKey("KEVS_IDENT", DefaultLanguageHighlighterColors.STATIC_METHOD);
    public static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey("KEVS_BAD_CHARACTER", new TextAttributes(Color.RED, null, null, null, Font.BOLD));

    private static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{BAD_CHARACTER};
    private static final TextAttributesKey[] KEYWORD_KEYS = new TextAttributesKey[]{KEYWORD};
    private static final TextAttributesKey[] IDENT_KEYS = new TextAttributesKey[]{IDENT};
    private static final TextAttributesKey[] STRING_KEYS = new TextAttributesKey[]{STRING};
    private static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{COMMENT};
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
    private static final TextAttributesKey[] SEPARATOR_KEYS = new TextAttributesKey[]{SEPARATOR};

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new FlexAdapter(new KevScriptLexer());
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        /* Entities OPERATIONS */
        if (tokenType.equals(KevScriptTypes.ADD)) {
            return KEYWORD_KEYS;
        }
        if (tokenType.equals(KevScriptTypes.REMOVE)) {
            return KEYWORD_KEYS;
        }
        if (tokenType.equals(KevScriptTypes.ATTACH)) {
            return KEYWORD_KEYS;
        }
        if (tokenType.equals(KevScriptTypes.DETACH)) {
            return KEYWORD_KEYS;
        }
        if (tokenType.equals(KevScriptTypes.BIND)) {
            return KEYWORD_KEYS;
        }
        if (tokenType.equals(KevScriptTypes.UNBIND)) {
            return KEYWORD_KEYS;
        }
        if (tokenType.equals(KevScriptTypes.SET)) {
            return KEYWORD_KEYS;
        }
        if (tokenType.equals(KevScriptTypes.MOVE)) {
            return KEYWORD_KEYS;
        }
        /* Model manipulation */
        if (tokenType.equals(KevScriptTypes.NAMESPACE)) {
            return KEYWORD_KEYS;
        }
        if (tokenType.equals(KevScriptTypes.NETWORK)) {
            return KEYWORD_KEYS;
        }
        if (tokenType.equals(KevScriptTypes.REPO)) {
            return KEYWORD_KEYS;
        }
        if (tokenType.equals(KevScriptTypes.INCLUDE)) {
            return KEYWORD_KEYS;
        }
        /* Separator */
        if (tokenType.equals(KevScriptTypes.COLON)) {
            return SEPARATOR_KEYS;
        }
        if (tokenType.equals(KevScriptTypes.COMMA)) {
            return SEPARATOR_KEYS;
        }
        if (tokenType.equals(KevScriptTypes.SUB)) {
            return SEPARATOR_KEYS;
        }

        /* Basic elem */
        if (tokenType.equals(KevScriptTypes.IDENT)) {
            return IDENT_KEYS;
        }
        if (tokenType.equals(KevScriptTypes.COMMENT)) {
            return COMMENT_KEYS;
        }
        if (tokenType.equals(KevScriptTypes.STRING)) {
            return STRING_KEYS;
        }
        if (tokenType.equals(TokenType.BAD_CHARACTER)) {
            return BAD_CHAR_KEYS;
        }
        return EMPTY_KEYS;

    }

}
