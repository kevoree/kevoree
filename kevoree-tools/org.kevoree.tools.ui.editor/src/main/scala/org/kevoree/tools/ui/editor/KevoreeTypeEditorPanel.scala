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
package org.kevoree.tools.ui.editor

import org.kevoree._
import javax.swing.{BoxLayout, JPanel, JSplitPane}
import java.awt.{Dimension, BorderLayout}
import tools.ui.framework.elements.PortPanel


/**
 * User: ffouquet
 * Date: 26/08/11
 * Time: 10:51
 */

class KevoreeTypeEditorPanel(typeDefinitionPanel: JPanel, uikernel: KevoreeUIKernel) extends JPanel {

  val typeDef: TypeDefinition = uikernel.getUifactory.getMapping.get(typeDefinitionPanel).asInstanceOf[TypeDefinition]

  val hSplit: JSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
  hSplit.setBorder(null)
  hSplit.setDividerLocation(200)
  val leftPanel = new KevoreeTypeEditorSourceList(hSplit, uikernel, typeDef)
  hSplit.setTopComponent(leftPanel.getComponent)
  if (typeDef != null) {
    leftPanel.refresh()
  }
  hSplit.doLayout()


  val uifactory = new KevoreeUIFactory(uikernel)

  val panel: JPanel = typeDef match {
    case t: ComponentType => uifactory.createComponentTypeUI(t)
    case t: ChannelType => uifactory.createChannelTypeUI(t)
    case t: GroupType => uifactory.createGroupTypeUI(t)
    case t: NodeType => uifactory.createNodeTypeUI(t)
    case null => {
      val p = new JPanel; p.setOpaque(false); p
    }
  }
  hSplit.setBottomComponent(panel)

  //val layoutTop = new JPanel
  //layoutTop.setLayout(new BoxLayout(layoutTop, BoxLayout.LINE_AXIS))
  //layoutTop.add(panel)
  val typeList = new KevoreeTypeEditorList(typeDef, uikernel)
  val splitPane: JSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, hSplit, typeList)
  splitPane.setBorder(null)
  splitPane.setDividerLocation(200)
  this.setLayout(new BorderLayout())
  this.add(splitPane, BorderLayout.CENTER)


  def refresh() {
    //refresh elements tree
    leftPanel.refresh()

    //refresh graphical component view
    val panel: JPanel = typeDef match {
      case t: ComponentType => uifactory.createComponentTypeUI(t)
      case t: ChannelType => uifactory.createChannelTypeUI(t)
      case t: GroupType => uifactory.createGroupTypeUI(t)
      case t: NodeType => uifactory.createNodeTypeUI(t)
      case null => {
        val p = new JPanel; p.setOpaque(false); p
      }
    }
    hSplit.setBottomComponent(panel)

    //refresh componet element table
    typeList.reload()
  }

}