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
package org.kevoree.merger.resolver

import org.kevoree.framework.kaspects.ContainerRootAspect
import org.slf4j.LoggerFactory
import org.kevoree.{DictionaryType, ContainerRoot}

import scala.collection.JavaConversions._


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 19/10/11
 * Time: 11:03
 * To change this template use File | Settings | File Templates.
 */

trait DictionaryAttributeResolver {

  private val logger = LoggerFactory.getLogger(this.getClass)
  private val containerRootAspect = new ContainerRootAspect()

  def resolveDictionaryAttribute(model : ContainerRoot){
    containerRootAspect.getAllInstances(model).foreach{ instance =>

         val dictionaryInstance = instance.getDictionary()
         if (dictionaryInstance != null){
          dictionaryInstance.getValues.foreach{ value =>

            val targetNode = value.getTargetNode()
            if (targetNode != null){
              targetNode match {
                case targetNodeName : UnresolvedNode => {
                  model.getNodes.find(n => n.getName == targetNodeName.getName()) match {
                    case Some(node)=> value.setTargetNode(node)
                    case None => logger.error("Unconsitent model , node not found for name "+targetNodeName.getName())
                  }
                }
                case _ => logger.error("Already Dictionary Value targetNodeName for value "+value)
              } 
            }
            
            
            value.getAttribute match {
              case attName:UnresolvedDictionaryAttribute=> {
                instance.getTypeDefinition.getDictionaryType() match {
                  case dictionaryType: DictionaryType=> {
                    dictionaryType.getAttributes.find(att => att.getName() == attName.getName()) match {
                      case Some(attFound)=> value.setAttribute(attFound)
                      case None => {
                        dictionaryInstance.removeValues(value)
                        logger.error("Unconsitent dictionary type , att not found for name "+attName.getName())
                      }
                    }
                  }
                  case null => logger.error("Unconsistent dictionary")
                }
              }
              case _ @ e => {
                logger.error("Already resolved Dictionary Attribute "+e)
              }
            }
          }
        }
    }
    
    
  }
  
}