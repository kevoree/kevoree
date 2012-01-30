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
import java.awt.{Color, FlowLayout}
import java.awt.event.{FocusEvent, FocusListener, ActionEvent, ActionListener}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/10/11
 * Time: 17:34
 * To change this template use File | Settings | File Templates.
 */

trait DeployUnitForm {
  def createNewDeployUnitPanel(window: HudWindow, kernel: KevoreeUIKernel): Tuple2[JPanel, JPanel] = {
    val layout = new JPanel(new SpringLayout)
    layout.setOpaque(false)

    val groupName_lbl = new JLabel("GroupName", SwingConstants.TRAILING);
    groupName_lbl.setUI(new HudLabelUI());
    groupName_lbl.setOpaque(false);

    val artifactName_lbl = new JLabel("UnitName", SwingConstants.TRAILING);
    artifactName_lbl.setUI(new HudLabelUI());
    artifactName_lbl.setOpaque(false);

    val version_lbl = new JLabel("Version", SwingConstants.TRAILING);
    version_lbl.setUI(new HudLabelUI());
    version_lbl.setOpaque(false);

    val ok_lbl = new JLabel("  ")
    ok_lbl.setUI(new HudLabelUI())
    ok_lbl.setOpaque(false)

    //GroupName
    val groupName_txt = new JTextField()
    groupName_txt.setUI(new HudTextFieldUI())
    groupName_txt.addFocusListener(new FocusListener() {
      def focusGained(p1: FocusEvent) {
        groupName_lbl.setForeground(Color.WHITE)
      }
      def focusLost(p1: FocusEvent) {}
    })
    groupName_txt.setOpaque(false)

    groupName_lbl.setLabelFor(groupName_txt);
    layout.add(groupName_lbl)
    layout.add(groupName_txt)


    //UnitName
    val artifactName_txt = new JTextField()
    artifactName_txt.setUI(new HudTextFieldUI())
    artifactName_txt.addFocusListener(new FocusListener() {
      def focusGained(p1: FocusEvent) {
        artifactName_lbl.setForeground(Color.WHITE)
      }
      def focusLost(p1: FocusEvent) {}
    })
    artifactName_txt.setOpaque(false)

    artifactName_lbl.setLabelFor(artifactName_txt);
    layout.add(artifactName_lbl)
    layout.add(artifactName_txt)

    //version
    val version_txt = new JTextField()
    version_txt.setUI(new HudTextFieldUI())
    version_txt.addFocusListener(new FocusListener() {
      def focusGained(p1: FocusEvent) {
        version_lbl.setForeground(Color.WHITE)
      }
      def focusLost(p1: FocusEvent) {}
    })
    version_txt.setOpaque(false)

    version_lbl.setLabelFor(version_txt);
    layout.add(version_lbl)
    layout.add(version_txt)

    //EXECUTE KEVSCRIPT COMMAND
    val btAdd = new JButton("Add DeployUnit")
    btAdd.setUI(new HudButtonUI)
    btAdd.addActionListener(new ActionListener {
      def actionPerformed(p1: ActionEvent) {
        var proceed = true
        if(artifactName_txt.getText.equals("")) {
          proceed = false
          artifactName_lbl.setForeground(Color.RED)
        }
        if(groupName_txt.getText.equals("")) {
          proceed = false
          groupName_lbl.setForeground(Color.RED)
        }

        if(version_txt.getText.equals("")) {
          proceed = false
          version_lbl.setForeground(Color.RED)
        }
        if (proceed) {
          val cmd = new KevScriptCommand
          cmd.setKernel(kernel)
          cmd.execute("tblock { addDeployUnit \"" + artifactName_txt.getText + "\" \"" + groupName_txt.getText + "\" \"" + version_txt.getText + "\" } ")
          //window.getJDialog.dispose()

          val updateCmd = new ReloadTypePalette
          updateCmd.setKernel(kernel)
          updateCmd.execute(None)

          ok_lbl.setText("OK")
          ok_lbl.setForeground(Color.GREEN)
          window.getContentPane.repaint()

        }
      }
    })
    btAdd.setOpaque(false)

    val bottomLine = new JPanel(new FlowLayout(FlowLayout.CENTER))
    bottomLine.add(btAdd)
    bottomLine.add(ok_lbl)
    bottomLine.setOpaque(false)

    //window.getJDialog.getRootPane.setDefaultButton(btAdd)
    SpringUtilities.makeCompactGrid(layout, 3, 2, 6, 6, 6, 6)
    Tuple2(layout, bottomLine)
  }
}