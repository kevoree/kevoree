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
import org.kevoree.framework.KevoreeUtility
import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.kevoree.tools.ui.framework.elements._

import Art2UIAspects._

case class ComponentAspect(self : ComponentInstance) {

  def removeModelAndUI(kernel : KevoreeUIKernel)={
    var node : ContainerNode = self.eContainer.asInstanceOf[ContainerNode]
    var root : ContainerRoot = self.eContainer.eContainer.asInstanceOf[ContainerRoot]

    //BINDING
    import scala.collection.JavaConversions._
    KevoreeUtility.getRelatedBinding(self).foreach{b=>
      b.removeModelAndUI(kernel)
    }

    //REMOVE UI
    var nodepanel = kernel.getUifactory().getMapping().get(node).asInstanceOf[NodePanel]
    var componentPanel = kernel.getUifactory().getMapping().get(self).asInstanceOf[ComponentPanel]
    nodepanel.remove(componentPanel)

    //REMOVE INSTANCE
    node.removeComponents(self)


    //UNBIND
    kernel.getUifactory().getMapping().unbind(componentPanel, self)



  }

}
