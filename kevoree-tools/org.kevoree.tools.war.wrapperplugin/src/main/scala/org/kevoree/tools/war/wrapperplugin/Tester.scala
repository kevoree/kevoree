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
package org.kevoree.tools.war.wrapperplugin

import scala.collection.JavaConversions._
import java.util.jar.Attributes.Name
import java.util.jar.Attributes
import io.Source
import java.io._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 18/12/11
 * Time: 08:29
 * To change this template use File | Settings | File Templates.
 */

object Tester extends App {

  println("Test")

  val mf = new java.util.jar.Manifest

  val attName = new Attributes.Name("MyNAme")
  val attName2 = new Attributes.Name("MyNAme2")


  mf.getMainAttributes.put(attName,"1.2.0")
  mf.getMainAttributes.put(attName2,"org.valyejl,org.valyejl,org.valyejl,org.valyejl,org.valyejl,org.valyejl,org.valyejl,org.valyejl,org.valyejl")
  mf.getMainAttributes.put(Attributes.Name.MANIFEST_VERSION,"1.0")

  mf.getEntries.foreach{ e =>
    
    println(e._1)
    println(e._2)
    
  }

  val fos = new FileOutputStream("boo.MF");

  mf.write(fos)


}