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
import org.kevoree.merger.resolver.UnresolvedTypeDefinition._
import org.kevoree.merger.resolver.UnresolvedTypeDefinition
import org.slf4j.LoggerFactory
import org.kevoree.{KevoreeFactory, ComponentInstance, ContainerNode, ContainerRoot}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/10/11
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */

trait ChannelMerger extends Merger {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def mergeAllChannels(actualModel: ContainerRoot, modelToMerge: ContainerRoot) = {
    actualModel.getHubs.foreach {
      hub => hub.setTypeDefinition(UnresolvedTypeDefinition(hub.getTypeDefinition.getName))
    }
    //MERGE CHANNEL
    modelToMerge.getHubs.foreach {
      hub =>
      val currentHub = actualModel.getGroups.find(phub => phub.getName == hub.getName) match {
        case Some(e) => e
        case None => {
          actualModel.addHubs(hub)
          hub
        }
      }
    }
    //MERGE NEW BINDING
    modelToMerge.getMBindings.foreach {
      mb =>
        actualModel.getHubs.find(hub => hub.getName == mb.getHub.getName) match {
          case Some(foundHub) => {
            actualModel.getNodes.find(node => node.getName == mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName) match {
              case Some(foundNode) => {
                foundNode.getComponents.find(component => component.getName == mb.getPort.eContainer.asInstanceOf[ComponentInstance].getName) match {
                  case Some(foundComponent) => {
                    (foundComponent.getRequired ++ foundComponent.getProvided).find(port => port.getPortTypeRef.getName == mb.getPort.getPortTypeRef.getName) match {
                      case Some(foundPort) => {
                        val newbinding = KevoreeFactory.eINSTANCE.createMBinding
                        newbinding.setHub(foundHub)
                        newbinding.setPort(foundPort)
                        if (!actualModel.getMBindings.exists(mb => mb.getHub == foundHub && mb.getPort == foundPort)) {
                          actualModel.addMBindings(newbinding)
                        }
                      }
                      case None => logger.error("Error while merging binding, can't found port for name " + mb.getPort.getPortTypeRef.getName)
                    }
                  }
                  case None => logger.error("Error while merging binding, can't found component for name " + mb.getPort.eContainer.asInstanceOf[ComponentInstance].getName)
                }
              }
              case None => logger.error("Error while merging binding, can't found node for name " + mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName)
            }
          }
          case None => logger.error("Error while merging binding, can't found channel for name " + mb.getHub.getName)
        }
    }


  }
}