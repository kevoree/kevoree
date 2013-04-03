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
package org.kevoree.platform.android.core

import org.slf4j.LoggerFactory
import org.kevoree.impl.DefaultKevoreeFactory
import org.kevoree.ContainerRoot

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 29/02/12
 * Time: 17:39
 */

class BootstrapHelper {

    val logger = LoggerFactory.getLogger(this.javaClass)!!

    val factory = DefaultKevoreeFactory()

    fun initModelInstance(model: ContainerRoot, defType: String, defGroupType: String, nodeName: String) {
        val nodeFound = model.findNodesByID(nodeName)
        if(nodeFound == null){
            val td = model.findTypeDefinitionsByID(defType)
            if(td != null){
                logger.warn("Init default node instance for name " + nodeName)
                val node = factory.createContainerNode()
                node.setName(nodeName)
                node.setTypeDefinition(td)
                model.addNodes(node)

                val gtd = model.findTypeDefinitionsByID(defGroupType)
                if(gtd != null){
                    val group = factory.createGroup()
                    group.setTypeDefinition(gtd)
                    group.setName("sync")
                    group.addSubNodes(node)
                    model.addGroups(group)
                } else {
                    logger.error("Default group type not found for name " + defGroupType)
                }

            } else {
                logger.error("Default node type not found for name " + defType)
            }
        } else {
            logger.info("the model is already init for nodename " + nodeName)
        }
    }
}