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
package org.kevoree.tools.annotation.generator

import org.kevoree.TypedElement
import scala.collection.JavaConversions._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 28/03/13
 * Time: 15:42
 *
 * @author Erwan Daubert
 * @version 1.0
 */
 object Printer {

  def print(element : TypedElement, openSep : Char,closeSep:Char) : String = {
    val res : StringBuilder = new StringBuilder
    res.append(element.getName)
    if(element.getGenericTypes.size>0){ res.append(openSep) }
    element.getGenericTypes.foreach{gt=>
      res.append(print(gt, openSep,closeSep))

      if(gt != element.getGenericTypes.last ) res append ","
    }

    if(element.getGenericTypes.size>0){  res.append(closeSep) }
    res.toString()
  }
}
