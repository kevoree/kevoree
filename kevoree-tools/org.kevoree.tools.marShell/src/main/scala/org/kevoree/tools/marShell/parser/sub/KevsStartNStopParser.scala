package org.kevoree.tools.marShell.parser.sub

import org.kevoree.tools.marShell.ast._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 25/06/13
 * Time: 15:48
 *
 * @author Erwan Daubert
 * @version 1.0
 */
trait KevsStartNStopParser extends KevsAbstractParser with KevsPropertiesParser {
  val startInstanceCommandFormat = "startInstance <nodeName> [ , <nodeName> ] [@ <parentNodeName>]"

  def parseStartChild: Parser[List[Statment]] =
    "startInstance" ~ orFailure(rep1sep(ident, ","), startInstanceCommandFormat) ~ opt(parseParentName) ^^ {
      case _ ~ instanceIds ~ parentNodeName =>
        var res: List[Statment] = List()
        instanceIds.foreach {
          instanceId =>
            res = res ++ List(StartNStopInstanceStatment(instanceId, parentNodeName, true))
        }
        res
    }

  val stopInstanceCommandFormat = "stopInstance <instanceName> [ , <instanceName> ] [@ <parentNodeName>]"

  def parseStopChild: Parser[List[Statment]] =
    "stopInstance" ~ orFailure(rep1sep(ident, ","), stopInstanceCommandFormat) ~ opt(parseParentName) ^^ {
      case _ ~ instanceIds ~ parentNodeName =>
        var res: List[Statment] = List()
        instanceIds.foreach {
          instanceId =>
            res = res ++ List(StartNStopInstanceStatment(instanceId, parentNodeName, false))
        }
        res
    }

  def parseParentName: Parser[String] = "@" ~ ident ^^ {
    case _ ~ parentName => parentName
  }
}
