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
package org.kevoree.tools.aether.framework

import org.kevoree.framework.MavenResolver
import java.lang.String
import java.io.File
import java.util.List

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 24/01/12
 * Time: 19:17
 */

class AetherMavenResolver extends MavenResolver {
  def resolveArtifact(artId: String, groupId: String, version: String, repos: List[String]): File = AetherUtil.resolveMavenArtifact4J(artId,groupId,version,repos)

  def resolveKevoreeArtifact(artId: String, groupId: String, version: String): File = AetherUtil.resolveKevoreeArtifact(artId,groupId,version)
}