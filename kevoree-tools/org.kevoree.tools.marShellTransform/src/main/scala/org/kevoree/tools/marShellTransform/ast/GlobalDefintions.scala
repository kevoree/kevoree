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
package org.kevoree.tools.marShellTransform.ast

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 29/03/12
 * Time: 16:53
 */

case class GlobalDefintions(properties : java.util.List[String],typedefinition : java.util.List[String],portdefinition : java.util.List[String]) extends  Adaptation{

  def getPropertieById(id : Int) : String = {
    try {
      properties.get(id)
    }catch {
      case _ @ e => " "
    }

  }

  def getTypedefinitionById(id : Int): String = {
    try {
      typedefinition.get(id)
    }catch {
      case _ @ e => " "
    }
  }

  def getPortdefinitionById(id : Int) : String = {
    try {
      portdefinition.get(id)
    }catch {
      case _ @ e => " "
    }
  }

}