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
package org.kevoree.core.basechecker.dictionaryChecker

import java.util.ArrayList
import java.util.HashMap
import org.kevoree.Channel
import org.kevoree.ContainerNode
import org.kevoree.ContainerRoot
import org.kevoree.Group
import org.kevoree.Instance
import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation
import org.kevoree.modeling.api.KMFContainer
import org.kevoree.NodeNetwork

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 22/09/12
 * Time: 07:57
 */
class DictionaryNetworkPortChecker: CheckerService {
    override fun check (model: ContainerRoot?): MutableList<CheckerViolation> {
        val violations = ArrayList<CheckerViolation>()
        if (model != null) {
            val collectedPort = HashMap<String, HashMap<String, HashMap<String, KMFContainer>>>()

            for (instance in model.nodes) {
                instanceCollect(instance, collectedPort, instance.name!!)
                for (component in instance.components){
                    instanceCollect(component, collectedPort, instance.name!!)
                }
            }

            for (hub in model.hubs) {
                instanceDCollect(/*model, */hub, collectedPort)
            }

            for (group in model.groups) {
                instanceDCollect(/*model, */group, collectedPort)
            }

            // println(collectedPort)

            for ((node, portMap) in collectedPort) {
                for ((port, users) in portMap) {
                    if (users.size() > 1) {
                        val violation: CheckerViolation = CheckerViolation()
                        var objs = ArrayList<KMFContainer>()
                        val builder = StringBuilder()
                        builder.append("<")
                        for ((key, value) in users) {
                            objs.add(value)
                            builder.append(key).append(", ")
                        }
                        violation.setMessage("Duplicated collected port usage " + port + "-" + builder.substring(0, builder.length() - 2) + ">")
                        violation.setTargetObjects(objs)
                        violations.add(violation)
                    }
                }
            }
        }
        return violations
    }

    fun instanceDCollect (/*model: ContainerRoot, */ist: Instance, collector: HashMap<String, HashMap<String, HashMap<String, KMFContainer>>>) {
        val nodeConnected = ArrayList<String>()
        if (ist is Channel) {
            for (mb in (ist.eContainer() as ContainerRoot).mBindings) {
                if (mb.hub == ist) {
                    nodeConnected.add((mb.port!!.eContainer()!!.eContainer() as ContainerNode).name!!)
                }
            }
        }
        if (ist is Group) {
            for (sub in (ist as Group).subNodes) {
                nodeConnected.add(sub.name!!)
            }
        }

        for (node in nodeConnected) {
            instanceCollect(ist, collector, node)
        }
    }


    fun instanceCollect (ist: Instance, collector: HashMap<String, HashMap<String, HashMap<String, KMFContainer>>>, nodeName: String) {
        var portFound: String? = null
        if(ist.typeDefinition != null && ist.typeDefinition!!.dictionaryType != null) {
            val dicType = ist.typeDefinition!!.dictionaryType!!
            for (att in dicType.attributes) {
                if (att.name.equals("port") || att.name!!.endsWith("_port") || att.name!!.startsWith("port_")) {
                    portFound = att.defaultValue
                }
            }
        }
        if(ist.dictionary != null) {
            val dic = ist.dictionary!!
            for (dv in dic.values) {
                if ((dv.attribute!!.name.equals("port") || dv.attribute!!.name!!.endsWith("_port") || dv.attribute!!.name!!.startsWith("port_")) && (dv.targetNode == null || dv.targetNode!!.name == nodeName)) {
                    portFound = dv.value
                }
            }
        }

        if (portFound != null) {
            var nodeIPs = ArrayList<String>()
            val nodeIps = getNetworkProperties(ist.typeDefinition!!.eContainer() as ContainerRoot, nodeName, "KEVOREE.remote.node.ip")
            if (nodeIps.size() == 0) {
                nodeIPs.add("localhost")
            } else {
                nodeIPs.addAll(nodeIps)
            }

            for (nodeIP in nodeIPs) {
                var nodeCollector = collector.get(nodeIP)
                if (nodeCollector == null) {
                    nodeCollector = HashMap<String, HashMap<String, KMFContainer>>()
                    collector.put(nodeIP, nodeCollector!!)
                }
                var nodePortCollector = nodeCollector!!.get(portFound)
                if (nodePortCollector == null) {
                    nodePortCollector = HashMap<String, KMFContainer>()
                    nodeCollector!!.put(portFound!!, nodePortCollector!!)
                }
                //if (ist.isInstanceOf[ComponentInstance]){
                nodePortCollector!!.put(nodeName + "." + ist.name, ist)
                //} else {
                //  nodePortCollector.put(nodeName, ist)
                // }
            }

        }
    }

    fun getNetworkProperties (model: ContainerRoot, targetNodeName: String, key: String): List<String> {
        val properties = ArrayList<String>()
        val filteredNodeNetwork = ArrayList<NodeNetwork>()
        for (lNN in model.nodeNetworks){
            if (lNN.target!!.name == targetNodeName) {
                filteredNodeNetwork.add(lNN)
            }
        }
        for (fnn in filteredNodeNetwork) {
            for (fnl in fnn.link) {
                for (p in fnl.networkProperties) {
                    if (p.name == key) {
                        properties.add(p.value!!)
                        break
                    }
                }
            }
        }
        return properties
    }

}
