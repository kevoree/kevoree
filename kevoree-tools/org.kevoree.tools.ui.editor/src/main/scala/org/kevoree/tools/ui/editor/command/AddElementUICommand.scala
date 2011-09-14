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
import javax.swing._
import com.explodingpixels.macwidgets.plaf.{HudTextFieldUI, HudLabelUI, HudComboBoxUI}

/**
 * User: ffouquet
 * Date: 13/09/11
 * Time: 13:12
 */

class AddElementUICommand extends Command {

  val LibraryLabel = "Library"
  val DeployUnit = "DeployUnit"

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
    newElementsModel.addElement("ComponentType")
    newElementsModel.addElement("ChannelType")
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
          case LibraryLabel => layoutPopup.add(createNewLibraryPanel(), BorderLayout.CENTER)
          case DeployUnit => layoutPopup.add(createNewDeployUnitPanel(), BorderLayout.CENTER)
          case _ =>
        }
        layoutPopup.add(layoutPopupTop, BorderLayout.NORTH)
        layoutPopup.repaint()
        layoutPopup.revalidate()
      }
    })

    layoutPopup.add(layoutPopupTop, BorderLayout.NORTH)
    newPopup.getContentPane.add(layoutPopup)
    layoutPopup.add(createNewLibraryPanel(), BorderLayout.CENTER)
    newPopup.getJDialog.setVisible(true)
  }


  def createNewLibraryPanel(): JPanel = {
    val layout = new JPanel()
    layout.setOpaque(false)
    val nameTextField = new JTextField()
    nameTextField.setUI(new HudTextFieldUI())
    layout.add(nameTextField)
    layout
  }

  def createNewDeployUnitPanel(): JPanel = {
    val layout = new JPanel()
    layout
  }

  def createNewComponentTypePanel(): JPanel = {
    val layout = new JPanel()
    layout
  }

  def createNewChannelTypePanel(): JPanel = {
    val layout = new JPanel()
    layout
  }

}