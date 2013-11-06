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

package org.kevoree.tools.annotation.mavenplugin

import org.apache.maven.project.MavenProject
import org.kevoree.DeployUnit
import org.kevoree.impl.DefaultKevoreeFactory
import org.kevoree.modeling.api.KMFContainer
import org.kevoree.modeling.api.util.ModelVisitor
import org.sonatype.aether.collection.CollectRequest
import org.sonatype.aether.graph.{DependencyNode, Dependency}
import org.sonatype.aether.repository.RemoteRepository
import org.sonatype.aether.resolution.DependencyRequest
import org.sonatype.aether.util.artifact.{JavaScopes, DefaultArtifact}
import org.sonatype.aether.util.filter.DependencyFilterUtils
import org.sonatype.aether.{RepositorySystemSession, RepositorySystem}
import scala.collection.JavaConversions._
import util.matching.Regex
import org.apache.maven.plugin.logging.Log
import java.util

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 17/02/12
 * Time: 09:21
 */
object ThirdPartyManagement {

  val includeProp = "kevoree.include"
  val excludeProp = "kevoree.exclude"
  val seperatorProp = ","
  val groupSepProp = ":"
  val propWildcard = "*"

  val factory = new DefaultKevoreeFactory()

  def processKevoreeProperty(pomModel: MavenProject, log: Log, repoSystem: RepositorySystem, repoSession: RepositorySystemSession, projectRepos: util.List[RemoteRepository]): java.util.List[DeployUnit] = {

    if (pomModel.getProperties.get(includeProp) == null) {
      pomModel.getProperties.put(includeProp, "*:*")
    }

    var includeRegex = List[(Regex, Regex)]()
    if (pomModel.getProperties.containsKey(includeProp)) {
      pomModel.getProperties.get(includeProp).toString.split(seperatorProp).foreach {
        loopProp =>
          val loopProps = loopProp.trim().split(groupSepProp)
          if (loopProps.size == 2) {
            includeRegex = includeRegex ++ List((new Regex(loopProps(0).replace("*", ".*").trim()), new Regex(loopProps(1).replace("*", ".*").trim())))
          } else {
            log.error("Ignore include statement because it is not well-formed: " + loopProp)
          }
      }
    }

    var excludeRegex = List[(Regex, Regex)]()
    if (pomModel.getProperties.containsKey(excludeProp)) {
      pomModel.getProperties.get(excludeProp).toString.split(seperatorProp).foreach {
        loopProp =>
          val loopProps = loopProp.trim().split(groupSepProp)
          if (loopProps.size == 2) {
            excludeRegex = excludeRegex ++ List((new Regex(loopProps(0).replace("*", ".*").trim()), new Regex(loopProps(1).replace("*", ".*").trim())))
          } else {
            log.error("Ignore exclude statement because it is not well-formed: " + loopProp)
          }
      }
    }

    val selectedDeps = new util.ArrayList[DeployUnit]()
    val excludedScope = List[String](/*"test"*/)
    val artifact = new DefaultArtifact(pomModel.getGroupId + ":" + pomModel.getArtifactId + ":" + pomModel.getVersion)
    val collectRequest = new CollectRequest()
    collectRequest.setRoot(new Dependency(artifact, ""))
    collectRequest.setRepositories(projectRepos)

    //    printTopLevelKCLLine(pomModel.getGroupId, pomModel.getArtifactId, pomModel.getVersion)
    val collectResult = repoSystem.collectDependencies(repoSession, collectRequest)
    collectResult.getRoot.getChildren.foreach {
      dep =>
        val collectRequest = new CollectRequest()
        collectRequest.setRoot(dep.getDependency)
        collectRequest.setRepositories(projectRepos)
        collectRequest.setRoot(dep.getDependency)

        val dependencyResult = repoSystem.collectDependencies(repoSession, collectRequest)

        val deployUnit = recursiveDeployUnitExploration(dependencyResult.getRoot, selectedDeps, log, excludedScope, includeRegex, excludeRegex, repoSystem, repoSession, projectRepos)
        recursivelyUpdateRequireLib(deployUnit)
        if (deployUnit != null) {
          //          printFakeKCL("\t->", deployUnit)

        }
      /*dependencyResult.getRoot.getChildren.foreach {
        transitiveDep =>
        //            recursivelyUpdateRequireLib(deployUnit, recursiveDeployUnitExploration(transitiveDep, deployUnits, log, excludedScopes, includeRegexes, excludeRegexes, repoSystem, repoSession, projectRepos))
          val deployUnit = recursiveDeployUnitExploration(dependencyResult.getRoot, selectedDeps, log, excludedScope, includeRegex, excludeRegex, repoSystem, repoSession, projectRepos)
          if (deployUnit != null) {
            printFakeKCL("\t->", deployUnit)

          }
      }*/

    }

    selectedDeps.foreach {
      dep => {
        if (selectedDeps.count(d => d.getGroupName == dep.getGroupName && d.getName == dep.getName && d.getVersion != dep.getVersion) > 0) {
          log.warn("Multiple dependencies found for " + dep.getGroupName + ":" + dep.getName + " with different versions")
        }
      }
    }
    selectedDeps
  }

  private def printTopLevelKCLLine(groupName: String, name: String, version: String) {
    System.out.println(groupName + ":" + name + ":" + version)
  }

  private def printFakeKCL(prefix: String, deployUnit: DeployUnit) {
    System.out.println(prefix + deployUnit.getGroupName + ":" + deployUnit.getName + ":" + deployUnit.getVersion)
    deployUnit.getRequiredLibs.foreach {
      dep =>
        printFakeKCL("\t" + prefix, dep)
    }
  }


