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

package org.kevoree.merger.sub

import org.kevoree.merger.aspects.KevoreeAspects._
import org.slf4j.LoggerFactory
import org.kevoree.{KevoreeFactory, Dictionary, DictionaryType}
import scala.collection.JavaConversions._


trait DictionaryMerger {
  private val logger = LoggerFactory.getLogger(this.getClass);
  private val kevoreeFactory = new org.kevoree.impl.DefaultKevoreeFactory

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
    val targetDictionary = target.getDictionary()
    if (targetDictionary != null) {
      if (current.getDictionary() == null) {
        current.setDictionary(kevoreeFactory.createDictionary)
      }
      targetDictionary.getValues.foreach {
        targetValue =>
          current.getDictionary().getValues.find(value => {
            (value.getAttribute.getName == targetValue.getAttribute.getName) && (if (targetValue.getTargetNode() != null) {
              value.getTargetNode() != null && value.getTargetNode().getName() == targetValue.getTargetNode().getName()
            } else {
              value.getTargetNode == null
            })
          }) match {
            case Some(previousValue) => {
              previousValue.setValue(targetValue.getValue)
            }
            case None => {
              val newDictionaryValue = kevoreeFactory.createDictionaryValue
              newDictionaryValue.setValue(targetValue.getValue)
              newDictionaryValue.setAttribute(targetValue.getAttribute)
              newDictionaryValue.setTargetNode(targetValue.getTargetNode)
              current.getDictionary().addValues(newDictionaryValue)
            }
          }

      }

    }
  }

}
