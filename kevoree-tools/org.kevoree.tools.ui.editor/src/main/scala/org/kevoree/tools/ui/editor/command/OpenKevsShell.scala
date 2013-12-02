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

import javax.swing.filechooser.FileFilter
import javax.swing._
import java.awt.BorderLayout
import java.awt.event.{MouseEvent, MouseAdapter}
import org.kevoree.tools.ui.editor.{PositionedEMFHelper, KevoreeUIKernel}
import java.io._
import org.slf4j.LoggerFactory
import org.kevoree.tools.ui.kevscript.KevScriptEditorPanel

class OpenKevsShell extends Command {

  var logger = LoggerFactory.getLogger(this.getClass)

  private var current: KevsEditorFrame = null;
  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k

  class KevsEditorFrame extends JFrame {

    protected var kevsPanel = new KevScriptEditorPanel(kernel.getModelHandler)
    this.setTitle("Kevoree Script Editor")
    add(kevsPanel, BorderLayout.CENTER)

    var buttons = new JPanel
    buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS))
    var btSave = new JButton("Save");
    btSave.addMouseListener(new MouseAdapter() {
      override def mouseClicked(p1: MouseEvent) = {
        val fileChooser = new JFileChooser
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY)
        fileChooser.setMultiSelectionEnabled(false)
        fileChooser.setAcceptAllFileFilterUsed(true)
        fileChooser.setFileHidingEnabled(true)
        fileChooser.addChoosableFileFilter(new FileFilter() {
          override def getDescription(): String = "KevScript Files"
          override def accept(f: File): Boolean = {
            if (f.isDirectory()) {
              return false;
            }
            val extension = f.getName.substring(f.getName.lastIndexOf(".") + 1)
            if (extension != null) {
              if (extension.equals("kevs")) {
                return true;
              } else {
                return false;
              }
            }
            return false;
          }
        })
        val result = fileChooser.showSaveDialog(kevsPanel);
        if (result == JFileChooser.APPROVE_OPTION) {
          val destFile = fileChooser.getSelectedFile
          if (!destFile.exists) {
            destFile.createNewFile
          }

          val pr = new PrintWriter(new FileOutputStream(destFile))
          pr.append(kevsPanel.getContent)
          pr.flush()
          pr.close()
        }

      }
    })
    buttons.add(btSave)

    var btLoad = new JButton("Load");
    btLoad.addMouseListener(new MouseAdapter() {
      override def mouseClicked(p1: MouseEvent) = {
        //Save Script
        val fileChooser = new JFileChooser
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY)
        fileChooser.setMultiSelectionEnabled(false)
        fileChooser.setAcceptAllFileFilterUsed(true)
        fileChooser.setFileHidingEnabled(true)
        fileChooser.addChoosableFileFilter(new FileFilter() {
          override def getDescription(): String = "KevScript Files"
          override def accept(f: File): Boolean = {
            if (f.isDirectory()) {
              return false;
            }
            val extension = f.getName.substring(f.getName.lastIndexOf(".") + 1)
            if (extension != null) {
              if (extension.equals("kevs")) {
                return true;
              } else {
                return false;
              }
            }

            return false;
          }
        })
        val result = fileChooser.showOpenDialog(kevsPanel);
        if (result == JFileChooser.APPROVE_OPTION) {
          val loadFilePath = fileChooser.getSelectedFile.getAbsolutePath
          val res = new StringBuilder
          scala.io.Source.fromFile(loadFilePath).getLines.foreach {
            l => res.append(l); res.append('\n')
          }
          kevsPanel.setContent(null)
          kevsPanel.setContent(res.toString().trim())
          kevsPanel.repaint()
        }
      }
    })
    buttons.add(btLoad)


    var bTCurrent = new JButton("CurrentModel")
    bTCurrent.addMouseListener(new MouseAdapter() {
      override def mouseClicked(p1: MouseEvent) = {
        val cmdSave = new SaveAsKevScript()
        cmdSave.setKernel(kernel)
        val buffer = new StringBuffer()
        cmdSave.execute(buffer)
        kevsPanel.setContent(null)
        kevsPanel.setContent(buffer.toString)
        kevsPanel.repaint()

      }
    })
    buttons.add(bTCurrent)

    var btExecution = new JButton("execute")
    btExecution.addMouseListener(new MouseAdapter() {
      override def mouseClicked(p1: MouseEvent) = {
        //TODO SAVE CURRENT MODEL
        val script = kevsPanel.getContent
        if (script != null) {
          PositionedEMFHelper.updateModelUIMetaData(kernel)
          val kevSCommand = new KevScriptCommand
          kevSCommand.setKernel(kernel)
          kevSCommand.execute(kevsPanel.getContent)
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
    } else {
      current.dispose;
      current = new KevsEditorFrame
      current.setVisible(true)
    }
    current.toFront
  }
}