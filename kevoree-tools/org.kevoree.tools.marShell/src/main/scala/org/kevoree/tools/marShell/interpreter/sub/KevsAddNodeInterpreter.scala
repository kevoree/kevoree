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
import org.kevoree.NodeType
import org.kevoree.tools.marShell.ast.AddNodeStatment
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import scala.collection.JavaConversions._
import org.kevoree.tools.marShell.interpreter.utils.Merger
import org.slf4j.LoggerFactory

case class KevsAddNodeInterpreter(addN: AddNodeStatment) extends KevsAbstractInterpreter {

  var logger = LoggerFactory.getLogger(this.getClass)

  def interpret(context: KevsInterpreterContext): Boolean = {

    context.model.getTypeDefinitions.find(p => p.getName == addN.nodeTypeName && p.isInstanceOf[NodeType]) match {
      case None => logger.error("Node Type not found for name " + addN.nodeTypeName); false
      case Some(nodeType) => {
        context.model.getNodes.find(n => n.getName == addN.nodeName) match {
          case Some(e) => {
            logger.warn("Node Already exist with name " + e.getName)
            if (e.getTypeDefinition == null) {
              context.model.getTypeDefinitions.find(td => td.getName == addN.nodeTypeName) match {
                case Some(td) => {
                  e.setTypeDefinition(td)
                  Merger.mergeDictionary(e, addN.props)
                  true
                }
                case None => {
                  logger.error("Type definition not found "+addN.nodeTypeName)
                  false
                }
              }

            } else {
              if (e.getTypeDefinition.getName == addN.nodeTypeName) {
                Merger.mergeDictionary(e, addN.props)
                true
              } else {
                logger.error("Type != from previous created node")
                false
              }
            }


          }
          case None => {
            val newnode = KevoreeFactory.eINSTANCE.createContainerNode
            newnode.setName(addN.nodeName)
            newnode.setTypeDefinition(nodeType)

            Merger.mergeDictionary(newnode, addN.props)

            context.model.getNodes.add(newnode)
            true
          }
        }
      }
    }
  }
}
