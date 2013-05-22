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

import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.ast.AddDeployUnitStatment
import scala.collection.JavaConversions._
import org.kevoree.log.Log

case class KevsAddDeployUnitInterpreter(statment : AddDeployUnitStatment) extends KevsAbstractInterpreter {

  def interpret(context : KevsInterpreterContext):Boolean={

    context.model.getDeployUnits.find(du=> du.getUnitName == statment.unitName && du.getGroupName == statment.groupName && du.getVersion == statment.version) match {
      case Some(du) =>  Log.warn("DeployUnit already exist");true
      case None => {
        val newDeployUnit = context.kevoreeFactory.createDeployUnit
        newDeployUnit.setUnitName(statment.unitName)
        newDeployUnit.setGroupName(statment.groupName)
        newDeployUnit.setVersion(statment.version)
        context.model.addDeployUnits(newDeployUnit)
        true
      }
    }

  }

}
