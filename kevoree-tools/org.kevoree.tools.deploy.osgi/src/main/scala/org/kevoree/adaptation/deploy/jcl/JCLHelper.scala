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
package org.kevoree.adaptation.deploy.jcl

import org.kevoree.{ContainerRoot, TypeDefinition, DeployUnit}
import org.kevoree.framework.aspects.KevoreeAspects._


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 26/01/12
 * Time: 17:00
 */

object JCLHelper {

  def isJCLManaged(du : DeployUnit) : Boolean = {
    du.getType == "jar" || du.getType == "kjar"
  }

  def isJCLManaged(td : TypeDefinition,nodeName:String) : Boolean = {
    val node = td.eContainer.asInstanceOf[ContainerRoot].getNodes.find(n => n.getName == nodeName).get
    val deployUnit = td.foundRelevantDeployUnit(node)
    deployUnit.getType == "jar" || deployUnit.getType == "kjar"
  }



}