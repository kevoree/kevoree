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

import java.awt.event.{ActionEvent, ActionListener}
import org.kevoree._
import framework.KevoreeXmiHelper
import com.explodingpixels.macwidgets.{IAppWidgetFactory, HudWidgetFactory}
import com.explodingpixels.macwidgets.plaf.{HudButtonUI, HudLabelUI, HudTextFieldUI, HudComboBoxUI}
import java.awt.{FlowLayout, Dimension, BorderLayout}
import javax.swing._

/**
 * User: ffouquet
 * Date: 14/09/11
 * Time: 20:11
 */

class NodeTypeBootStrapUI(pkernel: ContainerRoot) extends JPanel {

  var nodeTypeComboBox: JComboBox = _
  var groupTypeComboBox: JComboBox = _
  var nodeInstancePanel : InstanceParamPanel = _
  var groupInstancePanel : InstanceParamPanel = _

  def getKevName = {
    nodeInstancePanel.getInstanceName
  }
  def getKevGroupName = {
    groupInstancePanel.getInstanceName
  }
  def getKevTypeName = {
    nodeTypeComboBox.getSelectedItem
  }

  def getKevGroupTypeName = {
    groupTypeComboBox.getSelectedItem
  }

  private var model = pkernel
  def getCurrentModel = model

  this.setOpaque(false)
  init(pkernel)

  //CALL INIT
  def init(kernel: ContainerRoot) {
    this.setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS))
    this.removeAll()
    model = kernel
    val nodeTypeModel = new DefaultComboBoxModel
    kernel.getTypeDefinitions.
      filter(td => td.isInstanceOf[org.kevoree.NodeType] && td.getDeployUnits.exists(du => du.getTargetNodeType != null ))
      .sortWith( (td,td2) => if(td2.getName=="JavaSENode"){true}else{td.getName < td2.getName } )
      .reverse
      .foreach {
      td =>
        nodeTypeModel.addElement(td.getName)
    }
    nodeTypeComboBox = new JComboBox(nodeTypeModel)
    nodeTypeComboBox.setUI(new HudComboBoxUI())

    val groupTypeModel = new DefaultComboBoxModel
    kernel.getTypeDefinitions.
      filter(td => td.isInstanceOf[org.kevoree.GroupType] && td.getDeployUnits.exists(du => du.getTargetNodeType != null ))
      .sortWith( (td,td2) => if(td2.getName=="RestGroup"){true}else{td.getName < td2.getName } )
      .reverse
      .foreach {
          td =>
            groupTypeModel.addElement(td.getName)
        }
    groupTypeComboBox = new JComboBox(groupTypeModel)
    groupTypeComboBox.setUI(new HudComboBoxUI())

    nodeInstancePanel = new InstanceParamPanel(getTypeDefinition(nodeTypeComboBox),"node1")
    groupInstancePanel = new InstanceParamPanel(getTypeDefinition(groupTypeComboBox),"sync")

    val nodeTypeLabel = new JLabel("Node type", SwingConstants.TRAILING);
    nodeTypeLabel.setUI(new HudLabelUI());
    nodeTypeLabel.setOpaque(false);
    nodeTypeLabel.setLabelFor(nodeTypeComboBox);

    val groupTypeLabel = new JLabel("Group type", SwingConstants.TRAILING);
    groupTypeLabel.setUI(new HudLabelUI());
    groupTypeLabel.setOpaque(false);
    groupTypeLabel.setLabelFor(groupTypeComboBox);

    val btBrowse: JButton = new JButton("Browse")
    btBrowse.setUI(new HudButtonUI)
    btBrowse.addActionListener(new ActionListener {
      def actionPerformed(p1: ActionEvent) {
        val filechooser: JFileChooser = new JFileChooser
        val returnVal: Int = filechooser.showOpenDialog(null)
        if (filechooser.getSelectedFile != null && returnVal == JFileChooser.APPROVE_OPTION) {
          try {
            val lastLoadedModel = filechooser.getSelectedFile.getAbsolutePath.toString
            val newModel = KevoreeXmiHelper.load(lastLoadedModel)
            init(newModel)
            repaint()
            revalidate()
          } catch {
            case _@e => e.printStackTrace()
          }
        }
      }
    })

    val bootModelLabel = new JLabel("Bootstrap", SwingConstants.TRAILING);
    bootModelLabel.setUI(new HudLabelUI());
    bootModelLabel.setOpaque(false);
    bootModelLabel.setLabelFor(btBrowse);

    val browseLayout = new JPanel()
    browseLayout.setOpaque(false)
    browseLayout.setLayout(new SpringLayout)
    browseLayout.add(bootModelLabel)
    browseLayout.add(btBrowse)
    SpringUtilities.makeCompactGrid(browseLayout, 1, 2, 3, 3, 3, 3)
    add(browseLayout)

    val nodeLayout = new JPanel()
    nodeLayout.setOpaque(false)
    nodeLayout.setLayout(new SpringLayout)
    nodeLayout.add(nodeTypeLabel)
    nodeLayout.add(nodeTypeComboBox)
    SpringUtilities.makeCompactGrid(nodeLayout, 1, 2, 3, 3, 3, 3)
    add(nodeLayout)
    add(nodeInstancePanel)

    val groupLayout = new JPanel()
    groupLayout.setOpaque(false)
    groupLayout.setLayout(new SpringLayout)
    groupLayout.add(groupTypeLabel)
    groupLayout.add(groupTypeComboBox)
    SpringUtilities.makeCompactGrid(groupLayout, 1, 2, 3, 3, 3, 3)
    add(groupLayout)
    add(groupInstancePanel)

    val pointer = this
    
    nodeTypeComboBox.addActionListener(new ActionListener() {
      override def actionPerformed(actionEvent: ActionEvent) {
        nodeInstancePanel.setNodeTypeDefinition(getTypeDefinition(nodeTypeComboBox))
        nodeInstancePanel.reload()
        revalidate()
        repaint()
      }
    })

    groupTypeComboBox.addActionListener(new ActionListener() {
      override def actionPerformed(actionEvent: ActionEvent) {
        groupInstancePanel.setNodeTypeDefinition(getTypeDefinition(groupTypeComboBox))
        groupInstancePanel.reload()
        revalidate()
        repaint()
      }
    })
    //SpringUtilities.makeCompactGrid(this, 5, 1, 1, 1, 1, 1)
  }

  def getTypeDefinition(box : JComboBox) : TypeDefinition = {
    pkernel.getTypeDefinitions.find(td => td.getName == box.getSelectedItem).get
  }

}