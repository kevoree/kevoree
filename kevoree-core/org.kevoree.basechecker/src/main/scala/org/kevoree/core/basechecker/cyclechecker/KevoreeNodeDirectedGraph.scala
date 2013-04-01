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
import org.kevoree._
import org.kevoree.framework.kaspects.{PortAspect, ComponentInstanceAspect, ContainerNodeAspect, ChannelAspect}
import scala.collection.JavaConversions._
import java.util.List



case class KevoreeNodeDirectedGraph(model: ContainerRoot)
  extends DefaultDirectedGraph[Object, BindingFragment](new KevoreeFragmentBindingEdgeFactory) {

  private val channelAspect = new ChannelAspect()
  private val containerNodeAspect = new ContainerNodeAspect()
  private val componentInstanceAspect = new ComponentInstanceAspect()
  private val portAspect = new PortAspect()

  model.getNodes.foreach {
    node =>
      containerNodeAspect.getChannelFragment(node).foreach {
        channel =>
          val connectedNodes = channelAspect.getConnectedNode(channel, node.getName)
          if (connectedNodes.size > 0) {
            node.getComponents.foreach {
              component =>
                componentInstanceAspect.getRelatedBindings(component).foreach {
                  binding =>
                    if (portAspect.isRequiredPort(binding.getPort)
                      && binding.getPort.getPortTypeRef.getNoDependency != null &&
                      binding.getPort.getPortTypeRef.getNoDependency == false) {
                      if (binding.getHub == channel) {
                        connectedNodes.foreach {
                          node1 =>
                            node1.getComponents.foreach {
                              component1 =>
                                componentInstanceAspect.getRelatedBindings(component1).foreach {
                                  binding1 =>
                                    if (binding1.getHub == channel) {
                                      if (portAspect.isProvidedPort(binding.getPort) &&
                                        portAspect.isRequiredPort(binding1.getPort)) {
                                        val fragment = new ChannelFragment(binding.getHub, binding)
                                        val fragment1 = new ChannelFragment(binding1.getHub, binding1)
                                        addVertex(node)
                                        addVertex(fragment)
                                        addVertex(fragment1)
                                        addVertex(node1)
                                        addEdge(node, fragment, new BindingFragment(binding, null))
                                        addEdge(fragment, fragment1, new BindingFragment(binding, binding1))
                                        addEdge(fragment1, node1, new BindingFragment(binding1, null))
                                      } else if (portAspect.isProvidedPort(binding1.getPort) &&
                                        portAspect.isRequiredPort(binding.getPort)) {
                                        val fragment = new ChannelFragment(binding.getHub, binding)
                                        val fragment1 = new ChannelFragment(binding1.getHub, binding1)
                                        addVertex(node)
                                        addVertex(fragment)
                                        addVertex(fragment1)
                                        addVertex(node1)
                                        addEdge(node1, fragment1, new BindingFragment(binding1, null))
                                        addEdge(fragment1, fragment, new BindingFragment(binding1, binding))
                                        addEdge(fragment, node, new BindingFragment(binding, null))
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
