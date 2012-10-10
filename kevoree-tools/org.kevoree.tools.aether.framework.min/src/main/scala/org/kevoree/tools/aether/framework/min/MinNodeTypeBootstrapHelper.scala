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
package org.kevoree.tools.aether.framework.min

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


import org.kevoree.tools.aether.framework.NodeTypeBootstrapHelper
import java.io.File
import org.kevoree.DeployUnit
import org.kevoree.kcl.KevoreeJarClassLoader

/**
 * User: ffouquet
 * Date: 10/08/11
 * Time: 12:01
 */

class MinNodeTypeBootstrapHelper extends NodeTypeBootstrapHelper {

  classLoaderHandler = new MinJCLContextHandler

  override def resolveArtifact(artId: String, groupId: String, version: String, repos: java.util.List[String]): File = AetherUtil.resolveMavenArtifact(artId, groupId, version, repos)


  override def resolveArtifact(artId: String, groupId: String, version: String, extension : String, repos: java.util.List[String]): File = AetherUtil.resolveMavenArtifact(artId, groupId, version, extension, repos)

  override def resolveKevoreeArtifact(artId: String, groupId: String, version: String): File = AetherUtil.resolveKevoreeArtifact(artId, groupId, version)

  override def resolveDeployUnit(du: DeployUnit): File = AetherUtil.resolveDeployUnit(du)

  override def installDeployUnit(du: DeployUnit): Option[KevoreeJarClassLoader] = {
    try {
      val previousKCL = getKevoreeClassLoaderHandler.getKevoreeClassLoader(du)
      if (previousKCL != null) {
        Some(previousKCL)
      } else {
        val arteFile = AetherUtil.resolveDeployUnit(du)
        if (arteFile != null) {
          logger.debug("trying to install {}", arteFile.getAbsolutePath)
          val kcl = getKevoreeClassLoaderHandler.installDeployUnit(du, arteFile)

          //bundle = bundleContext.installBundle("file:///" + arteFile.getAbsolutePath, new FileInputStream(arteFile))
          //        bundle.start()
          // KevoreeDeployManager.addMapping(KevoreeJCLBundle(buildKEY(du), du.getClass.getName,kcl, -1 ))
          Some(kcl)
        } else {
          logger.error("Can't resolve deploy unit " + du.getUnitName)
          None
        }
      }
    } catch {
      case _@e => {
        logger.error("Can't install node type", e)
        None
      }
    }
  }

}
