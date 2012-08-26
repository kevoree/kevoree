package org.kevoree.tools.ui.editor

import command.Command
import java.util

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 26/08/12
 * Time: 19:13
 */
object UIEventHandler {

  def info(msg: String) {
    import scala.collection.JavaConversions._
    cmds.foreach(cmd => cmd.execute(msg))
  }

  private val cmds = new util.ArrayList[Command]()

  def addCommand(cmd: Command) {
    cmds.add(cmd)
  }

}
