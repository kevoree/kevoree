package org.kevoree.tools.ui.editor.standalone

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

import javax.swing.filechooser.FileFilter
import java.awt.event.{MouseEvent, MouseAdapter}
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.framework.KevoreeXmiHelper
import java.util.Random
import org.kevoree.tools.marShell.parser.ParserUtil
import org.kevoree.tools.ui.editor.{PositionedEMFHelper, KevoreeUIKernel}
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import java.io._
import org.slf4j.LoggerFactory
import org.kevoree.tools.marShellGUI.{KevsPanel, KevsModelHandlers, KevsFrame}
import java.awt.BorderLayout
import org.kevoree.tools.ui.editor.command.{Command, LoadModelCommand}
import javax.swing._
import com.explodingpixels.macwidgets.MacButtonFactory
import java.net.URL


class LocalKevsShell extends JPanel {

  var logger = LoggerFactory.getLogger(this.getClass)
  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = {
    kernel = k
    KevsModelHandlers.put(1, kernel.getModelHandler.getActualModel)
    kernel.getModelHandler.addListenerCommand(new Command {
      def execute(p: java.lang.Object): Unit = {
        KevsModelHandlers.put(1, kernel.getModelHandler.getActualModel)
      }
    })
  }

  setLayout(new BorderLayout())

  var kevsPanel = new KevsPanel

  var url: URL = classOf[App].getClassLoader.getResource("runprog2.png")
  var icon: ImageIcon = new ImageIcon(url)

  var btExecution = MacButtonFactory.makeUnifiedToolBarButton(new JButton("Run", icon))


  btExecution.addMouseListener(new MouseAdapter() {
    override def mouseClicked(p1: MouseEvent) = {
      //TODO SAVE CURRENT MODEL
      val script = kevsPanel.getModel
      if (script != null) {
        import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
        PositionedEMFHelper.updateModelUIMetaData(kernel)

        val outputStream: ByteOutputStream = new ByteOutputStream
        KevoreeXmiHelper.saveStream(outputStream, kernel.getModelHandler.getActualModel)
        val ghostModel = KevoreeXmiHelper.loadStream(outputStream.newInputStream())

        val result = script.interpret(KevsInterpreterContext(ghostModel))
        logger.info("Interpreter Result : " + result)
        if (result) {
          //reload
          val file = File.createTempFile("kev", new Random().nextInt + "")

          KevoreeXmiHelper.save("file:///" + file.getAbsolutePath, ghostModel);

          val loadCMD = new LoadModelCommand
          loadCMD.setKernel(kernel)
          loadCMD.execute("file:///" + file.getAbsolutePath)


        }
      }
    }
  })
  add(btExecution, BorderLayout.WEST);
  add(kevsPanel, BorderLayout.CENTER)

}
