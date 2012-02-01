package org.kevoree.tools.aether.framework

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

import java.io.File
import org.slf4j.LoggerFactory
import org.kevoree.DeployUnit
import org.kevoree.extra.jcl.KevoreeJarClassLoader

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 26/01/12
 * Time: 14:29
 */

object JCLContextHandler {

  private val kcl_cache = new java.util.HashMap[String, KevoreeJarClassLoader]()
  private val kcl_cache_file = new java.util.HashMap[String, File]()
  val logger = LoggerFactory.getLogger(this.getClass)

  def getCacheFile(du: DeployUnit): File = {
    kcl_cache_file.get(buildKEY(du))
  }

  def installDeployUnit(du: DeployUnit, file: File): KevoreeJarClassLoader = {
    val previousKCL = getKCL(du)
    if (previousKCL != null) {
      logger.debug("Take already installed {}", buildKEY(du))
      previousKCL
    } else {
      logger.debug("Install {} , file {}", buildKEY(du), file)
      val newcl = new KevoreeJarClassLoader
      if (du.getVersion.contains("SNAPSHOT")) {
        newcl.setLazyLoad(false)
      }
      newcl.add(file.getAbsolutePath)
      kcl_cache.put(buildKEY(du), newcl)
      kcl_cache_file.put(buildKEY(du), file)
      logger.debug("Add KCL for " + du.getUnitName + "->" + buildKEY(du))

      du.getRequiredLibs.foreach {
        rLib =>
          val kcl = getKCL(rLib)
          if (kcl != null) {
            logger.debug("Link KCL for " + du.getUnitName + "->" + rLib.getUnitName)
            newcl.addSubClassLoader(kcl)

            du.getRequiredLibs.filter(rLibIn => rLib != rLibIn).foreach(rLibIn => {
              val kcl2 = getKCL(rLibIn)
              if (kcl2 != null) {
                kcl.addWeakClassLoader(kcl2)
                logger.debug("Link Weak for " + rLib.getUnitName + "->" + rLibIn.getUnitName)
              }
            })


          }
      }
      newcl
    }
  }

  def getKCL(du: DeployUnit): KevoreeJarClassLoader = {
    kcl_cache.get(buildKEY(du))
  }

  def removeDeployUnit(du: DeployUnit) {
    val key = buildKEY(du)
    if (kcl_cache.containsKey(key)) {
      logger.debug("Remove KCL for " + du.getUnitName + "->" + buildKEY(du))
      kcl_cache.get(key).unload()
      kcl_cache.remove(key)
    }
  }


  def buildKEY(du: DeployUnit): String = {
    du.getName + "/" + buildQuery(du, None)
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
}