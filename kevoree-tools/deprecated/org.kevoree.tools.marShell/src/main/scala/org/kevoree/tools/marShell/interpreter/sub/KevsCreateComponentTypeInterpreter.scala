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

import org.kevoree.{TypeDefinition, TypeLibrary}
import org.kevoree.tools.marShell.ast.CreateComponentTypeStatment
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext

case class KevsCreateComponentTypeInterpreter(self : CreateComponentTypeStatment) extends KevsAbstractInterpreter {


  def interpret(context: KevsInterpreterContext): Boolean = {
    //LOOK FOR PREVIOUSLY EXSITING COMPONENT TYPE
    context.model.findByPath("typeDefinitions[" + self.newTypeName + "]", classOf[TypeDefinition])match {
      case e:TypeDefinition=> {
        context.appendInterpretationError("Could not create ComponentType named '"+self.newTypeName+"'. Type already exists.")
        //logger.error("TypeDefinition already exist with name => "+self.newTypeName)
        false
      }
      case null => {
          val newComponentTypeDef = context.kevoreeFactory.createComponentType
          newComponentTypeDef.setName(self.newTypeName)
          context.model.addTypeDefinitions(newComponentTypeDef)

          self.libName.map{ libName =>

            var internal = context.model.findByPath("libraries[" + libName + "]", classOf[TypeLibrary])
            if (internal == null){
              val newLib = context.kevoreeFactory.createTypeLibrary
              newLib.setName(libName)
              internal = newLib
            }
            internal.addSubTypes(newComponentTypeDef)
          }

          true
      }
    }
  }
  
  
}
