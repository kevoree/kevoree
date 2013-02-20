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
import org.slf4j.LoggerFactory
import org.kevoree.ContainerNode
import scala.collection.JavaConversions._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 21/11/11
 * Time: 16:12
 *
 * @author Erwan Daubert
 * @version 1.0
 */

case class KevsAddChildInterpreter(addChild: AddChildStatment) extends KevsAbstractInterpreter {
  val logger = LoggerFactory.getLogger(this.getClass)

  def interpret(context: KevsInterpreterContext): Boolean = {
    context.model.findByPath("nodes[" + addChild.childNodeName + "]", classOf[ContainerNode]) match {
      case null => logger.error("child node: {}\nThe node must already exist. Please check !", addChild.childNodeName); false
      case child => {
        context.model.findByPath("nodes[" + addChild.fatherNodeName + "]", classOf[ContainerNode]) match {
          case null => {
            logger.error("Unknown father name: {}\nThe node must already exist. Please check !", addChild.fatherNodeName)
            false
          }
          case father => {
            father.findByPath("hosts[" + child.getName + "]", classOf[ContainerNode]) match {
              case null => {
                context.model.getNodes.find(n => n.findByPath("hosts[" + child.getName + "]", classOf[ContainerNode]) != null) match {
                  case None => father.addHosts(child); true
                  case Some(f) => logger.error("The child {} has already a parent: {}", Array[AnyRef](child.getName, f.getName)); false
                }
              }
              case c => logger.warn("The node {} is already a child of {}", Array[AnyRef](child.getName, father.getName)); true
            }
          }
        }
      }
    }
  }
}