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
import reflect.BeanProperty
import org.kevoree.core.basechecker.RootChecker
import scala.collection.JavaConversions._
import org.kevoree.tools.ui.framework.ErrorHighlightableElement


class CheckCurrentModel extends Command {

  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k

  var checker = new RootChecker

  var objectInError: List[ErrorHighlightableElement] = List()

  def execute(p: Object) {

    objectInError.foreach(o => o.setState(ErrorHighlightableElement.STATE.NO_ERROR))
    objectInError = List()
    var result = checker.check(kernel.getModelHandler.getActualModel)
    result.foreach({
      res =>
        println("Violation msg=" + res.getMessage)

        //AGFFICHE OBJET ERROR
        res.getTargetObjects.foreach {
          target =>
            println(target)

            val uiObj = kernel.getUifactory.getMapping.get(target);
            if (uiObj != null) {

              println("ui="+uiObj)

              uiObj match {
                case hobj: ErrorHighlightableElement => {
                  objectInError = objectInError ++ List(hobj)
                  hobj.setState(ErrorHighlightableElement.STATE.IN_ERROR)
                }
                case _@e => println("Error checker obj = " + e)
              }
            }

        }
    })

    if (result.size == 0) {
      println("Model checked !")
    }

    kernel.getModelPanel.repaint()
    kernel.getModelPanel.revalidate()

  }

}