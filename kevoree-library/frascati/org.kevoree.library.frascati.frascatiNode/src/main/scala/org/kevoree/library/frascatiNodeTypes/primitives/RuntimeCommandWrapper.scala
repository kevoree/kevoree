package org.kevoree.library.frascatiNodeTypes.primitives

import org.kevoree.api.PrimitiveCommand
import actors.Actor
import org.kevoree.library.frascatiNodeTypes.{UndoContextCommand, ExecuteContextCommand}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/02/12
 * Time: 01:43
 */

case class RuntimeCommandWrapper(cmd : PrimitiveCommand, target : Actor) extends PrimitiveCommand {
  def execute(): Boolean = {
    (target !? ExecuteContextCommand(cmd)).asInstanceOf[Boolean]
  }

  def undo() {
    (target !? UndoContextCommand(cmd))
  }
}
