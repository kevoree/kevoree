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
package org.kevoree.merger.sub

import org.kevoree.merger.Merger
import org.kevoree.{ContainerNode, Group, ContainerRoot}
import org.kevoree.merger.resolver.UnresolvedTypeDefinition._
import org.kevoree.merger.resolver.UnresolvedTypeDefinition
import scala.collection.JavaConversions._


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/10/11
 * Time: 14:52
 */

trait GroupMerger extends Merger with DictionaryMerger{

  def mergeAllGroups(actualModel: ContainerRoot, modelToMerge: ContainerRoot) {
    modelToMerge.getGroups.foreach {
      group =>
      val currentGroup = actualModel.findByPath(group.path(),classOf[Group]) match {
        case e: Group => {
          mergeDictionaryInstance(e,group)
          e
        }
        case null => {
          actualModel.addGroups(group)
          group
        }
      }

      val subNodeName =  (currentGroup.getSubNodes.toList ++ group.getSubNodes).toSet
      currentGroup.removeAllSubNodes()
      subNodeName.foreach{ subNode =>
        actualModel.findByPath(subNode.path(),classOf[ContainerNode]) match {
       // actualModel.getNodes.find(pnode => pnode.getName == subNode) match {
           case currentNode : ContainerNode => currentGroup.addSubNodes(currentNode)
           case null => org.kevoree.log.Log.error("Unresolved node {}  in links for group => {} ",subNode,currentGroup.getName)
         }
      }



    }
  }
}