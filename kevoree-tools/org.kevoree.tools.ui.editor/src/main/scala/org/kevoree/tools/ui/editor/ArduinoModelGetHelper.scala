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
package org.kevoree.tools.ui.editor

import org.kevoree.tools.marShell.ast.Script
import org.kevoree.ContainerRoot
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShellTransform.KevScriptWrapper
import org.kevoree.extra.kserial.{KevoreeSharedCom, ContentListener}
import org.slf4j.LoggerFactory
import org.kevoree.cloner.ModelCloner
import org.kevoree.extra.kserial.SerialPort._


/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 30/03/12
 * Time: 14:49
 */
object ArduinoModelGetHelper {

  var logger = LoggerFactory.getLogger(this.getClass)

  def pull_model_arduino(boardPortName: String, timeout: Int): String = {
    var cscript_pulled: String = null
    var found: Boolean = false
    val scriptRaw = new StringBuilder()
    KevoreeSharedCom.addObserver(boardPortName, new ContentListener {
      def recContent(p1: String) {
        scriptRaw.append(p1.trim())
        if (scriptRaw.contains('{') && scriptRaw.contains('}') && scriptRaw.contains('@') && scriptRaw.contains('$') && scriptRaw.contains(':') && scriptRaw.contains('+') && scriptRaw.contains('!') && found != true) {
          // extract cscript
          cscript_pulled = scriptRaw.subSequence(scriptRaw.indexOf('$') + 1, scriptRaw.indexOf('!') + 1).toString
          // verify checksum
          if (KevScriptWrapper.checksum_csript(cscript_pulled) == true) {
            found = true
          } else {
            logger.warn("The checksum is not correct " + cscript_pulled)
            cscript_pulled = null
            scriptRaw.clear()
          }
        }
      }
    })
    try {
      scriptRaw.clear()
      var timer: Int = 0
      do {
        scriptRaw.clear()
        KevoreeSharedCom.send(boardPortName, "$g")
        Thread.sleep(500)
        timer += 1
      } while (found == false && timer < timeout)

      if (found) {
        cscript_pulled
      } else {
        null
      }
    } catch {
      case se: SerialPortException => {
        logger.error(boardPortName + " " + se.toString)
        cscript_pulled
      }
      case e: Exception => {
        logger.error("Fail to open serial port " + boardPortName + " " + e)
        cscript_pulled
      }
    }

  }


  def getCurrentModel(targetNewModel: ContainerRoot, boardPortName: String): ContainerRoot = {




    val cscript = pull_model_arduino(boardPortName, 5)
    var model: ContainerRoot = null
    if (cscript != null) {
      logger.debug("Compressed script from arduino node : " + cscript)
      //GET SCRIPT FROM COM PORT
      val script: Script = KevScriptWrapper.miniPlanKevScript(KevScriptWrapper.generateKevScriptFromCompressed(cscript.toString, targetNewModel))

      logger.debug("The plan script : " + script)
      //APPLY TO BUILD A CURRENT MODEL
      import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
      val cc = new ModelCloner
      val current = cc.clone(targetNewModel)
      current.removeAllGroups()
      // current.removeAllHubs()
      current.removeAllMBindings()
      current.getNodes.foreach {
        node =>
          current.removeNodes(node)
      }
      val result = script.interpret(KevsInterpreterContext(current))

      if (result) {
        model = current
      }
    }
    else {
      logger.error("The node did not respond in time or is not present on the port " + boardPortName + ". The firmware have to be flashed with a kevoree runtime")
    }
    model
  }

}
