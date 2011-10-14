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

package org.kevoree.framework.aspects

import org.kevoree._
import KevoreeAspects._

case class DictionaryTypeAspect (selfDT: DictionaryType) {

  def isModelEquals (otherDT: DictionaryType): Boolean = {

    //println(selfDT)
    //println(otherDT)


    if (selfDT != null) {
      if (otherDT != null) {
        if (otherDT.getAttributes == null) {
          return selfDT.getAttributes == null
        }

        if (selfDT.getAttributes.size == otherDT.getAttributes.size && selfDT.getDefaultValues.size == otherDT.getDefaultValues.size) {
          val selfRes = selfDT.getAttributes.forall(selfAtt => otherDT.getAttributes.exists(otherDTAtt => {
            //println(otherDTAtt.getName+"=="+selfAtt.getName)
            otherDTAtt.getName == selfAtt.getName && otherDTAtt.getDatatype == selfAtt.getDatatype
          }))
          val selfRes1 = selfDT.getDefaultValues.forall(selfAtt => otherDT.getDefaultValues.exists(otherDTAtt => {
            //println(otherDTAtt.getName+"=="+selfAtt.getName)
            otherDTAtt.getAttribute.getName == selfAtt.getAttribute.getName && otherDTAtt.getValue == selfAtt.getValue
          }))

          selfRes && selfRes1
        } else {
          false
        }
      } else {
        true
      }
    } else {
      otherDT != null
    }
  }

}
