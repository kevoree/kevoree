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
package org.kevoree.tools.aether.framework.common

import org.sonatype.aether.RepositorySystem
import org.apache.maven.repository.internal.DefaultServiceLocator
import org.sonatype.aether.spi.localrepo.LocalRepositoryManagerFactory
import org.sonatype.aether.impl.internal.EnhancedLocalRepositoryManagerFactory
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory

/**
 * Abstract maven repository system.
 */
trait AbstractRepositorySystem {
  def repositorySystem: RepositorySystem

  protected def buildLocator: DefaultServiceLocator = {
    val locator = new DefaultServiceLocator
    locator.addService(classOf[LocalRepositoryManagerFactory], classOf[EnhancedLocalRepositoryManagerFactory])
    locator.addService(classOf[RepositoryConnectorFactory],    classOf[FileRepositoryConnectorFactory])
    locator
  }
}