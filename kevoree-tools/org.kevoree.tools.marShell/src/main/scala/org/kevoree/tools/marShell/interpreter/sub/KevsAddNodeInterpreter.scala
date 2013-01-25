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

import org.kevoree.{ContainerNode, NodeType, TypeDefinition, KevoreeFactory}
import org.kevoree.tools.marShell.ast.AddNodeStatment
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext

import org.kevoree.tools.marShell.interpreter.utils.Merger
import org.slf4j.LoggerFactory

case class KevsAddNodeInterpreter(addN: AddNodeStatment) extends KevsAbstractInterpreter {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def interpret(context: KevsInterpreterContext): Boolean = {
    context.model.findByQuery("typeDefinitions[" + addN.nodeTypeName + "]", classOf[TypeDefinition]) match {
      case None => logger.error("Node Type not found for name " + addN.nodeTypeName); false
      case Some(nodeType) =>
        if (!nodeType.isInstanceOf[NodeType]) {
          logger.error("The type with name {} is not a NodeType", addN.nodeTypeName)
          false
        } else {
          context.model.findByQuery("nodes[" + addN.nodeName + "]", classOf[ContainerNode]) match {
            case Some(e) => {
              logger.warn("Node Already exist with name {}", e.getName)
              if (e.getTypeDefinition == null) {
                e.setTypeDefinition(nodeType)
                Merger.mergeDictionary(e, addN.props, None)
                true
              } else {
                if (e.getTypeDefinition.getName == addN.nodeTypeName) {
                  Merger.mergeDictionary(e, addN.props, None)
                  true
                } else {
                  logger.error("Type != from previous created node")
                  false
                }
              }
            }
            case None => {
              val newnode = KevoreeFactory.$instance.createContainerNode
              newnode.setName(addN.nodeName)
              newnode.setTypeDefinition(nodeType)
              Merger.mergeDictionary(newnode, addN.props, None)
              context.model.addNodes(newnode)
              true
            }
          }
        }
    }
  }
}
