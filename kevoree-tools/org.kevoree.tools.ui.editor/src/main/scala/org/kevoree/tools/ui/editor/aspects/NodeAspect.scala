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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.tools.ui.editor.aspects

import org.kevoree._
import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.kevoree.tools.ui.framework.elements.ModelPanel
import org.kevoree.tools.ui.framework.elements.NodePanel

import Art2UIAspects._

case class NodeAspect(self: ContainerNode) {

  def removeModelAndUI(kernel: KevoreeUIKernel) = {

    val root: ContainerRoot = self.eContainer.asInstanceOf[ContainerRoot]
    val nodePanel = kernel.getUifactory().getMapping().get(self).asInstanceOf[NodePanel]

    //REMOVE POTENTIAL GROUP LINK
    root.getGroups.foreach(g => {
      if (g.getSubNodes.contains(self)) {
        //REMOVE UI
        import scala.collection.JavaConversions._
        val bindings = kernel.getModelPanel.getBindings().toList ++ List()
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



    //REMOVE SUB Component
    val subcomponent = self.getComponents.toList ++ List()

    subcomponent.foreach {
      c => c.removeModelAndUI(kernel)
    }

    //REMOVE UI
    val modelPanel = kernel.getUifactory().getMapping().get(self.eContainer).asInstanceOf[ModelPanel]
    modelPanel.removeInstance(nodePanel)

    //REMOVE INSTANCE
    root.removeNodes(self)

    //CLEANUP HOST NODE
    kernel.getModelHandler.getActualModel.getNodes.foreach{ node =>
       if(node.getHosts.contains(self)){
         node.removeHosts(self)
       }
    }

    //UNBIND
    kernel.getUifactory().getMapping().unbind(nodePanel, self);

  }

}
