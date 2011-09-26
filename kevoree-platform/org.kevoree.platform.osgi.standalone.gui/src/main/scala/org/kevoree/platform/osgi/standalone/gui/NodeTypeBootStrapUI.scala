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

import javax.swing._
import event.{DocumentEvent, DocumentListener}
import java.awt.event.{ActionEvent, ActionListener}
import java.awt.{Dimension, BorderLayout}
import org.kevoree._
import framework.KevoreeXmiHelper
import java.util.Properties
import com.explodingpixels.macwidgets.{IAppWidgetFactory, HudWidgetFactory}
import scala.collection.JavaConversions._
import com.explodingpixels.macwidgets.plaf.{HudButtonUI, HudLabelUI, HudTextFieldUI, HudComboBoxUI}
import org.eclipse.emf.common.util.URI

/**
 * User: ffouquet
 * Date: 14/09/11
 * Time: 20:11
 */

class NodeTypeBootStrapUI(pkernel: ContainerRoot) extends JPanel {

  var instanceName: JTextField = _
  var nodeTypeComboBox: JComboBox = _
  var currentProperties = new Properties()
  var currentModel = pkernel

  var previousTopLayout : JComponent = null

  def getCurrentModel: ContainerRoot = currentModel

  private var previousFound = false

  def getKevName = {
    instanceName.getText
  }

  def getKevTypeName = {
    nodeTypeComboBox.getSelectedItem
  }

  init(pkernel)
  this.setOpaque(false)

  //CALL INIT
  def init(kernel: ContainerRoot) {
    currentModel = kernel
    this.removeAll()
    val nodeTypeModel = new DefaultComboBoxModel

    kernel.getTypeDefinitions.filter(td => td.isInstanceOf[org.kevoree.NodeType] && td.getDeployUnits.exists(du => du.getTargetNodeType != null )).foreach {
      td =>
        nodeTypeModel.addElement(td.getName)
    }
    nodeTypeComboBox = new JComboBox(nodeTypeModel)
    nodeTypeComboBox.setUI(new HudComboBoxUI())
    instanceName = new JTextField(10)
    instanceName.setUI(new HudTextFieldUI)

    instanceName.getDocument.addDocumentListener(new DocumentListener() {
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
        if (currentModel.getNodes.exists(n => n.getName == instanceName.getText)) {
          if (!previousFound) {
            previousFound = true
            nodeTypeComboBox.setSelectedItem(currentModel.getNodes.find(n => n.getName == instanceName.getText).get.getTypeDefinition.getName)
            refreshBottom()
          }
        } else {
          if (previousFound) {
            previousFound = false
            refreshBottom()
          }
        }
      }

      def refreshBottom() {
        currentProperties.clear() // CLEAR PREVIOUS DICTIONARY
        removeAll()
        val globalLayout = new JPanel()
        globalLayout.setOpaque(false)
        globalLayout.setLayout(new BorderLayout())
        globalLayout.add(previousTopLayout, BorderLayout.NORTH)
        globalLayout.add(
          getParamsPanel(
            kernel.getTypeDefinitions.find(td => td.getName == nodeTypeComboBox.getSelectedItem.toString).get
            , getDefValue(instanceName.getText, currentModel,nodeTypeComboBox.getSelectedItem.toString)), BorderLayout.CENTER)
        add(globalLayout)
        repaint()
        revalidate()
      }

    })

    val nodeNameLabel = new JLabel("Node name", SwingConstants.TRAILING);
    nodeNameLabel.setUI(new HudLabelUI());
    nodeNameLabel.setOpaque(false);
    nodeNameLabel.setLabelFor(instanceName);
    val nodeTypeLabel = new JLabel("Node type", SwingConstants.TRAILING);
    nodeTypeLabel.setUI(new HudLabelUI());
    nodeTypeLabel.setOpaque(false);
    nodeTypeLabel.setLabelFor(nodeTypeComboBox);

    val btBrowse: JButton = new JButton("Browse")
    btBrowse.setUI(new HudButtonUI)
    btBrowse.addActionListener(new ActionListener {
      def actionPerformed(p1: ActionEvent) {
        val filechooser: JFileChooser = new JFileChooser
        val returnVal: Int = filechooser.showOpenDialog(null)
        if (filechooser.getSelectedFile != null && returnVal == JFileChooser.APPROVE_OPTION) {
          try {
            val lastLoadedModel = URI.createFileURI(filechooser.getSelectedFile.getAbsolutePath).toString
            val newModel = KevoreeXmiHelper.load(lastLoadedModel)
            init(newModel)
            repaint()
            revalidate()
          } catch {
            case _@e =>
          }
        }
      }
    })
    val bootModelLabel = new JLabel("Bootstrap", SwingConstants.TRAILING);
    bootModelLabel.setUI(new HudLabelUI());
    bootModelLabel.setOpaque(false);
    bootModelLabel.setLabelFor(btBrowse);

