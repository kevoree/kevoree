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

package org.kevoree.framework.aspects

import org.kevoree._
import KevoreeAspects._
import org.slf4j.LoggerFactory
import collection.mutable.ListBuffer
import scala.Some
import scala.collection.JavaConversions._

case class ContainerNodeAspect(node: ContainerNode) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def isModelEquals(ct: ContainerNode): Boolean = {
    ct.getName == node.getName
    /* TODO deep compare */
  }

  def getComponentTypes: List[ComponentType] = {
    var alreadyDeployComponentType: List[ComponentType] = List()
    node.getComponents.foreach {
      c =>
        if (!alreadyDeployComponentType.exists({
          e => e.getName == c.getTypeDefinition.getName
        })) {
          alreadyDeployComponentType = alreadyDeployComponentType ++
            List(c.getTypeDefinition.asInstanceOf[ComponentType])
        }
    }
    alreadyDeployComponentType
  }

  def getUsedTypeDefinition: List[TypeDefinition] = {
    var usedType: Set[TypeDefinition] = Set()

    /* ADD NODE TYPE DEFINITION */
    usedType = usedType ++ getTypeAndInherited(node.getTypeDefinition)
    /* ADD COMPONENT TYPE USED */
    node.getComponents.foreach(c => usedType = usedType ++ getTypeAndInherited(c.getTypeDefinition))

    /* ADD CHANNEL TYPE USED */
    /* add channel fragment on node */
    node.eContainer.asInstanceOf[ContainerRoot].getMBindings.foreach {
      mb =>
        if (mb.getPort.eContainer.eContainer == node) {
          if (!usedType.exists({
            e => e.getName == mb.getHub.getTypeDefinition.getName
          })) {
            usedType = usedType ++ getTypeAndInherited(mb.getHub.getTypeDefinition)
          }
        }
    }

    /* add group type on node */
    /* add group */
    node.eContainer.asInstanceOf[ContainerRoot].getGroups.filter(group => group.getSubNodes.contains(node)).foreach({
      c =>
        usedType = usedType ++ getTypeAndInherited(c.getTypeDefinition)

    })

    usedType.toList
  }

  def getChannelFragment: List[Channel] = {
    /* add channel fragment on node */
    val usedChannel: ListBuffer[Channel] = ListBuffer[Channel]()
    node.getComponents.foreach {
      component =>
        (component.getProvided.toList ++ component.getRequired.toList).foreach {
          port => port.getBindings.foreach {
            mbinding => if (!usedChannel.exists(c => c.getName == mbinding.getHub.getName)) {
              usedChannel += mbinding.getHub
            }
          }
        }
    }
    usedChannel.toList
  }

  def getGroups: List[Group] = {
    var usedGroup: List[Group] = List()
    node.eContainer.asInstanceOf[ContainerRoot].getGroups.filter(group => group.getSubNodes.contains(node))
      .foreach(group => {
      usedGroup = usedGroup ++ List(group)
    })

    usedGroup
  }


  def getInstances: List[Instance] = getGroups ++ getChannelFragment ++ node.getComponents

  private def getTypeAndInherited(t: TypeDefinition): List[TypeDefinition] = {
    var types = List[TypeDefinition]()
    if (t.getSuperTypes != null) {
      t.getSuperTypes.foreach {
        superT =>
          types = types ++ getTypeAndInherited(superT)
      }
      types = types ++ List(t)
    }
    types
  }

  def isDeployUnitUsed(du: DeployUnit): Boolean = {
    node.getUsedTypeDefinition.exists(usedTypeDef => {
      val usedDU = usedTypeDef.foundRelevantDeployUnit(node)
      usedDU.isDeployUnitUsed(du)
    })
  }


  def getKevoreeVersion: String = {
    try {

      val rDU = node.getTypeDefinition.foundRelevantDeployUnit(node)
      if (rDU != null) {
        rDU.getRequiredLibs.find(du => du.getGroupName == "org.kevoree" && du.getUnitName == "org.kevoree.api") match {
          case None => {
            logger.error("Error found api deploy unit for " + node.getName)
            ""
          } // must never appear
          case Some(du) => du.getVersion
        }

      } else {
        logger.error("Error found relevant deploy unit for " + node.getName)
        ""
      }
    } catch {
      case _@e => logger.debug("Unable to find kevoree version", e); ""
    }
  }


}
