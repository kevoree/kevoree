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
import org.kevoree.tools.marShell.ast.{AddToGroupStatement, AddBindingStatment}
import org.kevoree.{ContainerNode, Group, KevoreeFactory}

case class KevsAddToGroupInterpreter(addToGroup : AddToGroupStatement) extends KevsAbstractInterpreter {

  def interpret(context : KevsInterpreterContext):Boolean={

    var groups : List[Group] = List()
    if(addToGroup.groupName == "*"){
      groups = context.model.getGroups.toList
    } else {
       context.model.getGroups.find(g=> g.getName == addToGroup.groupName ) match {
         case Some(g)=> groups = List(g)
         case None => return false
       }
    }

    var nodes : List[ContainerNode] = List()
    if(addToGroup.nodeName == "*"){
      nodes = context.model.getNodes.toList
    } else {
       context.model.getNodes.find(g=> g.getName == addToGroup.nodeName ) match {
         case Some(g)=> nodes = List(g)
         case None => return false
       }
    }

    groups.foreach{group=>
      nodes.foreach{ node =>
          if(!group.getSubNodes.contains(node)){
              group.getSubNodes.add(node)
          }
      }


    }

    true

  }

}
