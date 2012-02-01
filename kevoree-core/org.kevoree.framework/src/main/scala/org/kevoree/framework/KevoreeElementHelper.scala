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

import org.kevoree._


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 31/01/12
 * Time: 11:20
 *
 * This helper allows to find a element on the model according to its name
 *
 * @author Erwan Daubert
 * @version 1.0
 */
object KevoreeElementHelper {

  def getChannelElement (channelName: String, model: ContainerRoot): Option[Channel] = {
    model.getHubs.find(channel => channel.getName == channelName) 
  }

  def getComponentElement (componentName: String, nodeName : String, model: ContainerRoot): Option[ComponentInstance] = {
    val nodeOption = getNodeElement(nodeName,  model)
    if (nodeOption.isDefined) {
      nodeOption.get.getComponents.find(component => component.getName == componentName)
    } else {
      None
    }
  }

  def getNodeElement (nodeName: String, model: ContainerRoot): Option[ContainerNode] = {
    model.getNodes.find(node => node.getName == nodeName) 
  }

  def getGroupElement (groupName: String, model: ContainerRoot): Option[Group] = {
    model.getGroups.find(group => group.getName == groupName) 
  }

}