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
package org.kevoree.framework.aspects

import org.kevoree._
 import KevoreeAspects._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 04/11/11
 * Time: 21:21
 * To change this template use File | Settings | File Templates.
 */

case class PortTypeAspect(pt : PortType) {

  def isModelEquals(targetPT : PortType) : Boolean = {
    if(pt.getClass.getName == targetPT.getClass.getName){
      pt match {
        case mpt : MessagePortType => {
          mpt.getDictionaryType match {
            case Some(dt)=> {
              targetPT.getDictionaryType match {
                case Some(tdt)=> {
                 // println("check "+dt.isModelEquals(tdt))
                  dt.isModelEquals(tdt)
                }
                case None => false
              }
            }
            case None => targetPT.getDictionaryType.isEmpty
          }
        }
        case spt : ServicePortType => {
          targetPT.getName == spt.getName
        }
      }
    } else {
      false
    }
  }
  
}