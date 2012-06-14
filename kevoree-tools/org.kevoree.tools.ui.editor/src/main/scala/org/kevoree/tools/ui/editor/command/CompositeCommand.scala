package org.kevoree.tools.ui.editor.command


/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 14/06/12
 * Time: 23:25
 */

class CompositeCommand extends Command {

  private val cmds: scala.collection.mutable.HashSet[Command] = scala.collection.mutable.HashSet[Command]()

  def addCommand(c: Command) {
    cmds.add(c)
  }

  def execute(p: Any) {
    cmds.foreach{ c =>
      c.execute(p)
    }
  }
}
