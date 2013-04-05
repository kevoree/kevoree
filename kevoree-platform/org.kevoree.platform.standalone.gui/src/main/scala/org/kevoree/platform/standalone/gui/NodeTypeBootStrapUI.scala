/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.platform.standalone.gui

import java.awt.event.{ActionEvent, ActionListener}
import org.kevoree._
import framework.KevoreeXmiHelper
import com.explodingpixels.macwidgets.{IAppWidgetFactory, HudWidgetFactory}
import com.explodingpixels.macwidgets.plaf.{HudButtonUI, HudLabelUI, HudTextFieldUI, HudComboBoxUI}
import java.awt.{ItemSelectable, FlowLayout, Dimension, BorderLayout}
import javax.swing._
import scala.collection.JavaConversions._

/**
 * User: ffouquet
 * Date: 14/09/11
 * Time: 20:11
 */

class NodeTypeBootStrapUI(private var pkernel: ContainerRoot) extends JPanel {

  var nodeTypeComboBox: JComponent = _
  var groupTypeComboBox: JComponent = _
  var nodeInstancePanel : InstanceParamPanel = _
  var groupInstancePanel : InstanceParamPanel = _

  def getKevName = {
    nodeInstancePanel.getInstanceName
  }
  def getKevGroupName = {
    groupInstancePanel.getInstanceName
  }
  def getKevTypeName = {
    nodeTypeComboBox.asInstanceOf[{def getSelectedItem : Object}].getSelectedItem
  }

  def getKevGroupTypeName = {
    groupTypeComboBox.asInstanceOf[{def getSelectedItem : Object}].getSelectedItem
  }

//  private var model = pkernel
  def getCurrentModel = pkernel

  this.setOpaque(false)
  init(pkernel)

  //CALL INIT
  def init(kernel: ContainerRoot) {
    this.setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS))
    this.removeAll()
    pkernel = kernel
    val nodeTypeModel = new DefaultComboBoxModel

    kernel.getTypeDefinitions.
      filter(td => td.isInstanceOf[org.kevoree.NodeType] && td.getDeployUnits.exists(du => du.getTargetNodeType != null ))
      .sortWith( (td,td2) => {if(td.getName=="JavaSENode"){true}else{td.getName < td2.getName }} )
      .foreach {
      td =>
        UIHelper.addItem(nodeTypeModel,td.getName)
    }
    nodeTypeComboBox = UIHelper.createJComboBox(nodeTypeModel)

    val groupTypeModel = new DefaultComboBoxModel


    if(kernel.getTypeDefinitions.exists(td => td.getName == "BasicGroup")){
      UIHelper.addItem(groupTypeModel,"BasicGroup")
    }

    kernel.getTypeDefinitions.
      filter(td => td.isInstanceOf[org.kevoree.GroupType] && td.getDeployUnits.exists(du => du.getTargetNodeType != null ))
      .filter(td => td.getName != "BasicGroup")
      //.sortWith( (td,td2) => {if({td.getName=="BasicGroup"}){true}else{td.getName < td2.getName }} )
      .foreach {
          td =>{
            UIHelper.addItem(groupTypeModel,td.getName)
          }
        }
    groupTypeComboBox = UIHelper.createJComboBox(groupTypeModel)
    System.out.println(System.getProperty("node.name"))
    if (System.getProperty("node.name") != null && System.getProperty("node.name") != "") {
      System.out.println(System.getProperty("node.name") + "2")
      nodeInstancePanel = new InstanceParamPanel(getTypeDefinition(nodeTypeComboBox),System.getProperty("node.name"))
    } else {
      nodeInstancePanel = new InstanceParamPanel(getTypeDefinition(nodeTypeComboBox),"node0")
    }
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
            val newModel = KevoreeXmiHelper.instance$.load(lastLoadedModel)
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

    nodeTypeComboBox.asInstanceOf[{def addActionListener(l:ActionListener)}].addActionListener(new ActionListener() {
      override def actionPerformed(actionEvent: ActionEvent) {
        nodeInstancePanel.setNodeTypeDefinition(getTypeDefinition(nodeTypeComboBox))
        nodeInstancePanel.reload()
        revalidate()
        repaint()
      }
    })

    groupTypeComboBox.asInstanceOf[{def addActionListener(l:ActionListener)}].addActionListener(new ActionListener() {
      override def actionPerformed(actionEvent: ActionEvent) {
        groupInstancePanel.setNodeTypeDefinition(getTypeDefinition(groupTypeComboBox))
        groupInstancePanel.reload()
        revalidate()
        repaint()
      }
    })
    //SpringUtilities.makeCompactGrid(this, 5, 1, 1, 1, 1, 1)
  }

  def getTypeDefinition(box : Object) : TypeDefinition = {
    pkernel.getTypeDefinitions.find(td => td.getName == box.asInstanceOf[{def getSelectedItem : Object}].getSelectedItem).get
  }

}