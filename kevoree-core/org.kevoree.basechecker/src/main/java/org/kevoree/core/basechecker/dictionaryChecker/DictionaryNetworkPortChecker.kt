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
import org.kevoree.container.KMFContainer
import org.kevoree.framework.Constants
import org.kevoree.framework.KevoreePropertyHelper

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

            for (instance in model.getNodes()) {
                instanceCollect(instance, collectedPort, instance.getName())
                for (component in instance.getComponents()){
                    instanceCollect(component, collectedPort, instance.getName())
                }
            }

            for (hub in model.getHubs()) {
                instanceDCollect(/*model, */hub, collectedPort)
            }

            for (group in model.getGroups()) {
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
            for (mb in (ist.eContainer() as ContainerRoot).getMBindings()) {
                if (mb.getHub() == ist) {
                    nodeConnected.add((mb.getPort()!!.eContainer()!!.eContainer() as ContainerNode).getName())
                }
            }
        }
        if (ist is Group) {
            for (sub in (ist as Group).getSubNodes()) {
                nodeConnected.add(sub.getName())
            }
        }

        for (node in nodeConnected) {
            instanceCollect(ist, collector, node)
        }
    }


    fun instanceCollect (ist: Instance, collector: HashMap<String, HashMap<String, HashMap<String, KMFContainer>>>, nodeName: String) {
        var portFound: String? = null
        if(ist.getTypeDefinition() != null && ist.getTypeDefinition()!!.getDictionaryType() != null) {
            val dicType = ist.getTypeDefinition()!!.getDictionaryType()!!
            for (dv in dicType.getDefaultValues()) {
                if (dv.getAttribute()!!.getName().equals("port") || dv.getAttribute()!!.getName().endsWith("_port") || dv.getAttribute()!!.getName().startsWith("port_")) {
                    portFound = dv.getValue()
                }
            }
        }
        if(ist.getDictionary() != null) {
            val dic = ist.getDictionary()!!
            for (dv in dic.getValues()) {
                if ((dv.getAttribute()!!.getName().equals("port") || dv.getAttribute()!!.getName().endsWith("_port") || dv.getAttribute()!!.getName().startsWith("port_")) && (dv.getTargetNode() == null || dv.getTargetNode()!!.getName() == nodeName)) {
                    portFound = dv.getValue()
                }
            }
        }

        if (portFound != null) {
            var nodeIPs = ArrayList<String>()
            val nodeIps = KevoreePropertyHelper.getNetworkProperties(ist.getTypeDefinition()!!.eContainer() as ContainerRoot, nodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
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
                nodePortCollector!!.put(nodeName + "." + ist.getName(), ist)
                //} else {
                //  nodePortCollector.put(nodeName, ist)
                // }
            }

        }
    }

}
