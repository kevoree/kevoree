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

import org.kevoree.tools.marShell.ast.UpdateDictionaryStatement
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.interpreter.utils.Merger

import org.kevoree._

case class KevsUpdateDictionaryInterpreter(statement: UpdateDictionaryStatement) extends KevsAbstractInterpreter {

  def interpret(context: KevsInterpreterContext): Boolean = {
    
    var targetInstance : List[Instance] = List()
    statement.nodeName match {
      case Some(nodeID) => {
          //SEARCH NODE
          var nodes : List[ContainerNode] = List()
          if(nodeID == "*"){
            nodes = context.model.getNodes.toList
          } else {
            nodes = context.model.getNodes.filter(n=>n.getName == nodeID).toList
          }
          nodes.foreach{ targetNode =>   
            if(statement.instanceName == "*"){
              targetInstance = targetInstance ++ targetNode.getComponents.toList
            } else {
              targetInstance = targetInstance ++ targetNode.getComponents.filter(n=>n.getName == statement.instanceName).toList
            }
          }
        }
      case None => {
          if(statement.instanceName == "*"){
            targetInstance = targetInstance ++ context.model.getHubs.toList ++ context.model.getGroups.toList
          } else {
            targetInstance = targetInstance ++ context.model.getHubs.filter(n=>n.getName == statement.instanceName).toList ++ context.model.getGroups.filter(n=>n.getName == statement.instanceName).toList
          }
        }
    }    
    targetInstance.foreach{instance =>
      Merger.mergeFragmentDictionary(instance, statement.fraProperties)
    }
    
    if(targetInstance.isEmpty ){
      println("Warning : No dictionary merged")
    }
    
    true// ALWAYS RETURN TRUE 
    //TODO BETTER ERROR OR AMBIGUITY MANAGEMENT

  }


}
