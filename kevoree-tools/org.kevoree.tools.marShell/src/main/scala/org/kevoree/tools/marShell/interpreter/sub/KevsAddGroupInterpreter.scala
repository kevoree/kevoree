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

import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import scala.collection.JavaConversions._
import org.kevoree.tools.marShell.ast.{AddGroupStatment}
import org.kevoree.{GroupType, ChannelType, KevoreeFactory}
import org.kevoree.tools.marShell.interpreter.utils.Merger

case class KevsAddGroupInterpreter(addGroup: AddGroupStatment) extends KevsAbstractInterpreter {

  def interpret(context: KevsInterpreterContext): Boolean = {
    context.model.getGroups.find(n => n.getName == addGroup.groupName) match {
      case Some(target) => {
        println("Warning : Group already exist with name " + addGroup.groupName);
        if (target.getTypeDefinition.getName == addGroup.groupTypeName) {
          Merger.mergeDictionary(target, addGroup.props)
          true
        } else {
          println("Error : Type != from previous created group")
          false
        }
      }
      case None => {
        //SEARCH TYPE DEF
        context.model.getTypeDefinitions.find(td => td.getName == addGroup.groupTypeName) match {
          case Some(targetGroupType) if (targetGroupType.isInstanceOf[GroupType]) => {

            val newGroup = KevoreeFactory.eINSTANCE.createGroup
            newGroup.setTypeDefinition(targetGroupType)
            newGroup.setName(addGroup.groupName)
            Merger.mergeDictionary(newGroup, addGroup.props)
            context.model.getGroups.add(newGroup)

          }
          case Some(targetGroupType) if (!targetGroupType.isInstanceOf[GroupType]) => {
            println("Type definition is not a groupType " + addGroup.groupTypeName);
            false
          }
          case _ => {
            println("Type definition not found " + addGroup.groupTypeName);
            false
          }
        }
        true
      }
    }
  }

}
