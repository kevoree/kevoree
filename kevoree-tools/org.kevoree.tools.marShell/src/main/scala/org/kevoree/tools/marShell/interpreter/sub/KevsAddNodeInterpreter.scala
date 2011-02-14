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

import org.kevoree.KevoreeFactory
import org.kevoree.tools.marShell.ast.AddNode
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import scala.collection.JavaConversions._

case class KevsAddNodeInterpreter(addN : AddNode) extends KevsAbstractInterpreter {

  def interpret(context : KevsInterpreterContext):Boolean={

    context.model.getNodes.find(n=>n.getName == addN.nodeName) match {
      case Some(e)=> println("Node Already existe");false
      case None => {
          var newnode = KevoreeFactory.eINSTANCE.createContainerNode
          newnode.setName(addN.nodeName)
          context.model.getNodes.add(newnode)
          true
        }
    }
  }

}
