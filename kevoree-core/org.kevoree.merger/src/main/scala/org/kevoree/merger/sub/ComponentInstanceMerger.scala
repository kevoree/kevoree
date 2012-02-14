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

package org.kevoree.merger.sub

import org.kevoree.ComponentInstance
import org.kevoree.ContainerRoot
import org.slf4j.LoggerFactory

trait ComponentInstanceMerger extends DictionaryMerger {

  private val logger = LoggerFactory.getLogger(this.getClass);

  /* Expect TYPE DEFINITION MERGE BEFORE */

  def mergeComponentInstance(actualModel: ContainerRoot, c: ComponentInstance, targetExistingInstance: ComponentInstance) {
    if (targetExistingInstance == null) {
    } else {

      mergeDictionaryInstance(targetExistingInstance,c)

      /*
      targetExistingInstance.getDictionary.map {
        previousDictionary =>
        //TRY TO MIX 2 DICTIONARY
          c.getDictionary.map {
            dic =>
              dic.getValues.foreach{ newDicValue =>
                previousDictionary.getValues.find(previousVal=> previousVal.getAttribute.getName == newDicValue.getAttribute.getName) match {
                  case Some(previousVal)=> previousVal.setValue(newDicValue.getValue)
                  case None => {
                    previousDictionary.addValues(newDicValue)
                  }
                }
              }
          }
      }*/
    }


    /*
  //FIND CT
  val ctOpt = actualModel.getTypeDefinitions.find(p=> p.isModelEquals(c.getTypeDefinition)  )
  ctOpt match {
    case Some(cti) => {
        val ct = cti.asInstanceOf[ComponentType]
        c.setTypeDefinition(ct)
        //MERGE PORT
        val providedPort = c.getProvided.toList ++ List()
        providedPort.foreach{pport=>
          ct.getProvided.find(p=> p.getName == pport.getPortTypeRef.getName) match {
            case None => pport.removeAndUnbind(); logger.debug("Warning => Port deleted")
            case Some(ptref)=> pport.setPortTypeRef(ptref)
          }
        }
        val requiredPort = c.getRequired.toList ++ List()
        requiredPort.foreach{rport=>
          ct.getRequired.find(p=> p.getName == rport.getPortTypeRef.getName) match {
            case None => rport.removeAndUnbind(); logger.debug("Warning => Port deleted")
            case Some(ptref)=> rport.setPortTypeRef(ptref)
          }
        }
      }
    case None => {
        logger.debug("Warning => TypeDefinition not found");
      }
  }  */
  }


}
