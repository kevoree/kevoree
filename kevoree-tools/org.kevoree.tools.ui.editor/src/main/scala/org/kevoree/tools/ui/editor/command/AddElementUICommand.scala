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

import org.kevoree.tools.ui.editor.KevoreeUIKernel
import com.explodingpixels.macwidgets.HudWindow
import java.awt.BorderLayout
import java.awt.event.{ActionEvent, ActionListener}
import java.util.Properties
import org.kevoree.tools.ui.editor.property.SpringUtilities
import com.explodingpixels.macwidgets.plaf.{HudButtonUI, HudTextFieldUI, HudLabelUI, HudComboBoxUI}
import javax.swing._

/**
 * User: ffouquet
 * Date: 13/09/11
 * Time: 13:12
 */

class AddElementUICommand extends Command {

  val LibraryLabel = "Library"
  val DeployUnit = "DeployUnit"
  val ComponentType = "ComponentType"
  val ChannelType = "ChannelType"


  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) {
    kernel = k
  }

  def execute(p: AnyRef) {
    val newPopup = new HudWindow("Add new (Library/DeployUnit/TypeDefinition)")
    newPopup.getJDialog.setSize(400, 200)
    newPopup.getJDialog.setLocationRelativeTo(null)
    val layoutPopup = new JPanel()
    layoutPopup.setOpaque(false)
    layoutPopup.setLayout(new BorderLayout())

    val newElementsModel = new DefaultComboBoxModel
    newElementsModel.addElement(LibraryLabel)
    newElementsModel.addElement(DeployUnit)
    newElementsModel.addElement(ComponentType)
    newElementsModel.addElement(ChannelType)
    newElementsModel.addElement("GroupType")
    val newElements = new JComboBox(newElementsModel)
    newElements.setUI(new HudComboBoxUI())
    val newElementsLabel = new JLabel("Add new : ", SwingConstants.TRAILING)
    newElementsLabel.setUI(new HudLabelUI)
    newElementsLabel.setLabelFor(newElements)
    val layoutPopupTop = new JPanel()
    layoutPopupTop.setOpaque(false)
    layoutPopupTop.add(newElementsLabel)
    layoutPopupTop.add(newElements)

    //LISTENER
    newElements.addActionListener(new ActionListener() {
      override def actionPerformed(actionEvent: ActionEvent) {
        layoutPopup.removeAll()
        newElements.getSelectedItem match {
          case LibraryLabel => {
            val uiElems = createNewLibraryPanel(newPopup)
            layoutPopup.add(uiElems._1, BorderLayout.CENTER)
            layoutPopup.add(uiElems._2, BorderLayout.SOUTH)
          }
          case DeployUnit => {
            val uiElems = createNewDeployUnitPanel(newPopup)
            layoutPopup.add(uiElems._1, BorderLayout.CENTER)
            layoutPopup.add(uiElems._2, BorderLayout.SOUTH)
          }
          case ComponentType => {
            val uiElems = createNewComponentTypePanel(newPopup)
            layoutPopup.add(uiElems._1, BorderLayout.CENTER)
            layoutPopup.add(uiElems._2, BorderLayout.SOUTH)
          }

          case _ =>
        }
        layoutPopup.add(layoutPopupTop, BorderLayout.NORTH)
        layoutPopup.repaint()
        layoutPopup.revalidate()
      }
    })

    layoutPopup.add(layoutPopupTop, BorderLayout.NORTH)
    newPopup.getContentPane.add(layoutPopup)
    val uiElems = createNewLibraryPanel(newPopup)
    layoutPopup.add(uiElems._1, BorderLayout.CENTER)
    layoutPopup.add(uiElems._2, BorderLayout.SOUTH)

    newPopup.getJDialog.setVisible(true)
  }


  val currentProps = new Properties

  def createNewLibraryPanel(window: HudWindow): Tuple2[JPanel, JButton] = {
    val layout = new JPanel(new SpringLayout)
    layout.setOpaque(false)
    val nameTextField = new JTextField()
    nameTextField.setUI(new HudTextFieldUI())
    val nodeNameLabel = new JLabel("Library name", SwingConstants.TRAILING);
    nodeNameLabel.setUI(new HudLabelUI());
    nodeNameLabel.setOpaque(false);
    nodeNameLabel.setLabelFor(nameTextField);
    layout.add(nodeNameLabel)
    layout.add(nameTextField)
    //EXECUTE KEVSCRIPT COMMAND
    val btAdd = new JButton("Add Library")
    btAdd.setUI(new HudButtonUI)
    btAdd.addActionListener(new ActionListener {
      def actionPerformed(p1: ActionEvent) {
        if (nameTextField.getText != "") {
          val cmd = new KevScriptCommand
          cmd.setKernel(kernel)
          cmd.execute("tblock { addLibrary " + nameTextField.getText + " } ")
          window.getJDialog.dispose()
        }
      }
    })
    window.getJDialog.getRootPane.setDefaultButton(btAdd)
    SpringUtilities.makeCompactGrid(layout, 1, 2, 6, 6, 6, 6)
    Tuple2(layout, btAdd)
  }

  def createNewDeployUnitPanel(window: HudWindow): Tuple2[JPanel, JButton] = {
    val layout = new JPanel(new SpringLayout)
    layout.setOpaque(false)

    //UnitName
    val nameTextField = new JTextField()
    nameTextField.setUI(new HudTextFieldUI())
    val nodeNameLabel = new JLabel("UnitName", SwingConstants.TRAILING);
    nodeNameLabel.setUI(new HudLabelUI());
    nodeNameLabel.setOpaque(false);
    nodeNameLabel.setLabelFor(nameTextField);
    layout.add(nodeNameLabel)
    layout.add(nameTextField)
    //GroupName
    val groupnameTextField = new JTextField()
    groupnameTextField.setUI(new HudTextFieldUI())
    val groupNameLabel = new JLabel("GroupName", SwingConstants.TRAILING);
    groupNameLabel.setUI(new HudLabelUI());
    groupNameLabel.setOpaque(false);
    groupNameLabel.setLabelFor(groupnameTextField);
    layout.add(groupNameLabel)
    layout.add(groupnameTextField)
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
          window.getJDialog.dispose()
        }
      }
    })
    window.getJDialog.getRootPane.setDefaultButton(btAdd)
    SpringUtilities.makeCompactGrid(layout, 3, 2, 6, 6, 6, 6)
    Tuple2(layout, btAdd)
  }

  def createNewComponentTypePanel(window: HudWindow): Tuple2[JPanel, JButton] = {
    val layout = new JPanel(new SpringLayout)
    layout.setOpaque(false)
    val nameTextField = new JTextField()
    nameTextField.setUI(new HudTextFieldUI())
    val componentTypeNameLabel = new JLabel("ComponentType name", SwingConstants.TRAILING);
    componentTypeNameLabel.setUI(new HudLabelUI());
    componentTypeNameLabel.setOpaque(false);
    componentTypeNameLabel.setLabelFor(nameTextField);
    layout.add(componentTypeNameLabel)
    layout.add(nameTextField)
    //EXECUTE KEVSCRIPT COMMAND
    val btAdd = new JButton("Add ComponentType")
    btAdd.setUI(new HudButtonUI)
    btAdd.addActionListener(new ActionListener {
      def actionPerformed(p1: ActionEvent) {
        if (nameTextField.getText != "") {
          val cmd = new KevScriptCommand
          cmd.setKernel(kernel)
          cmd.execute("tblock { createComponentType " + nameTextField.getText + " } ")
          window.getJDialog.dispose()
        }
      }
    })
    window.getJDialog.getRootPane.setDefaultButton(btAdd)
    SpringUtilities.makeCompactGrid(layout, 1, 2, 6, 6, 6, 6)
    Tuple2(layout, btAdd)
  }

  def createNewChannelTypePanel(): JPanel = {
    val layout = new JPanel()
    layout
  }

}