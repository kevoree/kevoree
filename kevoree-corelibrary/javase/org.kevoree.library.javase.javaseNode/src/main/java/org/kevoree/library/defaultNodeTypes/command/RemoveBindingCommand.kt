package org.kevoree.library.defaultNodeTypes.command

import org.kevoree.MBinding
import org.kevoree.api.PrimitiveCommand
import org.slf4j.LoggerFactory
import org.kevoree.library.defaultNodeTypes.context.KevoreeDeployManager
import org.kevoree.ComponentInstance
import org.kevoree.framework.osgi.KevoreeComponentActivator
import org.kevoree.framework.AbstractComponentType
import org.kevoree.framework.KevoreeChannelFragment
import org.kevoree.framework.KevoreePort
import org.kevoree.framework.message.PortUnbindMessage
import org.kevoree.framework.message.FragmentUnbindMessage
import org.kevoree.framework.KevoreeComponent
import org.kevoree.framework.osgi.KevoreeInstanceActivator

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


class RemoveBindingCommand(val c : MBinding,val nodeName:String) : PrimitiveCommand {

    var logger = LoggerFactory.getLogger(this.javaClass)!!

    override fun undo() {
        AddBindingCommand(c, nodeName).execute()
    }

    override fun execute(): Boolean {
        if(c == null){
            return false
        }else{
            val kevoreeChannelFound = KevoreeDeployManager.getRef(c.getHub().javaClass.getName(), c.getHub()!!.getName())
            val kevoreeComponentFound = KevoreeDeployManager.getRef((c.getPort()!!.eContainer()as ComponentInstance).javaClass.getName(), (c.getPort()!!.eContainer()as ComponentInstance).getName())
            if(kevoreeChannelFound != null && kevoreeComponentFound != null && kevoreeComponentFound is KevoreeComponentActivator){
                val casted = kevoreeComponentFound.getKInstance() as KevoreeComponent
                val channelCasted = kevoreeChannelFound as KevoreeInstanceActivator
                val foundNeedPort = casted.getKevoreeComponentType()!!.getNeededPorts()!!.get(c.getPort()!!.getPortTypeRef()!!.getName())
                val foundHostedPort = casted.getKevoreeComponentType()!!.getHostedPorts()!!.get(c.getPort()!!.getPortTypeRef()!!.getName())

                if(foundNeedPort == null && foundNeedPort == null){
                    logger.info("Port instance not found in component")
                    logger.info("Look for "+c.getPort()!!.getPortTypeRef()!!.getName());
                    logger.info(casted.getKevoreeComponentType()!!.getNeededPorts()!!.values().toString());
                    return false
                }
                if (foundNeedPort != null) {
                    /* Bind port to Channel */
                    val newbindmsg = FragmentUnbindMessage()
                    newbindmsg.setChannelName(c.getHub()!!.getName())
                    return (foundNeedPort as KevoreePort).processAdminMsg(newbindmsg)
                }
                if(foundHostedPort != null){
                    val bindmsg = PortUnbindMessage()
                    bindmsg.setNodeName(nodeName)
                    bindmsg.setComponentName((c.getPort()!!.eContainer() as ComponentInstance).getName())
                    bindmsg.setPortName((foundHostedPort as KevoreePort).getName())
                    return (channelCasted.getKInstance() as KevoreeChannelFragment).processAdminMsg(bindmsg)
                }
                return false
            } else {
                return false
            }
        }
    }

}
