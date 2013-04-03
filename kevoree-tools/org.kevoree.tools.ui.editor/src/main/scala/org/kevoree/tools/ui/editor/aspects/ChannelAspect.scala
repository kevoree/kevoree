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
import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.kevoree.tools.ui.framework.elements.ChannelPanel
import org.kevoree.tools.ui.framework.elements.ModelPanel
import Art2UIAspects._
import scala.collection.JavaConversions._

case class ChannelAspect(self: Channel) {

  private val channelAspect = new org.kevoree.framework.kaspects.ChannelAspect()

  def removeModelAndUI(kernel: KevoreeUIKernel) {
    val root: ContainerRoot = self.eContainer.asInstanceOf[ContainerRoot]
    root.getNodes.foreach {
      node =>
        channelAspect.getRelatedBindings(self, node).foreach {
          b =>
            b.removeModelAndUI(kernel)
        }
    }
    val panel = kernel.getUifactory().getMapping().get(self).asInstanceOf[ChannelPanel]
    val modelPanel = kernel.getUifactory().getMapping().get(root).asInstanceOf[ModelPanel]
    modelPanel.removeInstance(panel)
    root.removeHubs(self)
    kernel.getUifactory().getMapping().unbind(panel, self)
  }

}
