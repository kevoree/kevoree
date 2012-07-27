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
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import interpreter.KevsInterpreterAspects._
import org.kevoree.api.service.core.script.{KevScriptEngine, KevScriptEngineException}
import org.kevoree.api.Bootstraper

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 11/12/11
 * Time: 20:32
 */

class KevScriptCoreEngine (core: KevoreeModelHandlerService, bootstraper: Bootstraper) extends KevScriptAbstractEngine {

  clearVariables()

  def clearVariables () {
    varMap.clear()
    varMap.put("nodename", core.getNodeName)
  }

  @throws(classOf[KevScriptEngineException])
  def interpret (): ContainerRoot = {
    val resolvedScript = resolveVariables
    logger.debug("KevScriptEngine before execution with script = {}", resolvedScript)
    parser.parseScript(resolvedScript) match {
      case Some(s) => {
        val inputModel = core.getLastModel
        val ctx = KevsInterpreterContext(inputModel)
        ctx.setBootstraper(bootstraper)
        if (s.interpret(ctx.setVarMap(varMap))) {
          return inputModel
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

  @throws(classOf[KevScriptEngineException])
  def interpretDeploy () {
    internal_interpret_deploy(false)
  }

  @throws(classOf[KevScriptEngineException])
  def atomicInterpretDeploy () {
    internal_interpret_deploy(true)
  }


  @throws(classOf[KevScriptEngineException])
  private def internal_interpret_deploy (atomic: Boolean): Boolean = {
    try {
      val resolvedScript = resolveVariables
      logger.debug("KevScriptEngine before execution with script = {}", resolvedScript)
      parser.parseScript(resolvedScript) match {
        case Some(s) => {
          val inputModel = core.getLastUUIDModel
          val targetModel = modelCloner.clone(inputModel.getModel)

          val ctx = KevsInterpreterContext(targetModel)
          ctx.setBootstraper(bootstraper)

          if (s.interpret(ctx.setVarMap(varMap))) {
            if (atomic) {
              try {
                core.atomicCompareAndSwapModel(inputModel, targetModel)
                //                return true
              } catch {
                case _@e => throw new KevScriptEngineException {
                  override def getMessage = "Unable to compare and swap model : " + e.getMessage

                  override def getCause = e
                } //return false
              }
            } else {
              core.compareAndSwapModel(inputModel, targetModel)
              //              return true
            }
          }
          throw new KevScriptEngineException {
            override def getMessage = "Interpreter Error : "
          }
        }
        case None => throw new KevScriptEngineException {
          override def getMessage = "Parser Error : " + parser.lastNoSuccess.toString
        }
      }
    } catch {
      case _@e => throw new KevScriptEngineException {
        override def getMessage = e.getMessage
      }
    }
  }


}