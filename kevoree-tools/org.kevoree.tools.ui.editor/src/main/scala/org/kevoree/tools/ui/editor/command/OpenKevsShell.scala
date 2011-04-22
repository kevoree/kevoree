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

import javax.swing.filechooser.FileFilter
import javax.swing.{JButton, BoxLayout, JPanel, JFileChooser}
import java.awt.BorderLayout
import java.awt.event.{MouseEvent, MouseAdapter}
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.framework.KevoreeXmiHelper
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.PrintWriter
import java.util.Random
import org.kevoree.tools.marShell.parser.ParserUtil
import org.kevoree.tools.marShellGUI.{KevsModelHandlers, KevsFrame}
import org.kevoree.tools.ui.editor.{PositionedEMFHelper, KevoreeUIKernel}

class OpenKevsShell extends Command {

  private var current: KevsEditorFrame = null;
  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k

  class KevsEditorFrame extends KevsFrame {
    var buttons = new JPanel
    buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS))


    var btSave = new JButton("Save");
    btSave.addMouseListener(new MouseAdapter() {
        override def mouseClicked(p1: MouseEvent) = {
          //Save Script

          val fileChooser = new JFileChooser
          fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY)
          fileChooser.setMultiSelectionEnabled(false)
          fileChooser.setAcceptAllFileFilterUsed(true)
          fileChooser.setFileHidingEnabled(true)
          fileChooser.addChoosableFileFilter(new FileFilter() {

              override def getDescription() : String = "KevScript Files"

              override def accept(f : File) : Boolean = {
                if (f.isDirectory()) {
                  return false;
                }

                val extension = f.getName.substring(f.getName.lastIndexOf(".")+1)
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
          var result = fileChooser.showSaveDialog(kevsPanel);
          if(result == JFileChooser.APPROVE_OPTION) {
            var destFile = fileChooser.getSelectedFile
            if( !destFile.exists) {
              destFile.createNewFile
            }

            var pr = new PrintWriter(new FileOutputStream(destFile))
            pr.append(kevsPanel.codeEditor.getText)
            pr.flush
            pr.close
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
              
              override def getDescription() : String = "KevScript Files"

              override def accept(f : File) : Boolean = {
                if (f.isDirectory()) {
                  return false;
                }

                val extension = f.getName.substring(f.getName.lastIndexOf(".")+1)
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
          var result = fileChooser.showOpenDialog(kevsPanel);
          if(result == JFileChooser.APPROVE_OPTION) {
            val loadFilePath = fileChooser.getSelectedFile.getAbsolutePath()
            val kevsContent = ParserUtil.loadFile(loadFilePath)
            kevsPanel.codeEditor.setText(null)
            kevsPanel.codeEditor.setText(kevsContent.toString.trim)
            kevsPanel.repaint()
          }
        }
      })
    buttons.add(btLoad)


    var btExecution = new JButton("execute");
    btExecution.addMouseListener(new MouseAdapter() {
        override def mouseClicked(p1: MouseEvent) = {
          //TODO SAVE CURRENT MODEL
          val script = kevsPanel.getModel
          if (script != null) {
            import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
            PositionedEMFHelper.updateModelUIMetaData(kernel)
            var result = script.interpret(KevsInterpreterContext(kernel.getModelHandler.getActualModel))
            println("Interpreter Result : " + result)
            if (result) {
              //reload
              val file = File.createTempFile("kev", new Random().nextInt + "")

              KevoreeXmiHelper.save("file:///"+file.getAbsolutePath, kernel.getModelHandler().getActualModel);

              var loadCMD = new LoadModelCommand
              loadCMD.setKernel(kernel)
              loadCMD.execute("file:///"+file.getAbsolutePath)


            }
          }
        }
      })
    buttons.add(btExecution)


    add(buttons, BorderLayout.SOUTH)


  }

  def execute(p: Object) = {

    KevsModelHandlers.put(1,kernel.getModelHandler.getActualModel)
    kernel.getModelHandler.addListenerCommand(new Command{
      def execute(p:java.lang.Object): Unit = {
          KevsModelHandlers.put(1,kernel.getModelHandler.getActualModel)
      }
    })


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