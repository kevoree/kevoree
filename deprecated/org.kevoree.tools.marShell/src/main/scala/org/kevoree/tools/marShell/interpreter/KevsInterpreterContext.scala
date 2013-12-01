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

package org.kevoree.tools.marShell.interpreter

import org.kevoree.{KevoreeFactory, ContainerRoot}
import org.kevoree.api.Bootstraper
import org.kevoree.impl.DefaultKevoreeFactory
import java.util
import org.kevoree.log.Log

case class KevsInterpreterContext(model : ContainerRoot) {

  protected var varMap = new util.HashMap[String, String]()

  def setVarMap( v : util.HashMap[String, String]) : KevsInterpreterContext = { varMap = v ; this }

  def getVarMap = varMap

  protected var bootstraper : Bootstraper = null

  def getBootstraper = bootstraper

  def setBootstraper(b : Bootstraper) {
    bootstraper = b
  }

  val kevoreeFactory : KevoreeFactory = new DefaultKevoreeFactory

  var interpretationErrors : util.ArrayList[String] = new util.ArrayList[String]()

  def appendInterpretationError(error : String) {
    interpretationErrors.add(error)
    Log.error(error)
  }

}
