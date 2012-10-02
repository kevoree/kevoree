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
package org.kevoree.framework.aspects

import org.kevoree.{NodeType}
import org.slf4j.LoggerFactory

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 01/10/12
 * Time: 12:52
 */
case class NodeTypeAspect(selfTD: NodeType) {

  val logger = LoggerFactory.getLogger(this.getClass)

  def defineAdaptationPrimitiveType(aTypeName : String): Boolean = {
    selfTD.getManagedPrimitiveTypes.foreach {
      ptype =>
        if (ptype.getName.toLowerCase.equals(aTypeName)) {
          return true
        }
    }
    if(selfTD.getSuperTypes.size == 0){
      false
    } else {
      selfTD.getSuperTypes.exists(stp=> stp.isInstanceOf[NodeType] && NodeTypeAspect(stp.asInstanceOf[NodeType]).defineAdaptationPrimitiveType(aTypeName) )
    }
  }

}
