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

import javax.jmdns.{ServiceInfo, JmDNS}
import java.util.HashMap

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


  var pairservice: ServiceInfo = ServiceInfo.create(REMOTE_TYPE, nodeName, modelPort, 0, 0, values)
  jmdns.registerService(pairservice)


  def close(){
    jmdns.close()
  }


}