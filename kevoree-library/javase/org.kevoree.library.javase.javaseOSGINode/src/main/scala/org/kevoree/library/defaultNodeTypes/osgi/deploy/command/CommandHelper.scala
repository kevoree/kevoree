package org.kevoree.library.defaultNodeTypes.osgi.deploy.command

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

import org.kevoree.DeployUnit

object CommandHelper {

  def buildKEY(du: DeployUnit): String = {
    du.getName + "/" + buildQuery(du, None)
  }

  /*def buildAllQuery(du: DeployUnit): List[String] = {
    var res: List[String] = List()
    val root = du.eContainer.asInstanceOf[ContainerRoot]

    res = res ++ List(buildQuery(du, None))

    //add First the repo where the artifact have been deployed
    root.getRepositories.foreach {
      repo =>
        if (repo.getUnits.exists(p => p == du)) {
          res = res ++ List(buildQuery(du, Some(repo.getUrl)))
        }
    }

    //Add other available repos
    root.getRepositories.foreach {
      repo =>
        res = res ++ List(buildQuery(du,Some(repo.getUrl)))
    }

    root.getNodes.foreach {
      node =>
        res = res ++ List(buildQuery(du, Some(buildURL(root, node.getName))))
    }
    /*
    res match {
      case List() => println("Add default location"); res = res ++ List(buildQuery(du, None))
      case _ =>
    } */


    //DEBUG
    /*
    res.foreach({u=>
      println("potential url="+u)
    })  */

    res
  }*/

  def buildQuery(du: DeployUnit, repoUrl: Option[String]): String = {
    val query = new StringBuilder
    query.append("mvn:")
    repoUrl match {
      case Some(r) => query.append(r); query.append("!")
      case None =>
    }
    query.append(du.getGroupName)
    query.append("/")
    query.append(du.getUnitName)
    du.getVersion match {
      case "default" =>
      case "" =>
      case _ => query.append("/"); query.append(du.getVersion)
    }
    query.toString
  }


  /*def buildPotentialMavenURL(root: ContainerRoot): List[String] = {
    var result: List[String] = List()
    //BUILD FROM ALL REPO
    root.getRepositories.foreach {
      repo =>
        result = result ++ List(repo.getUrl)
    }
    //BUILD FROM ALL NODE
    root.getNodes.foreach {
      node =>
        result = result ++ List(buildURL(root, node.getName))
    }
    result
  }*/

  /*def buildURL(root: ContainerRoot, nodeName: String): String = {
    var ip = KevoreePlatformHelper.getProperty(root, nodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP);
    if (ip == null || ip == "" ) {
      ip = "127.0.0.1";
    }
    var port = KevoreePlatformHelper.getProperty(root, nodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT);
    if (port == null || port == "") {
      port = "8000";
    }
    return "http://" + ip + ":" + port + "/provisioning/";
  }*/

}
