package org.kevoree.tools.marShell.interpreter.sub

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

import org.kevoree.KevoreeFactory
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext

import org.slf4j.LoggerFactory
import org.kevoree.tools.marShell.ast.{AddDeployUnitStatment, AddLibraryStatment}

case class KevsAddDeployUnitInterpreter(statment : AddDeployUnitStatment) extends KevsAbstractInterpreter {

  var logger = LoggerFactory.getLogger(this.getClass);

  def interpret(context : KevsInterpreterContext):Boolean={

    context.model.getDeployUnits.find(du=> du.getUnitName == statment.unitName && du.getGroupName == statment.groupName && du.getVersion == statment.version) match {
      case Some(du) =>  logger.warn("DeployUnit already exist");true
      case None => {
        val newDeployUnit = KevoreeFactory.eINSTANCE.createDeployUnit
        newDeployUnit.setUnitName(statment.unitName)
        newDeployUnit.setGroupName(statment.groupName)
        newDeployUnit.setVersion(statment.version)
        context.model.addDeployUnits(newDeployUnit)
        true
      }
    }

  }

}