  private def recursivelyUpdateRequireLib(currentDeployUnit: DeployUnit /*, requireLib: DeployUnit*/) {
    if (currentDeployUnit != null) {
      val dependencies = new util.ArrayList[DeployUnit]()
      currentDeployUnit.visit(new ModelVisitor() {
        override def visit(elem: KMFContainer, refNameInParent: String, parent: KMFContainer) {
          if (elem.isInstanceOf[DeployUnit] && !dependencies.contains(elem)) {
            dependencies.add(elem.asInstanceOf[DeployUnit])
          }
        }
      }, true, false, true)
      /*if (currentDeployUnit != null && requireLib != null) {
        currentDeployUnit.addRequiredLibs(requireLib)
        requireLib.getRequiredLibs.foreach {
          innerDependency =>
            recursivelyUpdateRequireLib(currentDeployUnit, innerDependency)
        }
      }*/
      currentDeployUnit.addAllRequiredLibs(dependencies)
    }
  }

  def recursiveDeployUnitExploration(dependency: DependencyNode, deployUnits: util.ArrayList[DeployUnit], log: Log,
                                     excludedScopes: List[String], includeRegexes: List[(Regex, Regex)], excludeRegexes: List[(Regex, Regex)],
                                     repoSystem: RepositorySystem, repoSession: RepositorySystemSession, projectRepos: util.List[RemoteRepository]): DeployUnit = {

    //    System.out.println(dependency.getDependency.getArtifact.getGroupId + ":" + dependency.getDependency.getArtifact.getArtifactId + ":" + dependency.getDependency.getArtifact.getVersion)
    val mustBeExcludedByScope = excludedScopes.exists(exScope => dependency.getDependency.getScope.equals(exScope))

    if (!mustBeExcludedByScope && !dependency.getDependency.getArtifact.getProperties.get("type").equals("pom")) {

      val mustBeExcluded = excludeRegexes.exists(rt => rt._1.unapplySeq(dependency.getDependency.getArtifact.getGroupId).isDefined && rt._2.unapplySeq(dependency.getDependency.getArtifact.getArtifactId).isDefined)

      if (!mustBeExcluded) {
        var deployUnit: DeployUnit = deployUnits.find(du =>
          du.getGroupName == dependency.getDependency.getArtifact.getGroupId
            && du.getName == dependency.getDependency.getArtifact.getArtifactId
            && du.getVersion == dependency.getDependency.getArtifact.getVersion).getOrElse(null)

        if (deployUnit == null) {
          includeRegexes.exists {
            rt =>
              if (rt._1.unapplySeq(dependency.getDependency.getArtifact.getGroupId).isDefined && rt._2.unapplySeq(dependency.getDependency.getArtifact.getArtifactId).isDefined) {

                var url: String = null
                if (dependency.getDependency.getArtifact.getFile != null) {
                  url = dependency.getDependency.getArtifact.getFile.getAbsolutePath
                }

                deployUnit = buildDeployUnit(dependency.getDependency.getArtifact.getGroupId, dependency.getDependency.getArtifact.getArtifactId, dependency.getDependency.getArtifact.getVersion,
                  dependency.getDependency.getArtifact.getProperties.get("type"), url, dependency.getDependency.getScope)

                deployUnits.add(deployUnit)
                true
              } else {
                log.warn("The DeployUnit for " + dependency.getDependency.getArtifact.getGroupId + ":" + dependency.getDependency.getArtifact.getArtifactId + ":" + dependency.getDependency.getArtifact.getVersion + " is not built because this dependency is not included")
                false
              }
          }
          if (deployUnit != null) {
            dependency.getChildren.foreach {
              dep =>
                val collectRequest = new CollectRequest()
                collectRequest.setRoot(dep.getDependency)
                collectRequest.setRepositories(projectRepos)
                collectRequest.setRoot(dep.getDependency)
                val dependencyResult = repoSystem.collectDependencies(repoSession, collectRequest)
                val requireLib = recursiveDeployUnitExploration(dependencyResult.getRoot, deployUnits, log, excludedScopes, includeRegexes, excludeRegexes, repoSystem, repoSession, projectRepos)
                if (requireLib != null) {
                  deployUnit.addRequiredLibs(requireLib)
                }
            }
          }
        }
        recursivelyUpdateRequireLib(deployUnit)
        return deployUnit
      } else {
        log.warn("The DeployUnit for " + dependency.getDependency.getArtifact.getGroupId + ":" + dependency.getDependency.getArtifact.getArtifactId + ":" + dependency.getDependency.getArtifact.getVersion + " is not built because this dependency is explicitly excluded")
        return null
      }
    } else {
      log.warn("The DeployUnit for " + dependency.getDependency.getArtifact.getGroupId + ":" + dependency.getDependency.getArtifact.getArtifactId + ":" + dependency.getDependency.getArtifact.getVersion + " is not built because this dependency is explicitly excluded according to its scope")
      return null
    }

  }

  private def buildDeployUnit(groupId: String, artifactId: String, version: String, packaging: String, url: String, scope: String): DeployUnit = {
    val deployUnit = factory.createDeployUnit()
    deployUnit.setGroupName(groupId)
    deployUnit.setName(artifactId)
    deployUnit.setVersion(version)

    if (packaging != null && !packaging.equals("")) {
      deployUnit.setType(packaging)
    } else {
      deployUnit.setType("jar")
    }


    if (scope != null && scope.equals(JavaScopes.SYSTEM) && url != null && !url.equals("")) {
      if (url.startsWith("http://")) {
        deployUnit.setUrl(url)
      } else {
        deployUnit.setUrl("file://" + url)
      }
    }
    return deployUnit
  }

}
