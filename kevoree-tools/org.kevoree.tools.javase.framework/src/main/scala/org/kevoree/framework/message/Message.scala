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
package org.kevoree.framework.message

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

import java.util.UUID

class Message extends Serializable {

  var destNodeName = "default"

  def getDestNodeName: String = {
    destNodeName
  }

  def setDestNodeName (newDestNodeName: String) {
    destNodeName = newDestNodeName
  }

  var destChannelName = "default"

  def getDestChannelName: String = {
    destChannelName
  }

  def setDestChannelName (newDestChannelName: String) {
    destChannelName = newDestChannelName
  }

  var content: Any = null

  def getContent: Any = {
    content
  }

  def setContent (newContent: Any) {
    content = newContent
  }

  var contentClass: String = null

  def getContentClass: String = {
    contentClass
  }

  def setContentClass (newContentClass: String) {
    contentClass = newContentClass
  }

  var inOut: java.lang.Boolean = false

  def getInOut: java.lang.Boolean = {
    inOut
  }

  def setInOut (newInOut: java.lang.Boolean) {
    inOut = newInOut
  }

  var responseTag = ""

  def getResponseTag: String = {
    responseTag
  }

  def setResponseTag (newResponseTag: String) {
    responseTag = newResponseTag
  }

  var timeout: Long = 3000

  def getTimeout: Long = {
    timeout
  }

  def setTimeout (newTimeout: Long) {
    timeout = newTimeout
  }

  var passedNodes: java.util.List[String] = new java.util.ArrayList[String]()

  def getPassedNodes: java.util.List[String] = {
    passedNodes
  }

  def setPassedNodes (newPassedNodes: java.util.List[String]) {
    passedNodes = newPassedNodes
  }

  var uuid: UUID = UUID.randomUUID()

  def getUuid: UUID = {
    uuid
  }

  def setUuid (newUuid: UUID) {
    uuid = newUuid
  }

  override def clone: Message = {
    val clone = new Message
    clone.setDestNodeName(this.getDestNodeName)
    clone.setDestChannelName(this.getDestChannelName)
    clone.setContent(this.getContent)
    clone.setContentClass(this.getContentClass)
    clone.setInOut(this.getInOut)
    clone.setResponseTag(this.getResponseTag)
    clone.setTimeout(this.getTimeout)
    clone.setPassedNodes(this.getPassedNodes)
    clone.setUuid(this.getUuid)
    clone
  }


}

