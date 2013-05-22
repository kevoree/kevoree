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

import org.kevoree.tools.marShell.interpreter.{KevsInterpreterContext, KevsAbstractInterpreter}
import org.kevoree.tools.marShell.ast.IncludeStatement
import java.io.File
import org.kevoree.tools.marShell.interpreter
import interpreter.KevsInterpreterAspects._
import org.kevoree.tools.marShell.parser.{ParserUtil, KevsParser}


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/12/11
 * Time: 14:55
 */

case class KevsIncludeInterpreter(includeStatement: IncludeStatement) extends KevsAbstractInterpreter {

  def interpret(context: KevsInterpreterContext): Boolean = {

    val parser = new KevsParser

    try {
      val inputFile = new File(includeStatement.url)
      var includeScript =  ParserUtil.loadFile(inputFile.getAbsolutePath)
      import scala.collection.JavaConversions._
      context.getVarMap.foreach {
        varR =>
          includeScript = includeScript.replace("{" + varR._1 + "}", varR._2)
      }
      includeScript = includeScript.replace("'", "\"")
      includeScript = "tblock{\n" + includeScript + "\n}"

     // println("-----"+includeScript+"---------")
      if(!parser.parseScript(includeScript.trim).get.interpret(context)) {
        context.appendInterpretationError("KevScript interpretation failed while trying to include script from '"+includeStatement.url+"'. ")
        false
      } else {
        true
      }
    } catch {
      case _@e => {
        context.appendInterpretationError("Could not include model from '"+includeStatement.url+"'. " + e.toString)
        //logger.error("Error interpret include statement : " + includeStatement.getTextualForm + "\n" + parser.lastNoSuccess)
        false
      }
    }
  }

}