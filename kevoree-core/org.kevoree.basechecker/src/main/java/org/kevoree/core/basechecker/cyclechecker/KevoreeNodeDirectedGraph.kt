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
import org.kevoree.ContainerRoot
import org.kevoree.framework.kaspects.ChannelAspect
import org.kevoree.framework.kaspects.ComponentInstanceAspect
import org.kevoree.framework.kaspects.ContainerNodeAspect
import org.kevoree.framework.kaspects.PortAspect

class KevoreeNodeDirectedGraph(model: ContainerRoot): DefaultDirectedGraph<Any, BindingFragment>(KevoreeFragmentBindingEdgeFactory()) {

    {
        val channelAspect = ChannelAspect()
        val containerNodeAspect = ContainerNodeAspect()
        val componentInstanceAspect = ComponentInstanceAspect()
        val portAspect = PortAspect()
        for (node in model.getNodes()) {
            for (channel in containerNodeAspect.getChannelFragment(node)) {
                val connectedNodes = channelAspect.getConnectedNode(channel, node.getName())
                if (connectedNodes.size > 0) {
                    for (component in node.getComponents()) {
                        for (binding in componentInstanceAspect.getRelatedBindings(component)) {
                            if (portAspect.isRequiredPort(binding.getPort()!!) &&
                            binding.getPort()!!.getPortTypeRef()!!.getNoDependency() == false) {
                                if (binding.getHub() == channel) {
                                    for (node1 in connectedNodes) {
                                        for (component1 in node1.getComponents()) {
                                            for (binding1 in componentInstanceAspect.getRelatedBindings(component1)) {
                                                if (binding1.getHub() == channel) {
                                                    if (portAspect.isProvidedPort(binding.getPort()!!) &&
                                                    portAspect.isRequiredPort(binding1.getPort()!!)) {
                                                        val fragment = ChannelFragment(binding.getHub()!!, binding)
                                                        val fragment1 = ChannelFragment(binding1.getHub()!!, binding1)
                                                        addVertex(node)
                                                        addVertex(fragment)
                                                        addVertex(fragment1)
                                                        addVertex(node1)
                                                        addEdge(node, fragment, BindingFragment(binding, null))
                                                        addEdge(fragment, fragment1, BindingFragment(binding, binding1))
                                                        addEdge(fragment1, node1, BindingFragment(binding1, null))
                                                    } else if (portAspect.isProvidedPort(binding1.getPort()!!) &&
                                                    portAspect.isRequiredPort(binding.getPort()!!)) {
                                                        val fragment = ChannelFragment(binding.getHub()!!, binding)
                                                        val fragment1 = ChannelFragment(binding1.getHub()!!, binding1)
                                                        addVertex(node)
                                                        addVertex(fragment)
                                                        addVertex(fragment1)
                                                        addVertex(node1)
                                                        addEdge(node1, fragment1, BindingFragment(binding1, null))
                                                        addEdge(fragment1, fragment, BindingFragment(binding1, binding))
                                                        addEdge(fragment, node, BindingFragment(binding, null))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
