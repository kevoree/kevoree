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

import org.kevoree.{TypeDefinition, TypeLibrary, KevoreeFactory}
import org.kevoree.tools.marShell.ast.CreateChannelTypeStatment
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import scala.collection.JavaConversions._


import org.slf4j.LoggerFactory

case class KevsCreateChannelTypeInterpreter(self: CreateChannelTypeStatment) extends KevsAbstractInterpreter {

  var logger = LoggerFactory.getLogger(this.getClass)

  def interpret(context: KevsInterpreterContext): Boolean = {
    //LOOK FOR PREVIOUSLY EXSITING COMPONENT TYPE
    context.model.findByQuery("typeDefinitions[" + self.newTypeName + "]", classOf[TypeDefinition]) match {
      case e:TypeDefinition => logger.error("TypeDefinition already exist with name => " + self.newTypeName); false
      case null => {
        val newComponentTypeDef = KevoreeFactory.$instance.createChannelType
        newComponentTypeDef.setName(self.newTypeName)
        context.model.addTypeDefinitions(newComponentTypeDef)

        self.libName.map {
          libName =>

            var newLic = context.model.findByQuery("libraries[" + libName + "]", classOf[TypeLibrary])
            if (newLic == null) {
              newLic = KevoreeFactory.$instance.createTypeLibrary
              newLic.setName(libName)
            }

            newLic.addSubTypes(newComponentTypeDef)
        }


        true
      }
    }
  }


}
