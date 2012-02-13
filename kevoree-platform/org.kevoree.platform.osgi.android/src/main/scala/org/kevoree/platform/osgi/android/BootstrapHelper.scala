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
package org.kevoree.platform.osgi.android

import org.kevoree.{KevoreeFactory, ContainerRoot}
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 24/10/11
 * Time: 13:41
 */

object BootstrapHelper {

  val logger = LoggerFactory.getLogger(this.getClass)

  def initModelInstance(model: ContainerRoot, defType: String, defGroupType: String) {

    val nodeName = KevoreeActivity.nodeName
    if (!model.getNodes.exists(n => n.getName == nodeName)) {
      //CREATE DEFAULT
      model.getTypeDefinitions.find(td => td.getName == defType) match {
        case Some(typeDefFound) => {
          logger.warn("Init default node instance for name " + nodeName)
          val node = KevoreeFactory.createContainerNode
          node.setName(nodeName)
          node.setTypeDefinition(typeDefFound)
          model.addNodes(node)

          model.getTypeDefinitions.find(td => td.getName == defGroupType) match {
            case Some(groupDef)=> {
              val group = KevoreeFactory.createGroup
              group.setTypeDefinition(groupDef)
              group.setName("sync")
              group.setSubNodes(List(node))
              model.addGroups(group)
            }
            case None => logger.error("Default group type not found for name " + defGroupType)
          }


        }
        case None => logger.error("Default node type not found for name " + defType)
      }


    }
  }


}