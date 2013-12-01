/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.tools.marShell.interpreter.sub

import org.kevoree.tools.marShell.ast.UpdateDictionaryStatement
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.interpreter.utils.Merger

import org.kevoree._
import scala.collection.JavaConversions._
import org.kevoree.log.Log

case class KevsUpdateDictionaryInterpreter(statement: UpdateDictionaryStatement) extends KevsAbstractInterpreter {

  def interpret(context: KevsInterpreterContext): Boolean = {

    var targetInstance: List[Instance] = List()
    statement.nodeName match {
      case Some(nodeID) => {
        //SEARCH NODE
        var nodes: List[ContainerNode] = List()
        if (nodeID == "*") {
          nodes = context.model.getNodes.toList
        } else {
          val option = context.model.findByPath("nodes[" + nodeID + "]", classOf[ContainerNode])
          if (option == null) {
            nodes = List()
          } else {
            nodes = List(option)
          }

        }
        nodes.foreach {
          targetNode =>
            if (statement.instanceName == "*") {
              targetInstance = targetInstance ++ targetNode.getComponents.toList
            } else {
              val option = context.model.findByPath("nodes[" + nodeID + "]/components[" + statement.instanceName + "]", classOf[ComponentInstance])
              if (option != null) {
                targetInstance = targetInstance ++ List(option)
              }
            }
        }
      }
      case None => {
        if (statement.instanceName == "*") {
          targetInstance = targetInstance ++ context.model.getHubs.toList ++ context.model.getGroups.toList ++ context.model.getNodes
        } else {
          var option : Instance = context.model.findByPath("hubs[" + statement.instanceName + "]", classOf[Channel])
          if (option != null) {
            targetInstance = targetInstance ++ List(option)
          }
          option = context.model.findByPath("groups[" + statement.instanceName + "]", classOf[Group])
          if (option != null) {
            targetInstance = targetInstance ++ List(option)
          }
          option = context.model.findByPath("nodes[" + statement.instanceName + "]", classOf[ContainerNode])
          if (option != null) {
            targetInstance = targetInstance ++ List(option)
          }
        }
      }
    }

    val errorDetected = false

    targetInstance.foreach {
      instance =>
        Merger.mergeFragmentDictionary(instance, statement.fraProperties)
    }

    if (targetInstance.isEmpty) {
      Log.debug("Warning : No dictionary merged")
    }

    !errorDetected // ALWAYS RETURN TRUE
    //TODO BETTER ERROR OR AMBIGUITY MANAGEMENT

  }
}
