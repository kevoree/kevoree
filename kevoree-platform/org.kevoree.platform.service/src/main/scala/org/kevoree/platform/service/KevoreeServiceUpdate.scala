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
package org.kevoree.platform.service

import org.kevoree.KevoreeFactory
import java.io.File
import org.kevoree.tools.aether.framework.AetherUtil

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/12/11
 * Time: 14:35
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object KevoreeServiceUpdate extends App{

  val defaultLocation=System.getProperty("user.home") + File.separator + "kevoree-runtime.jar"
  def getLocation : String = defaultLocation

  val model = KevoreeFactory.eINSTANCE.createContainerRoot

  // define repositories
  var repository = KevoreeFactory.eINSTANCE.createRepository
  repository.setUrl("http://maven.kevoree.org/release")
  model.addRepositories(repository)
  repository = KevoreeFactory.eINSTANCE.createRepository
  repository.setUrl("http://maven.kevoree.org/snapshots")
  model.addRepositories(repository)


  val deployUnit = KevoreeFactory.eINSTANCE.createDeployUnit
  deployUnit.setGroupName("org.kevoree.platform")
  deployUnit.setUnitName("org.kevoree.platform.osgi.standalone")
  model.addDeployUnits(deployUnit)
  val jarFile: File = AetherUtil.resolveDeployUnit(deployUnit)

  if (jarFile.exists) {
    Runtime.getRuntime.exec(Array[String]("cp", jarFile.getAbsolutePath, defaultLocation))
  }
  println(defaultLocation)
}