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
package org.kevoree.adaptation.deploy.jcl

import org.kevoree.extra.jcl.KevoreeJarClassLoader
import java.io.File
import java.util.HashMap
import org.kevoree.DeployUnit
import org.kevoree.adaptation.deploy.osgi.command.CommandHelper
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 26/01/12
 * Time: 14:29
 */

object JCLContextHandler {

  private val kcl_cache = new java.util.HashMap[String,KevoreeJarClassLoader]()
  private val kcl_cache_file = new java.util.HashMap[String,File]()
  val logger = LoggerFactory.getLogger(this.getClass)

  def getCacheFile(du : DeployUnit) : File = {
    kcl_cache_file.get(CommandHelper.buildKEY(du))
  }

  def installDeployUnit(du : DeployUnit, file : File) : KevoreeJarClassLoader = {
    logger.debug("Install {} , file {}",CommandHelper.buildKEY(du),file)
    val newcl = new KevoreeJarClassLoader
    if(du.getVersion.contains("SNAPSHOT")){
      newcl.setLazyLoad(false)
    }
    newcl.add(file.getAbsolutePath)
    kcl_cache.put(CommandHelper.buildKEY(du),newcl)
    kcl_cache_file.put(CommandHelper.buildKEY(du),file)
    logger.debug("Add KCL for "+du.getUnitName+"->"+CommandHelper.buildKEY(du))

    du.getRequiredLibs.foreach{ rLib =>
      val kcl = getKCL(rLib)
      if(kcl != null){
        logger.debug("Link KCL for "+du.getUnitName+"->"+rLib.getUnitName)
        newcl.addSubClassLoader(kcl)
      }
    }
    newcl
  }
  
  def getKCL(du : DeployUnit) : KevoreeJarClassLoader = {
    kcl_cache.get(CommandHelper.buildKEY(du))
  }
  
  def removeDeployUnit(du : DeployUnit) {
    val key = CommandHelper.buildKEY(du)
    if(kcl_cache.containsKey(key)){
      logger.debug("Remove KCL for "+du.getUnitName+"->"+CommandHelper.buildKEY(du))
      kcl_cache.get(key).unload()
      kcl_cache.remove(key)
    }
  }

}