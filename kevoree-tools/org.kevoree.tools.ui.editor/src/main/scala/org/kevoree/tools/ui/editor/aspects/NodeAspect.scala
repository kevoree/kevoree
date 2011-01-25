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
import scala.collection.JavaConversions._
import Art2UIAspects._

case class NodeAspect(self : ContainerNode) {

  def removeModelAndUI(kernel : KevoreeUIKernel)={

    var root : ContainerRoot = self.eContainer.asInstanceOf[ContainerRoot]

    //REMOVE SUB NODE

    var subcomponent = self.getComponents.toList ++ List()

    subcomponent.foreach{c => c.removeModelAndUI(kernel)}

    //REMOVE UI
    var nodePanel = kernel.getUifactory().getMapping().get(self).asInstanceOf[NodePanel]
    var modelPanel = kernel.getUifactory().getMapping().get(self.eContainer).asInstanceOf[ModelPanel]

    println(nodePanel+"-"+modelPanel)

    modelPanel.removeInstance(nodePanel)

    //REMOVE INSTANCE
    root.getNodes.remove(self)

    //UNBIND
    kernel.getUifactory().getMapping().unbind(nodePanel, self);

  }

}
