// This is a generated file. Not intended for manual editing.
package org.kevoree.tools.kevscript.idea.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.openapi.diagnostic.Logger;
import static org.kevoree.tools.kevscript.idea.psi.KevScriptTypes.*;
import static org.kevoree.tools.kevscript.idea.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class KevScriptParser implements PsiParser {

  public static final Logger LOG_ = Logger.getInstance("org.kevoree.tools.kevscript.idea.parser.KevScriptParser");

  public ASTNode parse(IElementType root_, PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, null);
    Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    if (root_ == ADD_STATEMENT) {
      result_ = ADD_STATEMENT(builder_, 0);
    }
    else {
      result_ = parse_root_(root_, builder_, 0);
    }
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
    return builder_.getTreeBuilt();
  }

  protected boolean parse_root_(final IElementType root_, final PsiBuilder builder_, final int level_) {
    return KEVSCRIPT(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // ADD IDENT (COMMA IDENT)*
  public static boolean ADD_STATEMENT(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ADD_STATEMENT")) return false;
    if (!nextTokenIs(builder_, ADD)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, ADD, IDENT);
    result_ = result_ && ADD_STATEMENT_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, ADD_STATEMENT, result_);
    return result_;
  }

  // (COMMA IDENT)*
  private static boolean ADD_STATEMENT_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ADD_STATEMENT_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!ADD_STATEMENT_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ADD_STATEMENT_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // COMMA IDENT
  private static boolean ADD_STATEMENT_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ADD_STATEMENT_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, COMMA, IDENT);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // (CRLF|COMMENT|ADD_STATEMENT)*
  static boolean KEVSCRIPT(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "KEVSCRIPT")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!KEVSCRIPT_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "KEVSCRIPT", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // CRLF|COMMENT|ADD_STATEMENT
  private static boolean KEVSCRIPT_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "KEVSCRIPT_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CRLF);
    if (!result_) result_ = consumeToken(builder_, COMMENT);
    if (!result_) result_ = ADD_STATEMENT(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

}
