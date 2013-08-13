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

package org.kevoree.tools.marShell.test

import org.kevoree.tools.aether.framework.NodeTypeBootstrapHelper
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.parser.KevsParser
import org.kevoree.tools.marShell.parser.ParserUtil
import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.impl.DefaultKevoreeFactory

object MainRunner {

  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = {

    val newModel = new DefaultKevoreeFactory().createContainerRoot()

    val parser =new KevsParser();
    //val oscript = parser.parseScript(ParserUtil.loadFile("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-tools/org.kevoree.tools.marShell/src/test/resources/scripts/kevsCloud.kevs"));
    val oscript = parser.parseScript("{" + ParserUtil.loadFile("/home/edaubert/workspace/daum/daum-library/javase/org.daum.library.javase.copterManager/src/main/kevs/main.kevs") + "}");

    oscript match {
      case None => println("Error"+parser.lastNoSuccess)
      case Some(script)=> {
        val ctx = KevsInterpreterContext(newModel)
        ctx.setBootstraper(new NodeTypeBootstrapHelper)
          import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
        val result = script.interpret(ctx)
          println("Interpreter Result : "+result)
        println(ctx.interpretationErrors)

      }
    }
    KevoreeXmiHelper.instance$.save("/home/edaubert/modified.kev", newModel)
  }





}
