package org.kevoree.tools.aether.framework

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

import org.sonatype.aether.RepositorySystem
import org.apache.maven.repository.internal.DefaultServiceLocator
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory
import org.sonatype.aether.connector.async.AsyncRepositoryConnectorFactory
import org.sonatype.aether.resolution.ArtifactRequest
import org.sonatype.aether.util.artifact.DefaultArtifact
import org.apache.maven.repository.internal.MavenRepositorySystemSession
import org.kevoree.{ContainerRoot, DeployUnit}
import java.io.File
import org.sonatype.aether.repository.{RepositoryPolicy, RemoteRepository, LocalRepository}
import org.sonatype.aether.artifact.Artifact
import org.kevoree.framework.KevoreePlatformHelper
import scala.collection.JavaConversions._

/**
 * User: ffouquet
 * Date: 25/07/11
 * Time: 15:06
 */

object AetherUtil {

  val newRepositorySystem: RepositorySystem = {
    val locator = new DefaultServiceLocator()
    locator.addService(classOf[RepositoryConnectorFactory], classOf[FileRepositoryConnectorFactory])
    locator.addService(classOf[RepositoryConnectorFactory], classOf[AsyncRepositoryConnectorFactory])
    locator.getService(classOf[RepositorySystem])
  }

  def resolveDeployUnit(du: DeployUnit): File = {

    var artifact: Artifact = null
    if (du.getUrl != null && du.getUrl.contains("mvn:")) {
      artifact = new DefaultArtifact(du.getUrl.replaceAll("mvn:", "").replace("/", ":"))
    } else {
      artifact = new DefaultArtifact(List(du.getGroupName, du.getUnitName, du.getVersion).mkString(":"))
    }

    val artifactRequest = new ArtifactRequest
    artifactRequest.setArtifact(artifact)
    val urls = buildPotentialMavenURL(du.eContainer().asInstanceOf[ContainerRoot])

    val repositories: java.util.List[RemoteRepository] = new java.util.ArrayList();
    urls.foreach {
      url =>
        val repo = new RemoteRepository
        repo.setId(url)
        repo.setUrl(url)
        val repositoryPolicy = new RepositoryPolicy()
        repositoryPolicy.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_WARN)
        repositoryPolicy.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS)
        repo.setPolicy(true, repositoryPolicy)
        repositories.add(repo)
    }

    artifactRequest.setRepositories(repositories)
    val artefactResult = newRepositorySystem.resolveArtifact(newRepositorySystemSession, artifactRequest)
    artefactResult.getArtifact.getFile
  }

  val newRepositorySystemSession = {
    val session = new MavenRepositorySystemSession()
    /*
   val factory = new DefaultSettingsBuilderFactory
   val settingBuilder = factory.newInstance()
   val settingBuilderRequest = new DefaultSettingsBuildingRequest
   val settingResult = settingBuilder.build(settingBuilderRequest)

   println(settingResult.getEffectiveSettings.getProfiles.size())
    */
    session.setLocalRepositoryManager(newRepositorySystem.newLocalRepositoryManager(new LocalRepository(System.getProperty("user.home").toString + "/.m2/repository")))
    session
  }


  def buildPotentialMavenURL(root: ContainerRoot): List[String] = {
    var result: List[String] = List()
    //BUILD FROM ALL REPO
    root.getRepositories.foreach {
      repo => result = result ++ List(repo.getUrl)
    }
    //BUILD FROM ALL NODE
    root.getNodes.foreach {
      node =>
        result = result ++ List(buildURL(root, node.getName))
    }
    result
  }

  def buildURL(root: ContainerRoot, nodeName: String): String = {
    var ip = KevoreePlatformHelper.getProperty(root, nodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP);
    if (ip == null || ip == "") {
      ip = "127.0.0.1";
    }
    var port = KevoreePlatformHelper.getProperty(root, nodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT);
    if (port == null || port == "") {
      port = "8000";
    }
    "http://" + ip + ":" + port + "/provisioning/";
  }


}