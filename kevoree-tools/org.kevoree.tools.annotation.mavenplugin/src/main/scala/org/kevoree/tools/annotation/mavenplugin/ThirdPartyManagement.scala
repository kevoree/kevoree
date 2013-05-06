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
import org.kevoree.ContainerRoot
import scala.collection.JavaConversions._
import org.apache.maven.model.Dependency
import util.matching.Regex
import org.apache.maven.plugin.logging.Log

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

  def processKevoreeProperty(pomModel: MavenProject, log: Log): java.util.List[Dependency] = {

    if (!pomModel.getPackaging.equals("bundle") && pomModel.getProperties.get(includeProp) == null) {
      pomModel.getProperties.put(includeProp, "*:*")
    }

    var includeRegex = List[Tuple2[Regex, Regex]]()
    if (pomModel.getProperties.containsKey(includeProp)) {
      pomModel.getProperties.get(includeProp).toString.split(seperatorProp).foreach {
        loopProp =>
          val loopProps = loopProp.trim().split(groupSepProp)
          if (loopProps.size == 2) {
            includeRegex = includeRegex ++ List((new Regex(loopProps(0).replace("*", ".*").trim()), new Regex(loopProps(1).replace("*", ".*").trim())))
          } else {
            log.error("Ignore include statement -> " + loopProp)
          }
      }
    } /*else {
      includeRegex = includeRegex ++ List((new Regex("*"), new Regex("*")))
    }*/

    var excludeRegex = List[Tuple2[Regex, Regex]]()
    if (pomModel.getProperties.containsKey(excludeProp)) {
      pomModel.getProperties.get(excludeProp).toString.split(seperatorProp).foreach {
        loopProp =>
          val loopProps = loopProp.trim().split(groupSepProp)
          if (loopProps.size == 2) {
            excludeRegex = excludeRegex ++ List((new Regex(loopProps(0).replace("*", ".*").trim()), new Regex(loopProps(1).replace("*", ".*").trim())))
          } else {
            log.error("Ignore include statement -> " + loopProp)
          }
      }
    }

    var selectedDeps = List[Dependency]()

    val excludedScope = List[String]("test")


    //FILTER
    pomModel.getDependencies.foreach {
      loopDep => {
        if (loopDep.getScope.equals("provided") || loopDep.getType.equals("bundle")) {
          if (!excludedScope.exists(exScope => loopDep.getScope == exScope) && !selectedDeps.exists(preDep => preDep.getGroupId == loopDep.getGroupId && preDep.getArtifactId == loopDep.getArtifactId && preDep.getVersion == loopDep.getVersion)) {
            selectedDeps = selectedDeps ++ List(loopDep)
          }
        } else {
          includeRegex.foreach {
            rt =>
              if (rt._1.unapplySeq(loopDep.getGroupId).isDefined && rt._2.unapplySeq(loopDep.getArtifactId).isDefined) {
                if (!selectedDeps.exists(preDep => preDep.getGroupId == loopDep.getGroupId && preDep.getArtifactId == loopDep.getArtifactId && preDep.getVersion == loopDep.getVersion)) {
                  if (!excludedScope.exists(exScope => loopDep.getScope == exScope)) {
                    selectedDeps = selectedDeps ++ List(loopDep)
                  }
                }
              }
          }
        }
      }
    }

    //FILTER NOT
    selectedDeps.foreach {
      previousSelectedDep =>
        excludeRegex.foreach {
          rt =>
            if (rt._1.unapplySeq(previousSelectedDep.getGroupId).isDefined && rt._2.unapplySeq(previousSelectedDep.getArtifactId).isDefined) {
              selectedDeps = selectedDeps.filter(s => s != previousSelectedDep) //REMOVE
            }
        }
    }

    selectedDeps.foreach {
      previousSelectedDep =>
        if (previousSelectedDep.getType == "pom") {
          selectedDeps = selectedDeps.filter(s => s != previousSelectedDep) //REMOVE
        }
    }



    selectedDeps

  }


}
