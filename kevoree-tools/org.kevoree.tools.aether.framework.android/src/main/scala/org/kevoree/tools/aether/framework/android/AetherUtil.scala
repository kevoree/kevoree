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

package org.kevoree.tools.aether.framework.android

import org.sonatype.aether.spi.connector.RepositoryConnectorFactory
import org.apache.maven.repository.internal.MavenRepositorySystemSession
import java.io.File
import org.sonatype.aether.artifact.Artifact
import org.sonatype.aether.connector.wagon.WagonProvider
import org.sonatype.aether.repository.{RepositoryPolicy, LocalRepository}
import org.sonatype.aether.{ConfigurationProperties, RepositorySystem}
import org.kevoree.{ContainerNode, ContainerRoot, DeployUnit}
import org.kevoree.tools.aether.framework.common._


object AetherUtil extends NodeUrlBuilder
                  with AndroidRepositorySystem
                  with AndroidRepositorySession
                  with AndroidDeployUnitResolver
                  with MavenResolver




trait AndroidDeployUnitResolver extends AbstractDeployUnitResolver { this: NodeUrlBuilder =>
  def buildUrls(unit: DeployUnit): List[String] = buildPotentialMavenURL(unit.eContainer.asInstanceOf[ContainerRoot])
  def processResult(artifact: Artifact): File = artifact.getFile
}


trait NodeUrlBuilder extends MavenUrlBuilder with IPRetriever {

  override def buildPotentialMavenURL(root: ContainerRoot): List[String] = {
    mavenUrl(root) ++ (root.getNodes flatMap {
      node => urlFor(node, ipFor(root, node.getName))
    }).distinct
  }

  private def urlFor(node: ContainerNode, ip: String): Option[String] = {
    node.getDictionary match {
      case Some(dic) =>
        dic.getValues.find(_.getAttribute.getName == "port") match {
          case Some(att) =>
            Some("http://" + ip + ":" + att.getValue + "/provisioning/")

          case None => None
        }

      case None => None
    }
  }
}


trait AndroidRepositorySession extends AbstractRepositorySession { this: AbstractRepositorySystem =>
  def repositorySystemSession: MavenRepositorySystemSession = {
    val session = new MavenRepositorySystemSession()
    session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS)
    session.setConfigProperty("aether.connector.ahc.provider", "jdk")
    session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(new LocalRepository(System.getProperty("user.home").toString + "/.m2/repository")))
    session.getConfigProperties.put(ConfigurationProperties.REQUEST_TIMEOUT, 3000.asInstanceOf[java.lang.Integer])
    session.getConfigProperties.put(ConfigurationProperties.CONNECT_TIMEOUT, 5000.asInstanceOf[java.lang.Integer])
    session
  }
}


trait AndroidRepositorySystem extends AbstractRepositorySystem {

  def repositorySystem: RepositorySystem = {
    val locator = buildLocator
    locator.setServices(classOf[WagonProvider], new ManualWagonProvider)
    locator.addService(classOf[RepositoryConnectorFactory], classOf[WagonRepositoryConnectorFactoryFork])
    locator.getService(classOf[RepositorySystem])
  }
}


