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
import javax.swing._
import com.explodingpixels.macwidgets.plaf.{HudButtonUI, HudLabelUI, HudTextFieldUI}
import java.awt.event.{ActionEvent, ActionListener}
import org.kevoree.tools.ui.editor.property.SpringUtilities
import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.kevoree.tools.ui.editor.command.{ReloadTypePalette, KevScriptCommand}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/10/11
 * Time: 17:34
 * To change this template use File | Settings | File Templates.
 */

trait DeployUnitForm {
  def createNewDeployUnitPanel(window: HudWindow,kernel : KevoreeUIKernel): Tuple2[JPanel, JButton] = {
      val layout = new JPanel(new SpringLayout)
      layout.setOpaque(false)

    //GroupName
      val groupnameTextField = new JTextField()
      groupnameTextField.setUI(new HudTextFieldUI())
      val groupNameLabel = new JLabel("GroupName", SwingConstants.TRAILING);
      groupNameLabel.setUI(new HudLabelUI());
      groupNameLabel.setOpaque(false);
      groupNameLabel.setLabelFor(groupnameTextField);
      layout.add(groupNameLabel)
      layout.add(groupnameTextField)


      //UnitName
      val nameTextField = new JTextField()
      nameTextField.setUI(new HudTextFieldUI())
      val nodeNameLabel = new JLabel("UnitName", SwingConstants.TRAILING);
      nodeNameLabel.setUI(new HudLabelUI());
      nodeNameLabel.setOpaque(false);
      nodeNameLabel.setLabelFor(nameTextField);
      layout.add(nodeNameLabel)
      layout.add(nameTextField)

      //version
      val versionTextField = new JTextField()
      versionTextField.setUI(new HudTextFieldUI())
      val versionTextLabel = new JLabel("Version", SwingConstants.TRAILING);
      versionTextLabel.setUI(new HudLabelUI());
      versionTextLabel.setOpaque(false);
      versionTextLabel.setLabelFor(versionTextField);
      layout.add(versionTextLabel)
      layout.add(versionTextField)

      //EXECUTE KEVSCRIPT COMMAND
      val btAdd = new JButton("Add DeployUnit")
      btAdd.setUI(new HudButtonUI)
      btAdd.addActionListener(new ActionListener {
        def actionPerformed(p1: ActionEvent) {
          if (nameTextField.getText != "") {
            val cmd = new KevScriptCommand
            cmd.setKernel(kernel)
            cmd.execute("tblock { addDeployUnit \"" + nameTextField.getText + "\" \"" + groupnameTextField.getText + "\" \"" + versionTextField.getText + "\" } ")
            //window.getJDialog.dispose()

            val updateCmd = new ReloadTypePalette
            updateCmd.setKernel(kernel)
            updateCmd.execute(None)
          }
        }
      })
      window.getJDialog.getRootPane.setDefaultButton(btAdd)
      SpringUtilities.makeCompactGrid(layout, 3, 2, 6, 6, 6, 6)
      Tuple2(layout, btAdd)
    }
}