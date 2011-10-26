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

package org.kevoree.framework.aspects

import org.kevoree._

case class ContainerNodeAspect (node: ContainerNode) {

  def isModelEquals (ct: ContainerNode): Boolean = {
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

    /* ADD SUPER TYPE USED BY NODE TYPE DEFINITION */
    //  if (node.getTypeDefinition.getSuperTypes != null) {
    //   usedType = usedType ++ getTypeAndInherited(node.getTypeDefinition)
    //  }

    /* ADD COMPONENT TYPE USED */
    node.getComponents.foreach(c => usedType = usedType ++ getTypeAndInherited(c.getTypeDefinition) )
      /*
    node.getComponents.foreach { c =>
        if (!usedType.exists({
          e => e.getName == c.getTypeDefinition.getName
        })) {
          //usedType = usedType ++ List(c.getTypeDefinition)
          //if (c.getTypeDefinition.getSuperTypes != null) {
          usedType = usedType ++ getTypeAndInherited(c.getTypeDefinition)
          //}
        }
    }   */

    /* ADD CHANNEL TYPE USED */
    /* add channel fragment on node */
    node.eContainer.asInstanceOf[ContainerRoot].getMBindings.foreach {
      mb =>
        if (mb.getPort.eContainer.asInstanceOf[KevoreeContainer].eContainer == node) {
          if (!usedType.exists({
            e => e.getName == mb.getHub.getTypeDefinition.getName
          })) {
            // usedType = usedType ++ List(mb.getHub.getTypeDefinition)
            // if (mb.getHub.getTypeDefinition.getSuperTypes != null) {
            usedType = usedType ++ getTypeAndInherited(mb.getHub.getTypeDefinition)
            // }
          }
        }
    }

    /* add group type on node */
    /* add group */
    node.eContainer.asInstanceOf[ContainerRoot].getGroups.filter(group => group.getSubNodes.contains(node)).foreach({
      c =>
      //usedType = usedType ++ List(c.getTypeDefinition)
      //if (node.getTypeDefinition.getSuperTypes != null) {
        usedType = usedType ++ getTypeAndInherited(c.getTypeDefinition)
      //}
    })

    usedType.toList
  }

  def getChannelFragment: List[Channel] = {
    var usedChannel: List[Channel] = List()
    /* add channel fragment on node */
    node.eContainer.asInstanceOf[ContainerRoot].getMBindings.foreach {
      mb =>
        if (mb.getPort.eContainer.asInstanceOf[KevoreeContainer].eContainer == node) {
          if (!usedChannel.exists({
            e => e.getName == mb.getHub.getName
          })) {
            usedChannel = usedChannel ++ List(mb.getHub)
          }
        }
    }
    usedChannel
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

  private def getTypeAndInherited (t: TypeDefinition): List[TypeDefinition] = {
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

}
