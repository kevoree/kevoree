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

import org.apache.maven.repository.internal.MavenRepositorySystemSession
import org.sonatype.aether.RepositorySystem
import java.io.File
import org.sonatype.aether.repository.RemoteRepository
import org.sonatype.aether.resolution.ArtifactRequest
import org.sonatype.aether.util.artifact.DefaultArtifact
import org.sonatype.aether.artifact.Artifact
import scala.collection.JavaConversions._


trait MavenResolver {

  def repositorySystemSession: MavenRepositorySystemSession
  def repositorySystem: RepositorySystem

  def resolveMavenArtifact(unitName: String, groupName: String, version: String, repositoriesUrl: List[String]): File

  def resolveMavenArtifact4J(unitName: String, groupName: String, version: String, repositoriesUrl: java.util.List[String]): File = {
    resolveMavenArtifact(unitName, groupName, version, repositoriesUrl.toList)
  }


  protected def resolve(unitName: String, groupName: String, version: String, repositoriesUrl: List[String]): Artifact = {
    val artifact: Artifact = new DefaultArtifact(List(groupName.trim, unitName.trim, version.trim).mkString(":"))
    val artifactRequest = new ArtifactRequest

    artifactRequest.setArtifact(artifact)

    val repositories: java.util.List[RemoteRepository] = new java.util.ArrayList()

    repositoriesUrl.foreach {
      repository =>
        val repo = new RemoteRepository
        val purl = repository.trim.replace(':', '_').replace('/', '_').replace('\\', '_')
        repo.setId(purl)
        repo.setUrl(repository)
        repo.setContentType("default")
        repositories.add(repo)
    }

    artifactRequest.setRepositories(repositories)
    val artifactResult = repositorySystem.resolveArtifact(repositorySystemSession, artifactRequest)

    artifactResult.getArtifact
  }
}
