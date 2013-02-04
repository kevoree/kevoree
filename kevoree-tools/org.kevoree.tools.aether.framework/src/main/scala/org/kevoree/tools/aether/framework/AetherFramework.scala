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

import org.slf4j.LoggerFactory
import java.io.File
import org.sonatype.aether.artifact.Artifact
import org.sonatype.aether.util.artifact.DefaultArtifact
import org.kevoree.{Dictionary, ContainerRoot, DeployUnit}
import util.matching.Regex
import org.sonatype.aether.repository.{Authentication, RemoteRepository}
import org.kevoree.framework.KevoreePlatformHelper
import org.sonatype.aether.resolution.{VersionRequest, ArtifactRequest}
import scala.collection.JavaConversions._
import java.util


/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 02/05/12
 * Time: 14:06
 */

trait AetherFramework extends TempFileCacheManager with AetherRepositoryHandler with CorruptedFileChecker {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def resolveKevoreeArtifact(unitName: String, groupName: String, version: String): File = {
    if (version.endsWith("SNAPSHOT")) {
      resolveMavenArtifact(unitName, groupName, version, List("http://maven.kevoree.org/snapshots/"))
    } else if (version.equals("LATEST")) {
      resolveMavenArtifact(unitName, groupName, version, List("http://maven.kevoree.org/release/", "http://maven.kevoree.org/snapshots/"))
    } else {
      resolveMavenArtifact(unitName, groupName, version, List("http://maven.kevoree.org/release/"))
    }
  }

  def resolveMavenArtifact4J(unitName: String, groupName: String, version: String, repositoriesUrl: util.List[_ <: String]): File =
    resolveMavenArtifact(unitName, groupName, version, repositoriesUrl.toList)

  def resolveMavenArtifact4J(unitName: String, groupName: String, version: String, extension: String, repositoriesUrl: util.List[_ <: String]): File =
    resolveMavenArtifact(unitName, groupName, version, extension, repositoriesUrl.toList)


  def resolveMavenArtifact(unitName: String, groupName: String, version: String, repositoriesUrl: util.List[_ <: String]): File = {
    resolveMavenArtifact(unitName, groupName, version, null, repositoriesUrl)
  }

  def resolveMavenArtifact(unitName: String, groupName: String, version: String, extension: String, repositoriesUrl: util.List[_ <: String]): File = {
    val artifact: Artifact = if (extension == null) {
      new DefaultArtifact(List(groupName.trim(), unitName.trim(), version.trim()).mkString(":"))
    } else {
      new DefaultArtifact(groupName.trim(), unitName.trim(), extension.trim(), version.trim())
    }
    val artifactRequest = new ArtifactRequest
    artifactRequest.setArtifact(artifact)
    val repositories: java.util.List[RemoteRepository] = new java.util.ArrayList()
    populate(repositoriesUrl)
    populate(getDefaultURLS)
    def populate(rurl: util.List[_ <: String]) {
      rurl.foreach {
        repository =>
          val repo = new RemoteRepository
          val purl = repository.trim.replace(':', '_').replace('/', '_').replace('\\', '_')
          repo.setId(purl)
          repo.setUrl(repository)
          repo.setContentType("default")
          repositories.add(repo)
          logger.debug("Add URL for Request {}", repository)
      }
    }

    artifactRequest.setRepositories(repositories)
    var artefactResult = getRepositorySystem.resolveArtifact(getRepositorySystemSession, artifactRequest)

    val corruptedFile = artefactResult.getArtifact
    if (corruptedFile == null || !checkFile(corruptedFile.getFile)) {
      //OPTIONAL REMOVE : _maven.repositories
      clearRepoCacheFile(getRepositorySystemSession, artifactRequest.getArtifact)
      artefactResult = getRepositorySystem.resolveArtifact(getRepositorySystemSession, artifactRequest)
    }

    if (checkFile(artefactResult.getArtifact.getFile)) {
      installInCache(artefactResult.getArtifact)
    } else {
      logger.warn("Aether return bad Corrupted File after second try , abording")
      null
    }

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
    var urls: List[String] = null
    if (System.getProperty("kevoree.offline") != null && System.getProperty("kevoree.offline").equals("true")) {
      urls = List()
    } else {
      urls = buildPotentialMavenURL(du.eContainer.asInstanceOf[ContainerRoot])
    }

    val repositories: java.util.List[RemoteRepository] = new java.util.ArrayList()
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

    try {
      artifactRequest.setRepositories(repositories)
      var artefactResult = getRepositorySystem.resolveArtifact(getRepositorySystemSession, artifactRequest)
      val corruptedFile = artefactResult.getArtifact
      if (corruptedFile == null || !checkFile(corruptedFile.getFile)) {
        //OPTIONAL REMOVE : _maven.repositories
        clearRepoCacheFile(getRepositorySystemSession, artifactRequest.getArtifact)
        artefactResult = getRepositorySystem.resolveArtifact(getRepositorySystemSession, artifactRequest)
      }
      if (checkFile(artefactResult.getArtifact.getFile)) {
        installInCache(artefactResult.getArtifact)
      } else {
        logger.warn("Aether return bad Corrupted File after second try , abording")
        null
      }
    } catch {
      case _@e => {
        try {
          logger.debug("Error while resolving " + du.getUnitName.trim + " -> second try", e)
          clearRepoCacheFile(getRepositorySystemSession, artifactRequest.getArtifact)
          val artefactResult = getRepositorySystem.resolveArtifact(getRepositorySystemSession, artifactRequest)
          if (checkFile(artefactResult.getArtifact.getFile)) {
            installInCache(artefactResult.getArtifact)
          } else {
            logger.warn("Aether return bad Corrupted File after second try , abording")
            null
          }
        } catch {
          case _ => {
            logger.debug("Error while resolving " + du.getUnitName.trim(), e)
            null
          }
        }
      }
    }
  }

