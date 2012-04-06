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
package org.kevoree.tools.aether.framework.common

import org.kevoree.kcl.KevoreeJarClassLoader
import org.kevoree.DeployUnit
import java.io.File

import scala.collection.mutable.HashMap

trait DeployUnitInstallComponent extends Key {

  def resolveDeployUnit(unit: DeployUnit): Option[File]
  def getKCLInternals(du: DeployUnit): Option[KevoreeJarClassLoader]

  def cache:       HashMap[String, KevoreeJarClassLoader]
  def fileCache:   HashMap[String, File]
  def failedLinks: HashMap[DeployUnit, KevoreeJarClassLoader]

  def installDeployUnit(unit: DeployUnit): KevoreeJarClassLoader = {
    resolveDeployUnit(unit) match {
      case Some(file) => installDeployUnitInternals(unit, file)
      case None => null
    }
  }

  def installDeployUnitInternals(unit: DeployUnit, file: File): KevoreeJarClassLoader = {
    getKCLInternals(unit).getOrElse {

      val current = new KevoreeJarClassLoader
      val key = buildKey(unit)

      current.setLazyLoad(false)
      current.add(file.getAbsolutePath)

      cache.put(key, current)
      fileCache.put(key, file)

      failedLinks.get(unit) match {
        case Some(loader) =>
          loader.addSubClassLoader(current)
          current.addWeakClassLoader(loader)
          failedLinks.remove(unit)
        case None =>
      }

      unit.getRequiredLibs foreach {
        lib =>
          getKCLInternals(lib) match {
            case Some(loader) =>
              current.addSubClassLoader(loader)
              loader.addWeakClassLoader(current)

              unit.getRequiredLibs filter { _ != lib } foreach {
                lib =>
                  getKCLInternals(lib) match {
                    case Some(l) =>
                      loader.addWeakClassLoader(l)
                    case None =>
                  }
              }
            case None => failedLinks.put(lib, current)
          }
      }
      current
    }
  }
}
