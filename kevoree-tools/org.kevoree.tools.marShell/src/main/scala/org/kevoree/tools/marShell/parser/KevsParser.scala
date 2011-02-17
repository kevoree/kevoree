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

package org.kevoree.tools.marShell.parser

import org.kevoree.tools.marShell.ast.ComponentInstanceID
import org.kevoree.tools.marShell.ast.Script
import org.kevoree.tools.marShell.ast.Statment
import sub._

class KevsParser extends KevsAbstractParser
                    with KevsInstParser
                    with KevsComponentInstanceParser
                    with KevsScriptParser
                    with KevsNodeParser
                    with KevsBindingParser{

  /**
   * extend the fExpression parser with sub parser
   */
  override def kevStatement : Parser[List[Statment]] = (parseInst | parseNode | parseBindingsStatments)
  override def componentID : Parser[ComponentInstanceID] = parseCID

  def parseScript(content : String) : Option[Script] = {

    val tokens = new lexical.Scanner(content+"\n")
    val result = phrase(parseScript)(tokens)
    result match {
      case Success(tree, _) => {Some(tree) }
      case e: NoSuccess => {None}
    }
  }

 // var e : NoSuccess = null
 // def getLastNoSucess = e

}
