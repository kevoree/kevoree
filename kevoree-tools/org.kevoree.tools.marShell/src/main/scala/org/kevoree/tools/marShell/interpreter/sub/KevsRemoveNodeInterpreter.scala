/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
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

import org.kevoree.KevoreeFactory
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import scala.collection.JavaConversions._
import org.kevoree.tools.marShell.ast.{RemoveNodeStatment, AddNodeStatment}
import org.slf4j.LoggerFactory

case class KevsRemoveNodeInterpreter(addN: RemoveNodeStatment) extends KevsAbstractInterpreter {

  var logger = LoggerFactory.getLogger(this.getClass)

  def interpret(context: KevsInterpreterContext): Boolean = {

    context.model.getNodes.find(n => n.getName == addN.nodeName) match {
      case Some(targetNode) => {
        //DELETE ALL GROUP DEPENDENCY
         context.model.getGroups.foreach{g=>
             if(g.getSubNodes.contains(targetNode)){
               g.getSubNodes.remove(targetNode)
             }
         }
        //DELETE ALL COMPONENT
        (targetNode.getComponents.toList++ List()).foreach(c => {
          KevsRemoveComponentInstanceInterpreter(null).deleteComponent(targetNode, c)
        })
        //DELETE NODE
        context.model.getNodes.remove(targetNode)
        true
      }
      case None => {
        logger.error("Node Already existe"); false
      }
    }
  }

}
