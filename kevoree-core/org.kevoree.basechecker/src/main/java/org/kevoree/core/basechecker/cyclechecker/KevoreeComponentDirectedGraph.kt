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

package org.kevoree.core.basechecker.cyclechecker

import org.jgrapht.graph.DefaultDirectedGraph
import org.kevoree.ContainerNode
import org.kevoree.ContainerRoot
import org.kevoree.Instance
import org.kevoree.MBinding
import org.kevoree.framework.kaspects.ComponentInstanceAspect

class KevoreeComponentDirectedGraph(model: ContainerRoot, nodeName: String): DefaultDirectedGraph<Instance, MBinding>(KevoreeMBindingEdgeFactory(model)) {
    {
        val componentInstanceAspect = ComponentInstanceAspect()
        val node = model.findByPath("nodes[" + nodeName + "]",  javaClass<ContainerNode>())
        if (node != null) {
            for (componentInstance in node.getComponents()) {
                for (binding in componentInstanceAspect.getRelatedBindings(componentInstance)) {
                    if (binding.getPort()!!.getPortTypeRef()!!.getNoDependency() == false) {
                        addVertex(binding.getHub())
                        addVertex(componentInstance)
                        if (componentInstance.getProvided().contains(binding.getPort())) {
                            addEdge(binding.getHub(), componentInstance, binding)
                        } else {
                            addEdge(componentInstance, binding.getHub(), binding)
                        }
                    }
                }
            }
        }
    }
}
