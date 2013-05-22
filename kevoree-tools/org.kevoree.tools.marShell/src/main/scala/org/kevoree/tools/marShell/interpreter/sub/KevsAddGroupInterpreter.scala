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

import org.kevoree.tools.marShell.ast.AddGroupStatment
import org.kevoree.{TypeDefinition, Group, GroupType}
import org.kevoree.tools.marShell.interpreter.utils.Merger
import org.kevoree.log.Log

case class KevsAddGroupInterpreter(addGroup: AddGroupStatment) extends KevsAbstractInterpreter {

  def interpret(context: KevsInterpreterContext): Boolean = {
    context.model.findByPath("groups[" + addGroup.groupName + "]", classOf[Group]) match {
      case target:Group => {
        Log.warn("Group already exist with name " + addGroup.groupName)
        if (target.getTypeDefinition.getName == addGroup.groupTypeName) {
          Merger.mergeDictionary(target, addGroup.props, null)
          true
        } else {
          context.appendInterpretationError("Could add group '"+addGroup.groupName+"' of type '"+addGroup.groupTypeName+"'. A group instance already exists with the same name, but with a different type: '"+target.getTypeDefinition.getName+"'.")
          false
        }
      }
      case null => {
        //SEARCH TYPE DEF
        context.model.findByPath("typeDefinitions[" + addGroup.groupTypeName + "]", classOf[TypeDefinition]) match {
          case targetGroupType:TypeDefinition if (targetGroupType.isInstanceOf[GroupType]) => {

            val newGroup = context.kevoreeFactory.createGroup
            newGroup.setTypeDefinition(targetGroupType)
            newGroup.setName(addGroup.groupName)
            Merger.mergeDictionary(newGroup, addGroup.props, null)
            context.model.addGroups(newGroup)

          }
          case targetGroupType:TypeDefinition if (!targetGroupType.isInstanceOf[GroupType]) => {
            context.appendInterpretationError("Could add group '"+addGroup.groupName+"' of type '"+addGroup.groupTypeName+"'. Type of the new group is not a GroupType: '"+targetGroupType.getClass.getName+"'.")
            false
          }
          case _ => {
            context.appendInterpretationError("Could add group '"+addGroup.groupName+"' of type '"+addGroup.groupTypeName+"'. TypeDefinition not found.")
            false
          }
        }
        true
      }
    }
  }

}
