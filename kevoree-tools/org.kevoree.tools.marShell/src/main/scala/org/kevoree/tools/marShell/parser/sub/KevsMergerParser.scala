package org.kevoree.tools.marShell.parser.sub

import util.parsing.combinator.Parsers.Parser
import org.kevoree.tools.marShell.parser.sub.KevsAbstractParser._
import util.parsing.combinator.Parsers.~._
import org.kevoree.tools.marShell.ast.AddBindingStatment._
import org.kevoree.tools.marShell.ast.ComponentInstanceID._
import org.kevoree.tools.marShell.ast.{MergeStatement, Statment}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/12/11
 * Time: 14:51
 * To change this template use File | Settings | File Templates.
 */

trait KevsMergerParser extends KevsAbstractParser {

  val mergeCommandFormat = "merge <url>"
  def parseMerge : Parser[List[Statment]] = "merge" ~ orFailure(stringLit,mergeCommandFormat) ^^{ case _ ~ url =>
      List(MergeStatement(url))
  }

}