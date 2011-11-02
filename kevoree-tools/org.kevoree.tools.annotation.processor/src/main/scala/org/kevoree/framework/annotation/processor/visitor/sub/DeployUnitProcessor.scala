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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.framework.annotation.processor.visitor.sub


//import scala.collection.JavaConversions._

import org.kevoree._
import javax.lang.model.element.TypeElement
import javax.annotation.processing.ProcessingEnvironment

trait DeployUnitProcessor {

  def processDeployUnit(typeDef: TypeDefinition, classdef: TypeElement, env: ProcessingEnvironment,options : java.util.HashMap[String,String]) = {
    val root: ContainerRoot = typeDef.eContainer.asInstanceOf[ContainerRoot]
    import scala.collection.JavaConversions._

    /* CREATE COMPONENT TYPE DEPLOY UNIT IF NEEDED */
    val unitName = options.get("kevoree.lib.id")
    val groupName = options.get("kevoree.lib.group")
    val version = options.get("kevoree.lib.version")
    val tag = options.get("kevoree.lib.tag")
    val repositories = options.get("repositories")
    val repositoriesList: List[String] = repositories.split(";").filter(r => r != null && r != "").toList

    val tRepositories = options.get("otherRepositories")
    val tRepositoriesList: List[String] = tRepositories.split(";").filter(r => r != null && r != "").toList

    val nodeTypeNames = options.get("nodeTypeNames")
    val nodeTypeNamesS: List[String] = nodeTypeNames.split(",").filter(r => r != null && r != "").toList

    var deployUnits: List[DeployUnit] = List()
    nodeTypeNamesS.foreach {
      nodeTypeName =>

        val ctdeployunit = root.getDeployUnits.find({
          du => du.getUnitName == unitName && du.getGroupName == groupName && du.getVersion == version && du.getTargetNodeType.get.getName == nodeTypeName
        }) match {
          case None => {
            val newdeploy = KevoreeFactory.eINSTANCE.createDeployUnit
            newdeploy.setUnitName(unitName)
            newdeploy.setGroupName(groupName)
            newdeploy.setVersion(version)
            newdeploy.setHashcode(tag)

            /* ROOT ADD NODE TYPE IF NECESSARY */
            //nodeTypeNameList.foreach {
            //nodeTypeName =>
            root.getTypeDefinitions.filter(p => p.isInstanceOf[NodeType]).find(nt => nt.getName == nodeTypeName) match {
              case Some(existingNodeType) => newdeploy.setTargetNodeType(Some(existingNodeType.asInstanceOf[NodeType]))
              case None => {
                if (typeDef.getName == nodeTypeName) {
                  newdeploy.setTargetNodeType(Some(typeDef.asInstanceOf[NodeType]))
                } else {
                  val nodeType = KevoreeFactory.eINSTANCE.createNodeType
                  nodeType.setName(nodeTypeName)
                  root.addTypeDefinitions(nodeType)
                  newdeploy.setTargetNodeType(Some(nodeType))
                }
              }
            }

            root.addDeployUnits(newdeploy)
            deployUnits = deployUnits ++ List(newdeploy)
            newdeploy
          }
          case Some(fdu) => fdu.setHashcode(tag); fdu
        }
        if (!typeDef.getDeployUnits.contains(ctdeployunit)) {
          typeDef.addDeployUnits(ctdeployunit)
        }


    }




    /* ADD DEPLOY UNIT to RepositoryList */
    repositoriesList.foreach {
      repoUrl =>
        if (repoUrl != "") {
          val repo = root.getRepositories.find(r => r.getUrl == repoUrl) match {
            case None => {
              val newrepo = KevoreeFactory.eINSTANCE.createRepository
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
              val newrepo = KevoreeFactory.eINSTANCE.createRepository
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
