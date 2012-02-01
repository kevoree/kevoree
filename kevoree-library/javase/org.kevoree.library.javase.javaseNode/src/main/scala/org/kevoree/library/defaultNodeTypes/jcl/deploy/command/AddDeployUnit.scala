package org.kevoree.library.defaultNodeTypes.jcl.deploy.command

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

import org.kevoree.framework.PrimitiveCommand
import org.kevoree.DeployUnit
import org.slf4j.LoggerFactory
import java.io.File
import org.kevoree.tools.aether.framework.{JCLContextHandler, AetherUtil}
import org.kevoree.library.defaultNodeTypes.jcl.deploy.context.{KevoreeMapping, KevoreeDeployManager}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 26/01/12
 * Time: 16:35
 */

case class AddDeployUnit(du: DeployUnit) extends PrimitiveCommand {

  val logger = LoggerFactory.getLogger(this.getClass)

  def undo() {
    JCLContextHandler.removeDeployUnit(du)
    KevoreeDeployManager.bundleMapping.foreach(bm => {
      if (bm.ref == du) {
        KevoreeDeployManager.removeMapping(bm)
      }
    })
  }

  def execute(): Boolean = {
    try {
      if (JCLContextHandler.getKCL(du) == null) {
        val arteFile: File = AetherUtil.resolveDeployUnit(du)
        JCLContextHandler.installDeployUnit(du, arteFile)
      }
      KevoreeDeployManager.bundleMapping.find(bm => bm.ref == du) match {
        case Some(bm)=>
        case None => KevoreeDeployManager.addMapping(KevoreeMapping(du.getName, du.getClass.getName, du))
      }
      true
    } catch {
      case _@e => logger.debug("error ", e); false
    }
  }
}