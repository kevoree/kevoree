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
package org.kevoree.merger.resolver

import org.kevoree.framework.aspects.KevoreeAspects._
import org.slf4j.LoggerFactory
import org.kevoree.ContainerRoot


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 19/10/11
 * Time: 11:03
 * To change this template use File | Settings | File Templates.
 */

trait DictionaryAttributeResolver {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def resolveDictionaryAttribute(model : ContainerRoot){
    model.getAllInstances.foreach{ instance =>
        instance.getDictionary.map{ dictionaryInstance =>
          dictionaryInstance.getValues.foreach{ value =>
            value.getAttribute match {
              case UnresolvedDictionaryAttribute(attName)=> {
                instance.getTypeDefinition.getDictionaryType match {
                  case Some(dictionaryType)=> {
                    dictionaryType.getAttributes.find(att => att.getName == attName) match {
                      case Some(attFound)=> value.setAttribute(attFound)
                      case None => {
                        logger.error("Unconsitent dictionary type , att not found for name "+attName)
                      }
                    }
                  }
                  case None => logger.error("Unconsistent dictionary")
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