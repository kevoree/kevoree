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
package org.kevoree.tools.aether.framework.android

import org.kevoree.DeployUnit
import java.io.File
import org.kevoree.kcl.KevoreeJarClassLoader
import scala.collection.JavaConversions._
import org.kevoree.tools.aether.framework.{JCLContextHandler}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 05/02/12
 * Time: 17:50
 */

class AndroidJCLContextHandler(ctx: android.content.Context, parent: ClassLoader) extends JCLContextHandler {

  override def installDeployUnitNoFileInternals(du: DeployUnit): KevoreeJarClassLoader = {
    var resolvedFile : File = null
    resolvers.exists{ res =>
      try {
        resolvedFile = res.resolve(du)
        true
      } catch {
        case _ @ e => false
      }
    }
    if (resolvedFile == null) {
      resolvedFile = org.kevoree.tools.aether.framework.android.AetherUtil.resolveDeployUnit(du)
    }

    if (resolvedFile != null) {
      installDeployUnitInternals(du, resolvedFile)
    } else {
      logger.error("Error while resolving deploy unit " + du.getUnitName)
      null
    }
  }

  override def installDeployUnitInternals(du: DeployUnit, file: File): KevoreeJarClassLoader = {
    val previousKCL = getKCLInternals(du)
    val res = if (previousKCL != null) {
      logger.debug("Take already installed {}", buildKEY(du))
      previousKCL
    } else {
      logger.debug("Install {} , file {}", Array[AnyRef](buildKEY(du), file))

      val cleankey = buildKEY(du).replace(File.separator, "_")
      val newcl = new AndroidKevoreeJarClassLoader(cleankey, ctx, parent)

      //if (du.getVersion.contains("SNAPSHOT")) {
      newcl.setLazyLoad(false)
      // }

      newcl.add(file.getAbsolutePath)
      kcl_cache.put(buildKEY(du), newcl)
      kcl_cache_file.put(buildKEY(du), file)
      logger.debug("Add KCL for {}->{}", Array[AnyRef](du.getUnitName, buildKEY(du)))

      //TRY TO RECOVER FAILED LINK
      if (failedLinks.containsKey(buildKEY(du))) {
        failedLinks.get(buildKEY(du)).addSubClassLoader(newcl)
        newcl.addWeakClassLoader(failedLinks.get(buildKEY(du)))
        failedLinks.remove(buildKEY(du))
        logger.debug("Failed Link {} remain size : {}", du.getUnitName, failedLinks.size())
      }

      du.getRequiredLibs.foreach {
        rLib =>
          val kcl = getKCLInternals(rLib)
          if (kcl != null) {
            logger.debug("Link KCL for {}->{}", Array[AnyRef](du.getUnitName, rLib.getUnitName))
            newcl.addSubClassLoader(kcl)
            kcl.addWeakClassLoader(newcl)

            du.getRequiredLibs.filter(rLibIn => rLib != rLibIn).foreach(rLibIn => {
              val kcl2 = getKCLInternals(rLibIn)
              if (kcl2 != null) {
                kcl.addWeakClassLoader(kcl2)
                // logger.debug("Link Weak for {}->{}", rLib.getUnitName, rLibIn.getUnitName)
              }
            })
          } else {
            logger.debug("Fail link ! Warning ")
            failedLinks.put(buildKEY(du), newcl)
          }
      }


      newcl
    }
    /*
    if (logger.isDebugEnabled) {
      printDumpInternals()
    }*/
    res
  }


      /*

  def installDeployUnitInternals(du: DeployUnit, file: File): KevoreeJarClassLoader = {
    val previousKCL = getKCLInternals(du)
    val res = if (previousKCL != null) {
      logger.debug("Take already installed {}", buildKEY(du))
      previousKCL
    } else {
      logger.debug("Install {} , file {}", buildKEY(du), file)
      val cleankey = buildKEY(du).replace(File.separator, "_")
      val newcl = new AndroidKevoreeJarClassLoader(cleankey, ctx, parent)
      if (du.getVersion.contains("SNAPSHOT")) {
        newcl.setLazyLoad(false)
      }
      logger.debug("Before add the jar {} to classloader",file.getAbsolutePath)
      try {
        newcl.add(file.getAbsolutePath)
      } catch {
        case _ @ e => {
          logger.error("Can't add Jar to class path",e)
          return null
        }
      }
      logger.debug("After Adding the jar ")
      kcl_cache.put(buildKEY(du), newcl)
      kcl_cache_file.put(buildKEY(du), file)
      logger.debug("Add KCL for {}->{}", du.getUnitName, buildKEY(du))

      du.getRequiredLibs.foreach {
        rLib =>
          val kcl = getKCLInternals(rLib)
          if (kcl != null) {
            logger.debug("Link KCL for {}->{}", du.getUnitName, rLib.getUnitName)
            newcl.addSubClassLoader(kcl)

            du.getRequiredLibs.filter(rLibIn => rLib != rLibIn).foreach(rLibIn => {
              val kcl2 = getKCLInternals(rLibIn)
              if (kcl2 != null) {
                kcl.addWeakClassLoader(kcl2)
                logger.debug("Link Weak for {}->{}", rLib.getUnitName, rLibIn.getUnitName)
              }
            })


          }
      }
      newcl
    }
    if (logger.isDebugEnabled) {
      printDumpInternals()
    }
    res
  }   */


}
