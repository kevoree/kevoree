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
package org.kevoree.tools.ui.editor.command

import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.kevoree.core.basechecker.RootChecker
import scala.collection.JavaConversions._
import org.kevoree.tools.ui.framework.ErrorHighlightableElement
import actors.DaemonActor
import org.slf4j.LoggerFactory


class CheckCurrentModel extends Command {

  var logger = LoggerFactory.getLogger(this.getClass)

  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k

  var checker = new RootChecker
  var objectInError: List[ErrorHighlightableElement] = List()

  object notificationSeamless extends DaemonActor {
    start()
    var checkNeeded = false
    def act() {
      loop {
        reactWithin(1000) {
          case scala.actors.TIMEOUT => if(checkNeeded){ effectiveCheck();checkNeeded=false }
          case _ => checkNeeded = true
        }
      }
    }
  }

  def execute(p: Object) {
      notificationSeamless ! "checkNeeded"
  }

  def effectiveCheck() {
    objectInError.foreach(o => o.setState(ErrorHighlightableElement.STATE.NO_ERROR))
    objectInError = List()
    val result = checker.check(kernel.getModelHandler.getActualModel)
    result.foreach({
      res =>
        logger.warn("Violation msg=" + res.getMessage)
        //AGFFICHE OBJET ERROR
        res.getTargetObjects.foreach {
          target =>
            //println(target)
            val uiObj = kernel.getUifactory.getMapping.get(target)
            if (uiObj != null) {
              //println("ui=" + uiObj)
              uiObj match {
                case hobj: ErrorHighlightableElement => {
                  objectInError = objectInError ++ List(hobj)
                  hobj.setState(ErrorHighlightableElement.STATE.IN_ERROR)
                }
                case _@e => logger.error("checker obj = " + e)
              }
            }
        }
    })

    kernel.getModelPanel.repaint()
    kernel.getModelPanel.revalidate()
  }


}