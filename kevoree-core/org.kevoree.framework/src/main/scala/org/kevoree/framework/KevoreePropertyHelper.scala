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
package org.kevoree.framework

import org.kevoree.{TypeDefinition, Instance, ContainerRoot}
import java.util.ArrayList
import java.util


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/01/12
 * Time: 15:08
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object KevoreePropertyHelper {

  def getNetworkProperties (model: ContainerRoot, targetNodeName: String, key: String): java.util.List[String] = {
    val properties = new util.ArrayList[String]()
    val filteredNodeNetwork = model.getNodeNetworks.filter(lNN => lNN.getTarget.getName == targetNodeName)
    filteredNodeNetwork.foreach {
      fnn =>
        fnn.getLink.foreach {
          fnl =>
            fnl.getNetworkProperties.find(p => p.getName == key) match {
              case None =>
              case Some(prop) => properties.add(prop.getValue)
            }
        }
    }
    properties
  }

  def getProperty (instance: Instance, key: String, isFragment: Boolean = false, nodeNameForFragment: String = ""): Option[String] = {
    instance.getDictionary match {
      case None => {
        getDefaultValue(instance.getTypeDefinition, key)
      }
      case Some(dictionary) => {
        dictionary.getValues.find(dictionaryAttribute =>
          dictionaryAttribute.getAttribute.getName == key &&
            ((isFragment && dictionaryAttribute.getTargetNode.isDefined && dictionaryAttribute.getTargetNode.get.getName == nodeNameForFragment) || !isFragment)) match {
          case None => getDefaultValue(instance.getTypeDefinition, key)
          case Some(dictionaryAttribute) => Some(dictionaryAttribute.getValue)
        }
      }
    }
  }

  private def getDefaultValue (typeDefinition: TypeDefinition, key: String): Option[String] = {
    if (typeDefinition.getDictionaryType.isDefined) {
      typeDefinition.getDictionaryType.get.getDefaultValues.find(defaultValue => defaultValue.getAttribute.getName == key) match {
        case None => None
        case Some(defaultValue) => Some(defaultValue.getValue)
      }
    } else {
      None
    }
  }
}