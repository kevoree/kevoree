/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.arduinoNodeType

import org.kevoree.ContainerRoot
import org.kevoree.DeployUnit
import scala.collection.JavaConversions._
import org.kevoree.framework.KevoreePlatformHelper

object CommandHelper {
def buildKEY(du: DeployUnit): String = {
    du.getName + "/" + buildQuery(du, None)
  }

  def buildAllQuery(du: DeployUnit): List[String] = {
    var res: List[String] = List()
    val root = du.eContainer.asInstanceOf[ContainerRoot]
    root.getRepositories.foreach {
      repo =>
        if (repo.getUnits.exists(p => p == du)) {
          res = res ++ List(buildQuery(du, Some(repo.getUrl)))
        }
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
    res = res ++ List(buildQuery(du, None))

    //DEBUG
    /*
    res.foreach({u=>
      println("potential url="+u)
    })  */

    res
  }

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


  def buildPotentialMavenURL(root: ContainerRoot): List[String] = {
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
  }

  def buildURL(root: ContainerRoot, nodeName: String): String = {
    var ip = KevoreePlatformHelper.getProperty(root, nodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP);
    if (ip == null || ip == "" ) {
      ip = "127.0.0.1";
    }
    var port = KevoreePlatformHelper.getProperty(root, nodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT);
    if (port == null || port == "") {
      port = "8000";
    }
    return "http://" + ip + ":" + port + "/provisioning/";
  }

}
