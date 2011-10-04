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
package org.kevoree.tools.ui.editor.aspects

import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.kevoree.{ContainerRoot, Group}
import org.kevoree.tools.ui.framework.elements.{Binding, ModelPanel, GroupPanel}


case class GroupAspect(self: Group) {

  def removeModelAndUI(kernel: KevoreeUIKernel) = {
    val root: ContainerRoot = self.eContainer.asInstanceOf[ContainerRoot]
    val panel = kernel.getUifactory().getMapping().get(self).asInstanceOf[GroupPanel]
    val modelPanel = kernel.getUifactory().getMapping().get(root).asInstanceOf[ModelPanel]

    //REMOVE UI BINDING
    val bindings = kernel.getModelPanel.getBindings().toList  ++ List()
    bindings.foreach{b=>
      if(b.getFrom.equals(panel.getAnchor) || b.getTo.equals(panel.getAnchor)){
          kernel.getModelPanel.removeBinding(b)
      }
    }

    modelPanel.removeInstance(panel)
    root.getGroups.remove(self)
    kernel.getUifactory().getMapping().unbind(panel, self);
  }

}