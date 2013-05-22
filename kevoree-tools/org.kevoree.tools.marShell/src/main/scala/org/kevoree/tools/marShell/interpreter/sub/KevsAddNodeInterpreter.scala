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

import org.kevoree.{ContainerNode, NodeType, TypeDefinition}
import org.kevoree.tools.marShell.ast.AddNodeStatment
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.interpreter.utils.Merger
import org.kevoree.log.Log

case class KevsAddNodeInterpreter(addN: AddNodeStatment) extends KevsAbstractInterpreter {


  def interpret(context: KevsInterpreterContext): Boolean = {
    context.model.findByPath("typeDefinitions[" + addN.nodeTypeName + "]", classOf[TypeDefinition]) match {
      case null => {
        context.appendInterpretationError("Could add node '"+addN.nodeTypeName+"' of type '"+addN.nodeTypeName+"'. NodeType not found.")
        false
      }
      case nodeType =>
        if (!nodeType.isInstanceOf[NodeType]) {
          context.appendInterpretationError("Could add node '"+addN.nodeTypeName+"' of type '"+addN.nodeTypeName+"'. Type of the new node is not a NodeType: '"+nodeType.getClass.getName+"'.")
          false
        } else {
          context.model.findByPath("nodes[" + addN.nodeName + "]", classOf[ContainerNode]) match {
            case e:ContainerNode => {
              Log.warn("Node Already exist with name {}", e.getName)
              if (e.getTypeDefinition == null) {
                e.setTypeDefinition(nodeType)
                Merger.mergeDictionary(e, addN.props, null)
                true
              } else {
                if (e.getTypeDefinition.getName == addN.nodeTypeName) {
                  Merger.mergeDictionary(e, addN.props, null)
                  true
                } else {
                  context.appendInterpretationError("Could add node '"+addN.nodeName+"' of type '"+addN.nodeTypeName+"'. A node already exists with the same name, but with a different type: '"+e.getTypeDefinition.getName+"'.")
                  false
                }
              }
            }
            case null => {
              val newnode = context.kevoreeFactory.createContainerNode
              newnode.setName(addN.nodeName)
              newnode.setTypeDefinition(nodeType)
              Merger.mergeDictionary(newnode, addN.props, null)
              context.model.addNodes(newnode)
              true
            }
          }
        }
    }
  }
}
