package org.kevoree.tools.marShell.interpreter.sub

import org.kevoree.tools.marShell.interpreter.{KevsInterpreterContext, KevsAbstractInterpreter}
import org.kevoree.tools.marShell.ast.StartNStopInstanceStatment
import org.kevoree.log.Log

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 25/06/13
 * Time: 15:52
 *
 * @author Erwan Daubert
 * @version 1.0
 */
case class KevsStartNStopInstanceInterpreter(statement: StartNStopInstanceStatment) extends KevsAbstractInterpreter {
  def interpret(context: KevsInterpreterContext): Boolean = {
    if (statement.parentNodeName.isDefined) {
      // find child node and parent node
      val parentNode = context.model.findNodesByID(statement.parentNodeName.get)
      if (parentNode != null) {
        // check if instance name (as a node) is hosted by parent node
        val childNode = parentNode.findHostsByID(statement.instanceName)
        if (childNode != null) {
          // check if child node is already started
          if (childNode.getStarted && !statement.start) {
            // modify started property of child node
            childNode.setStarted(false)
            true
          } else if (!childNode.getStarted && statement.start) {
            // modify started property of child node
            childNode.setStarted(true)
            true
          } else {
            Log.debug("The instance element (" + statement.instanceName + ") is already started/stopped")
            true
          }
        } else {
          // check if instance name (as a node) is hosted by parent node
          val component = parentNode.findComponentsByID(statement.instanceName)
          if (component != null) {
            // check if component is already started
            if (component.getStarted && !statement.start) {
              // modify started property of component
              component.setStarted(false)
              true
            } else if (!component.getStarted && statement.start) {
              // modify started property of component
              component.setStarted(true)
              true
            } else {
              Log.debug("The instance element (" + statement.instanceName + ") is already started/stopped")
              true
            }
          } else {
            context.appendInterpretationError("The instance (" + statement.instanceName + ") is not hosted (or not exist) in parent node (" + statement.parentNodeName + ").")
            false
          }
        }
      } else {
        context.appendInterpretationError("The parent node (" + statement.parentNodeName + ") doesn't exist.")
        false
      }
    } else {
      val channel = context.model.findHubsByID(statement.instanceName)
      if (channel != null) {
        // check if channel is already started
        if (channel.getStarted && !statement.start) {
          // modify started property of channel
          channel.setStarted(false)
          true
        } else if (!channel.getStarted && statement.start) {
          // modify started property of channel
          channel.setStarted(true)
          true
        } else {
          Log.debug("The instance element (" + statement.instanceName + ") is already started/stopped")
          true
        }
      } else {
        val group = context.model.findGroupsByID(statement.instanceName)
        if (group != null) {
          // check if group is already started
          if (group.getStarted && !statement.start) {
            // modify started property of group
            group.setStarted(false)
            true
          } else if (!group.getStarted && statement.start) {
            // modify started property of group
            group.setStarted(true)
            true
          } else {
            Log.debug("The instance element (" + statement.instanceName + ") is already started/stopped")
            true
          }
        } else {
          // check if instance name (as a node) is hosted by parent node
          val node = context.model.findNodesByID(statement.instanceName)
          if (node != null) {
            // check if child node is already started
            if (node.getStarted && !statement.start) {
              // modify started property of node
              node.setStarted(false)
              true
            } else if (!node.getStarted && statement.start) {
              // modify started property of node
              node.setStarted(true)
              true
            } else {
              Log.debug("The instance element (" + statement.instanceName + ") is already started/stopped")
              true
            }
          } else {
            context.appendInterpretationError("The instance (" + statement.parentNodeName + ") doesn't exist.")
            false
          }
        }
      }
    }
  }
}
