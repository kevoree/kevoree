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
import org.kevoree.framework.kaspects.ComponentInstanceAspect
import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.kevoree.tools.ui.framework.elements._
import scala.collection.JavaConversions._
import Art2UIAspects._


case class ComponentAspect(self : ComponentInstance) {

  def removeModelAndUI(kernel : KevoreeUIKernel)={
    val node : ContainerNode = self.eContainer.asInstanceOf[ContainerNode]
    var root : ContainerRoot = self.eContainer.eContainer.asInstanceOf[ContainerRoot]

    //BINDING
    new ComponentInstanceAspect().getRelatedBindings(self).foreach{b=>
      b.removeModelAndUI(kernel)
    }

    //REMOVE UI
    val nodepanel = kernel.getUifactory().getMapping().get(node).asInstanceOf[NodePanel]
    val componentPanel = kernel.getUifactory().getMapping().get(self).asInstanceOf[ComponentPanel]
    nodepanel.remove(componentPanel)

    //REMOVE INSTANCE
    node.removeComponents(self)


    //UNBIND
    kernel.getUifactory().getMapping().unbind(componentPanel, self)



  }

}
