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

import org.kevoree._
import org.kevoree.merger.Merger
import scala.collection.JavaConversions._

trait TypeLibraryMerger extends Merger {

  def mergeLibrary(actualModel : ContainerRoot,modelToMerge : ContainerRoot) : Unit = {
    val ctLib : List[TypeLibrary] = List()++modelToMerge.getLibraries.toList
    ctLib.foreach{libtomerge=>
      actualModel.getLibraries.find({elib=> elib.getName.equals(libtomerge.getName) }) match {
        case Some(elib) => {
            libtomerge.getSubTypes.filter{st=> st.isInstanceOf[TypeDefinition]}.foreach{libCTtomerge=>
              elib.getSubTypes.filter{st=> st.isInstanceOf[TypeDefinition]}.find({esublib=>esublib.getName.equals(libCTtomerge.getName)}) match {
                case Some(subct)=> //CHECK CONSISTENCY DONE BY PREVIOUS STEP
                case None => elib.getSubTypes.add(libCTtomerge)
              }
            }
          }
        case None => actualModel.getLibraries.add(libtomerge)
      }
    }
  }

}
