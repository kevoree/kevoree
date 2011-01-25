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

import org.kevoree.Dictionary
import org.kevoree.DictionaryType
import scala.collection.JavaConversions._
import org.kevoree.framework.aspects.KevoreeAspects._

trait DictionaryMerger {

  def mergeDictionary(dictionary : Dictionary,newtype : DictionaryType) : Unit = {
    if(dictionary != null){
      var values = dictionary.getValues ++ List()
      values.foreach{v=>

        var newAttribute = newtype.getAttributes.find(att=> att.getName == v.getAttribute.getName)
        newAttribute match {
          case None => {
              println("ART2 Merger remove unavailable Dictionary Value => "+v.getValue +" for old key => "+v.getAttribute.getName)
              dictionary.getValues.remove(v)
            } //REMOVE DICTIONARY INSTANCE , NO AVAILABLE IN NEW TYPE
          case Some(found)=> v.setAttribute(found) //TODO CHECK TYPE // ACTUALLY ONLY STRING
        }

      }
    }
  }

}
