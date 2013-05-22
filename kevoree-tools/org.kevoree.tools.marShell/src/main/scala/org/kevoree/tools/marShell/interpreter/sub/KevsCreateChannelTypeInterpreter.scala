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

import org.kevoree.{TypeDefinition, TypeLibrary}
import org.kevoree.tools.marShell.ast.CreateChannelTypeStatment
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext

case class KevsCreateChannelTypeInterpreter(self: CreateChannelTypeStatment) extends KevsAbstractInterpreter {


  def interpret(context: KevsInterpreterContext): Boolean = {
    //LOOK FOR PREVIOUSLY EXSITING COMPONENT TYPE
    context.model.findByPath("typeDefinitions[" + self.newTypeName + "]", classOf[TypeDefinition]) match {
      case e:TypeDefinition => {
        context.appendInterpretationError("Could not create ChannelType '"+self.newTypeName+"'. ChannelType already exists.")
        //logger.error("TypeDefinition already exist with name => " + self.newTypeName)
        false
      }
      case null => {
        val newComponentTypeDef = context.kevoreeFactory.createChannelType
        newComponentTypeDef.setName(self.newTypeName)
        context.model.addTypeDefinitions(newComponentTypeDef)

        self.libName.map {
          libName =>

            var newLic = context.model.findByPath("libraries[" + libName + "]", classOf[TypeLibrary])
            if (newLic == null) {
              newLic = context.kevoreeFactory.createTypeLibrary
              newLic.setName(libName)
            }

            newLic.addSubTypes(newComponentTypeDef)
        }


        true
      }
    }
  }


}
