package org.kevoree.tools.kevscript.idea.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import org.kevoree.tools.kevscript.idea.psi.KevScriptTypes;
import com.intellij.psi.TokenType;

%%

%public
%class KevScriptLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}
CRLF= \n|\r|\r\n
WHITE_SPACE=[\ \t\f]
END_OF_LINE_COMMENT=("#"|"!")[^\r\n]*
IDENTIFIER=[^:=\ \n\r\t\f\\] | "\\"{CRLF} | "\\".
%state WAITING_VALUE

%%

{CRLF}                                                     { return KevScriptTypes.CRLF; }

<YYINITIAL> {
  /* keywords */
  "add"                          { return KevScriptTypes.ADD; }
  ","                            { return KevScriptTypes.COMMA; }
  /* identifiers */
  {IDENTIFIER}                   { return KevScriptTypes.IDENT; }
}

<WAITING_VALUE> {END_OF_LINE_COMMENT}                           { yybegin(WAITING_VALUE); return KevScriptTypes.COMMENT; }

<WAITING_VALUE> {CRLF}                                     { yybegin(WAITING_VALUE); return KevScriptTypes.CRLF; }

<WAITING_VALUE> {WHITE_SPACE}+                              { yybegin(WAITING_VALUE); return TokenType.WHITE_SPACE; }

{WHITE_SPACE}+                                              { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }

.                                                           { }