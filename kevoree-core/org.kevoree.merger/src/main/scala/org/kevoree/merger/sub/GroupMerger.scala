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
package org.kevoree.merger.sub

import org.kevoree.merger.Merger
import org.kevoree.ContainerRoot
import org.kevoree.merger.resolver.UnresolvedTypeDefinition._
import org.kevoree.merger.resolver.UnresolvedTypeDefinition
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/10/11
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */

trait GroupMerger extends Merger with DictionaryMerger{

  private val logger = LoggerFactory.getLogger(this.getClass)

  def mergeAllGroups(actualModel: ContainerRoot, modelToMerge: ContainerRoot) = {
    modelToMerge.getGroups.foreach {
      group =>
      val currentGroup = actualModel.getGroups.find(pgroup => pgroup.getName == group.getName) match {
        case Some(e) => {
          mergeDictionaryInstance(e,group)
          e
        }
        case None => {
          actualModel.addGroups(group)
          group
        }
      }

      val subNodeName =  (currentGroup.getSubNodes.map{sub => sub.getName} ++ group.getSubNodes.map{sub => sub.getName}).toSet
      currentGroup.removeAllSubNodes()
      subNodeName.foreach{ subNode =>
         actualModel.getNodes.find(pnode => pnode.getName == subNode) match {
           case Some(currentNode)=> currentGroup.addSubNodes(currentNode)
           case None => logger.error("Unresolved node "+subNode+" in links for group => "+currentGroup.getName)
         }
      }



    }
  }
}