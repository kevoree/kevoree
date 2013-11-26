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

import org.kevoree.tools.marShell.ast.AddChildStatment
import org.kevoree.tools.marShell.interpreter.{KevsInterpreterContext, KevsAbstractInterpreter}
import org.kevoree.ContainerNode
import scala.collection.JavaConversions._
import org.kevoree.log.Log

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 21/11/11
 * Time: 16:12
 *
 * @author Erwan Daubert
 * @version 1.0
 */

case class KevsAddChildInterpreter(addChild: AddChildStatment) extends KevsAbstractInterpreter {

  def interpret(context: KevsInterpreterContext): Boolean = {
    context.model.findByPath("nodes[" + addChild.childNodeName + "]", classOf[ContainerNode]) match {
      case null => {
        context.appendInterpretationError("Could not add child node '"+addChild.childNodeName+"' to parent '"+addChild.fatherNodeName+"'. Child Node not found.")
        false
      }
      case child => {
        context.model.findByPath("nodes[" + addChild.fatherNodeName + "]", classOf[ContainerNode]) match {
          case null => {
            context.appendInterpretationError("Could not add child node '"+addChild.childNodeName+"' to parent '"+addChild.fatherNodeName+"'. Parent Node not found.")
            false
          }
          case father => {
            father.findByPath("hosts[" + child.getName + "]", classOf[ContainerNode]) match {
              case null => {
                context.model.getNodes.find(n => n.findByPath("hosts[" + child.getName + "]", classOf[ContainerNode]) != null) match {
                  case None => father.addHosts(child); true
                  case Some(f) => {
                    context.appendInterpretationError("Could not add child node '"+addChild.childNodeName+"' to parent '"+addChild.fatherNodeName+"'. Child Node already has a parent. Consider using MoveComponentInstance instead.")
                    //logger.error("The child {} has already a parent: {}", Array[AnyRef](child.getName, f.getName))
                    false
                  }
                }
              }
              case c => Log.warn("The node {} is already a child of {}", child.getName, father.getName); true
            }
          }
        }
      }
    }
  }
}