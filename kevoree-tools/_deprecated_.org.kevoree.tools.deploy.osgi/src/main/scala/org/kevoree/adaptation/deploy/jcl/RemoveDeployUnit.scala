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

import org.kevoree.DeployUnit
import org.slf4j.LoggerFactory
import org.kevoree.framework.{FileNIOHelper, PrimitiveCommand}
import java.io.{FileInputStream, File}
import java.util.Random

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 26/01/12
 * Time: 16:35
 */

case class RemoveDeployUnit(du : DeployUnit) extends PrimitiveCommand {

  val logger = LoggerFactory.getLogger(this.getClass)
  var lastTempFile : File = _
  var random = new Random

  def undo() {
    if(lastTempFile != null){
      JCLContextHandler.installDeployUnit(du,lastTempFile)
    }

  }

  def execute(): Boolean = {
    try {
      lastTempFile = File.createTempFile(random.nextInt() + "", ".jar")
      val jarStream = new FileInputStream(JCLContextHandler.getCacheFile(du));
      FileNIOHelper.copyFile(jarStream, lastTempFile)
      jarStream.close()
      JCLContextHandler.removeDeployUnit(du)
      true
    } catch {
      case _@ e =>logger.debug("error ",e);false
    }
  }
}