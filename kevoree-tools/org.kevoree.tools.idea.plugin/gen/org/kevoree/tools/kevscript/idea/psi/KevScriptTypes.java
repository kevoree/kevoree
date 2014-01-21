// This is a generated file. Not intended for manual editing.
package org.kevoree.tools.kevscript.idea.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.kevoree.tools.kevscript.idea.psi.impl.*;

public interface KevScriptTypes {

  IElementType ADD_STATEMENT = new KevScriptElementType("ADD_STATEMENT");

  IElementType ADD = new KevScriptTokenType("ADD");
  IElementType COMMA = new KevScriptTokenType("COMMA");
  IElementType COMMENT = new KevScriptTokenType("COMMENT");
  IElementType CRLF = new KevScriptTokenType("CRLF");
  IElementType IDENT = new KevScriptTokenType("IDENT");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == ADD_STATEMENT) {
        return new KevScriptADDSTATEMENTImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
