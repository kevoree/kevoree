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
package org.kevoree.kompare.scheduling

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

import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.traverse.TopologicalOrderIterator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.kevoree.*
import java.util.ArrayList
import org.kevoreeAdaptation.AdaptationPrimitive
import java.util.HashMap

class SchedulingWithTopologicalOrderAlgo {

    val logger = LoggerFactory.getLogger(this.javaClass)!!

    fun schedule(commands: List<AdaptationPrimitive>, start: Boolean): List<AdaptationPrimitive> {
        if (commands.size > 1) {
            val graph = buildGraph(commands, start)
            val topologicAlgorithm = TopologicalOrderIterator(graph)

            var listCommands = ArrayList<AdaptationPrimitive>()
            while (topologicAlgorithm.hasNext()) {
                listCommands.add(topologicAlgorithm.next())
            }
            if (listCommands.size == commands.size) {
                return listCommands
            }
        }
        return commands
    }


    private class Assoc2<E,G>(_1 : E, _2:G){
    }


    /**
     * each command is a vertex and edges represent dependency order between commands
     */
    private fun buildGraph(commands: List<AdaptationPrimitive>,
                           start: Boolean): DefaultDirectedGraph<AdaptationPrimitive, Assoc2<AdaptationPrimitive, AdaptationPrimitive>> {

        val clazz = Assoc2<AdaptationPrimitive, AdaptationPrimitive>(commands.get(0),commands.get(0))!!.javaClass

        val graph = DefaultDirectedGraph<AdaptationPrimitive, Assoc2<AdaptationPrimitive, AdaptationPrimitive>>(clazz)

        val map = lookForPotentialConstraints(commands)

        for (command in commands) {
            for (command2 in commands) {
                graph.addVertex(command2)
                if (!command.equals(command2)) {
                    val cmdDep = (map.get(command2.getRef() as Instance))
                    if(cmdDep != null){
                        if (cmdDep.contains(command.getRef() as Instance)) {
                            if (start) {
                                graph.addEdge(command2, command, Assoc2(command2, command))
                            } else {
                                graph.addEdge(command, command2, Assoc2(command, command2))
                            }
                        }
                    }
                }

            }
        }
        return graph
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


    private fun lookForPotentialConstraints(commands: List<AdaptationPrimitive>): Map<Instance, List<Instance>> {
        val instanceDependencies: HashMap<Instance, MutableList<Instance>> = HashMap<Instance, MutableList<Instance>>()

        var rootContainer: ContainerRoot? = null
        val firstCommand = (commands.get(0)).getRef() as Instance

        if(firstCommand is Group){
            rootContainer = (firstCommand as Group).eContainer() as ContainerRoot
        }
        if(firstCommand is Channel){
            rootContainer = (firstCommand as Channel).eContainer() as ContainerRoot
        }
        if(firstCommand is ComponentInstance){
            rootContainer = ((firstCommand as ComponentInstance).eContainer() as KevoreeContainer).eContainer() as ContainerRoot
        }

        val bindingIterator = rootContainer!!.getMBindings().iterator()
        while (bindingIterator.hasNext()) {
            val binding = bindingIterator.next()
            for (command in commands) {
                if(command.getRef() is ComponentInstance) {
                    // test if instance is the container of the port of the binding
                    if ((command.getRef() as ComponentInstance).equals(binding.getPort()!!.eContainer())) {
                        // test all provided port
                        // the instance of the provided port must be stopped before those which are connected to him
                        val pit = (command.getRef() as ComponentInstance).getProvided().iterator()
                        while (pit.hasNext()) {
                            val port = pit.next()
                            if (binding.getPort().equals(port)) {
                                var newL: MutableList<Instance>? = instanceDependencies.get((command.getRef() as ComponentInstance))
                                if(newL == null){
                                    newL = ArrayList<Instance>()
                                }
                                newL!!.add(binding.getHub()!!)
                                instanceDependencies.put((command.getRef() as ComponentInstance), newL!!)
                            }
                        }
                        // test all required port
                        // the instance wait stops of all of those which are connected to him
                        val rit = (command.getRef() as ComponentInstance).getRequired().iterator()
                        while (rit.hasNext()) {
                            val port = rit.next()
                            // !port.getPortTypeRef.getNoDependency is used to be sure that we need to know if the use of this port on the start or stop method of the component can introduce deadlock or not
                            if (binding.getPort().equals(port) &&
                            (port.getPortTypeRef()!!.getNoDependency() == null || !port.getPortTypeRef()!!.getNoDependency())) {
                                var newL = instanceDependencies.get(binding.getHub())
                                if(newL == null){
                                    newL = ArrayList<Instance>()
                                }
                                newL!!.add((command.getRef() as ComponentInstance)!!)
                                instanceDependencies.put(binding.getHub()!!, newL!!)
                            }
                        }
                    }
                }
            }
        }

        return instanceDependencies
    }
}
