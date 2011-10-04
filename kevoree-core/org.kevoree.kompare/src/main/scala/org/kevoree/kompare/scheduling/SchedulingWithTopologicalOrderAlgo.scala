package org.kevoree.kompare.scheduling

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

import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.traverse.TopologicalOrderIterator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.kevoreeAdaptation.AdaptationPrimitive
import org.kevoree._

class SchedulingWithTopologicalOrderAlgo {

  private val logger: Logger = LoggerFactory.getLogger(classOf[SchedulingWithTopologicalOrderAlgo])

  def schedule (commands: List[AdaptationPrimitive], start: Boolean): List[AdaptationPrimitive] = {
    if (commands.size > 1) {
      val graph: DefaultDirectedGraph[AdaptationPrimitive, (AdaptationPrimitive, AdaptationPrimitive)] = buildGraph(commands,
                                                                                                            start)
      val topologicAlgorithm: TopologicalOrderIterator[AdaptationPrimitive, (AdaptationPrimitive, AdaptationPrimitive)] = new
          TopologicalOrderIterator(graph)

      var listCommands = List[AdaptationPrimitive]()
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
  private def buildGraph (commands: List[AdaptationPrimitive],
    start: Boolean): DefaultDirectedGraph[AdaptationPrimitive, (AdaptationPrimitive, AdaptationPrimitive)] = {
    val graph = new
        DefaultDirectedGraph[AdaptationPrimitive, (AdaptationPrimitive, AdaptationPrimitive)](classOf[(AdaptationPrimitive, AdaptationPrimitive)])

    val map = lookForPotentialConstraints(commands)

    for (command <- commands) {
      for (command2 <- commands) {
        graph.addVertex(command2)
        if (!command.equals(command2)) {
          (map.get(command2.getRef.asInstanceOf[Instance])) match {
            case Some(cmdDep) => {
              if (cmdDep.contains(command.getRef.asInstanceOf[Instance])) {
                if (start) {
                  graph.addEdge(command2, command, (command2, command))
                } else {
                  graph.addEdge(command, command2, (command, command2))
                }
              }
            }
            case _ =>
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
  private def lookForPotentialConstraints (
    commands: List[AdaptationPrimitive]): scala.collection.mutable.Map[Instance, java.util.List[Instance]] = {
    val instanceDependencies: scala.collection.mutable.Map[Instance, java.util.List[Instance]] = scala.collection
      .mutable.Map[Instance, java.util.List[Instance]]()

    var rootContainer: ContainerRoot = null
    val firstCommand = (commands(0)).getRef.asInstanceOf[Instance]
    firstCommand match {
      case c: Group => rootContainer = c.eContainer.asInstanceOf[ContainerRoot]
      case c: Channel => rootContainer = c.eContainer.asInstanceOf[ContainerRoot]
      case c: ComponentInstance => rootContainer = c.eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[ContainerRoot]
    }


    if (rootContainer.getMBindings != null) {
      val bindingIterator = rootContainer.getMBindings.iterator
      while (bindingIterator.hasNext) {
        val binding = bindingIterator.next
        for (command <- commands) {
          command.getRef.asInstanceOf[Instance] match {
            case instance: ComponentInstance => {
              // test if instance is the container of the port of the binding
              if (instance.equals(binding.getPort.eContainer)) {
                // test all provided port
                // the instance of the provided port must be stopped before those which are connected to him
                val pit = instance.getProvided.iterator
                while (pit.hasNext) {
                  val port = pit.next
                  if (binding.getPort.equals(port)) {
                    val newL = instanceDependencies.get(instance).getOrElse(new java.util.ArrayList[Instance]())
                    newL.add(binding.getHub)
                    instanceDependencies.update(instance, newL)
                  }
                }
                // test all required port
                // the instance wait stops of all of those which are connected to him
                val rit = instance.getRequired.iterator
                while (rit.hasNext) {
                  val port = rit.next
                  // !port.getPortTypeRef.getNoDependency is used to be sure that we need to know if the use of this port on the start or stop method of the component can introduce deadlock or not
                  if (binding.getPort.equals(port) &&
                    (port.getPortTypeRef.getNoDependency == null || !port.getPortTypeRef.getNoDependency)) {
                    val newL = instanceDependencies.get(binding.getHub).getOrElse(new java.util.ArrayList[Instance]())
                    newL.add(instance)
                    instanceDependencies.update(binding.getHub, newL)
                  }
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
