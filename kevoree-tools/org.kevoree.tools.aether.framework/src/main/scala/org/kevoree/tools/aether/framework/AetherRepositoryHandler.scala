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
package org.kevoree.tools.aether.framework

import org.sonatype.aether.{RepositorySystemSession, RepositorySystem}
import org.slf4j.LoggerFactory

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/04/12
 * Time: 22:06
 */

trait AetherRepositoryHandler {

  private val logger = LoggerFactory.getLogger(this.getClass)

  var repositorySystem: RepositorySystem = null

  def setRepositorySystem(rs: RepositorySystem) {
    repositorySystem = rs
  }

  def getRepositorySystem: RepositorySystem = {
    if (repositorySystem == null) {
      AetherRepositoryStandalone.newRepositorySystem
    } else {
      repositorySystem
    }
  }


  var repositorySession: RepositorySystemSession = null

  def setRepositorySystemSession(rs: RepositorySystemSession) {
    repositorySession = rs
  }

  def getRepositorySystemSession: RepositorySystemSession = {
    if (repositorySession == null) {
      AetherRepositoryStandalone.newRepositorySystemSession
    } else {
      repositorySession
    }
  }

  def getDefaultURLS =  AetherRepositoryStandalone.getConfigURLS


}
