package org.kevoree.library.kevscript

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

import java.awt.event.{MouseEvent, MouseAdapter}
import org.slf4j.LoggerFactory
import org.kevoree.tools.marShellGUI.{KevsPanel, KevsModelHandlers}
import java.awt.BorderLayout
import javax.swing._
import com.explodingpixels.macwidgets.MacButtonFactory
import java.net.URL
import org.kevoree.api.service.core.handler.{ModelListener, KevoreeModelHandlerService}
import org.kevoree.ContainerRoot
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.cloner.ModelCloner


class LocalKevsShell(mhs: KevoreeModelHandlerService, kevsfact: KevScriptEngineFactory) extends JPanel {

  var logger = LoggerFactory.getLogger(this.getClass)
  setLayout(new BorderLayout())

  var kevsPanel = new KevsPanel

  var url: URL = classOf[LocalKevsShell].getClassLoader.getResource("runprog2.png")
  var icon: ImageIcon = new ImageIcon(url)

  var btExecution = MacButtonFactory.makeUnifiedToolBarButton(new JButton("Run", icon))


  KevsModelHandlers.put(1, mhs.getLastModel)
  mhs.registerModelListener(new ModelListener {
    def preUpdate(p1: ContainerRoot, p2: ContainerRoot) = true

    def initUpdate(p1: ContainerRoot, p2: ContainerRoot) = true

    def modelUpdated() {
      KevsModelHandlers.put(1, mhs.getLastModel)
    }

    def afterLocalUpdate(currentModel: ContainerRoot, proposedModel: ContainerRoot) = true

    def preRollback(currentModel: ContainerRoot, proposedModel: ContainerRoot) {}

    def postRollback(currentModel: ContainerRoot, proposedModel: ContainerRoot) {}
  })






  btExecution.addMouseListener(new MouseAdapter() {
    override def mouseClicked(p1: MouseEvent) = {

      //TODO SAVE CURRENT MODEL
      val script = kevsPanel.getModel
      if (script != null) {
        import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
        val ghostModel = mhs.getLastUUIDModel
        val modelClone = new ModelCloner
        val cloneModel = modelClone.clone(ghostModel.getModel)

        val result = script.interpret(KevsInterpreterContext(cloneModel))
        logger.info("Interpreter Result : " + result)
        if (result) {
          //reload


          mhs.compareAndSwapModel(ghostModel, cloneModel)
        }
      }
    }
  })
  add(btExecution, BorderLayout.WEST)
  add(kevsPanel, BorderLayout.CENTER)

}
