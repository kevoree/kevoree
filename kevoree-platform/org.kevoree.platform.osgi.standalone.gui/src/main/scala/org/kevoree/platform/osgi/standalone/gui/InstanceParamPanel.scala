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
package org.kevoree.platform.osgi.standalone.gui

import java.util.Properties
import java.awt.event.{ActionEvent, ActionListener}
import com.explodingpixels.macwidgets.plaf.{HudTextFieldUI, HudLabelUI}
import javax.swing._
import com.explodingpixels.macwidgets.{IAppWidgetFactory, HudWidgetFactory}
import event.{DocumentEvent, DocumentListener}
import java.awt.BorderLayout
import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoree.{GroupType, ContainerRoot, TypeDefinition}


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 18/10/11
 * Time: 16:13
 * To change this template use File | Settings | File Templates.
 */

class InstanceParamPanel(pnodeTypeDefinition: TypeDefinition, defaultName: String) extends JPanel {

  var nodeTypeDefinition = pnodeTypeDefinition
  def setNodeTypeDefinition(pnodeTypeDefinition: TypeDefinition){
    nodeTypeDefinition = pnodeTypeDefinition
  }
  
  val stringLabel = nodeTypeDefinition match {
    case c: GroupType => "Group name"
    case c: org.kevoree.NodeType => "Node name"
  }
  val instanceTextField = HudWidgetFactory.createHudTextField("");
  instanceTextField.setText(defaultName)
  instanceTextField.setUI(new HudTextFieldUI)
  instanceTextField.setText(defaultName)
  val instanceNameLabel = new JLabel(stringLabel, SwingConstants.TRAILING);
  instanceNameLabel.setUI(new HudLabelUI());
  instanceNameLabel.setOpaque(false);
  instanceNameLabel.setLabelFor(instanceTextField);

  def getInstanceName = instanceTextField.getText

  var currentProperties = new Properties()
  this.setLayout(new BorderLayout)
  val global = this
  this.setOpaque(false)
  reload()

  var previousFound = false
  instanceTextField.getDocument.addDocumentListener(new DocumentListener() {
    def insertUpdate(p1: DocumentEvent) {
      execute()
    }

    def removeUpdate(p1: DocumentEvent) {
      execute()
    }

    def changedUpdate(p1: DocumentEvent) {
      execute()
    }

    def execute() {

      if (nodeTypeDefinition.eContainer.asInstanceOf[ContainerRoot].getAllInstances.exists(n => n.getName == instanceTextField.getText && n.getTypeDefinition == nodeTypeDefinition)) {
        if (!previousFound) {
          previousFound = true
          reload()
        }
      } else {
        if (previousFound) {
          previousFound = false
          reload()
        }
      }
    }
  })

  def reload() {
    currentProperties.clear()
    val props = getDefValue(instanceTextField.getText, nodeTypeDefinition.eContainer.asInstanceOf[ContainerRoot], nodeTypeDefinition)
    currentProperties.putAll(props)
    global.removeAll()
    val p = new JPanel(new SpringLayout)
    p.setOpaque(false)
    global.add(p, BorderLayout.CENTER)
    p.setBorder(null)
    
    p.add(instanceNameLabel)
    p.add(instanceTextField)
    
    if (nodeTypeDefinition.getDictionaryType.isDefined) {
      nodeTypeDefinition.getDictionaryType.get.getAttributes.foreach {
        att =>
          val l = new JLabel(att.getName, SwingConstants.TRAILING)
          l.setUI(new HudLabelUI)
          p.add(l)
          if (att.getDatatype != "") {
            if (att.getDatatype.startsWith("enum=")) {
              val values: String = att.getDatatype.replaceFirst("enum=", "")
              val valuesModel = new DefaultComboBoxModel
              values.split(",").foreach {
                v => valuesModel.addElement(v)
              }
              val comboBox: JComboBox = HudWidgetFactory.createHudComboBox(valuesModel)
              l.setLabelFor(comboBox)
              p.add(comboBox)

              if (props.get(att.getName) != null) {
                comboBox.setSelectedItem(props.get(att.getName))
              }
              comboBox.addActionListener(new ActionListener {
                def actionPerformed(actionEvent: ActionEvent): Unit = {
                  currentProperties.put(att.getName, comboBox.getSelectedItem.toString)
                }
              })
            }
          } else {
            val textField: JTextField = new JTextField(10)
            textField.setUI(new HudTextFieldUI)
            l.setLabelFor(textField)
            p.add(textField)
            if (props.get(att.getName) != null) {
              textField.setText(props.get(att.getName).toString)
            }
            textField.addActionListener(new ActionListener {
              def actionPerformed(p1: ActionEvent) {
                currentProperties.put(att.getName, textField.getText)
              }
            })
          }
      }
      SpringUtilities.makeCompactGrid(p, nodeTypeDefinition.getDictionaryType.get.getAttributes.size+1, 2, 6, 6, 6, 6)
      //p.revalidate()
      //p.repaint()
    } else {
      SpringUtilities.makeCompactGrid(p, 1, 2, 3, 3, 3, 3)
    }
  }

  private def getDefValue(instanceName: String, model: ContainerRoot, typeDef: TypeDefinition): Properties = {
    val props = new Properties
    model.getTypeDefinitions.find(td => td.getName == typeDef.getName).map(td => {
      if (td.getDictionaryType.isDefined && td.getDictionaryType.get.getDefaultValues != null) {
        td.getDictionaryType.get.getDefaultValues.foreach {
          defVal =>
            props.put(defVal.getAttribute.getName, defVal.getValue)
        }
      }
    })
    model.getAllInstances.find(ist => ist.getName == instanceName).map {
      istFound =>
        if (istFound.getDictionary.isDefined) {
          istFound.getDictionary.get.getValues.foreach {
            dicVal =>
              props.put(dicVal.getAttribute.getName, dicVal.getValue)
          }
        }
    }
    props
  }


}