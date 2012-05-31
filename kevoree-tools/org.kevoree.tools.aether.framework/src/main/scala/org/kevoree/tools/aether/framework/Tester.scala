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

import org.kevoree.KevoreeFactory
import org.kevoree.kcl.KevoreeJarClassLoader
import java.io.File

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 31/05/12
 * Time: 02:47
 */

object Tester extends App {

  val handler = new JCLContextHandler
  val graph = new File("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-corelibrary/javase/org.kevoree.library.javase.grapher/target/org.kevoree.library.javase.grapher-1.7.5-SNAPSHOT.jar")

  for (i <- 0 until 30) {
    val du = KevoreeFactory.createDeployUnit
    du.setGroupName("org.kevoree.corelibrary.javase")
    du.setUnitName("org.kevoree.library.javase.grapher")
    du.setVersion("1.7.5-SNPASHOT")

    val kcl = handler.installDeployUnit(du)
    kcl.loadClass("org.kevoree.library.javase.grapher.Grapher")
    println("iteration i=" + i)

    //handler.removeDeployUnitClassLoader(du)

  }



  Thread.sleep(Long.MaxValue)

}
