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
package org.kevoree.framework

import org.kevoree.{TypeDefinition, Instance, ContainerRoot}


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/01/12
 * Time: 15:08
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object KevoreePropertyHelper {

  def getBooleanPropertyForGroup (model: ContainerRoot, groupName: String, key: String, isFragment: Boolean = false, nodeNameForFragment: String = ""): Option[Boolean] = {
    getPropertyForGroup(model, groupName, key, isFragment, nodeNameForFragment) match {
      case None => None
      case Some(value) => try {
        Some(value.toString.toLowerCase == "true")
      } catch {
        case _@e => None
      }
    }
  }

  def getIntPropertyForGroup (model: ContainerRoot, groupName: String, key: String, isFragment: Boolean = false, nodeNameForFragment: String = ""): Option[java.lang.Integer] = {
    getPropertyForGroup(model, groupName, key, isFragment, nodeNameForFragment) match {
      case None => None
      case Some(value) => try {
        Some(Integer.parseInt(value.toString))
      } catch {
        case _@e => None
      }
    }
  }

  def getStringPropertyForGroup (model: ContainerRoot, groupName: String, key: String, isFragment: Boolean = false, nodeNameForFragment: String = ""): Option[String] = {
    getPropertyForGroup(model, groupName, key, isFragment, nodeNameForFragment) match {
      case None => None
      case Some(value) => try {
        Some(value.toString)
      } catch {
        case _@e => None
      }
    }
  }

  def getPropertyForGroup (model: ContainerRoot, groupName: String, key: String, isFragment: Boolean = false, nodeNameForFragment: String = ""): Option[Object] = {
    model.getGroups.find(group => group.getName == groupName) match {
      case Some(group) => getProperty(model, group, groupName, key, isFragment, nodeNameForFragment)
      case None => None
    }
  }

  def getBooleanPropertyForChannel (model: ContainerRoot, channelName: String, key: String, isFragment: Boolean = false, nodeNameForFragment: String = ""): Option[Boolean] = {
    getPropertyForChannel(model, channelName, key, isFragment, nodeNameForFragment) match {
      case None => None
      case Some(value) => try {
        Some(value.toString.toLowerCase == "true")
      } catch {
        case _@e => None
      }
    }
  }

  def getIntPropertyForChannel (model: ContainerRoot, channelName: String, key: String, isFragment: Boolean = false, nodeNameForFragment: String = ""): Option[java.lang.Integer] = {
    getPropertyForChannel(model, channelName, key, isFragment, nodeNameForFragment) match {
      case None => None
      case Some(value) => try {
        Some(Integer.parseInt(value.toString))
      } catch {
        case _@e => None
      }
    }
  }

  def getStringPropertyForChannel (model: ContainerRoot, channelName: String, key: String, isFragment: Boolean = false, nodeNameForFragment: String = ""): Option[String] = {
    getPropertyForChannel(model, channelName, key, isFragment, nodeNameForFragment) match {
      case None => None
      case Some(value) => try {
        Some(value.toString)
      } catch {
        case _@e => None
      }
    }
  }

  def getPropertyForChannel (model: ContainerRoot, channelName: String, key: String, isFragment: Boolean = false, nodeNameForFragment: String = ""): Option[Object] = {
    model.getHubs.find(channel => channel.getName == channelName) match {
      case Some(channel) => getProperty(model, channel, channelName, key, isFragment, nodeNameForFragment)
      case None => None
    }
  }

  def getBooleanPropertyForNode (model: ContainerRoot, nodeName: String, key: String): Option[Boolean] = {
    getPropertyForNode(model, nodeName, key) match {
      case None => None
      case Some(value) => try {
        Some(value.toString.toLowerCase == "true")
      } catch {
        case _@e => None
      }
    }
  }

  def getIntPropertyForNode (model: ContainerRoot, nodeName: String, key: String): Option[java.lang.Integer] = {
    getPropertyForNode(model, nodeName, key) match {
      case None => None
      case Some(value) => try {
        Some(Integer.parseInt(value.toString))
      } catch {
        case _@e => None
      }
    }
  }

  def getStringPropertyForNode (model: ContainerRoot, nodeName: String, key: String): Option[String] = {
    getPropertyForNode(model, nodeName, key) match {
      case None => None
      case Some(value) => try {
        Some(value.toString)
      } catch {
        case _@e => None
      }
    }
  }

  def getPropertyForNode (model: ContainerRoot, nodeName: String, key: String): Option[Object] = {
    model.getNodes.find(node => node.getName == nodeName) match {
      case Some(node) => getProperty(model, node, nodeName, key)
      case None => None
    }
  }

  def getBooleanPropertyForComponent (model: ContainerRoot, componentName: String, key: String): Option[Boolean] = {
    getPropertyForComponent(model, componentName, key) match {
      case None => None
      case Some(value) => try {
        Some(value.toString.toLowerCase == "true")
      } catch {
        case _@e => None
      }
    }
  }

  def getIntPropertyForComponent (model: ContainerRoot, componentName: String, key: String): Option[java.lang.Integer] = {
    getPropertyForComponent(model, componentName, key) match {
      case None => None
      case Some(value) => try {
        Some(Integer.parseInt(value.toString))
      } catch {
        case _@e => None
      }
    }
  }

  def getStringPropertyForComponent (model: ContainerRoot, componentName: String, key: String): Option[String] = {
    getPropertyForComponent(model, componentName, key) match {
      case None => None
      case Some(value) => try {
        Some(value.toString)
      } catch {
        case _@e => None
      }
    }
  }

  def getPropertyForComponent (model: ContainerRoot, componentName: String, key: String): Option[Object] = {
    var result: Option[Object] = None
    model.getNodes.forall {
      node => node.getComponents.find(component => component.getName == componentName) match {
        case Some(component) => result = getProperty(model, component, componentName, key)
        case None => None
      }
      result.isEmpty
    }
    result
  }

  def getBooleanNetworkProperty (model: ContainerRoot, targetNodeName: String, key: String): Option[Boolean] = {
      getNetworkProperty(model, targetNodeName, key) match {
        case None => None
        case Some(value) => try {
          Some(value.toString.toLowerCase == "true")
        } catch {
          case _@e => None
        }
      }
    }

  def getIntNetworkProperty (model: ContainerRoot, targetNodeName: String, key: String): Option[java.lang.Integer] = {
      getNetworkProperty(model, targetNodeName, key) match {
        case None => None
        case Some(value) => try {
          Some(Integer.parseInt(value.toString))
        } catch {
          case _@e => None
        }
      }
    }

  def getStringNetworkProperty (model: ContainerRoot, targetNodeName: String, key: String): Option[String] = {
    getNetworkProperty(model, targetNodeName, key) match {
      case None => None
      case Some(value) => try {
        Some(value.toString)
      } catch {
        case _@e => None
      }
    }
  }

  def getNetworkProperty (model: ContainerRoot, targetNodeName: String, key: String): Option[Object] = {
    val filteredNodeNetwork = model.getNodeNetworks.filter(lNN => lNN.getTarget.getName == targetNodeName)
    var result: Option[Object] = None
    filteredNodeNetwork.foreach {
      fnn =>
        fnn.getLink.foreach {
          fnl =>
            fnl.getNetworkProperties.find(p => p.getName == key) match {
              case None =>
              case Some(prop) => result = Some(prop.getValue)
            }
        }
    }
    result
  }

  private def getProperty (model: ContainerRoot, instance: Instance, name: String, key: String, isFragment: Boolean = false, nodeNameForFragment: String = ""): Option[Object] = {
    instance.getDictionary match {
      case None => {
        getDefaultValue(instance.getTypeDefinition, key)
      }
      case Some(dictionary) => {
        dictionary.getValues.find(dictionaryAttribute => dictionaryAttribute.getAttribute.getName == key &&
          (if (isFragment && dictionaryAttribute.getTargetNode.isDefined) {
            dictionaryAttribute.getTargetNode.get.getName == nodeNameForFragment
          } else if (!isFragment) {
            true
          } else {
            false
          })) match {
          case None => getDefaultValue(instance.getTypeDefinition, key)
          case Some(dictionaryAttribute) => Some(dictionaryAttribute.getValue)
        }
      }
    }
  }

  private def getDefaultValue (typeDefinition: TypeDefinition, key: String): Option[Object] = {
    typeDefinition.getDictionaryType.get.getDefaultValues
      .find(defaultValue => defaultValue.getAttribute.getName == key) match {
      case None => None
      case Some(defaultValue) => Some(defaultValue.getValue)
    }
  }
}