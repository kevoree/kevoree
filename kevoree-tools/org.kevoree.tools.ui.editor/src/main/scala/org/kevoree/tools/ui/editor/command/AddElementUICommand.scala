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
import com.explodingpixels.macwidgets.plaf.{HudLabelUI, HudComboBoxUI}
import javax.swing._
import org.kevoree.tools.ui.editor.form._

/**
 * User: ffouquet
 * Date: 13/09/11
 * Time: 13:12
 */

class   AddElementUICommand extends Command with ComponentTypeForm with DeployUnitForm with LibraryForm with ChannelTypeForm with PortTypeForm with DictionaryForm {

  val LibraryLabel = "Library"
  val DeployUnit = "DeployUnit"
  val ComponentType = "ComponentType"
  val ChannelType = "ChannelType"
  val GroupType = "GroupType"


  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) {
    kernel = k
  }

  def execute(p: AnyRef) {
    val popupWindow = new HudWindow("Add new (Library/DeployUnit/TypeDefinition/Port/Property)")
    popupWindow.getJDialog.setSize(400, 200)
    popupWindow.getJDialog.setLocationRelativeTo(null)
    val popupLayout = new JPanel()
    popupLayout.setOpaque(false)
    popupLayout.setLayout(new BorderLayout())

    val elemComboBoxModel = new DefaultComboBoxModel
    elemComboBoxModel.addElement(LibraryLabel)
    elemComboBoxModel.addElement(DeployUnit)
    elemComboBoxModel.addElement(ComponentType)
    //newElementsModel.addElement(ChannelType)
    //newElementsModel.addElement(GroupType)
    if (p != null) {
      elemComboBoxModel.setSelectedItem(p)
    }
    val elemComboBox = new JComboBox(elemComboBoxModel)
    elemComboBox.setUI(new HudComboBoxUI())
    val elemLabel = new JLabel("Add new : ", SwingConstants.TRAILING)
    elemLabel.setUI(new HudLabelUI)
    elemLabel.setLabelFor(elemComboBox)
    val popupTopLayout = new JPanel()
    popupTopLayout.setOpaque(false)
    popupTopLayout.add(elemLabel)
    popupTopLayout.add(elemComboBox)

    //LISTENER
    elemComboBox.addActionListener(new ActionListener() {
      override def actionPerformed(actionEvent: ActionEvent) {
        popupLayout.removeAll()
        elemComboBox.getSelectedItem match {
          case LibraryLabel => {
            val uiElems = createNewLibraryPanel(popupWindow, kernel)
            popupLayout.add(uiElems._1, BorderLayout.CENTER)
            popupLayout.add(uiElems._2, BorderLayout.SOUTH)
          }
          case DeployUnit => {
            val uiElems = createNewDeployUnitPanel(popupWindow, kernel)
            popupLayout.add(uiElems._1, BorderLayout.CENTER)
            popupLayout.add(uiElems._2, BorderLayout.SOUTH)
          }
          case ComponentType => {
            val uiElems = createNewComponentTypePanel(popupWindow, kernel)
            popupLayout.add(uiElems._1, BorderLayout.CENTER)
            popupLayout.add(uiElems._2, BorderLayout.SOUTH)
          }
          case _@e => throw new UnsupportedOperationException("No popup implemeted for item:" + e)
        }
        popupLayout.add(popupTopLayout, BorderLayout.NORTH)
        popupLayout.repaint()
        popupLayout.revalidate()
      }
    })

    popupLayout.add(popupTopLayout, BorderLayout.NORTH)
    popupWindow.getContentPane.add(popupLayout)
    val uiElems = if (p != null) {
      p match {
        case LibraryLabel => createNewLibraryPanel(popupWindow, kernel)
        case DeployUnit => createNewDeployUnitPanel(popupWindow, kernel)
        case ComponentType => createNewComponentTypePanel(popupWindow, kernel)
        case _ => createNewLibraryPanel(popupWindow, kernel)
      }
    } else {
      createNewLibraryPanel(popupWindow, kernel)
    }
    popupLayout.add(uiElems._1, BorderLayout.CENTER)
    popupLayout.add(uiElems._2, BorderLayout.SOUTH)

    popupWindow.getJDialog.setVisible(true)
  }

  //val currentProps = new Properties


}