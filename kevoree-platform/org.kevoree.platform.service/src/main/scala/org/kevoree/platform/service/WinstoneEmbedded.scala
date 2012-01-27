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

import org.xeustechnologies.jcl.JclObjectFactory
import java.util.HashMap
import java.lang.Thread
import org.kevoree.extra.jcl.KevoreeJarClassLoader


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/01/12
 * Time: 17:22
 */

object WinstoneEmbedded extends App {

  val jcl2 = new KevoreeJarClassLoader
  jcl2.add("/Users/duke/.m2/repository/org/kevoree/platform/org.kevoree.platform.osgi.standalone.gui/1.5.1-SNAPSHOT/org.kevoree.platform.osgi.standalone.gui-1.5.1-SNAPSHOT.jar")

   var jcl = new KevoreeJarClassLoader
   jcl.addSubClassLoader(jcl2)


  val factory = JclObjectFactory.getInstance
  val obj = factory.create(jcl, "org.kevoree.platform.osgi.standalone.gui.App");
  obj.getClass.getMethods.find(m => m.getName == "main") match {
    case Some(m) => {
      m.invoke(obj, Array[String]())
    }
    case None =>
  }


}