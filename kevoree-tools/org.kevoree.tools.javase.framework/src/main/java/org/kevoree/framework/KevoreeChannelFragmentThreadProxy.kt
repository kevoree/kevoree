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
package org.kevoree.framework

import java.util.HashMap
import org.kevoree.ContainerRoot
import org.kevoree.framework.message.Message

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
class KevoreeChannelFragmentThreadProxy(val remoteNodeName: String, val remoteChannelName: String): KevoreeChannelFragment {
    override fun kInstanceStart(tmodel: ContainerRoot): Boolean {
        return false
    }
    override fun kInstanceStop(tmodel: ContainerRoot): Boolean {
        return false
    }
    override fun kUpdateDictionary(d: Map<String, Any>, cmodel: ContainerRoot): Map<String, Any>? {
        return null
    }
    override fun send(o: Any?) {
        internal_process(o)
    }
    override fun sendWait(o: Any?): Any? {
        return internal_process(o)
    }

    override fun getNodeName(): String {
        return remoteNodeName
    }

    override fun  getName(): String {
        return remoteChannelName
    }

    fun internal_process(msg: Any?): Any? {
        return when(msg){
            is Message -> {
                channelSender!!.sendMessageToRemote(msg)
            }
            else -> {
                println("WTF")
            }
        }
    }

    public var channelSender: ChannelFragmentSender? = null

    override fun processAdminMsg(o: Any): Boolean {
        return false
    }

}
