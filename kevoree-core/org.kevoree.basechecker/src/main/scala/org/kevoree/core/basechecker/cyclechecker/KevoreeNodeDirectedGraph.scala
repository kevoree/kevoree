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

package org.kevoree.core.basechecker.cyclechecker

import org.kevoree.framework.aspects.KevoreeAspects._
import scala.collection.JavaConversions._

import org.jgrapht.graph.DefaultDirectedGraph
import org.kevoree._

case class KevoreeNodeDirectedGraph(model: ContainerRoot) extends DefaultDirectedGraph[Object, BindingFragment](classOf[BindingFragment]) {

	model.getNodes.foreach {
		node =>
			node.getInstances.filter(p => p.isInstanceOf[Channel]).foreach {
				instance =>
					var channel = instance.asInstanceOf[Channel]
					channel.getConnectedNode(node.getName).foreach {
						node1 =>
							node1.getInstances.filter(p => p.isInstanceOf[ComponentInstance]).foreach {
								instance =>
									val componentInstance = instance.asInstanceOf[ComponentInstance]
									componentInstance.getRelatedBindings.foreach {
										binding =>
											if (binding.getHub() == channel) {
												addVertex(node)
												addVertex(node1)
												val fragment = new ChannelFragment(binding.getHub, node.getName)
												val fragment1 = new ChannelFragment(binding.getHub, node1.getName)
												addVertex(fragment)
												addVertex(fragment1)
												if (componentInstance.getProvided.contains(binding.getPort)) {
													addEdge(node, fragment, new BindingFragment(binding, node.getName))
													addEdge(fragment, fragment1, new BindingFragment(binding, null))
													addEdge(fragment1, node1, new BindingFragment(binding, node1.getName))
												} else {
													addEdge(node1, fragment1, new BindingFragment(binding, node1.getName))
													addEdge(fragment1, fragment, new BindingFragment(binding, null))
													addEdge(fragment, node, new BindingFragment(binding, node.getName))
												}
											}
									}
							}
					}
			}
	}
}
