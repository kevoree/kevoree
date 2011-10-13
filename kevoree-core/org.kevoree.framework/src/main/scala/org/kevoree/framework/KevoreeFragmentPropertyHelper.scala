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

import org.kevoree.ContainerRoot
import org.slf4j.LoggerFactory

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


  def getIntPropertyFromFragmentChannel (model: ContainerRoot, channelName: String, key: String, nodeNameForFragment : String): Int = {
    model.getHubs.find(c => c.getName == channelName) match {
      case None => 0
      case Some(channel) => {
        channel.getDictionary match {
          case None => 0
          case Some(dictionary) => {
            dictionary.getValues.find(dictionaryAttribute => dictionaryAttribute.getAttribute.getName == key &&
              dictionaryAttribute.getTargetNode == nodeNameForFragment) match {
              case None => 0
              case Some(dictionaryAttribute) => {
                try {
                  Integer.parseInt(dictionaryAttribute.getValue)
                } catch {
                  case _@e =>
                    logger.warn("Unknown error while trying to get an integer property", e)
                    0
                }
              }
            }
          }
        }
      }
    }
  }

  def getBooleanPropertyFromFragmentChannel (model: ContainerRoot, channelName: String, key: String, nodeNameForFragment : String): Boolean = {
    model.getHubs.find(c => c.getName == channelName) match {
      case None => false
      case Some(channel) => {
        channel.getDictionary match {
          case None => false
          case Some(dictionary) => {
            dictionary.getValues.find(dictionaryAttribute => dictionaryAttribute.getAttribute.getName == key &&
              dictionaryAttribute.getTargetNode == nodeNameForFragment) match {
              case None => false
              case Some(dictionaryAttribute) => {
                dictionaryAttribute.getValue.equalsIgnoreCase("true")
              }
            }
          }
        }
      }
    }
  }

  def getPropertyFromFragmentChannel (model: ContainerRoot, channelName: String, key: String,
    nodeNameForFragment : String): String = {
    model.getHubs.find(c => c.getName == channelName) match {
      case None => ""
      case Some(channel) => {
        channel.getDictionary match {
          case None => ""
          case Some(dictionary) => {
            dictionary.getValues.find(dictionaryAttribute => dictionaryAttribute.getAttribute.getName == key &&
              dictionaryAttribute.getTargetNode == nodeNameForFragment) match {
              case None => ""
              case Some(dictionaryAttribute) => {
                dictionaryAttribute.getValue
              }
            }
          }
        }
      }
    }
  }

  def getIntPropertyFromFragmentGroup (model: ContainerRoot, groupName: String, key: String,
    nodeNameForFragment : String): Int = {
    model.getGroups.find(g => g.getName == groupName) match {
      case None => 0
      case Some(group) => {
        group.getDictionary match {
          case None => 0
          case Some(dictionary) => {
            dictionary.getValues.find(dictionaryAttribute => dictionaryAttribute.getAttribute.getName == key &&
              dictionaryAttribute.getTargetNode == nodeNameForFragment) match {
              case None => 0
              case Some(dictionaryAttribute) => {
                try {
                  Integer.parseInt(dictionaryAttribute.getValue)
                } catch {
                  case _@e =>
                    logger.warn("Unknown error while trying to get an integer property", e)
                    0
                }
              }
            }
          }
        }
      }
    }
  }

  def getBooleanPropertyFromFragmentGroup (model: ContainerRoot, groupName: String, key: String,
    nodeNameForFragment : String): Boolean = {
    model.getGroups.find(g => g.getName == groupName) match {
      case None => false
      case Some(group) => {
        group.getDictionary match {
          case None => false
          case Some(dictionary) => {
            dictionary.getValues.find(dictionaryAttribute => dictionaryAttribute.getAttribute.getName == key &&
              dictionaryAttribute.getTargetNode == nodeNameForFragment) match {
              case None => false
              case Some(dictionaryAttribute) => {
                dictionaryAttribute.getValue.equalsIgnoreCase("true")
              }
            }
          }
        }
      }
    }
  }

  def getPropertyFromFragmentGroup (model: ContainerRoot, groupName: String, key: String,
    nodeNameForFragment : String): String = {
    model.getGroups.find(g => g.getName == groupName) match {
      case None => ""
      case Some(group) => {
        group.getDictionary match {
          case None => ""
          case Some(dictionary) => {
            dictionary.getValues.find(dictionaryAttribute => dictionaryAttribute.getAttribute.getName == key &&
              dictionaryAttribute.getTargetNode == nodeNameForFragment) match {
              case None => ""
              case Some(dictionaryAttribute) => {
                dictionaryAttribute.getValue
              }
            }
          }
        }
      }
    }
  }
}