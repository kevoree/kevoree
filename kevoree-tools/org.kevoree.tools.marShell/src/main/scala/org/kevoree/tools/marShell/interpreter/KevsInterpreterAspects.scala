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

package org.kevoree.tools.marShell.interpreter

import org.kevoree.tools.marShell.ast.AddComponentInstanceStatment
import org.kevoree.tools.marShell.ast.AddNode
import org.kevoree.tools.marShell.ast.Block
import org.kevoree.tools.marShell.ast.Script
import org.kevoree.tools.marShell.ast.Statment
import org.kevoree.tools.marShell.ast.TransactionalBloc
import org.kevoree.tools.marShell.interpreter.sub.KevsAddComponentInstanceInterpreter
import org.kevoree.tools.marShell.interpreter.sub.KevsAddNodeInterpreter
import org.kevoree.tools.marShell.interpreter.sub.KevsAddTBlockInterpreter

object KevsInterpreterAspects {

  implicit def rich(o : Script) = KevsScriptInterpreter(o)
  implicit def rich(o : TransactionalBloc) = KevsAddTBlockInterpreter(o)
  implicit def rich(o : AddNode) = KevsAddNodeInterpreter(o)


  implicit def rich(o : Object) : KevsAbstractInterpreter  = o match {
    
    case b : Block => b match {
        case tb : TransactionalBloc => KevsAddTBlockInterpreter(tb)
      }
    case st : Statment => st match {
        case addst : AddComponentInstanceStatment => KevsAddComponentInstanceInterpreter(addst)
        case addNodest : AddNode => KevsAddNodeInterpreter(addNodest)
      }
    case _ @ e => println(e);null
  }
}
