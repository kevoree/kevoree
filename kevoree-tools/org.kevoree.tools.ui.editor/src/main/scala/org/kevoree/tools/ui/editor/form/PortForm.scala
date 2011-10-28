package org.kevoree.tools.ui.editor.form

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

import com.explodingpixels.macwidgets.HudWindow
import java.awt.event.{ActionEvent, ActionListener}
import org.kevoree.tools.ui.editor.property.SpringUtilities
import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.kevoree.tools.ui.editor.command.ReloadTypePalette
import org.kevoree.tools.ui.framework.data.KevoreeHudComboBoxUI
import javax.swing._
import com.explodingpixels.macwidgets.plaf.{HudCheckBoxUI, HudButtonUI, HudLabelUI, HudTextFieldUI}
import org.kevoree._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/10/11
 * Time: 17:31
 * To change this template use File | Settings | File Templates.
 */

trait PortForm {

  val providedLabel = "Provided"
  val requiredLabel = "Required"
  val noPortTypeAvailableLabel = "No port type available"

  def createPortPanel(window: HudWindow, kernel: KevoreeUIKernel, componentType: ComponentType): Tuple2[JPanel, JButton] = {
    val layout = new JPanel(new SpringLayout)
    layout.setOpaque(false)

    //Port kind
    val portKindComboBoxModel = new DefaultComboBoxModel
    portKindComboBoxModel.addElement(providedLabel)
    portKindComboBoxModel.addElement(requiredLabel)
    val portKindComboBox = new JComboBox(portKindComboBoxModel)
    portKindComboBox.setUI(new KevoreeHudComboBoxUI())
    val portKindComboBoxLabel = new JLabel("Type: ", SwingConstants.TRAILING)
    portKindComboBoxLabel.setUI(new HudLabelUI)
    portKindComboBoxLabel.setLabelFor(portKindComboBox)
    layout.add(portKindComboBoxLabel)
    layout.add(portKindComboBox)



    //Port kind
    val portTypeComboBoxModel = new DefaultComboBoxModel
    val portTypeList = kernel.getModelHandler.getActualModel.getTypeDefinitions.filter(td => td.isInstanceOf[PortType])
    if (portTypeList.size > 0) {
      portTypeList.foreach {
        portType => portTypeComboBoxModel.addElement(portType)
      }
    } else {
      portTypeComboBoxModel.addElement(noPortTypeAvailableLabel)
    }
    val portTypeComboBox = new JComboBox(portTypeComboBoxModel)
    portTypeComboBox.setUI(new KevoreeHudComboBoxUI())
    val portTypeComboBoxLabel = new JLabel("Nature: ", SwingConstants.TRAILING)
    portTypeComboBoxLabel.setUI(new HudLabelUI)
    portTypeComboBoxLabel.setLabelFor(portTypeComboBox)
    layout.add(portTypeComboBoxLabel)
    layout.add(portTypeComboBox)


    val portNameTxt = new JTextField()
    portNameTxt.setUI(new HudTextFieldUI())
    val portNameLbl = new JLabel("Name: ", SwingConstants.TRAILING);
    portNameLbl.setUI(new HudLabelUI());
    portNameLbl.setOpaque(false);
    portNameLbl.setLabelFor(portNameTxt);
    layout.add(portNameLbl)
    layout.add(portNameTxt)


    val optionalCheckBox = new JCheckBox()
    optionalCheckBox.setUI(new HudCheckBoxUI())
    val optionalLabel = new JLabel("Optional:", SwingConstants.TRAILING);
    optionalLabel.setUI(new HudLabelUI());
    optionalLabel.setOpaque(false);
    optionalLabel.setLabelFor(optionalCheckBox);
    layout.add(optionalLabel)
    layout.add(optionalCheckBox)

    //EXECUTE KEVSCRIPT COMMAND
    val btAdd = new JButton("Add Port")
    btAdd.setUI(new HudButtonUI)
    btAdd.addActionListener(new ActionListener {
      def actionPerformed(p1: ActionEvent) {
        if(portTypeComboBoxModel.getSelectedItem != noPortTypeAvailableLabel && !portNameTxt.getText.equals("")) {

          val portType = portTypeComboBoxModel.getSelectedItem.asInstanceOf[PortType]

          val portTypeRef = KevoreeFactory.createPortTypeRef
          portTypeRef.setRef(portType)
          portTypeRef.setName(portNameTxt.getText)
          portTypeRef.setOptional(optionalCheckBox.isSelected)

          portKindComboBoxModel.getSelectedItem.asInstanceOf[String] match {
            case `providedLabel` => {
              componentType.addProvided(portTypeRef)
            }
            case `requiredLabel` => {
              componentType.addRequired(portTypeRef)
            }
          }
        }
        kernel.getEditorPanel.getTypeEditorPanel.refresh()
      }
    })
    window.getJDialog.getRootPane.setDefaultButton(btAdd)
    SpringUtilities.makeCompactGrid(layout, 4, 2, 6, 6, 6, 6)
    Tuple2(layout, btAdd)
  }


}