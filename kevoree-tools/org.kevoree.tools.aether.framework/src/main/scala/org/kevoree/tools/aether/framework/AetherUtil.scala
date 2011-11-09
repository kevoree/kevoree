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
import org.sonatype.aether.resolution.ArtifactRequest
import org.sonatype.aether.util.artifact.DefaultArtifact
import org.apache.maven.repository.internal.MavenRepositorySystemSession
import org.kevoree.{ContainerRoot, DeployUnit}
import java.io.File
import org.sonatype.aether.artifact.Artifact
import org.kevoree.framework.KevoreePlatformHelper
import org.sonatype.aether.connector.async.AsyncRepositoryConnectorFactory
import org.sonatype.aether.spi.log.Logger
import org.sonatype.aether.spi.localrepo.LocalRepositoryManagerFactory
import org.sonatype.aether.impl.internal.EnhancedLocalRepositoryManagerFactory
import util.matching.Regex
import org.sonatype.aether.repository.{Authentication, RepositoryPolicy, RemoteRepository, LocalRepository}
import scala.collection.JavaConversions._

/**
 * User: ffouquet
 * Date: 25/07/11
 * Time: 15:06
 */

object AetherUtil {

  val newRepositorySystem: RepositorySystem = {
    val locator = new DefaultServiceLocator()
    locator.addService(classOf[Logger], classOf[AetherLogger])
    locator.addService(classOf[LocalRepositoryManagerFactory], classOf[EnhancedLocalRepositoryManagerFactory])
    locator.addService(classOf[RepositoryConnectorFactory], classOf[FileRepositoryConnectorFactory])
    locator.addService(classOf[RepositoryConnectorFactory], classOf[AsyncRepositoryConnectorFactory])
    locator.getService(classOf[RepositorySystem])
  }

  def resolveKevoreeArtifact(unitName:String, groupName:String,version : String) : File = {
    if(version.endsWith("SNAPSHOT")){
      resolveMavenArtifact(unitName,groupName,version,List("http://maven.kevoree.org/snapshots/"))
    } else {
      resolveMavenArtifact(unitName,groupName,version,List("http://maven.kevoree.org/release/"))
    }
  }

  def resolveMavenArtifact4J(unitName:String, groupName:String,version : String, repositoriesUrl : java.util.List[String]) : File =
  resolveMavenArtifact(unitName, groupName,version, repositoriesUrl.toList)

  def resolveMavenArtifact(unitName:String, groupName:String,version : String, repositoriesUrl : List[String]) : File = {
    val artifact: Artifact = new DefaultArtifact(List(groupName, unitName, version).mkString(":"))
    val artifactRequest = new ArtifactRequest
    artifactRequest.setArtifact(artifact)
    val repositories: java.util.List[RemoteRepository] = new java.util.ArrayList();
    repositoriesUrl.foreach{ repository =>
      val repo = new RemoteRepository
      val purl = repository.trim.replace(':', '_').replace('/', '_').replace('\\', '_')
      repo.setId(purl)
      repo.setUrl(repository)
      repo.setContentType("default")
      repositories.add(repo)
    }
    artifactRequest.setRepositories(repositories)
    val artefactResult = newRepositorySystem.resolveArtifact(newRepositorySystemSession, artifactRequest)
    artefactResult.getArtifact.getFile
  }
  
  
  def resolveDeployUnit(du: DeployUnit): File = {


    var artifact: Artifact = null
    if (du.getUrl != null && du.getUrl.contains("mvn:")) {
      artifact = new DefaultArtifact(du.getUrl.replaceAll("mvn:", "").replace("/", ":"))
    } else {
      artifact = new DefaultArtifact(List(du.getGroupName.trim(), du.getUnitName.trim(), du.getVersion.trim()).mkString(":"))
    }

    val artifactRequest = new ArtifactRequest
    artifactRequest.setArtifact(artifact)
    val urls = buildPotentialMavenURL(du.eContainer.asInstanceOf[ContainerRoot])

    val repositories: java.util.List[RemoteRepository] = new java.util.ArrayList();
    urls.foreach {
      url =>
        val repo = new RemoteRepository
        val purl = url.trim.replace(':', '_').replace('/', '_').replace('\\', '_')
        repo.setId(purl)
        repo.setContentType("default")
        val HttpAuthRegex = new Regex("http://(.*):(.*)@(.*)")
        url match {
          case HttpAuthRegex(login, password, urlp) => {
            repo.setAuthentication(new Authentication(login, password))
            repo.setUrl("http://" + urlp)
          }
          case _ => repo.setUrl(url)
        }
        repositories.add(repo)
    }

    artifactRequest.setRepositories(repositories)
    val artefactResult = newRepositorySystem.resolveArtifact(newRepositorySystemSession, artifactRequest)
    artefactResult.getArtifact.getFile
  }

  val newRepositorySystemSession = {
    val session = new MavenRepositorySystemSession()
    session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS)
    session.setConfigProperty("aether.connector.ahc.provider","jdk")
    session.setLocalRepositoryManager(newRepositorySystem.newLocalRepositoryManager(new LocalRepository(System.getProperty("user.home").toString + "/.m2/repository")))
    session
  }


  def buildPotentialMavenURL(root: ContainerRoot): List[String] = {
    var result: List[String] = List()
    //BUILD FROM ALL REPO
    root.getRepositories.foreach {
      repo =>
        val nurl = repo.getUrl
        if (!result.exists(p => p == nurl)) {
          result = result ++ List(nurl)
        }
    }
    //BUILD FROM ALL NODE

    root.getNodes.foreach {
      node =>
        buildURL(root, node.getName).map {
          nurl =>
            if (!result.exists(p => p == nurl)) {
              result = result ++ List(nurl)
            }
        }

    }
    result
  }

  def buildURL(root: ContainerRoot, nodeName: String): Option[String] = {
    var ip = KevoreePlatformHelper.getProperty(root, nodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP);
    if (ip == null || ip == "") {
      ip = "127.0.0.1";
    }

    root.getNodes.find(n => n.getName == nodeName) match {
      case Some(node) => {
        node.getDictionary match {
          case Some(dic)=> {
             dic.getValues.find(v => v.getAttribute.getName == "port") match {
            case Some(att) => {
              Some("http://" + ip + ":" + att.getValue + "/provisioning/")
            }
            case None => None
          }
          }
          case None => None
        }
      }
      case None => None
    }

  }


}