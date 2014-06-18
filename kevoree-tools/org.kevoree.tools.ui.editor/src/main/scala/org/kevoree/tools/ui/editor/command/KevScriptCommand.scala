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
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.tools.ui.editor.command

import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.slf4j.LoggerFactory
import java.awt.Color
import com.explodingpixels.macwidgets.HudWindow
import javax.swing._
import org.kevoree.kevscript.KevScriptEngine
import org.kevoree.cloner.DefaultModelCloner
import org.kevoree.ContainerRoot

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 10/10/11
 * Time: 19:37
 */

class KevScriptCommand extends Command {

  private val logger = LoggerFactory.getLogger(this.getClass)

  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) {
    kernel = k
  }

  def displayError(msg: String) {


    val hud = new JFrame("KevScript Error");
    hud.setSize(500,350)

    //val hud = new HudWindow("KevScript Error");
    //hud.getJDialog.setSize(500, 350);
    //hud.getJDialog.setLocationRelativeTo(null);

    val msgLabel = new JTextArea(msg)
    msgLabel.setForeground(Color.WHITE)
    msgLabel.setOpaque(false)
    msgLabel.setLineWrap(true);
    msgLabel.setWrapStyleWord(true);
    msgLabel.setSize(480, 320)
    msgLabel.setPreferredSize(msgLabel.getSize)

    val layoutPopupTop = new JPanel()
    layoutPopupTop.setOpaque(false)
    layoutPopupTop.add(msgLabel)

    //hud.getJDialog.getContentPane.add(layoutPopupTop)
    //hud.getJDialog.setVisible(true)

    hud.getContentPane.add(layoutPopupTop)
    hud.setVisible(true)
    println("Display MSG : " + msg)

  }

  var engine = new KevScriptEngine
  var cloner = new DefaultModelCloner()

  def execute(p: AnyRef) {
    try {
      val clonedModel = cloner.clone(kernel.getModelHandler.getActualModel)
      engine.execute(p.toString, clonedModel.asInstanceOf[ContainerRoot])
      val loadCMD = new LoadModelCommand
      loadCMD.setKernel(kernel)
      loadCMD.execute(clonedModel)
    } catch {
      case e: Throwable => {
        displayError(e.getMessage)
      }
    }
  }
}