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
import scala.collection.JavaConversions._
import KevoreeAspects._

case class TypeDefinitionAspect(selfTD : TypeDefinition) {

  def isModelEquals(pct : TypeDefinition) : Boolean = {
    pct.getName == selfTD.getName
    /* deep compare */
  }

  def isUpdated(pTD : TypeDefinition) : Boolean = {
    if(selfTD.getDeployUnit != null){
      if(pTD.getDeployUnit != null){
        selfTD.getDeployUnit.getHashcode != pTD.getDeployUnit.getHashcode
      } else {
        true
      }
    } else {
      pTD.getDeployUnit != null
    }
  }

}
