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

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 03/01/12
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */

object ContextKeyHelper {

  def createKey(nodeID:String, instanceID : String, name:String, timestamp : java.lang.Long){
    CaseContextKey(nodeID,instanceID,name,timestamp)
  }

  def createAllQuery(nodeID:String, instanceID : String, name:String){
    CaseContextKey(nodeID,instanceID,name,-1)
  }
  
}