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
import org.kevoree.ComponentInstance
import org.kevoree.ContainerRoot
import org.kevoree.Instance
import org.kevoree.MBinding

case class KevoreeComponentDirectedGraph(model: ContainerRoot, nodeName: String) extends DefaultDirectedGraph[Instance, MBinding](classOf[MBinding]) {

	model.getNodes.find(node => node.getName == nodeName) match {
		case Some(node) =>
			node.getInstances.filter(p => p.isInstanceOf[ComponentInstance]).foreach {
				instance =>
					val componentInstance = instance.asInstanceOf[ComponentInstance]
					componentInstance.getRelatedBindings.foreach {
						binding =>
							addVertex(binding.getHub)
							addVertex(componentInstance)

							if (componentInstance.getProvided.contains(binding.getPort)) {
								addEdge(binding.getHub, componentInstance, binding)
							} else {
								addEdge(componentInstance, binding.getHub, binding)
							}
					}
			}
		case None =>
	}
}
