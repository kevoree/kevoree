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
import org.kevoree.tools.marShell.parser.KevsParser
import org.kevoree.tools.marShell.parser.ParserUtil
import org.kevoree.{ContainerRoot, KevoreeFactory}
import org.kevoree.framework.KevoreeXmiHelper

object MainRunner {

  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = {

    val newModel = KevoreeXmiHelper.load("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-tools/org.kevoree.tools.marShell/src/test/resources/baseModel/defaultLibrary.kev")



    val parser =new KevsParser();
    val oscript = parser.parseScript(ParserUtil.loadFile("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-tools/org.kevoree.tools.marShell/src/test/resources/scripts/t1.kevs"));

    oscript match {
      case None => println("Error"+parser.lastNoSuccess)
      case Some(script)=> {
          import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
          println("Interpreter Result : "+script.interpret(KevsInterpreterContext(newModel)))
      }
    }
    KevoreeXmiHelper.save("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-tools/org.kevoree.tools.marShell/src/test/resources/results/modified.kev", newModel)
  }





}
