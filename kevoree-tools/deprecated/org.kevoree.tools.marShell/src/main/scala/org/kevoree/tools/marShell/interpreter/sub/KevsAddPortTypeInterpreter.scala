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

import org.kevoree.{TypeDefinition, PortType, ComponentType}
import org.kevoree.tools.marShell.ast.AddPortTypeStatment
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.log.Log

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 28/09/12
 * Time: 09:30
 */
case class KevsAddPortTypeInterpreter(self: AddPortTypeStatment) extends KevsAbstractInterpreter {

  def interpret(context: KevsInterpreterContext): Boolean = {
    var success: Boolean = false
    context.model.findByPath("typeDefinitions[" + self.componentTypeName + "]", classOf[TypeDefinition]) match {
      case e:TypeDefinition => {
        if (!e.isInstanceOf[ComponentType]) {
          //logger.error("The type with name {} is not a ComponentType", self.componentTypeName)
          context.appendInterpretationError("Could add port '"+self.portTypeName+"' to ComponentType '"+self.componentTypeName+"'. TypeDefinition exists but is a ComponentType: '"+e.getClass.getName+"'.")

          success = false
        } else {
          val portTypeRef = context.kevoreeFactory.createPortTypeRef
          portTypeRef.setName(self.portTypeName)

          if (self.optional.isDefined) {
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
          if (self.typeport == "Message") {
            self.className = Some("org.kevoree.framework.MessagePort")
          }
          // find Class in model
          context.model.findByPath("typeDefinitions[" + self.className.get + "]", classOf[TypeDefinition]) match {
            case p:TypeDefinition =>
              portTypeRef.setRef(p.asInstanceOf[PortType])
              success = true
            case null =>
              Log.debug("The port service can't be associated with the interface => {} is not found", self.className.get)
              val messagePortType = context.kevoreeFactory.createMessagePortType
              messagePortType.setName(self.className.toString)
              context.model.addTypeDefinitions(messagePortType)

              success = true
          }
        }
      }
      case null => {
        context.appendInterpretationError("Could add port '"+self.portTypeName+"' to ComponentType '"+self.componentTypeName+"'. ComponentType not found.")

        success = false
      }
    }
    success
  }
}
