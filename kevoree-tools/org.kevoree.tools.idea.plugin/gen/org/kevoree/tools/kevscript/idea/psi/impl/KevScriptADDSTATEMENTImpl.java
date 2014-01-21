// This is a generated file. Not intended for manual editing.
package org.kevoree.tools.kevscript.idea.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.kevoree.tools.kevscript.idea.psi.KevScriptTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.kevoree.tools.kevscript.idea.psi.*;

public class KevScriptADDSTATEMENTImpl extends ASTWrapperPsiElement implements KevScriptADDSTATEMENT {

  public KevScriptADDSTATEMENTImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof KevScriptVisitor) ((KevScriptVisitor)visitor).visitADDSTATEMENT(this);
    else super.accept(visitor);
  }

}
