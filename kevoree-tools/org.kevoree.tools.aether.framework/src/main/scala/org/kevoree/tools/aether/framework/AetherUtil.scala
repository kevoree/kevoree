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

import common.{MavenUrlBuilder, AbstractDeployUnitResolver, AbstractRepositorySession, AbstractRepositorySystem}
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory
import org.apache.maven.repository.internal.MavenRepositorySystemSession
import java.io.File
import org.sonatype.aether.artifact.Artifact
import org.sonatype.aether.connector.async.AsyncRepositoryConnectorFactory
import org.sonatype.aether.spi.log.Logger
import org.sonatype.aether.repository.{RepositoryPolicy, LocalRepository}
import org.slf4j.LoggerFactory
import org.sonatype.aether.{ConfigurationProperties, RepositorySystem}
import org.sonatype.aether.transfer.{TransferEvent, TransferListener}
import org.kevoree._


object AetherUtil extends TempFileCacheManager
                  with MavenUrlBuilder
                  with JavaRepositorySystem
                  with JavaRepositorySession
                  with JavaDeployUnitResolver





trait JavaDeployUnitResolver extends AbstractDeployUnitResolver { this: MavenUrlBuilder =>

  def installInCache(artifact: Artifact): File

  def buildUrls(unit: DeployUnit): List[String] = {
    if (System.getProperty("kevoree.offline") != null && System.getProperty("kevoree.offline").equals("true"))
      Nil
    else
      buildPotentialMavenURL(unit.eContainer.asInstanceOf[ContainerRoot])
  }

  def processResult(artifact: Artifact): File = installInCache(artifact)
}

trait JavaRepositorySession extends AbstractRepositorySession { this: AbstractRepositorySystem =>

  val logger = LoggerFactory.getLogger(this.getClass)

  def repositorySystemSession: MavenRepositorySystemSession = {
    val session = new MavenRepositorySystemSession()
    session.setTransferListener(new TransferListener() {
      def transferInitiated(p1: TransferEvent) {
        logger.debug("Transfert init for Artifact " + p1.getResource.getResourceName)
      }

      def transferStarted(p1: TransferEvent) {
        logger.debug("Transfert begin for Artifact " + p1.getResource.getResourceName)
      }

      def transferProgressed(p1: TransferEvent) {
        logger.debug("Transfert in progress for Artifact " + p1.getResource.getResourceName)
      }

      def transferCorrupted(p1: TransferEvent) {
        logger.error("TransfertCorrupted : " + p1.getResource.getResourceName)
      }

      def transferSucceeded(p1: TransferEvent) {
        logger.debug("Transfert succeeded for Artifact " + p1.getResource.getResourceName)
      }

      def transferFailed(p1: TransferEvent) {
        logger.debug("TransferFailed : " + p1.getResource.getResourceName)
      }
    })
    session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS)
    session.setConfigProperty("aether.connector.ahc.provider", "jdk")
    //DEFAULT VALUE
    session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(new LocalRepository(System.getProperty("user.home").toString + "/.m2/repository")))
    //TRY TO FOUND MAVEN CONFIGURATION
    val configFile = new File(System.getProperty("user.home").toString + File.separator + ".m2" + File.separator + "settings.xml")
    if (configFile.exists()) {
      val configRoot = scala.xml.XML.loadFile(configFile)
      configRoot.child.find(c => c.label == "localRepository").map {
        localRepo =>
          logger.info("Found localRepository value from settings.xml in user path => " + localRepo.text)
          session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(new LocalRepository(localRepo.text)))
      }
    } else {
      logger.debug("settings.xml not found")
    }
    session.getConfigProperties.put(ConfigurationProperties.REQUEST_TIMEOUT, 2000.asInstanceOf[java.lang.Integer])
    session.getConfigProperties.put(ConfigurationProperties.CONNECT_TIMEOUT, 1000.asInstanceOf[java.lang.Integer])
    session
  }
}

trait JavaRepositorySystem extends AbstractRepositorySystem {

  def repositorySystem: RepositorySystem = {
    val locator = buildLocator
    locator.setServices(classOf[Logger], new AetherLogger)
    locator.addService(classOf[RepositoryConnectorFactory], classOf[AsyncRepositoryConnectorFactory])
    locator.getService(classOf[RepositorySystem])
  }
}

