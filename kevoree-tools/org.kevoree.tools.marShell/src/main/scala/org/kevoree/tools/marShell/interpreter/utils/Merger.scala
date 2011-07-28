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

package org.kevoree.tools.marShell.interpreter.utils

import org.kevoree.Instance
import org.kevoree.KevoreeFactory
import scala.collection.JavaConversions._

object Merger {

  /* Goal of this method is to merge dictionary definition with already exist instance defintion */
  def mergeDictionary(inst: Instance, props: java.util.Properties) = {
    props.keySet.foreach {
      key =>
        val newValue = props.get(key)

        var dictionary = inst.getDictionary
        if (dictionary == null) {
          dictionary = KevoreeFactory.eINSTANCE.createDictionary
          inst.setDictionary(dictionary)
        }

        inst.getDictionary.getValues.find(value => value.getAttribute.getName == key) match {
          //UPDATE VALUE CASE
          case Some(previousValue) => {
            previousValue.setValue(newValue.toString)
          }
          //MERGE NEW Dictionary Attribute
          case None => {
            //CHECK IF ATTRIBUTE ALREADY EXISTE WITHOUT VALUE
            val att = inst.getTypeDefinition.getDictionaryType.getAttributes.find(att => att.getName == key) match {
              case None => {
                val newDictionaryValue = KevoreeFactory.eINSTANCE.createDictionaryAttribute
                newDictionaryValue.setName(key.toString)
                inst.getTypeDefinition.getDictionaryType.getAttributes.add(newDictionaryValue)
                newDictionaryValue
              }
              case Some(previousAtt) => previousAtt
            }
            val newDictionaryValue = KevoreeFactory.eINSTANCE.createDictionaryValue
            newDictionaryValue.setValue(newValue.toString)
            newDictionaryValue.setAttribute(att)
            inst.getDictionary.getValues.add(newDictionaryValue)
          }
        }

    }

  }

}
