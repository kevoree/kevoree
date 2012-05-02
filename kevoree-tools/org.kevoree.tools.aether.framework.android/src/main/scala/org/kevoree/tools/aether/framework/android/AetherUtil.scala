package org.kevoree.tools.aether.framework.android

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

import org.apache.maven.repository.internal.DefaultServiceLocator
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory
import org.sonatype.aether.resolution.ArtifactRequest
import org.sonatype.aether.util.artifact.DefaultArtifact
import org.apache.maven.repository.internal.MavenRepositorySystemSession
import java.io.File
import org.sonatype.aether.artifact.Artifact
import org.kevoree.framework.KevoreePlatformHelper
import org.sonatype.aether.spi.localrepo.LocalRepositoryManagerFactory
import org.sonatype.aether.connector.wagon.WagonProvider
import org.kevoree.{ContainerRoot, DeployUnit}
import org.slf4j.LoggerFactory
import org.sonatype.aether.impl.internal.{EnhancedLocalRepositoryManagerFactory}
import util.matching.Regex
import org.sonatype.aether.repository.{Authentication, RepositoryPolicy, RemoteRepository, LocalRepository}
import scala.collection.JavaConversions._
import org.sonatype.aether.{ConfigurationProperties, RepositorySystem}
import org.kevoree.tools.aether.framework.AetherFramework

//import org.sonatype.aether.connector.wagon.WagonProvider

/**
 * User: ffouquet
 * Date: 25/07/11
 * Time: 15:06
 */

object AetherUtil extends AetherFramework {

  val logger = LoggerFactory.getLogger(this.getClass)

  override def getRepositorySystem: RepositorySystem = {
    val locator = new DefaultServiceLocator()
    locator.addService(classOf[LocalRepositoryManagerFactory], classOf[EnhancedLocalRepositoryManagerFactory])
    locator.addService(classOf[RepositoryConnectorFactory], classOf[FileRepositoryConnectorFactory])
    locator.setServices(classOf[WagonProvider], new ManualWagonProvider())
    //locator.setServices(classOf[UpdateCheckManager], new DefaultUpdateCheckManager())
    locator.addService(classOf[RepositoryConnectorFactory], classOf[WagonRepositoryConnectorFactoryFork])
    locator.getService(classOf[RepositorySystem])
  }

  override def getRepositorySystemSession = {
    val session = new MavenRepositorySystemSession()
    session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS)
    session.setConfigProperty("aether.connector.ahc.provider", "jdk")
    session.setLocalRepositoryManager(getRepositorySystem.newLocalRepositoryManager(new LocalRepository(System.getProperty("user.home").toString + "/.m2/repository")))
    session.getConfigProperties.put(ConfigurationProperties.REQUEST_TIMEOUT, 2000.asInstanceOf[java.lang.Integer])
    session.getConfigProperties.put(ConfigurationProperties.CONNECT_TIMEOUT, 2000.asInstanceOf[java.lang.Integer])
    session
  }

  override def installInCache(jarArtifact: Artifact): File = {
    jarArtifact.getFile
  }


}