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
package org.kevoree.framework

import java.net.InetAddress
import scala.collection.JavaConversions._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/06/12
 * Time: 12:57
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object NetworkHelper {

  def isAccessible (ip: String): Boolean = {
    val inet = InetAddress.getByName(ip)
    inet.isReachable(1000)
  }

  def getAccessibleIP (ips: java.util.List[String]): Option[String] = {
    var accessibleIP: Option[String] = None
    ips.forall {
      ip =>
        if (isAccessible(ip)) {
          accessibleIP = Some(ip)
          false
        } else {
          true
        }
    }
    accessibleIP
  }
}
