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

import org.sonatype.aether.util.artifact.DefaultArtifact
import org.sonatype.aether.artifact.Artifact
import org.kevoree.DeployUnit
import util.matching.Regex
import org.sonatype.aether.repository.{RemoteRepository, Authentication}
import org.sonatype.aether.resolution.ArtifactRequest
import java.io.File
import org.apache.maven.repository.internal.MavenRepositorySystemSession
import org.sonatype.aether.RepositorySystem


trait AbstractDeployUnitResolver {

  def repositorySystemSession: MavenRepositorySystemSession
  def repositorySystem: RepositorySystem

  def buildUrls(unit: DeployUnit): List[String]
  def processResult(artifact: Artifact): File

  def resolveDeployUnit(implicit unit: DeployUnit): File = {
    val artifact = buildArtifact
    val urls = buildUrls(unit)

    val artifactRequest = new ArtifactRequest
    val repositories: java.util.List[RemoteRepository] = new java.util.ArrayList()

    artifactRequest.setArtifact(artifact)

    urls foreach {
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
      val artifactResult = repositorySystem.resolveArtifact(repositorySystemSession, artifactRequest)
      processResult(artifactResult.getArtifact)
    } catch {
      case _@e => {
        null
      }
    }
  }

  private def buildArtifact(implicit unit: DeployUnit): Artifact = {
    if (unit.getUrl != null && unit.getUrl.contains("mvn:"))
      new DefaultArtifact(unit.getUrl.replaceAll("mvn:", "").replace("/", ":"))
    else
      new DefaultArtifact(List(unit.getGroupName.trim, unit.getUnitName.trim, unit.getVersion.trim).mkString(":"))
  }
}

