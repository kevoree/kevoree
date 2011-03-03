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

import javax.swing.{JButton, BoxLayout, JPanel}
import java.awt.BorderLayout
import org.kevoree.tools.marShellGUI.KevsFrame
import org.kevoree.tools.ui.editor.KevoreeUIKernel
import java.awt.event.{MouseEvent, MouseAdapter}
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.framework.KevoreeXmiHelper
import java.io.File
import java.util.Random

class OpenKevsShell extends Command {

  private var current: KevsEditorFrame = null;
  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k

  class KevsEditorFrame extends KevsFrame {


    var buttons = new JPanel
    buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS))
    var btExecution = new JButton("execute");
    btExecution.addMouseListener(new MouseAdapter() {
      override def mouseClicked(p1: MouseEvent) = {
        //TODO SAVE CURRENT MODEL
        var script = kevsPanel.getModel
        if (script != null) {
          import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._

          var result = script.interpret(KevsInterpreterContext(kernel.getModelHandler.getActualModel))
          println("Interpreter Result : " + result)
          if(result){
            //reload
            val file = File.createTempFile("kev", new Random().nextInt+"")

            KevoreeXmiHelper.save(file.getAbsolutePath, kernel.getModelHandler().getActualModel);

            var loadCMD = new LoadModelCommand
             loadCMD.setKernel(kernel)
            loadCMD.execute(file.getAbsolutePath)



          }
        }
      }
    })


    buttons.add(btExecution)
    add(buttons, BorderLayout.SOUTH)


  }

  def execute(p: Object) = {
    if (current == null) {
      current = new KevsEditorFrame
      current.setVisible(true)
    }
    current.toFront
  }
}