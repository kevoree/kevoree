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

import org.kevoree.framework.aspects.KevoreeAspects._
import org.slf4j.LoggerFactory
import org.kevoree.{KevoreeFactory, Dictionary, DictionaryType}

trait DictionaryMerger {
  private val logger = LoggerFactory.getLogger(this.getClass);

  def mergeDictionary(dictionary: Dictionary, newtype: DictionaryType): Unit = {
    if (dictionary != null) {
      val values = dictionary.getValues.toList ++ List()
      values.foreach {
        v =>
          //println("Consistency merge dictionary value "+v.getAttribute.getName+"-"+v.getValue)
          val newAttribute = newtype.getAttributes.find(att => att.getName == v.getAttribute.getName)
          newAttribute match {
            case None => {
              logger.debug("Merger remove unavailable Dictionary Value => " + v.getValue + " for old key => " + v.getAttribute.getName)
              dictionary.removeValues(v)
            } //REMOVE DICTIONARY INSTANCE , NO AVAILABLE IN NEW TYPE
            case Some(found) => {

              v.setAttribute(found)
            } //TODO CHECK TYPE // ACTUALLY ONLY STRING
          }

      }
    }
  }

  def mergeDictionaryInstance(current: org.kevoree.Instance, target: org.kevoree.Instance): Unit = {
    target.getDictionary.map {
      targetDictionary =>
        if (!current.getDictionary.isDefined) {
          current.setDictionary(Some(KevoreeFactory.eINSTANCE.createDictionary))
        }
        targetDictionary.getValues.foreach {
          targetValue =>
            current.getDictionary.get.getValues.find(value => {
              (value.getAttribute.getName == targetValue.getAttribute.getName) && (if (targetValue.getTargetNode.isDefined) {
                value.getTargetNode.isDefined && value.getTargetNode.get.getName == targetValue.getTargetNode.get.getName
              } else {
                value.getTargetNode.isEmpty
              })
            }) match {
              case Some(previousValue) => {
                previousValue.setValue(targetValue.getValue)
              }
              case None => {
                val newDictionaryValue = KevoreeFactory.eINSTANCE.createDictionaryValue
                newDictionaryValue.setValue(targetValue.getValue)
                newDictionaryValue.setAttribute(targetValue.getAttribute)
                newDictionaryValue.setTargetNode(targetValue.getTargetNode)
                current.getDictionary.get.addValues(newDictionaryValue)
              }
            }

        }

    }
  }

}
