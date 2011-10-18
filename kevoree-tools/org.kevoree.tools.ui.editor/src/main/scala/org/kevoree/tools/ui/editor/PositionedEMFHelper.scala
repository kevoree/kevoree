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

import org.kevoree.ContainerRoot

import org.kevoree.tools.ui.framework.elements.{GroupPanel, ChannelPanel, NodePanel}

object PositionedEMFHelper {

  def updateModelUIMetaData(kernel: KevoreeUIKernel) {
    //PREPROCESS UI POSITION
    val model = kernel.getModelHandler.getActualModel

    model.getNodes.foreach(node => {
      val nodePanel = kernel.getUifactory.getMapping.get(node).asInstanceOf[NodePanel];
      if (nodePanel != null) {
        val metadata = "x=" + nodePanel.getX + "," + "y=" + nodePanel.getY
        node.setMetaData(metadata)
      }
    })
    model.getHubs.foreach(hub => {
      val hubPanel = kernel.getUifactory.getMapping.get(hub).asInstanceOf[ChannelPanel];
      if (hubPanel != null) {
        val metadata = "x=" + hubPanel.getX + "," + "y=" + hubPanel.getY
        hub.setMetaData(metadata)
      }
    })
    model.getGroups.foreach(group => {
      val groupPanel = kernel.getUifactory.getMapping.get(group).asInstanceOf[GroupPanel];
      if (groupPanel != null) {
        val metadata = "x=" + groupPanel.getX + "," + "y=" + groupPanel.getY
        group.setMetaData(metadata)
      }
    })
  }

}