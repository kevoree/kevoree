package org.kevoree.library.defaultNodeTypes.command

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

import org.slf4j.LoggerFactory
import org.kevoree.Channel
import org.kevoree.api.PrimitiveCommand
import org.kevoree.library.defaultNodeTypes.context.KevoreeDeployManager
import org.kevoree.framework.osgi.KevoreeChannelFragmentActivator
import org.kevoree.framework.message.FragmentBindMessage

class AddFragmentBindingCommand(val c: Channel, val remoteNodeName: String, val nodeName: String): PrimitiveCommand {

    var logger = LoggerFactory.getLogger(this.javaClass)!!

    override fun execute(): Boolean {

        val kevoreeChannelFound = KevoreeDeployManager.getRef(c.javaClass.getName(), c.getName()) as KevoreeChannelFragmentActivator
        if(kevoreeChannelFound != null){
            val bindmsg = FragmentBindMessage()
            bindmsg.setChannelName(c.getName())
            bindmsg.setFragmentNodeName(remoteNodeName)
            return (kevoreeChannelFound.channelActor()!!.processAdminMsg(bindmsg))
        } else {
            return false
        }
    }

    override fun undo() {
        RemoveFragmentBindingCommand(c, remoteNodeName, nodeName).execute()
    }

}
