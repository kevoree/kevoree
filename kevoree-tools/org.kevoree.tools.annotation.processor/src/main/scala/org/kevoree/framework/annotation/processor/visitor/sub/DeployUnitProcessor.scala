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

package org.kevoree.framework.annotation.processor.visitor.sub


//import scala.collection.JavaConversions._

import org.kevoree._
import framework.annotation.processor.LocalUtility
import javax.lang.model.element.TypeElement
import javax.annotation.processing.ProcessingEnvironment


trait DeployUnitProcessor {

  def processDeployUnit(typeDef: TypeDefinition, classdef: TypeElement, env: ProcessingEnvironment, options: java.util.Map[String, Object]) = {
    val root: ContainerRoot = typeDef.eContainer.asInstanceOf[ContainerRoot]
    import scala.collection.JavaConversions._

    /* CREATE COMPONENT TYPE DEPLOY UNIT IF NEEDED */
    val name = options.get("kevoree.lib.id").toString
    val groupName = options.get("kevoree.lib.group").toString
    val version = options.get("kevoree.lib.version").toString
    val dutype = options.get("kevoree.lib.type").toString
    val tag = options.get("kevoree.lib.tag").toString
    val repositories = options.get("repositories").toString
    val repositoriesList: List[String] = repositories.split(";").filter(r => r != null && r != "").toList

    val tRepositories = options.get("otherRepositories").toString
    val tRepositoriesList: List[String] = tRepositories.split(";").filter(r => r != null && r != "").toList
    var deployUnits: List[DeployUnit] = List()
    val ctdeployunit = root.getDeployUnits.find({
      du => du.getName == name && du.getGroupName == groupName && du.getVersion == version
    }) match {
      case None => {
        val newdeploy = LocalUtility.kevoreeFactory.createDeployUnit
        newdeploy.setName(name)
        newdeploy.setGroupName(groupName)
        newdeploy.setVersion(version)
        newdeploy.setHashcode(tag)
        newdeploy.setType(dutype)
        root.addDeployUnits(newdeploy)
        deployUnits = deployUnits ++ List(newdeploy)
        newdeploy
      }
      case Some(fdu) => fdu.setHashcode(tag); fdu
    }
    if (!typeDef.getDeployUnits.contains(ctdeployunit)) {
      typeDef.addDeployUnits(ctdeployunit)
    }





    /* ADD DEPLOY UNIT to RepositoryList */
    repositoriesList.foreach {
      repoUrl =>
        if (repoUrl != "") {
          val repo = root.getRepositories.find(r => r.getUrl == repoUrl) match {
            case None => {
              val newrepo = LocalUtility.kevoreeFactory.createRepository
              newrepo.setUrl(repoUrl)
              root.addRepositories(newrepo)
              newrepo
            }
            case Some(e) => e
          }
          //repo.addAllUnits(deployUnits)
        }
    }

    tRepositoriesList.foreach {
      rRepoUrl =>
        if (rRepoUrl != "") {
          root.getRepositories.find(r => r.getUrl == rRepoUrl) match {
            case None => {
              val newrepo = LocalUtility.kevoreeFactory.createRepository
              newrepo.setUrl(rRepoUrl)
              root.addRepositories(newrepo)
              newrepo
            }
            case Some(e) =>
          }
        }
    }


  }


}
