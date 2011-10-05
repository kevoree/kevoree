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

import com.sun.mirror.apt.AnnotationProcessorEnvironment
import com.sun.mirror.declaration.TypeDeclaration

//import scala.collection.JavaConversions._

import org.kevoree._

trait DeployUnitProcessor {

  def processDeployUnit(typeDef: TypeDefinition, classdef: TypeDeclaration, env: AnnotationProcessorEnvironment) = {
    val root: ContainerRoot = typeDef.eContainer.asInstanceOf[ContainerRoot]
    import scala.collection.JavaConversions._

    /* CREATE COMPONENT TYPE DEPLOY UNIT IF NEEDED */
    val unitName = env.getOptions.find({
      op => op._1.contains("kevoree.lib.id")
    }).getOrElse {
      ("key=", "")
    }._1.split('=').toList.get(1)
    val groupName = env.getOptions.find({
      op => op._1.contains("kevoree.lib.group")
    }).getOrElse {
      ("key=", "")
    }._1.split('=').toList.get(1)
    val version = env.getOptions.find({
      op => op._1.contains("kevoree.lib.version")
    }).getOrElse {
      ("key=", "")
    }._1.split('=').toList.get(1)
    val tag = env.getOptions.find({
      op => op._1.contains("kevoree.lib.tag")
    }).getOrElse {
      ("key=", "")
    }._1.split('=').toList.get(1)

    val repositories = env.getOptions.find({
      op => op._1.contains("repositories")
    }).getOrElse {
      ("key=", "")
    }._1.split('=').toList.get(1)
    val repositoriesList: List[String] = repositories.split(";").filter(r => r != null && r != "").toList

    val tRepositories = env.getOptions.find({
      op => op._1.contains("otherRepositories")
    }).getOrElse {
      ("key=", "")
    }._1.split('=').toList.get(1)
    val tRepositoriesList: List[String] = tRepositories.split(";").filter(r => r != null && r != "").toList

    val nodeTypeNames = env.getOptions.find({
      op => op._1.contains("nodeTypeNames")
    }).getOrElse {
      ("key=", "")
    }._1.split('=').toList.get(1)
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
              case Some(existingNodeType) => newdeploy.setTargetNodeType(existingNodeType.asInstanceOf[NodeType])
              case None => {
                val nodeType = KevoreeFactory.eINSTANCE.createNodeType
                nodeType.setName(nodeTypeName)
                root.addTypeDefinitions(nodeType)
                newdeploy.setTargetNodeType(nodeType)
              }
            }

            //println("new dpeloy unit target node type "+newdeploy.getTargetNodeType)

            root.addDeployUnits(newdeploy)
            deployUnits = deployUnits ++ List(newdeploy)
            newdeploy
          }
          case Some(fdu) => fdu.setHashcode(tag); fdu
        }
        if(!typeDef.getDeployUnits.contains(ctdeployunit)){
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
          repo.addAllUnits(deployUnits)
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
