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
package org.kevoree.tools.ui.editor.form

import com.explodingpixels.macwidgets.HudWindow
import com.explodingpixels.macwidgets.plaf.{HudButtonUI, HudLabelUI, HudTextFieldUI}
import org.kevoree.tools.ui.editor.property.SpringUtilities
import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.kevoree.tools.ui.editor.command.{ReloadTypePalette, KevScriptCommand}
import javax.swing._
import java.awt.event.{FocusEvent, FocusListener, ActionEvent, ActionListener}
import java.awt.{Dimension, Color, FlowLayout}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/10/11
 * Time: 17:35
 * To change this template use File | Settings | File Templates.
 */

trait LibraryForm {

  def createNewLibraryPanel(window: HudWindow, kernel: KevoreeUIKernel): Tuple2[JPanel, JPanel] = {

    val layout = new JPanel(new SpringLayout)
    layout.setOpaque(false)

    val bottomLine = new JPanel()
    bottomLine.setLayout(new FlowLayout(FlowLayout.CENTER))


    val okLabel = new JLabel("  ")
    okLabel.setUI(new HudLabelUI)


    val libName_lbl = new JLabel("Library name", SwingConstants.TRAILING);
    libName_lbl.setUI(new HudLabelUI());
    libName_lbl.setOpaque(false);

    val libName_txt = new JTextField()
    libName_txt.setUI(new HudTextFieldUI())
    libName_txt.addFocusListener(new FocusListener() {
      def focusLost(p1: FocusEvent) {}
      def focusGained(p1: FocusEvent) {
        okLabel.setText("  ")
        libName_lbl.setForeground(Color.WHITE)
        window.getJDialog.repaint()
      }
    })
 
    libName_lbl.setLabelFor(libName_txt);
    layout.add(libName_lbl)
    layout.add(libName_txt)
    //EXECUTE KEVSCRIPT COMMAND


    val btAdd = new JButton("Add Library")
    btAdd.setUI(new HudButtonUI)
    btAdd.addActionListener(new ActionListener {
      def actionPerformed(p1: ActionEvent) {
        if (libName_txt.getText != "") {
          val cmd = new KevScriptCommand
          cmd.setKernel(kernel)
          cmd.execute("tblock { addLibrary " + libName_txt.getText + " } ")
          val updateCmd = new ReloadTypePalette
          updateCmd.setKernel(kernel)
          updateCmd.execute(None)
          okLabel.setText("OK")
          okLabel.setForeground(Color.GREEN)
          window.getContentPane.repaint()
          //window.getJDialog.dispose()
        } else {
          libName_lbl.setForeground(Color.RED)
        }
      }
    })
    btAdd.setOpaque(false);

    bottomLine.add(btAdd)
    bottomLine.add(okLabel)
    bottomLine.setOpaque(false);
    //window.getJDialog.getRootPane.setDefaultButton(btAdd)
    SpringUtilities.makeCompactGrid(layout, 1, 2, 6, 6, 6, 6)
    Tuple2(layout, bottomLine)
  }
}