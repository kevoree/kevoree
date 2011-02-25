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

package org.kevoree.adaptation.deploy.osgi.scheduling

import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.traverse.TopologicalOrderIterator
import org.kevoree.Channel
import org.kevoree.ComponentInstance
import org.kevoree.ContainerRoot
import org.kevoree.Instance
import org.kevoree.adaptation.deploy.osgi.command.LifeCycleCommand

class SchedulingWithTopologicalOrderAlgo {

	def schedule(commands: List[LifeCycleCommand], start: Boolean): List[LifeCycleCommand] = {
		if (commands.size > 1) {
			var graph: DefaultDirectedGraph[LifeCycleCommand, (LifeCycleCommand, LifeCycleCommand)] = buildGraph(commands, start)
			var topologicAlgorithm: TopologicalOrderIterator[LifeCycleCommand, (LifeCycleCommand, LifeCycleCommand)] = new TopologicalOrderIterator(graph)

			var listCommands = List[LifeCycleCommand]()
			while (topologicAlgorithm.hasNext) {
				listCommands = listCommands ++ List(topologicAlgorithm.next)
			}
			if (listCommands.size == commands.size) {
				return listCommands
			}
		}
		commands
	}

	/**
	 * each command is a vertex and edges represent dependency order between commands
	 */
	private def buildGraph(commands: List[LifeCycleCommand], start: Boolean): DefaultDirectedGraph[LifeCycleCommand, (LifeCycleCommand, LifeCycleCommand)] = {
		var graph = new DefaultDirectedGraph[LifeCycleCommand, (LifeCycleCommand, LifeCycleCommand)](classOf[(LifeCycleCommand, LifeCycleCommand)])

		var map = lookForPotentialConstraints(commands)

		for (command <- commands) {
			for (command2 <- commands) {
				graph.addVertex(command2)
				if (!command.equals(command2)) {
					(map.get(command2.getInstance)) match {
						case Some(cmdDep) => {
							if (cmdDep.contains(command.getInstance)) {
								if (start) {
									graph.addEdge(command2, command, (command2, command))
									//constraint1 = Choco.lt(variables.get(command2).get, variables.get(command).get)
									//println(command2.getInstance.getName+"<"+command.getInstance.getName)
								}
								else {
									graph.addEdge(command, command2, (command, command2))
									//constraint1 = Choco.lt(variables.get(command).get, variables.get(command2).get)
								}

							} /* else {
		  //println(command.getInstance.getName + "!="+command2.getInstance.getName)
		  constraint1 = Choco.neq(variables.get(command).get, variables.get(command2).get)
		  }*/
							//model.addConstraint(constraint1)
						}
						case _ => {
							//println(command.getInstance.getName + "!="+command2.getInstance.getName)
							/*var constraint1: Constraint = Choco.neq(variables.get(command).get, variables.get(command2).get)
																 model.addConstraint(constraint1)*/
						}
					}
				}

			}
		}
		graph
	}

	/*
			 * Return Map
			 *
			 * Instance Map key is a dependency of all List instances return by key
			 *
			 *
			 * [i:Instance,li:List[Instance]]
			 * li depends i
			 *
			 *
			 * */
	private def lookForPotentialConstraints(commands: List[LifeCycleCommand]): scala.collection.mutable.Map[Instance, java.util.List[Instance]] = {
		val instanceDependencies: scala.collection.mutable.Map[Instance, java.util.List[Instance]] = scala.collection.mutable.Map[Instance, java.util.List[Instance]]()

		var rootContainer: ContainerRoot = null
		var firstCommand = (commands(0)).getInstance
		firstCommand match {
			case c: Channel => rootContainer = c.eContainer.asInstanceOf[ContainerRoot]
			case c: ComponentInstance => rootContainer = c.eContainer.eContainer.asInstanceOf[ContainerRoot]
		}


		if (rootContainer.getMBindings != null) {
			var bindingIterator = rootContainer.getMBindings.iterator
			while (bindingIterator.hasNext) {
				var binding = bindingIterator.next
				for (command <- commands) {
					command.getInstance match {
						case instance: ComponentInstance => {
							// test all provided port
							// the instance of the provided port must be stopped before those which are connected to him
							var pit = instance.getProvided.iterator
							while (pit.hasNext) {
								var port = pit.next
								if (binding.getPort.equals(port)) {
									var newL = instanceDependencies.get(instance).getOrElse(new java.util.ArrayList())
									newL.add(binding.getHub)
									instanceDependencies.update(instance, newL)
								}
							}
							// test all required port
							// the instance wait stops of all of those which are connected to him
							var rit = instance.getRequired.iterator
							while (rit.hasNext) {
								var port = rit.next
								if (binding.getPort.equals(port)) {
									var newL = instanceDependencies.get(binding.getHub).getOrElse(new java.util.ArrayList())
									newL.add(instance)
									instanceDependencies.update(binding.getHub, newL)
								}
							}
						}
						case _ =>
					}
				}
			}
		}
		instanceDependencies
	}
}
