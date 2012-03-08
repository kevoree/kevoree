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

import java.io.File
import org.slf4j.LoggerFactory
import org.apache.maven.repository.internal.MavenRepositorySystemSession
import org.sonatype.aether.repository.{LocalRepository, RepositoryPolicy}
import org.apache.maven.repository.internal.DefaultServiceLocator
import org.sonatype.aether.spi.log.Logger
import org.sonatype.aether.spi.localrepo.LocalRepositoryManagerFactory
import org.sonatype.aether.connector.async.AsyncRepositoryConnectorFactory
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory
import org.sonatype.aether.installation.InstallRequest
import org.sonatype.aether.artifact.Artifact
import org.sonatype.aether.impl.internal.{DefaultInstaller, EnhancedLocalRepositoryManagerFactory}
import org.sonatype.aether.impl.RepositoryEventDispatcher
import org.sonatype.aether.{RepositoryEvent, ConfigurationProperties, RepositorySystem}
import org.sonatype.aether.RepositoryEvent.EventType

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 05/03/12
 * Time: 18:42
 */

trait MavenCacheManager {

  private val logger = LoggerFactory.getLogger(this.getClass)

  var cache_repository: File = _

  def getCacheRepository: File = {
    if (cache_repository == null) {
      cache_repository = File.createTempFile("kevoree_cache", "kevoree_cache")
      cache_repository.delete()
      cache_repository.mkdirs()
      logger.info("Create Cache Dir " + cache_repository.getAbsolutePath)
    }
    cache_repository
  }

  def newCacheRepositorySystemSession = {
    val session = new MavenRepositorySystemSession()
    session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_NEVER)
    session.setConfigProperty("aether.connector.ahc.provider", "jdk")
    //DEFAULT VALUE
    session.setLocalRepositoryManager(newCacheRepositorySystem.newLocalRepositoryManager(new LocalRepository(getCacheRepository)))
    session.getConfigProperties.put(ConfigurationProperties.REQUEST_TIMEOUT, 3000.asInstanceOf[java.lang.Integer])
    session.getConfigProperties.put(ConfigurationProperties.CONNECT_TIMEOUT, 5000.asInstanceOf[java.lang.Integer])
    session
  }

  private val locator = new DefaultServiceLocator()

  def newCacheRepositorySystem: RepositorySystem = {
    locator.setServices(classOf[Logger], new AetherLogger) // Doesn't work to JdkAsyncHttpProvider because this class uses its own logger and not the one provided by plexus and set with this line
    locator.setService(classOf[LocalRepositoryManagerFactory], classOf[EnhancedLocalRepositoryManagerFactory])
    locator.setService(classOf[RepositoryConnectorFactory], classOf[FileRepositoryConnectorFactory])
    locator.setService(classOf[RepositoryConnectorFactory], classOf[AsyncRepositoryConnectorFactory])
    locator.getService(classOf[RepositorySystem])
  }

  def installInCache(jarArtifact: Artifact): File = {
    val installRequest = new InstallRequest
    installRequest.addArtifact(jarArtifact)
    var installedFile: File = null
    val initInstaller = new DefaultInstaller()
    initInstaller.initService(locator)
    initInstaller.setRepositoryEventDispatcher(new RepositoryEventDispatcher() {
      def dispatch(p1: RepositoryEvent) {
        if (p1.getType == EventType.ARTIFACT_INSTALLED) {
          installedFile = new File(p1.getFile.getAbsolutePath)
          //println("InstallCachedFile="+jarArtifact.getArtifactId+"-"+installedFile.hashCode())
        }
      }
    })
    initInstaller.install(newCacheRepositorySystemSession,installRequest)
   // println("File="+jarArtifact.getArtifactId+"->"+installedFile.hashCode())
    installedFile
  }


}