    val topLayout = new JPanel()
    topLayout.setOpaque(false)
    topLayout.setLayout(new SpringLayout)
    topLayout.add(nodeNameLabel)
    topLayout.add(instanceName)
    topLayout.add(nodeTypeLabel)
    topLayout.add(nodeTypeComboBox)
    topLayout.add(bootModelLabel)
    topLayout.add(btBrowse)
    SpringUtilities.makeCompactGrid(topLayout, 3, 2, 6, 6, 6, 6)

    previousTopLayout = topLayout

    val globalLayout = new JPanel()
    globalLayout.setOpaque(false)
    globalLayout.setLayout(new BorderLayout())
    globalLayout.add(topLayout, BorderLayout.NORTH)
    globalLayout.add(
      getParamsPanel(
        kernel.getTypeDefinitions.find(td => td.getName == nodeTypeComboBox.getSelectedItem.toString).get
        , getDefValue(instanceName.getText, currentModel,nodeTypeComboBox.getSelectedItem.toString)), BorderLayout.CENTER)

    nodeTypeComboBox.addActionListener(new ActionListener() {
      override def actionPerformed(actionEvent: ActionEvent) {
        removeAll()
        val globalLayout = new JPanel()
        globalLayout.setOpaque(false)
        globalLayout.setLayout(new BorderLayout())
        globalLayout.add(topLayout, BorderLayout.NORTH)
        currentProperties.clear() // CLEAR PREVIOUS DICTIONARY
        //globalLayout.add(getParamsPanel(kernel.getTypeDefinitions.find(td => td.getName == nodeTypeComboBox.getSelectedItem.toString).get), BorderLayout.CENTER)
        globalLayout.add(
          getParamsPanel(
            kernel.getTypeDefinitions.find(td => td.getName == nodeTypeComboBox.getSelectedItem.toString).get
            , getDefValue(instanceName.getText, currentModel,nodeTypeComboBox.getSelectedItem.toString)), BorderLayout.CENTER)
        add(globalLayout)
        repaint()
        revalidate()
      }
    })

    add(globalLayout)
  }

  def refreshBottom(){

  }


  def getParamsPanel(nodeTypeDefinition: TypeDefinition, props: Properties): JComponent = {
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

              if (props.get(att.getName) != null) {
                comboBox.setSelectedItem(props.get(att.getName))
              }
              comboBox.addActionListener(new ActionListener {
                def actionPerformed(actionEvent: ActionEvent): Unit = {
                  currentProperties.put(att.getName, comboBox.getSelectedItem.toString)
                }
              })
            }
          }
          else {
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
      SpringUtilities.makeCompactGrid(p, nodeTypeDefinition.getDictionaryType.getAttributes.size, 2, 6, 6, 6, 6)
    }


    p.setOpaque(false)
    val scrollPane: JScrollPane = new JScrollPane(p)
    IAppWidgetFactory.makeIAppScrollPane(scrollPane)
    scrollPane.getViewport.setOpaque(false)
    scrollPane.setOpaque(false)
    scrollPane.setBorder(null)
    scrollPane.setPreferredSize(new Dimension(250, 80))
    scrollPane
  }

  def getDefValue(nodeName: String, model: ContainerRoot, typeName : String): Properties = {
    val props = new Properties
    model.getTypeDefinitions.find(td => td.getName == typeName).map( td => {
      if(td.getDictionaryType != null && td.getDictionaryType.getDefaultValues != null){
        td.getDictionaryType.getDefaultValues.foreach{ defVal =>
              props.put(defVal.getAttribute.getName, defVal.getValue)
        }
      }
    })
    model.getNodes.find(node => node.getName == nodeName).map {
      nodeFound =>
        if (nodeFound.getDictionary != null) {
          nodeFound.getDictionary.getValues.foreach {
            dicVal =>
              props.put(dicVal.getAttribute.getName, dicVal.getValue)
          }
        }
    }
    props
  }
}