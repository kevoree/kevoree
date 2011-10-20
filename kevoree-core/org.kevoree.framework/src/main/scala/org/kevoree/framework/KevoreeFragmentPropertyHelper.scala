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

import org.slf4j.LoggerFactory
import org.kevoree.{Instance, TypeDefinition, ContainerRoot}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 13/10/11
 * Time: 14:58
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object KevoreeFragmentPropertyHelper {

  val logger = LoggerFactory.getLogger(this.getClass)


  def getIntPropertyFromFragmentChannel (model: ContainerRoot, channelName: String, key: String,
    nodeNameForFragment: String): Int = {
    val stringProperty = getPropertyFromFragmentChannel(model, channelName, key, nodeNameForFragment)
    try {
      Integer.parseInt(stringProperty)
    } catch {
      case _@e =>
        logger.warn("Unknown error while trying to get an integer property", e)
        model.getHubs.find(channel => channel.getName == channelName) match {
          case None => 0
          case Some(channel) => {
            val stringProperty = getDefaultValue(channel.getTypeDefinition, key)
            try {
              Integer.parseInt(stringProperty)
            } catch {
              case _@e1 =>
                logger.warn("Unknown error while trying to get the default value of an integer property", e)
                0
            }
          }
        }

    }
  }

  def getBooleanPropertyFromFragmentChannel (model: ContainerRoot, channelName: String, key: String,
    nodeNameForFragment: String): Boolean = {
    getPropertyFromFragmentChannel(model, channelName, key, nodeNameForFragment).equalsIgnoreCase("true")
  }

  def getPropertyFromFragmentChannel (model: ContainerRoot, channelName: String, key: String,
    nodeNameForFragment: String): String = {
    model.getHubs.find(g => g.getName == channelName) match {
      case None => ""
      case Some(channel) => {
        getProperty(model, channel, channelName, key, nodeNameForFragment)
      }
    }
  }

  def getIntPropertyFromFragmentGroup (model: ContainerRoot, groupName: String, key: String,
    nodeNameForFragment: String): Int = {
    val stringProperty = getPropertyFromFragmentGroup(model, groupName, key, nodeNameForFragment)
    try {
      Integer.parseInt(stringProperty)
    } catch {
      case _@e =>
        logger.warn("Unknown error while trying to get an integer property", e)
        model.getGroups.find(g => g.getName == groupName) match {
          case None => 0
          case Some(group) => {
            val stringProperty = getDefaultValue(group.getTypeDefinition, key)
            try {
              Integer.parseInt(stringProperty)
            } catch {
              case _@e1 =>
                logger.warn("Unknown error while trying to get the default value of an integer property", e)
                0
            }
          }
        }

    }
  }

  def getBooleanPropertyFromFragmentGroup (model: ContainerRoot, groupName: String, key: String,
    nodeNameForFragment: String): Boolean = {
    getPropertyFromFragmentGroup(model, groupName, key, nodeNameForFragment).equalsIgnoreCase("true")
  }

  def getPropertyFromFragmentGroup (model: ContainerRoot, groupName: String, key: String,
    nodeNameForFragment: String): String = {
    model.getGroups.find(g => g.getName == groupName) match {
      case None => ""
      case Some(group) => {
        getProperty(model, group, groupName, key, nodeNameForFragment)
      }
    }
  }

  private def getProperty (model: ContainerRoot, instance: Instance, name: String, key: String,
    nodeNameForFragment: String): String = {
    instance.getDictionary match {
      case None => getDefaultValue(instance.getTypeDefinition, key)
      case Some(dictionary) => {
        dictionary.getValues.find(dictionaryAttribute => dictionaryAttribute.getAttribute.getName == key &&
          (if(dictionaryAttribute.getTargetNode.isDefined){dictionaryAttribute.getTargetNode.get.getName == nodeNameForFragment } else {false})) match {
          case None => getDefaultValue(instance.getTypeDefinition, key)
          case Some(dictionaryAttribute) => dictionaryAttribute.getValue
        }
      }
    }
  }

  private def getDefaultValue (typeDefinition: TypeDefinition, key: String): String = {
    typeDefinition.getDictionaryType.get.getDefaultValues
      .find(defaultValue => defaultValue.getAttribute.getName == key) match {
      case None => ""
      case Some(defaultValue) => defaultValue.getValue
    }
  }
}