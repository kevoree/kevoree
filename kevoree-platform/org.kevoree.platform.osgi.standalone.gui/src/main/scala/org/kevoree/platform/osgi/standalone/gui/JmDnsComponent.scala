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
package org.kevoree.platform.osgi.standalone.gui

import java.util.HashMap
import javax.jmdns.{ServiceEvent, ServiceListener, ServiceInfo, JmDNS}
import org.kevoree.framework.message.PlatformModelUpdate
import actors.Actor
import org.kevoree.remote.rest.Handler

/**
 * User: ffouquet
 * Date: 13/09/11
 * Time: 17:42
 */

class JmDnsComponent(nodeName: String, modelPort: Int) {
  final val REMOTE_TYPE: String = "_kevoree-remote._tcp.local."
  val values = new HashMap[String, String]
  // values.put("modelPort", modelPort)
  val jmdns = JmDNS.create(nodeName)
  jmdns.addServiceListener(REMOTE_TYPE, new ServiceListener() {
    def serviceAdded(p1: ServiceEvent) {

      val infos = jmdns.list(REMOTE_TYPE)
      infos.foreach {
        info =>
          val msg = new PlatformModelUpdate(info.getName.trim(), org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP, info.getInet4Addresses()(0).getHostAddress, "LAN", 100)
          Handler.modelhandler.asInstanceOf[Actor] ! msg
          val msg2 = new PlatformModelUpdate(info.getName.trim(), org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT, info.getPort.toString, "LAN", 100)
          Handler.modelhandler.asInstanceOf[Actor] ! msg2
      }
    }

    def serviceResolved(p1: ServiceEvent) {
      val msg = new PlatformModelUpdate(p1.getName.trim(), org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP, p1.getInfo.getInet4Addresses()(0).getHostAddress, "LAN", 100)
      Handler.modelhandler.asInstanceOf[Actor] ! msg
      val msg2 = new PlatformModelUpdate(p1.getName.trim(), org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT, p1.getInfo.getPort.toString, "LAN", 100)
      Handler.modelhandler.asInstanceOf[Actor] ! msg2
    }

    def serviceRemoved(p1: ServiceEvent) {}
  })

  var pairservice: ServiceInfo = ServiceInfo.create(REMOTE_TYPE, nodeName, modelPort, 0, 0, values)
  jmdns.registerService(pairservice)


  def close() {
    jmdns.close()
  }


}