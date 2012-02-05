package org.kevoree.api

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

import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import service.core.classloading.KevoreeClassLoaderHandler
import java.io.File
import org.kevoree.{DeployUnit, ContainerRoot}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 31/12/11
 * Time: 13:10
 */

trait Bootstraper {

  def getKevoreeClassLoaderHandler: KevoreeClassLoaderHandler

  def bootstrapNodeType(currentModel: ContainerRoot, nodeName: String, mservice: KevoreeModelHandlerService, kevsEngineFactory: KevScriptEngineFactory): Option[org.kevoree.api.NodeType]

  def resolveArtifact(artId: String, groupId: String, version: String, repos: List[String]): File

  def resolveKevoreeArtifact(artId: String, groupId: String, version: String): File

  def resolveDeployUnit(du: DeployUnit): File

  def close : Unit
  
  def clear : Unit
}