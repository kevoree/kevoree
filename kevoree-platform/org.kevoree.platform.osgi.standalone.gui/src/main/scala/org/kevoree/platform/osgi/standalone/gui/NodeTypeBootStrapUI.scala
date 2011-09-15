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

import scala.collection.JavaConversions._
import javax.swing._
import java.awt.event.{ActionEvent, ActionListener}
import com.explodingpixels.macwidgets.plaf.{HudLabelUI, HudTextFieldUI, HudComboBoxUI}
import com.explodingpixels.macwidgets.HudWidgetFactory
import java.awt.{Dimension, BorderLayout}
import org.kevoree._
import java.util.Properties

/**
 * User: ffouquet
 * Date: 14/09/11
 * Time: 20:11
 */

class NodeTypeBootStrapUI(pkernel: ContainerRoot) extends JPanel {

  var instanceName: JTextField = _
  var nodeTypeComboBox : JComboBox = _
  var currentProperties = new Properties()

  def getKevName = {instanceName.getText}
  def getKevTypeName = {nodeTypeComboBox.getSelectedItem}

  init(pkernel)
  this.setOpaque(false)

  //CALL INIT
  def init(kernel: ContainerRoot) {
    val nodeTypeModel = new DefaultComboBoxModel

    kernel.getTypeDefinitions.filter(td => td.isInstanceOf[org.kevoree.NodeType] && td.getDeployUnits.exists(du => du.getTargetNodeType != null && du.getTargetNodeType.getName == "JavaSENode")).foreach {
      td =>
        nodeTypeModel.addElement(td.getName)
    }
    nodeTypeComboBox = new JComboBox(nodeTypeModel)
    nodeTypeComboBox.setUI(new HudComboBoxUI())
    instanceName = new JTextField(10)
    instanceName.setUI(new HudTextFieldUI)

    val nodeNameLabel = new JLabel("Node name", SwingConstants.TRAILING);
    nodeNameLabel.setUI(new HudLabelUI());
    nodeNameLabel.setOpaque(false);
    nodeNameLabel.setLabelFor(instanceName);
    val nodeTypeLabel = new JLabel("Node type", SwingConstants.TRAILING);
    nodeTypeLabel.setUI(new HudLabelUI());
    nodeTypeLabel.setOpaque(false);
    nodeTypeLabel.setLabelFor(nodeTypeComboBox);

    val topLayout = new JPanel()
    topLayout.setOpaque(false)
    topLayout.setLayout(new SpringLayout)
    topLayout.add(nodeNameLabel)
    topLayout.add(instanceName)
    topLayout.add(nodeTypeLabel)
    topLayout.add(nodeTypeComboBox)

    SpringUtilities.makeCompactGrid(topLayout, 2, 2, 6, 6, 6, 6)

    val globalLayout = new JPanel()
    globalLayout.setOpaque(false)
    globalLayout.setLayout(new BorderLayout())
    globalLayout.add(topLayout, BorderLayout.NORTH)
    globalLayout.add(getParamsPanel(kernel.getTypeDefinitions.find(td => td.getName == nodeTypeComboBox.getSelectedItem.toString).get), BorderLayout.CENTER)

    nodeTypeComboBox.addActionListener(new ActionListener() {
      override def actionPerformed(actionEvent: ActionEvent) {
        removeAll()
        val globalLayout = new JPanel()
        globalLayout.setOpaque(false)
        globalLayout.setLayout(new BorderLayout())
        globalLayout.add(topLayout, BorderLayout.NORTH)
        currentProperties.clear() // CLEAR PREVIOUS DICTIONARY
        globalLayout.add(getParamsPanel(kernel.getTypeDefinitions.find(td => td.getName == nodeTypeComboBox.getSelectedItem.toString).get), BorderLayout.CENTER)
        add(globalLayout)
        repaint()
        revalidate()
      }
    })

    add(globalLayout)
  }

  def getParamsPanel(nodeTypeDefinition: TypeDefinition): JComponent = {
    val p = new JPanel(new SpringLayout)
    p.setBorder(null)
    if (nodeTypeDefinition.getDictionaryType != null) {
      nodeTypeDefinition.getDictionaryType.getAttributes.foreach {
        att =>
          val l = new JLabel(att.getName, SwingConstants.TRAILING)
          l.setUI(new HudLabelUI)
          p.add(l)
          if (att.getDatatype != null) {
            if (att.getDatatype.startsWith("enum=")) {
              val values: String = att.getDatatype.replaceFirst("enum=", "")
              val valuesModel = new DefaultComboBoxModel
              values.split(",").foreach {
                v => valuesModel.addElement(v)
              }
              val comboBox: JComboBox = HudWidgetFactory.createHudComboBox(valuesModel)
              l.setLabelFor(comboBox)
              p.add(comboBox)
             comboBox.addActionListener(new ActionListener {
               def actionPerformed(actionEvent: ActionEvent): Unit = {
                 currentProperties.put(att.getName,comboBox.getSelectedItem.toString)
               }
             })
            }
          }
          else {
            val textField: JTextField = new JTextField(10)
            textField.setUI(new HudTextFieldUI)
            l.setLabelFor(textField)
            p.add(textField)
            textField.addActionListener(new ActionListener {
              def actionPerformed(p1: ActionEvent) {
                 currentProperties.put(att.getName,textField.getText)
              }
            })
          }
      }
      SpringUtilities.makeCompactGrid(p, nodeTypeDefinition.getDictionaryType.getAttributes.size, 2, 6, 6, 6, 6)
    }


    p.setOpaque(false)
    val scrollPane: JScrollPane = new JScrollPane(p)
    scrollPane.getViewport.setOpaque(false)
    scrollPane.setOpaque(false)
    scrollPane.setBorder(null)
    scrollPane.setPreferredSize(new Dimension(250, 150))
    scrollPane
  }

  /*
  def getValue(att: DictionaryAttribute): String = {
    for (v <- instance.getTypeDefinition.getDictionaryType.getDefaultValues) {
      if (v.getAttribute == att) {
        return v.getValue
      }
    }
    return ""
  }  */


}