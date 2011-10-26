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
import com.explodingpixels.macwidgets.plaf.{HudButtonUI, HudComboBoxUI, HudLabelUI, HudTextFieldUI}
import java.awt.event.{ActionEvent, ActionListener}
import org.kevoree.tools.ui.editor.property.SpringUtilities
import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.kevoree.tools.ui.editor.command.{ReloadTypePalette, KevScriptCommand}
import org.kevoree.{DeployUnit, TypeLibrary, KevoreeFactory}
import org.kevoree.tools.ui.framework.data.{KevoreeHudComboBoxUI, NamedElementListRenderer, KevoreeComboBox}
import javax.swing._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/10/11
 * Time: 17:31
 * To change this template use File | Settings | File Templates.
 */

trait ComponentTypeForm {

  def createNewComponentTypePanel(window: HudWindow,kernel : KevoreeUIKernel): Tuple2[JPanel, JButton] = {
    val layout = new JPanel(new SpringLayout)
    layout.setOpaque(false)


    val packageTextField = new JTextField()
    packageTextField.setUI(new HudTextFieldUI())
    val packageTextFieldLabel = new JLabel("ComponentType package", SwingConstants.TRAILING);
    packageTextFieldLabel.setUI(new HudLabelUI());
    packageTextFieldLabel.setOpaque(false);
    packageTextFieldLabel.setLabelFor(packageTextField);
    layout.add(packageTextFieldLabel)
    layout.add(packageTextField)


    val nameTextField = new JTextField()
    nameTextField.setUI(new HudTextFieldUI())
    val componentTypeNameLabel = new JLabel("ComponentType name", SwingConstants.TRAILING);
    componentTypeNameLabel.setUI(new HudLabelUI());
    componentTypeNameLabel.setOpaque(false);
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
    val libraryCompoLabel = new JLabel("Library : ", SwingConstants.TRAILING)
    libraryCompoLabel.setUI(new HudLabelUI)
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
    val deployUnitComboLabel = new JLabel("Deploy Unit : ", SwingConstants.TRAILING)
    deployUnitComboLabel.setUI(new HudLabelUI)
    deployUnitComboLabel.setLabelFor(comboDeployUnit)
    layout.add(deployUnitComboLabel)
    layout.add(comboDeployUnit)


    //EXECUTE KEVSCRIPT COMMAND
    val btAdd = new JButton("Add ComponentType")
    btAdd.setUI(new HudButtonUI)
    btAdd.addActionListener(new ActionListener {
      def actionPerformed(p1: ActionEvent) {
        if (nameTextField.getText != "") {

          val newCt = KevoreeFactory.createComponentType
          newCt.setName(nameTextField.getText)
          if(!packageTextField.getText.equals("")) {
            var packName = packageTextField.getText
            if(!packageTextField.getText.endsWith(".")) {
              packName += "."
            }
            newCt.setBean(packName + newCt.getName)
          } else {
            //TODO retreive a package name from the selected deploy unit artifact ID
          }

          comboLibrary.getSelectedItem match {
            case lib:TypeLibrary => {
              lib.addSubTypes(newCt)
            }
            case _ @ e => System.out.println("Not a library. " + e.getClass)
          }

          comboDeployUnit.getSelectedItem match {
            case du : DeployUnit => {
              newCt.addDeployUnits(du)
            }
              case _ @ e => System.out.println("Not a DeployUnit. " + e.getClass)
          }

          kernel.getModelHandler.getActualModel.addTypeDefinitions(newCt)

          /*
          val cmd = new KevScriptCommand
          cmd.setKernel(kernel)
          //TODO: link the componentType with the deployUnit.
          if("no library" != comboLibrary.getSelectedItem){
            cmd.execute("tblock { createComponentType " + nameTextField.getText + " @ "+comboLibrary.getSelectedItem+"  } ")
          } else {
            cmd.execute("tblock { createComponentType " + nameTextField.getText + " } ")
          }
          */
          //window.getJDialog.dispose()

            val updateCmd = new ReloadTypePalette
            updateCmd.setKernel(kernel)
            updateCmd.execute(None)
        }
      }
    })
    window.getJDialog.getRootPane.setDefaultButton(btAdd)
    SpringUtilities.makeCompactGrid(layout, 4, 2, 6, 6, 6, 6)
    Tuple2(layout, btAdd)
  }


}