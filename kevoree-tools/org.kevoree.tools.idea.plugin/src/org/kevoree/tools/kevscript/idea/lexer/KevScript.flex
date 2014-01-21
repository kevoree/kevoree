package org.kevoree.tools.kevscript.idea.lexer;
import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import static org.kevoree.tools.kevscript.idea.psi.KevScriptTypes.*;

%%

%{
  public KevScriptLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class KevScriptLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL="\r"|"\n"|"\r\n"
LINE_WS=[\ \t\f]
WHITE_SPACE=({LINE_WS}|{EOL})+

NEWLINE=\n\t
COMMENT="//".*
IDENT=[\*\.a-zA-Z0-9_\-]+
STRING=('([^'\\]|\\.)*'|\"([^\"\\]|\\.)*\")

%%
<YYINITIAL> {
  {WHITE_SPACE}      { return com.intellij.psi.TokenType.WHITE_SPACE; }

  "add"              { return ADD; }
  "remove"           { return REMOVE; }
  "bind"             { return BIND; }
  "unbind"           { return UNBIND; }
  "attach"           { return ATTACH; }
  "detach"           { return DETACH; }
  "namespace"        { return NAMESPACE; }
  "set"              { return SET; }
  "repo"             { return REPO; }
  "include"          { return INCLUDE; }
  "move"             { return MOVE; }
  "network"          { return NETWORK; }
  ":"                { return COLON; }
  ","                { return COMMA; }
  "/"                { return SUB; }
  "="                { return EQ; }
  "<<EOF>>"          { return EOF; }

  {NEWLINE}          { return NEWLINE; }
  {COMMENT}          { return COMMENT; }
  {IDENT}            { return IDENT; }
  {STRING}           { return STRING; }

  [^] { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}
