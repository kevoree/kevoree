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

package org.kevoree.tools.marShell.interpreter.sub

import org.kevoree.TypeLibrary
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext

import org.kevoree.tools.marShell.ast.{AddLibraryStatment}
import org.kevoree.log.Log

case class KevsAddLibraryInterpreter(statment : AddLibraryStatment) extends KevsAbstractInterpreter {

  def interpret(context : KevsInterpreterContext):Boolean={

    context.model.findByPath("libraries[" + statment.libraryName + "]", classOf[TypeLibrary]) match {
      case library:TypeLibrary =>  Log.warn("Library already exist");true
      case null => {
        val newLibrary = context.kevoreeFactory.createTypeLibrary
        newLibrary.setName(statment.libraryName)
        context.model.addLibraries(newLibrary)
        true
      }
    }

  }

}
