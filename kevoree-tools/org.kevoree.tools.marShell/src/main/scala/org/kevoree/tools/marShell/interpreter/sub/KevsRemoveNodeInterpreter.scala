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

package org.kevoree.tools.marShell.interpreter.sub

import org.kevoree.ContainerNode
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext

import org.kevoree.tools.marShell.ast.RemoveNodeStatment
import org.slf4j.LoggerFactory

case class KevsRemoveNodeInterpreter(addN: RemoveNodeStatment) extends KevsAbstractInterpreter {

  var logger = LoggerFactory.getLogger(this.getClass)

  def removeNode(targetNode: ContainerNode, context: KevsInterpreterContext): Boolean = {
    //DELETE ALL GROUP DEPENDENCY
    context.model.getGroups.foreach {
      g =>
        if (g.getSubNodes.contains(targetNode)) {
          g.removeSubNodes(targetNode)
        }
    }

    //DELETE ALL COMPONENT
    targetNode.getComponents.toList.foreach(c => {
      KevsRemoveComponentInstanceInterpreter(null).deleteComponent(targetNode, c)
    })

    //REMOVE FROM NETWORK LINK
    context.model.getNodeNetworks.foreach {
      nn =>
        if (nn.getTarget.getName == addN.nodeName) {
          context.model.removeNodeNetworks(nn)
        } else {
          nn.getInitBy.map {
            initNode =>
              if (initNode.getName == addN.nodeName) {
                context.model.removeNodeNetworks(nn)
              }
          }
        }
    }

    //CLEANUP HOST NODE
    if (targetNode.getHost.isDefined) {
      targetNode.getHost.get.removeHosts(targetNode)
    }


    //CLEANUP DICTIONARY
    context.model.getHubs.foreach {
      inst =>
        inst.getDictionary.map {
          dico =>
            dico.getValues.filter(v => v.getTargetNode.isDefined && v.getTargetNode.get.getName == addN.nodeName).foreach {
              value =>
                dico.removeValues(value)
            }
        }
    }
    context.model.getGroups.foreach {
      inst =>
        inst.getDictionary.map {
          dico =>
            dico.getValues.filter(v => v.getTargetNode.isDefined && v.getTargetNode.get.getName == addN.nodeName).foreach {
              value =>
                dico.removeValues(value)
            }
        }
    }

    // DELETE hosted nodes
    targetNode.getHosts.foreach {
      childNode =>
        KevsRemoveNodeInterpreter(null).removeNode(childNode, context)
    }

    //DELETE NODE
    context.model.removeNodes(targetNode)
    true
  }

  def interpret(context: KevsInterpreterContext): Boolean = {

    context.model.findByQuery("nodes[" + addN.nodeName + "]", classOf[ContainerNode]) match {
      case Some(targetNode) => {
        removeNode(targetNode, context)
      }
      case None => {
        logger.error("Node Already existe")
        false
      }
    }
  }

}
