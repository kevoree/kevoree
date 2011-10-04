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

case class TypedElementAspect(e : TypedElement) {

  def isModelEquals(remote : TypedElement) : Boolean = {
    val nameEquality = e.getName == remote.getName
    val genericEquality = e.getGenericTypes.forall(p=> remote.getGenericTypes.exists(remoteP => remoteP.isModelEquals(p)  )  )
    val sizeEquality = e.getGenericTypes.size == remote.getGenericTypes.size
    nameEquality && genericEquality && sizeEquality
  }

  def print(openSep : Char,closeSep:Char) : String = {
    val res : StringBuilder = new StringBuilder
    res.append(e.getName)
    if(e.getGenericTypes.size>0){ res.append(openSep) }
    e.getGenericTypes.foreach{gt=>
      res.append(gt.print(openSep,closeSep))

      if(gt != e.getGenericTypes.last ) res append ","
    }

    if(e.getGenericTypes.size>0){  res.append(closeSep) }
    res.toString
  }

}
