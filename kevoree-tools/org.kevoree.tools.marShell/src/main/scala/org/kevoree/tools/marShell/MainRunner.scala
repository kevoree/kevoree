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

package org.kevoree.tools.marShell

import interpreter.KevsInterpreterContext
import org.kevoree.KevoreeFactory
import org.kevoree.tools.marShell.parser.KevsParser
import org.kevoree.tools.marShell.parser.ParserUtil
import scala.collection.JavaConversions._

object MainRunner {

  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = {

    var newModel = KevoreeFactory.eINSTANCE.createContainerRoot

    var parser =new KevsParser();
    var oscript = parser.parseScript(ParserUtil.loadFile("/Users/ffouquet/Documents/DEV/dukeboard_github/kevoree/kevoree-tools/org.kevoree.tools.marShell/src/test/resources/scripts/t1.kevs"));

    oscript match {
      case None => println("Error"+parser.lastNoSuccess)
      case Some(script)=> {
          import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
          println("Interpreter Result : "+script.interpret(KevsInterpreterContext(newModel)))
      }
    }

    ParserUtil.save("modified.kev", newModel)
    

    

  }

}
