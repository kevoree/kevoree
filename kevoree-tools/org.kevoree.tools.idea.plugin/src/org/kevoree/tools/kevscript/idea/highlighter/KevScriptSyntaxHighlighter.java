package org.kevoree.tools.kevscript.idea.highlighter;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.kevoree.tools.kevscript.idea.lexer.KevScriptLexer;
import org.kevoree.tools.kevscript.idea.parser.KevScriptParser;
import org.kevoree.tools.kevscript.idea.psi.KevScriptTypes;
import java.awt.*;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/**
 * Created by duke on 18/01/2014.
 */
public class KevScriptSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey OPERATOR = createTextAttributesKey("KEVS_OPERATOR", SyntaxHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey KEYWORD = createTextAttributesKey("KEVS_KEYWORD", SyntaxHighlighterColors.KEYWORD);
    public static final TextAttributesKey VALUE = createTextAttributesKey("KEVS_VALUE", SyntaxHighlighterColors.STRING);
    public static final TextAttributesKey COMMENT = createTextAttributesKey("KEVS_COMMENT", SyntaxHighlighterColors.LINE_COMMENT);
    static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey("KEVS_BAD_CHARACTER", new TextAttributes(Color.RED, null, null, null, Font.BOLD));

    private static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{BAD_CHARACTER};
    private static final TextAttributesKey[] OPERATOR_KEYS = new TextAttributesKey[]{OPERATOR};
    private static final TextAttributesKey[] KEYWORD_KEYS = new TextAttributesKey[]{KEYWORD};
    private static final TextAttributesKey[] VALUE_KEYS = new TextAttributesKey[]{VALUE};
    private static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{COMMENT};
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new FlexAdapter(new KevScriptLexer());
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if (tokenType.equals(KevScriptTypes.ADD)) {
            return OPERATOR_KEYS;
        } else if (tokenType.equals(KevScriptTypes.ADD)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(KevScriptTypes.IDENT)) {
            return VALUE_KEYS;
        } else if (tokenType.equals(KevScriptTypes.COMMENT)) {
            return COMMENT_KEYS;
        } else if (tokenType.equals(TokenType.BAD_CHARACTER)) {
            return BAD_CHAR_KEYS;
        } else {
            return EMPTY_KEYS;
        }
    }

}