  def buildPotentialMavenURL(root: ContainerRoot): List[String] = {
    var result: List[String] = List()
    //BUILD FROM ALL REPO
    if (root != null && root.getRepositories != null) {
      root.getRepositories.foreach {
        repo =>
          val nurl = repo.getUrl
          if (!result.exists(p => p == nurl)) {
            result = result ++ List(nurl)
          }
      }
    }


    //BUILD FROM ALL NODE
    /*
    root.getNodes.foreach {
      node =>
        buildURL(root, node.getName).map {
          nurl =>
            if (!result.exists(p => p == nurl)) {
              result = result ++ List(nurl)
            }
        }

    }*/
    result ++ getDefaultURLS
  }

  def buildURL(root: ContainerRoot, nodeName: String): Option[String] = {
    var ip = KevoreePlatformHelper.getProperty(root, nodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
    if (ip == null || ip == "") {
      ip = "127.0.0.1"
    }

    root.getNodes.find(n => n.getName == nodeName) match {
      case Some(node) => {
        node.getDictionary match {
          case dic: Dictionary => {
            dic.getValues.find(v => v.getAttribute.getName == "port") match {
              case Some(att) => {
                Some("http://" + ip + ":" + att.getValue + "/provisioning/")
              }
              case None => None
            }
          }
          case null => None
        }
      }
      case None => None
    }

  }


  def resolveVersion(groupName: String, unitName: String, versionProperty: String, repositoryUrls: List[String]): String = {
    val artifact: Artifact = new DefaultArtifact(List(groupName.trim(), unitName.trim(), versionProperty.trim()).mkString(":"))
    val versionRequest = new VersionRequest()
    versionRequest.setArtifact(artifact)

    val repositories: java.util.List[RemoteRepository] = new java.util.ArrayList();
    repositoryUrls.foreach {
      repository =>
        val repo = new RemoteRepository
        val purl = repository.trim.replace(':', '_').replace('/', '_').replace('\\', '_')
        repo.setId(purl)
        repo.setUrl(repository)
        repo.setContentType("default")
        repositories.add(repo)
    }
    versionRequest.setRepositories(repositories)
    val versionResult = getRepositorySystem.resolveVersion(getRepositorySystemSession, versionRequest)
    versionResult.getVersion
  }

}
