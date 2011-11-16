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

package org.kevoree.framework.message

import scala.reflect.BeanProperty
import java.util.UUID

class Message extends Serializable{

  @BeanProperty 
  var destNodeName = "default"

  @BeanProperty
  var destChannelName = "default"

  @BeanProperty
  var content : Any = null

  @BeanProperty
  var contentClass : String = null

  @BeanProperty
  var inOut : java.lang.Boolean = false

  @BeanProperty
  var responseTag = ""

  @BeanProperty
  var timeout : Long = 3000

  @BeanProperty
  var passedNodes : java.util.List[String] = new java.util.ArrayList[String]()

  @BeanProperty
  var uuid : UUID = UUID.randomUUID()

  
  def getClone : Message = {
    val clone = new Message
    clone.setDestNodeName(this.getDestNodeName())
    clone.setDestChannelName(this.getDestChannelName())
    clone.setContent(this.getContent())
    clone.setContentClass(this.getContentClass())
    clone.setInOut(this.getInOut())
    clone.setResponseTag(this.getResponseTag())
    clone.setTimeout(this.getTimeout())
    clone.setPassedNodes(this.getPassedNodes())
    clone.setUuid(this.getUuid())
    clone
  }


}

