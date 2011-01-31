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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.remote

import java.io.IOException
import java.net.DatagramSocket
import java.net.ServerSocket
import scala.collection.JavaConversions._

object NetworkUtility {

  def findNextAvailblePort(target : java.lang.Integer,max:java.lang.Integer) : java.lang.Integer = {
    var port : Int = target.intValue
    while( (!available(port)) && port < max.intValue  ){ port = port + 1 }
    port
  }

  /**
   * Checks to see if a specific port is available.
   *
   * @param port the port to check for availability
   */
  def available(port:Int) : Boolean = {
    /*
     if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
     throw new IllegalArgumentException("Invalid start port: " + port);
     }*/

    var ss : ServerSocket = null;
    var ds : DatagramSocket = null;
    try {
      ss = new ServerSocket(port);
      ss.setReuseAddress(true);
      ds = new DatagramSocket(port);
      ds.setReuseAddress(true);
      return true;
    } catch {
      case e : IOException =>
    } finally {
      if (ds != null) {
        ds.close();
      }

      if (ss != null) {
        try {
          ss.close();
        } catch {
          case e : IOException =>
            /* should not be thrown */
        }
      }
    }
    return false;
  }

}
