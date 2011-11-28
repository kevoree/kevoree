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
package org.kevoree.tools.ui.editor


import org.kevoree._
import org.kevoree.framework.aspects.KevoreeAspects._

/**
 * User: ffouquet
 * Date: 18/06/11
 * Time: 10:10
 */

object ModelHelper {

  def getTypeNbInstance(model: ContainerRoot, td: TypeDefinition) = {

    model.getAllInstances.filter(i => i.getTypeDefinition == td).size

  }
  
  def getNextAvailableNodeName(model : ContainerRoot) : String = {
    var i =0
    while(model.getNodes.exists(n => n.getName == ("node"+i))){i = i +1}
    "node"+i
  }



}