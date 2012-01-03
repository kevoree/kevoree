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

import org.kevoree.api.service.core.handler.ContextKey
import java.lang.String

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 03/01/12
 * Time: 13:55
 * To change this template use File | Settings | File Templates.
 */

case class CaseContextKey(nodeID: String, instanceID: String, name: String, timestamp: Long) extends ContextKey {
  def getNodeID: String = nodeID

  def getInstanceID: String = instanceID

  def getName: String = name

  def getTimestamp: Long = timestamp

  private def timeorstar : String = {
    if(timestamp > 0){
      timestamp+""
    } else {
      "*"
    }
  }
  
  override def toString: String = nodeID + "." + instanceID + "." + name + "." + timeorstar
}