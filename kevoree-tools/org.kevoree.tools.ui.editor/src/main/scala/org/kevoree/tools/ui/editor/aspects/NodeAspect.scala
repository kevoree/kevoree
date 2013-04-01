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

package org.kevoree.tools.ui.editor.aspects

import org.kevoree._
import framework.kaspects.ContainerRootAspect
import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.kevoree.tools.ui.framework.elements.ModelPanel
import org.kevoree.tools.ui.framework.elements.NodePanel
import scala.collection.JavaConversions._

import Art2UIAspects._

case class NodeAspect(self: ContainerNode) {

  val containerRootAspect = new ContainerRootAspect()

  def removeModelAndUI(kernel: KevoreeUIKernel) {

    val root: ContainerRoot = self.eContainer.asInstanceOf[ContainerRoot]
    val nodePanel = kernel.getUifactory.getMapping.get(self).asInstanceOf[NodePanel]

    //REMOVE POTENTIAL GROUP LINK
    root.getGroups.foreach(g => {
      if (g.getSubNodes.contains(self)) {
        //REMOVE UI
        import scala.collection.JavaConversions._
        val bindings = kernel.getModelPanel.getBindings.toList ++ List()
        bindings.foreach {
          b =>
            if (b.getFrom.equals(nodePanel) || b.getTo.equals(nodePanel)) {
              kernel.getModelPanel.removeBinding(b)
            }
        }

        //REMOVE GROUP LINK
        g.removeSubNodes(self)
      }
    })

    //REMOVE FROM NETWORK LINK
    root.getNodeNetworks.foreach {
      nn =>
        if (nn.getTarget == self) {
          root.removeNodeNetworks(nn)
        } else {
          val initNode = nn.getInitBy
          if(initNode != null) {
              if (initNode == self) {
                root.removeNodeNetworks(nn)
              }
          }
        }

    }




    //REMOVE SUB Component
    val subcomponent = self.getComponents.toList ++ List()

    subcomponent.foreach {
      c => c.removeModelAndUI(kernel)
    }

    //REMOVE UI

    val modelPanel = kernel.getUifactory.getMapping.get(self.eContainer).asInstanceOf[ModelPanel]

    modelPanel.removeInstance(nodePanel)
    if(nodePanel.getParent != null){
      nodePanel.getParent.remove(nodePanel)
    }

    //REMOVE INSTANCE
    root.removeNodes(self)

    //CLEANUP HOST NODE
    kernel.getModelHandler.getActualModel.getNodes.foreach {
      node =>
        if (node.getHosts.contains(self)) {
          node.removeHosts(self)
        }
    }

    //CLEANUP DICTIONARY
    containerRootAspect.getAllInstances(kernel.getModelHandler.getActualModel).foreach{ inst =>
      val dico = inst.getDictionary
      if (dico != null){
        dico.getValues.filter(v => v.getTargetNode != null && v.getTargetNode.getName == self.getName).foreach{ value =>
          dico.removeValues(value)
        }
      }
    }

    //UNBIND
    kernel.getUifactory.getMapping.unbind(nodePanel, self);

  }

}
