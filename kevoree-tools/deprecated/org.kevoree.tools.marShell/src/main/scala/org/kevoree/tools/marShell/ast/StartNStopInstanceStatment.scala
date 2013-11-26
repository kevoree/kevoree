package org.kevoree.tools.marShell.ast

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 25/06/13
 * Time: 15:52
 *
 * @author Erwan Daubert
 * @version 1.0
 */
case class StartNStopInstanceStatment(instanceName: String, parentNodeName: Option[String], start: Boolean) extends Statment {
  def getTextualForm: String = {
    var form = ""
    if (start) {
      form = "startInstance "
    } else {
      form = "stopInstance "
    }
    form = form + instanceName
    if (parentNodeName.isDefined) {
      form = form + "@" + parentNodeName
    }
    form
  }
}
