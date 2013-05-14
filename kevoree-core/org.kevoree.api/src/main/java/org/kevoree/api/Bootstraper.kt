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
package org.kevoree.api

import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import java.io.File
import org.kevoree.api.service.core.classloading.KevoreeClassLoaderHandler
import org.kevoree.api.service.core.logging.KevoreeLogService
import org.kevoree.DeployUnit
import org.kevoree.ContainerRoot

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 31/12/11
 * Time: 13:10
 */

trait Bootstraper {

  fun getKevoreeClassLoaderHandler() : KevoreeClassLoaderHandler
  fun bootstrapNodeType(currentModel: ContainerRoot, nodeName: String, mservice: KevoreeModelHandlerService, kevsEngineFactory: KevScriptEngineFactory): org.kevoree.api.NodeType?
  fun resolveArtifact(artId: String, groupId: String, version: String, repos: List<String>): File?
  fun resolveArtifact(artId: String, groupId: String, version: String, extension : String, repos: List<String>): File?
  fun resolveKevoreeArtifact(artId: String, groupId: String, version: String): File?
  fun resolveDeployUnit(du: DeployUnit): File?
  fun close() : Unit
  fun clear() : Unit
  fun getKevoreeLogService() : KevoreeLogService
  fun setKevoreeLogService(kl : KevoreeLogService)

}