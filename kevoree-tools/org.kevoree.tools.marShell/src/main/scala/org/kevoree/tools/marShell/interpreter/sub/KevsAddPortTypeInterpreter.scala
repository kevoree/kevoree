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

import org.kevoree.{TypeDefinition, PortType, ComponentType, KevoreeFactory}
import org.kevoree.tools.marShell.ast.{AddPortTypeStatment, CreateChannelTypeStatment}
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext

import org.slf4j.LoggerFactory

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 28/09/12
 * Time: 09:30
 */
case class KevsAddPortTypeInterpreter(self: AddPortTypeStatment) extends KevsAbstractInterpreter {

  var logger = LoggerFactory.getLogger(this.getClass)

  def interpret(context: KevsInterpreterContext): Boolean = {
    var success : Boolean = false
    context.model.getTypeDefinitions.filter(ct=> ct.isInstanceOf[ComponentType]).find(tdef => tdef.getName == self.componentTypeName) match {
      case Some(e) =>
      {
        val portTypeRef = KevoreeFactory.createPortTypeRef
        portTypeRef.setName(self.portTypeName)

        if (self.optional.isDefined){
          portTypeRef.setOptional(true)
        } else {
          portTypeRef.setOptional(false)
        }
        // add Provided and  Required
        self.in match {
          case true =>
            e.asInstanceOf[ComponentType].addProvided(portTypeRef)
          case false =>
            e.asInstanceOf[ComponentType].addRequired(portTypeRef)
        }
        // set manuel generics Class Message
        if(self.typeport == "Message"){ self.className = Some("org.kevoree.framework.MessagePort")}
        // find Class in model
        context.model.getTypeDefinitions.filter(ct => ct.isInstanceOf[PortType]).find(t => t.getName.trim == self.className.get ) match {
          case Some(p) =>
            portTypeRef.setRef(p.asInstanceOf[PortType])
            success = true
          case None =>
            logger.debug("The port service can't be associated with the interface => " + self.className+" is not found")
            val messagePortType = KevoreeFactory.eINSTANCE.createMessagePortType
            messagePortType.setName(self.className.toString)
            context.model.addTypeDefinitions(messagePortType)

            success = true

        }
      }
      case None => {
        logger.error("The componentTypeName does not exist with name => " + self.componentTypeName)
        success = false
      }
    }
    success
  }
}
