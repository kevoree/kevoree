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
    if (root_ == ACTIONS) {
      result_ = ACTIONS(builder_, 0);
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
  // ADD_STATEMENT | SET_STATEMENT | REMOVE_STATEMENT | BIND_STATEMENT | UNBIND_STATEMENT | ATTACH_STATEMENT | DETACH_STATEMENT | NAMESPACE_STATEMENNT | REPO_STATEMENNT | INCLUDE_STATEMENNT | MOVE_STATEMENT | NETWORK_STATEMENT | eof | newline | CRLF
  public static boolean ACTIONS(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ACTIONS")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<actions>");
    result_ = ADD_STATEMENT(builder_, level_ + 1);
    if (!result_) result_ = SET_STATEMENT(builder_, level_ + 1);
    if (!result_) result_ = REMOVE_STATEMENT(builder_, level_ + 1);
    if (!result_) result_ = BIND_STATEMENT(builder_, level_ + 1);
    if (!result_) result_ = UNBIND_STATEMENT(builder_, level_ + 1);
    if (!result_) result_ = ATTACH_STATEMENT(builder_, level_ + 1);
    if (!result_) result_ = DETACH_STATEMENT(builder_, level_ + 1);
    if (!result_) result_ = NAMESPACE_STATEMENNT(builder_, level_ + 1);
    if (!result_) result_ = REPO_STATEMENNT(builder_, level_ + 1);
    if (!result_) result_ = INCLUDE_STATEMENNT(builder_, level_ + 1);
    if (!result_) result_ = MOVE_STATEMENT(builder_, level_ + 1);
    if (!result_) result_ = NETWORK_STATEMENT(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, EOF);
    if (!result_) result_ = consumeToken(builder_, NEWLINE);
    if (!result_) result_ = consumeToken(builder_, CRLF);
    exit_section_(builder_, level_, marker_, ACTIONS, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ADD IDENT COMMA_SEP* COLON IDENT (SUB IDENT)?
  static boolean ADD_STATEMENT(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ADD_STATEMENT")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokens(builder_, 2, ADD, IDENT);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, ADD_STATEMENT_2(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeTokens(builder_, -1, COLON, IDENT)) && result_;
    result_ = pinned_ && ADD_STATEMENT_5(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, rule_start_parser_);
    return result_ || pinned_;
  }

  // COMMA_SEP*
  private static boolean ADD_STATEMENT_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ADD_STATEMENT_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!COMMA_SEP(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ADD_STATEMENT_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // (SUB IDENT)?
  private static boolean ADD_STATEMENT_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ADD_STATEMENT_5")) return false;
    ADD_STATEMENT_5_0(builder_, level_ + 1);
    return true;
  }

  // SUB IDENT
  private static boolean ADD_STATEMENT_5_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ADD_STATEMENT_5_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, SUB, IDENT);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ATTACH IDENT COMMA_SEP* IDENT
  static boolean ATTACH_STATEMENT(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ATTACH_STATEMENT")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokens(builder_, 2, ATTACH, IDENT);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, ATTACH_STATEMENT_2(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, IDENT) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, rule_start_parser_);
    return result_ || pinned_;
  }

  // COMMA_SEP*
  private static boolean ATTACH_STATEMENT_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ATTACH_STATEMENT_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!COMMA_SEP(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ATTACH_STATEMENT_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // BIND IDENT COMMA_SEP* IDENT
  static boolean BIND_STATEMENT(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BIND_STATEMENT")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokens(builder_, 2, BIND, IDENT);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, BIND_STATEMENT_2(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, IDENT) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, rule_start_parser_);
    return result_ || pinned_;
  }

  // COMMA_SEP*
  private static boolean BIND_STATEMENT_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BIND_STATEMENT_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!COMMA_SEP(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "BIND_STATEMENT_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // COLON IDENT
  static boolean COLON_SEP(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "COLON_SEP")) return false;
    if (!nextTokenIs(builder_, COLON)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, COLON, IDENT);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // COMMA IDENT
  static boolean COMMA_SEP(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "COMMA_SEP")) return false;
    if (!nextTokenIs(builder_, COMMA)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, COMMA, IDENT);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // DETACH IDENT COMMA_SEP* IDENT
  static boolean DETACH_STATEMENT(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "DETACH_STATEMENT")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokens(builder_, 2, DETACH, IDENT);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, DETACH_STATEMENT_2(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, IDENT) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, rule_start_parser_);
    return result_ || pinned_;
  }

  // COMMA_SEP*
  private static boolean DETACH_STATEMENT_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "DETACH_STATEMENT_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!COMMA_SEP(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "DETACH_STATEMENT_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // INCLUDE IDENT COLON_SEP*
  static boolean INCLUDE_STATEMENNT(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "INCLUDE_STATEMENNT")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokens(builder_, 2, INCLUDE, IDENT);
    pinned_ = result_; // pin = 2
    result_ = result_ && INCLUDE_STATEMENNT_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, rule_start_parser_);
    return result_ || pinned_;
  }

  // COLON_SEP*
  private static boolean INCLUDE_STATEMENNT_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "INCLUDE_STATEMENNT_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!COLON_SEP(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "INCLUDE_STATEMENNT_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // ACTIONS*
  static boolean KEVSCRIPT(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "KEVSCRIPT")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!ACTIONS(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "KEVSCRIPT", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // MOVE IDENT COMMA_SEP* IDENT
  static boolean MOVE_STATEMENT(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MOVE_STATEMENT")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokens(builder_, 2, MOVE, IDENT);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, MOVE_STATEMENT_2(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, IDENT) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, rule_start_parser_);
    return result_ || pinned_;
  }

  // COMMA_SEP*
  private static boolean MOVE_STATEMENT_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MOVE_STATEMENT_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!COMMA_SEP(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "MOVE_STATEMENT_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // NAMESPACE IDENT
  static boolean NAMESPACE_STATEMENNT(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NAMESPACE_STATEMENNT")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokens(builder_, 2, NAMESPACE, IDENT);
    exit_section_(builder_, level_, marker_, null, result_, false, rule_start_parser_);
    return result_;
  }

  /* ********************************************************** */
  // NETWORK IDENT IDENT
  static boolean NETWORK_STATEMENT(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NETWORK_STATEMENT")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokens(builder_, 2, NETWORK, IDENT, IDENT);
    pinned_ = result_; // pin = 2
    exit_section_(builder_, level_, marker_, null, result_, pinned_, rule_start_parser_);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // REMOVE IDENT COMMA_SEP*
  static boolean REMOVE_STATEMENT(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "REMOVE_STATEMENT")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokens(builder_, 2, REMOVE, IDENT);
    pinned_ = result_; // pin = 2
    result_ = result_ && REMOVE_STATEMENT_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, rule_start_parser_);
    return result_ || pinned_;
  }

  // COMMA_SEP*
  private static boolean REMOVE_STATEMENT_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "REMOVE_STATEMENT_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!COMMA_SEP(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "REMOVE_STATEMENT_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // REPO string
  static boolean REPO_STATEMENNT(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "REPO_STATEMENNT")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokens(builder_, 2, REPO, STRING);
    exit_section_(builder_, level_, marker_, null, result_, false, rule_start_parser_);
    return result_;
  }

  /* ********************************************************** */
  // SET IDENT (SUB IDENT)? EQ string
  static boolean SET_STATEMENT(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SET_STATEMENT")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokens(builder_, 2, SET, IDENT);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, SET_STATEMENT_2(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeTokens(builder_, -1, EQ, STRING)) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, rule_start_parser_);
    return result_ || pinned_;
  }

  // (SUB IDENT)?
  private static boolean SET_STATEMENT_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SET_STATEMENT_2")) return false;
    SET_STATEMENT_2_0(builder_, level_ + 1);
    return true;
  }

  // SUB IDENT
  private static boolean SET_STATEMENT_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SET_STATEMENT_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, SUB, IDENT);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // UNBIND IDENT COMMA_SEP* IDENT
  static boolean UNBIND_STATEMENT(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UNBIND_STATEMENT")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokens(builder_, 2, UNBIND, IDENT);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, UNBIND_STATEMENT_2(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, IDENT) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, rule_start_parser_);
    return result_ || pinned_;
  }

  // COMMA_SEP*
  private static boolean UNBIND_STATEMENT_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UNBIND_STATEMENT_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!COMMA_SEP(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "UNBIND_STATEMENT_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // !(ADD|REMOVE|BIND|UNBIND|ATTACH|DETACH|SET|NAMESPACE|REPO|INCLUDE|MOVE|NETWORK)
  static boolean rule_start(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rule_start")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !rule_start_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // ADD|REMOVE|BIND|UNBIND|ATTACH|DETACH|SET|NAMESPACE|REPO|INCLUDE|MOVE|NETWORK
  private static boolean rule_start_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rule_start_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ADD);
    if (!result_) result_ = consumeToken(builder_, REMOVE);
    if (!result_) result_ = consumeToken(builder_, BIND);
    if (!result_) result_ = consumeToken(builder_, UNBIND);
    if (!result_) result_ = consumeToken(builder_, ATTACH);
    if (!result_) result_ = consumeToken(builder_, DETACH);
    if (!result_) result_ = consumeToken(builder_, SET);
    if (!result_) result_ = consumeToken(builder_, NAMESPACE);
    if (!result_) result_ = consumeToken(builder_, REPO);
    if (!result_) result_ = consumeToken(builder_, INCLUDE);
    if (!result_) result_ = consumeToken(builder_, MOVE);
    if (!result_) result_ = consumeToken(builder_, NETWORK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  final static Parser rule_start_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return rule_start(builder_, level_ + 1);
    }
  };
}
