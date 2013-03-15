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
import java.util.ArrayList

/**
 * User: ffouquet
 * Date: 10/08/11
 * Time: 12:01
 */

class MinNodeTypeBootstrapHelper: NodeTypeBootstrapHelper() {

    MinNodeTypeBootstrapHelper(){
        classLoaderHandler = MinJCLContextHandler()
    }


    override fun resolveArtifact(artId: String, groupId: String, version: String, repos: List<String>): File {
        return AetherUtil.resolveMavenArtifact(artId, groupId, version, repos)!!
    }

    override fun resolveArtifact(artId: String, groupId: String, version: String, extension: String, repos: List<String>): File {
        return  AetherUtil.resolveMavenArtifact(artId, groupId, version, extension, repos)!!
    }

    override fun resolveKevoreeArtifact(artId: String, groupId: String, version: String): File {
        val l = ArrayList<String>()
        l.add("http://maven.kevoree.org/release")
        l.add("http://maven.kevoree.org/snapshots")
        return AetherUtil.resolveMavenArtifact(artId, groupId, version,l)!!
    }

    override fun resolveDeployUnit(du: DeployUnit): File {
        return AetherUtil.resolveDeployUnit(du)!!
    }

    override fun installDeployUnit(du: DeployUnit): KevoreeJarClassLoader? {
        try {
            val previousKCL = getKevoreeClassLoaderHandler().getKevoreeClassLoader(du)
            if (previousKCL != null) {
                return previousKCL
            } else {
                val arteFile = AetherUtil.resolveDeployUnit(du)
                if (arteFile != null) {
                    logger.debug("trying to install {}", arteFile.getAbsolutePath())
                    val kcl = getKevoreeClassLoaderHandler().installDeployUnit(du, arteFile)
                    return kcl
                } else {
                    logger.error("Can't resolve deploy unit " + du.getUnitName())
                    return null
                }
            }
        } catch(e: Exception) {
            logger.error("Can't install node type", e)
            return null
        }
    }

}
