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
package org.kevoree.tools.aether.framework

import org.kevoree.DeployUnit
import org.kevoree.extra.jcl.KevoreeJarClassLoader
import org.kevoree.framework.context.{KevoreeJCLBundle, KevoreeDeployManager}
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 30/01/12
 * Time: 16:05
 */

trait KCLBootstrap {

  protected val logger = LoggerFactory.getLogger(this.getClass)


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

  def installDeployUnit(du: DeployUnit): Option[KevoreeJarClassLoader] = {
      try {
        val arteFile = AetherUtil.resolveDeployUnit(du)
        if (arteFile != null) {
          logger.debug("trying to install {}", arteFile.getAbsolutePath)

          val kcl = JCLContextHandler.installDeployUnit(du,arteFile)

          //bundle = bundleContext.installBundle("file:///" + arteFile.getAbsolutePath, new FileInputStream(arteFile))
          //        bundle.start()
          KevoreeDeployManager.addMapping(KevoreeJCLBundle(buildKEY(du), du.getClass.getName,kcl, -1 ))
          Some(kcl)
        } else {
          logger.error("Can't resolve node type")
          None
        }
      } catch {
        case _@e => {
          logger.error("Can't install node type", e)
          None
        }
      }
    }

}