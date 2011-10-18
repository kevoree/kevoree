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
package org.kevoree.platform.osgi.standalone.gui

import org.slf4j.LoggerFactory
import java.util.Properties
import org.kevoree._

/**
 * User: ffouquet
 * Date: 15/09/11
 * Time: 10:04
 */

object NodeTypeBootStrapModel {

  val logger = LoggerFactory.getLogger(this.getClass)

  def checkAndCreate(model: ContainerRoot, nodeName: String, nodeTypeName: String, groupTypeName: String, groupName: String, propsNode: Properties, propsGroup: Properties) {
    val node: ContainerNode = model.getNodes.find {
      node => node.getName == nodeName
    } match {
      case Some(node) => {
        if (node.getTypeDefinition.getName != nodeTypeName) {
          logger.error("NodeType consistency error !")
          model.addNodes(node)
          createNode(model, nodeName, nodeTypeName, propsNode)
        } else {
          node
        }
      }
      case None => {
        createNode(model, nodeName, nodeTypeName, propsNode)
      }
    }
    model.getGroups.find(g => g.getName == groupName) match {
      case Some(g) => println("Already present group ")
      case None => {
        createGroup(node,model,groupName,groupTypeName,propsGroup)
      }
    }


  }

  private def createGroup(node : ContainerNode,model: ContainerRoot, groupName: String, groupTypeName: String, props: Properties): Group = {
    model.getTypeDefinitions.filter(td => td.isInstanceOf[GroupType]).find(td => td.getName == groupTypeName) match {
      case Some(groupTypeDef) => {
        val group = KevoreeFactory.eINSTANCE.createGroup
        group.setName(groupName)
        group.setTypeDefinition(groupTypeDef)
        val propsmodel = KevoreeFactory.eINSTANCE.createDictionary
        import scala.collection.JavaConversions._
        props.keySet().foreach {
          key =>
            if (groupTypeDef.getDictionaryType.isDefined) {
              groupTypeDef.getDictionaryType.get.getAttributes.find(att => att.getName == key) match {
                case Some(att) => {
                  val newValue = KevoreeFactory.eINSTANCE.createDictionaryValue
                  newValue.setAttribute(att)
                  newValue.setValue(props.get(key).toString)
                  propsmodel.addValues(newValue)
                }
                case None => logger.warn("Node bootstrap property lost " + key)
              }
            } else {
              logger.warn("Node bootstrap property lost " + key)
            }
        }
        group.setDictionary(Some(propsmodel))
        model.addGroups(group)
        group.addSubNodes(node)
        group
      }
      case None => logger.error("Type node found => " + groupTypeName); null
    }
  }

  private def createNode(model: ContainerRoot, nodeName: String, nodeTypeName: String, props: Properties): ContainerNode = {

    model.getTypeDefinitions.filter(td => td.isInstanceOf[NodeType]).find(td => td.getName == nodeTypeName) match {
      case Some(nodeTypeDef) => {
        val node = KevoreeFactory.eINSTANCE.createContainerNode
        node.setName(nodeName)
        node.setTypeDefinition(nodeTypeDef)
        model.addNodes(node)

        val propsmodel = KevoreeFactory.eINSTANCE.createDictionary
        import scala.collection.JavaConversions._
        props.keySet().foreach {
          key =>
            if (nodeTypeDef.getDictionaryType.isDefined) {
              nodeTypeDef.getDictionaryType.get.getAttributes.find(att => att.getName == key) match {
                case Some(att) => {
                  val newValue = KevoreeFactory.eINSTANCE.createDictionaryValue
                  newValue.setAttribute(att)
                  newValue.setValue(props.get(key).toString)
                  propsmodel.addValues(newValue)
                }
                case None => logger.warn("Node bootstrap property lost " + key)
              }
            } else {
              logger.warn("Node bootstrap property lost " + key)
            }
        }
        node.setDictionary(Some(propsmodel))
        node
      }
      case None => logger.error("NodeType definition not found"); null
    }
  }

}