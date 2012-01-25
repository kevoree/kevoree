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
import org.kevoree.tools.ui.editor.command.{ReloadTypePalette}
import org.kevoree.{DeployUnit, TypeLibrary, KevoreeFactory}
import org.kevoree.tools.ui.framework.data.{KevoreeHudComboBoxUI}
import javax.swing._
import event.{PopupMenuEvent, PopupMenuListener}
import java.awt.{Color, FlowLayout}
import java.awt.event.{FocusEvent, FocusListener, ActionEvent, ActionListener}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/10/11
 * Time: 17:31
 * To change this template use File | Settings | File Templates.
 */

trait ComponentTypeForm {

  def createNewComponentTypePanel(window: HudWindow, kernel: KevoreeUIKernel): Tuple2[JPanel, JPanel] = {
    val layout = new JPanel(new SpringLayout)
    layout.setOpaque(false)


    val packageTextFieldLabel = new JLabel("Package: ", SwingConstants.TRAILING);
    packageTextFieldLabel.setUI(new HudLabelUI());
    packageTextFieldLabel.setOpaque(false);

    val componentTypeNameLabel = new JLabel("Name: ", SwingConstants.TRAILING);
    componentTypeNameLabel.setUI(new HudLabelUI());
    componentTypeNameLabel.setOpaque(false);

    val libraryCompoLabel = new JLabel("Library: ", SwingConstants.TRAILING)
    libraryCompoLabel.setUI(new HudLabelUI)
    libraryCompoLabel.setOpaque(false);

    val deployUnitComboLabel = new JLabel("Deploy Unit: ", SwingConstants.TRAILING)
    deployUnitComboLabel.setUI(new HudLabelUI)
    deployUnitComboLabel.setOpaque(false);

    val ok_lbl = new JLabel("  ")
    ok_lbl.setUI(new HudLabelUI)


    val packageTextField = new JTextField()
    packageTextField.setUI(new HudTextFieldUI())
    packageTextField.addFocusListener(new FocusListener() {
      def focusGained(p1: FocusEvent) {
        packageTextFieldLabel.setForeground(Color.WHITE)
      }
      def focusLost(p1: FocusEvent) {}
    })
    packageTextField.setOpaque(false);

    packageTextFieldLabel.setLabelFor(packageTextField);
    layout.add(packageTextFieldLabel)
    layout.add(packageTextField)


    val nameTextField = new JTextField()
    nameTextField.setUI(new HudTextFieldUI())
    nameTextField.addFocusListener(new FocusListener() {
      def focusGained(p1: FocusEvent) {
        componentTypeNameLabel.setForeground(Color.WHITE)
      }
      def focusLost(p1: FocusEvent) {}
    })
    nameTextField.setOpaque(false);

    componentTypeNameLabel.setLabelFor(nameTextField);
    layout.add(componentTypeNameLabel)
    layout.add(nameTextField)

    //MENU LIBRARY
    val libraryModel = new DefaultComboBoxModel
    libraryModel.addElement("no library")
    kernel.getModelHandler.getActualModel.getLibraries.foreach {
      lib => libraryModel.addElement(lib)
    }
    val comboLibrary = new JComboBox(libraryModel)
    comboLibrary.setUI(new KevoreeHudComboBoxUI())
    comboLibrary.addPopupMenuListener(new PopupMenuListener {
      def popupMenuWillBecomeVisible(p1: PopupMenuEvent) {
        libraryCompoLabel.setForeground(Color.WHITE)
      }
      def popupMenuWillBecomeInvisible(p1: PopupMenuEvent) {}
      def popupMenuCanceled(p1: PopupMenuEvent) {}
    })
    comboLibrary.setOpaque(false);

    libraryCompoLabel.setLabelFor(comboLibrary)
    layout.add(libraryCompoLabel)
    layout.add(comboLibrary)


    //MENU DeployUnit
    val deployUnitModel = new DefaultComboBoxModel
    deployUnitModel.addElement("no deploy unit")
    kernel.getModelHandler.getActualModel.getDeployUnits.foreach {
      du => deployUnitModel.addElement(du)
    }
    val comboDeployUnit = new JComboBox(deployUnitModel)
    comboDeployUnit.setUI(new KevoreeHudComboBoxUI())
    comboDeployUnit.addPopupMenuListener(new PopupMenuListener() {
      def popupMenuWillBecomeVisible(p1: PopupMenuEvent) {
        deployUnitComboLabel.setForeground(Color.WHITE)
      }
      def popupMenuWillBecomeInvisible(p1: PopupMenuEvent) {}
      def popupMenuCanceled(p1: PopupMenuEvent) {}
    })
    comboDeployUnit.setOpaque(false);

    deployUnitComboLabel.setLabelFor(comboDeployUnit)
    layout.add(deployUnitComboLabel)
    layout.add(comboDeployUnit)


    //EXECUTE KEVSCRIPT COMMAND
    val btAdd = new JButton("Add ComponentType")
    btAdd.setUI(new HudButtonUI)
    btAdd.addActionListener(new ActionListener {
      def actionPerformed(p1: ActionEvent) {

        var proceed = true

        if (packageTextField.getText.equals("")) {
          proceed = false
          packageTextFieldLabel.setForeground(Color.RED)
        }

        if (nameTextField.getText.equals("")) {
          proceed = false
          componentTypeNameLabel.setForeground(Color.RED)
        }

        if (!comboLibrary.getSelectedItem.isInstanceOf[TypeLibrary]) {
          proceed = false
          libraryCompoLabel.setForeground(Color.RED)
        }

        if (!comboDeployUnit.getSelectedItem.isInstanceOf[DeployUnit]) {
          proceed = false
          deployUnitComboLabel.setForeground(Color.RED)
        }

        if (proceed) {

          val newCt = KevoreeFactory.createComponentType
          newCt.setName(nameTextField.getText)

          var packName = packageTextField.getText
          if (!packageTextField.getText.endsWith(".")) {
            packName += "."
          }
          newCt.setBean(packName + newCt.getName)

          comboLibrary.getSelectedItem match {
            case lib: TypeLibrary => {
              lib.addSubTypes(newCt)
            }
            case _@e => System.out.println("Not a library. " + e.getClass)
          }

          comboDeployUnit.getSelectedItem match {
            case du: DeployUnit => {
              newCt.addDeployUnits(du)
            }
            case _@e => System.out.println("Not a DeployUnit. " + e.getClass)
          }

          kernel.getModelHandler.getActualModel.addTypeDefinitions(newCt)

          val updateCmd = new ReloadTypePalette
          updateCmd.setKernel(kernel)
          updateCmd.execute(None)

          ok_lbl.setText("ADDED")
          ok_lbl.setForeground(Color.GREEN)
          window.getContentPane.repaint()
        }
      }
    })
    btAdd.setOpaque(false);

    val bottomLine = new JPanel(new FlowLayout(FlowLayout.CENTER))
    bottomLine.setOpaque(false)
    bottomLine.add(btAdd)
    bottomLine.add(ok_lbl)

    //window.getJDialog.getRootPane.setDefaultButton(btAdd)
    SpringUtilities.makeCompactGrid(layout, 4, 2, 6, 6, 6, 6)
    Tuple2(layout, bottomLine)
  }

}