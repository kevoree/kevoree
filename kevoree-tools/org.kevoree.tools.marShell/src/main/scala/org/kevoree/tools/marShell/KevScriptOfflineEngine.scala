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
package org.kevoree.tools.marShell

import interpreter.KevsInterpreterContext
import org.kevoree.ContainerRoot
import interpreter.KevsInterpreterAspects._
import org.kevoree.api.service.core.script.KevScriptEngineException


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 05/02/12
 * Time: 16:14
 */

class KevScriptOfflineEngine(srcModel: ContainerRoot) extends KevScriptAbstractEngine {
  
  def clearVariables() {
    varMap.clear();
    this
  }

  def interpret(): ContainerRoot = {
    val resolvedScript = resolveVariables
    logger.debug("KevScriptEngine before execution with script = {}", resolvedScript)
    parser.parseScript(resolvedScript) match {
      case Some(s) => {
        val inputModel = modelCloner.clone(srcModel)
        if (s.interpret(KevsInterpreterContext(inputModel))) {
          return inputModel;
        }
        throw new KevScriptEngineException {
          override def getMessage = "Interpreter Error : "
        }
      }
      case None => throw new KevScriptEngineException {
        override def getMessage = "Parser Error : " + parser.lastNoSuccess.toString
      }
    }
  }

  def interpretDeploy() {
    throw new Exception("Deploy not allowed for offline KevSEngine")
  }

  def atomicInterpretDeploy(): Boolean = false
  
}
