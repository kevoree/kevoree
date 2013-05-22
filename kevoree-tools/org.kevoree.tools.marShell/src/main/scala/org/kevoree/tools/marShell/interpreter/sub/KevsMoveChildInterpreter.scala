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

import org.kevoree.tools.marShell.interpreter.{KevsInterpreterContext, KevsAbstractInterpreter}
import org.kevoree.tools.marShell.ast.MoveChildStatment
import org.kevoree.ContainerNode
import org.kevoree.log.Log

case class KevsMoveChildInterpreter(moveChild: MoveChildStatment) extends KevsAbstractInterpreter {

  def interpret(context: KevsInterpreterContext): Boolean = {
    context.model.findByPath("nodes[" + moveChild.childNodeName + "]", classOf[ContainerNode]) match {
      case null => {
        context.appendInterpretationError("Could not move Child node '"+moveChild.childNodeName+"' from '"+moveChild.oldFatherNodeName+"' to '"+moveChild.fatherNodeName+"'. Child node not found.")
        //logger.error("Unknown child name: {}\nThe node must already exist. Please check !", moveChild.childNodeName)
        false
      }
      case child:ContainerNode => {
        context.model.findByPath("nodes[" + moveChild.oldFatherNodeName + "]", classOf[ContainerNode]) match {
          case null => {
            context.appendInterpretationError("Could not move Child node '"+moveChild.childNodeName+"' from '"+moveChild.oldFatherNodeName+"' to '"+moveChild.fatherNodeName+"'. Source parent not found.")
            //logger.error("Unknown old father node: {}\nThe node must already exist. Please check !", moveChild.oldFatherNodeName)
            false
          }
          case oldFather:ContainerNode => {
            if (!oldFather.getHosts.contains(child)) {
              Log.warn("The child node is not already contained by a father. Please prefer the addChild command!")
            }
            context.model.findByPath("nodes[" + moveChild.fatherNodeName + "]", classOf[ContainerNode]) match {
              case null => {
                context.appendInterpretationError("Could not move Child node '"+moveChild.childNodeName+"' from '"+moveChild.oldFatherNodeName+"' to '"+moveChild.fatherNodeName+"'. Target parent node not found.")
                //logger.error("Unknown father node: {}\nThe node must already exist. Please check !", moveChild.fatherNodeName)
                false
              }
              case father:ContainerNode => {
                oldFather.removeHosts(child)
                father.addHosts(child)
                true
              }
            }
          }
        }
      }
    }
  }
}