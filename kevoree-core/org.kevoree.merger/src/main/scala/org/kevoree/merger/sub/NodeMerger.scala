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

package org.kevoree.merger.sub

import org.kevoree.ContainerNode
import org.kevoree.ContainerRoot
import org.kevoree.merger.aspects.KevoreeAspects._
import org.kevoree.merger.resolver.{UnresolvedChildNode, UnresolvedTypeDefinition}
import scala.collection.JavaConversions._



trait NodeMerger extends ComponentInstanceMerger with DictionaryMerger {

  def mergeAllNode(actualModel: ContainerRoot, modelToMerge: ContainerRoot) {
    //BREAK CROSS REFERENCE NODE TYPE
    modelToMerge.getNodes.foreach {
      toMergeNode => mergeNode(actualModel, toMergeNode)
    }
  }

  private def mergeNode(actualModel: ContainerRoot, nodeToMerge: ContainerNode) {
    //actualModel.getNodes.find(loopNode => loopNode.getName == nodeToMerge.getName) match {
      actualModel.findByPath(nodeToMerge.path(),classOf[ContainerNode]) match {
      case null => {
        actualModel.addNodes(nodeToMerge)
        mergeAllInstances(actualModel, nodeToMerge, nodeToMerge)
        mergeChilds(actualModel, nodeToMerge)
      }
      case eNode : Any => {
        mergeDictionaryInstance(eNode, nodeToMerge)
        mergeAllInstances(actualModel, eNode, nodeToMerge)
        mergeChilds(actualModel, eNode, nodeToMerge)
      }
    }
  }

  private def mergeAllInstances(actualModel: ContainerRoot, targetInstance: ContainerNode, instanceToMerge: ContainerNode) {
    instanceToMerge.getComponents.foreach {
      c =>
        targetInstance.getComponents.find(eC => eC.isModelEquals(c)) match {
          case None => {
            targetInstance.setTypeDefinition(new UnresolvedTypeDefinition(targetInstance.getTypeDefinition.getName))
            targetInstance.addComponents(c)
            mergeComponentInstance(actualModel, c, null)
          }
          case Some(targetComponent) => mergeComponentInstance(actualModel, c, targetComponent)
        }
    }
  }

  private def mergeChilds(actualModel: ContainerRoot, instance: ContainerNode) {
    instance.getHosts.foreach {
      child =>
        actualModel.getNodes.find(c => c.getName == child.getName) match {
          case None => // the child is not already added on the model so it will be added later
          case Some(c) =>
            if (c != child) {
              // the child already exist on the model before the merge so we need to update the hosts
              instance.removeHosts(child)
              if (!instance.getHosts.exists(i => i.getName == child.getName)) {
                instance.addHosts(new UnresolvedChildNode(child.getName))
              }
            }
        }
    }
  }

  private def mergeChilds(actualModel: ContainerRoot, targetInstance: ContainerNode, instanceToMerge: ContainerNode) {
    instanceToMerge.getHosts.foreach {
      child =>
        targetInstance.getHosts.find(n => n.getName == child.getName) match {
          case None => {
            targetInstance.addHosts(new UnresolvedChildNode(child.getName))
          }
          case Some(n) => // the host node is already contained by the targetInstance node
        }
    }
  }


}
