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

import org.kevoree.framework.PrimitiveCommand
import org.kevoree.DeployUnit
import org.slf4j.LoggerFactory
import org.kevoree.tools.aether.framework.AetherUtil
import java.io.File

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 26/01/12
 * Time: 16:35
 */

case class AddDeployUnit(du : DeployUnit) extends PrimitiveCommand {

  val logger = LoggerFactory.getLogger(this.getClass)

  def undo() {
    JCLContextHandler.removeDeployUnit(du)
  }

  def execute(): Boolean = {
    try {
      val arteFile: File = AetherUtil.resolveDeployUnit(du)
      JCLContextHandler.installDeployUnit(du,arteFile)
      true
    } catch {
      case _@ e =>logger.debug("error ",e);false
    }
  }
}