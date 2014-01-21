// This is a generated file. Not intended for manual editing.
package org.kevoree.tools.kevscript.idea.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.kevoree.tools.kevscript.idea.psi.impl.*;

public interface KevScriptTypes {

  IElementType ACTIONS = new KevScriptElementType("ACTIONS");

  IElementType ADD = new KevScriptTokenType("add");
  IElementType ATTACH = new KevScriptTokenType("attach");
  IElementType BIND = new KevScriptTokenType("bind");
  IElementType COLON = new KevScriptTokenType(":");
  IElementType COMMA = new KevScriptTokenType(",");
  IElementType COMMENT = new KevScriptTokenType("comment");
  IElementType CRLF = new KevScriptTokenType("CRLF");
  IElementType DETACH = new KevScriptTokenType("detach");
  IElementType EOF = new KevScriptTokenType("<<EOF>>");
  IElementType EQ = new KevScriptTokenType("=");
  IElementType IDENT = new KevScriptTokenType("IDENT");
  IElementType INCLUDE = new KevScriptTokenType("include");
  IElementType MOVE = new KevScriptTokenType("move");
  IElementType NAMESPACE = new KevScriptTokenType("namespace");
  IElementType NETWORK = new KevScriptTokenType("network");
  IElementType NEWLINE = new KevScriptTokenType("newline");
  IElementType REMOVE = new KevScriptTokenType("remove");
  IElementType REPO = new KevScriptTokenType("repo");
  IElementType SET = new KevScriptTokenType("set");
  IElementType STRING = new KevScriptTokenType("string");
  IElementType SUB = new KevScriptTokenType("/");
  IElementType UNBIND = new KevScriptTokenType("unbind");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == ACTIONS) {
        return new KevScriptACTIONSImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
