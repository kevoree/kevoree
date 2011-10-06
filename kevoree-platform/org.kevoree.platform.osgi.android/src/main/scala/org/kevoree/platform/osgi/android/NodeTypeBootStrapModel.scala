package org.kevoree.platform.osgi.android

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
import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory
import java.util.Properties
import org.kevoree.{NodeType, KevoreeFactory, ContainerRoot}

/**
 * User: ffouquet
 * Date: 15/09/11
 * Time: 10:04
 */

object NodeTypeBootStrapModel {

  val logger = LoggerFactory.getLogger(this.getClass)

  def checkAndCreate(model: ContainerRoot, nodeName: String, nodeTypeName: String, props: Properties) {
    model.getNodes.find {
      node => node.getName == nodeName
    } match {
      case Some(node) => {
        if (node.getTypeDefinition.getName != nodeTypeName) {
          logger.error("NodeType consistency error !")
          model.getNodes.remove(node)
          createNode(model, nodeName, nodeTypeName, props)
        }
      }
      case None => {
        createNode(model, nodeName, nodeTypeName, props)
      }
    }
  }

  private def createNode(model: ContainerRoot, nodeName: String, nodeTypeName: String, props: Properties) {
    model.getTypeDefinitions.filter(td => td.isInstanceOf[NodeType]).find(td => td.getName == nodeTypeName) match {
      case Some(nodeTypeDef) => {
        val node = KevoreeFactory.eINSTANCE.createContainerNode
        node.setName(nodeName)
        node.setTypeDefinition(nodeTypeDef)
        model.getNodes.add(node)

        val propsmodel = KevoreeFactory.eINSTANCE.createDictionary
        props.keySet().foreach {
          key =>
            if (nodeTypeDef.getDictionaryType.isDefined) {
              nodeTypeDef.getDictionaryType.get.getAttributes.find(att => att.getName == key) match {
                case Some(att) => {
                  val newValue = KevoreeFactory.eINSTANCE.createDictionaryValue
                  newValue.setAttribute(att)
                  newValue.setValue(props.get(key).toString)
                }
                case None => logger.warn("Node bootstrap property lost " + key)
              }
            } else {
              logger.warn("Node bootstrap property lost " + key)
            }
        }
        node.setDictionary(propsmodel)
      }
      case None => logger.error("NodeType definition not found")
    }
  }

}