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


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/01/12
 * Time: 17:22
 */

object WinstoneEmbedded extends App {

  val jcl2 = new KevoreeJarClassLoader
  //  jcl2.add("/Users/duke/.m2/repository/org/kevoree/platform/org.kevoree.platform.osgi.standalone.gui/1.5.1-SNAPSHOT/org.kevoree.platform.osgi.standalone.gui-1.5.1-SNAPSHOT.jar")
  jcl2.add("/Users/duke/.m2/repository/org/kevoree/extra/org.kevoree.extra.winstone/0.9.10/org.kevoree.extra.winstone-0.9.10.jar")


  // var jcl = new KevoreeJarClassLoader
  // jcl.addLoader(jcl2.getCurrentLoader)

  // val context = new DefaultContextLoader(jcl)
  //context.loadContext()

   jcl2.getSystemLoader().setOrder(3); // Look in system class loader first
   jcl2.getLocalLoader().setOrder(1); // if not found look in local class loader
  //// jcl.getParentLoader().setOrder(2);
  // if not found look in parent class loader
  // if not found look in current class loader


  // val loader = jcl.getCurrentLoader


  println("super=>" + jcl2.getParent)


  jcl2.getParentLoader.setEnabled(false)
  jcl2.getCurrentLoader.setEnabled(false)
  jcl2.getOsgiBootLoader.setEnabled(false)
  jcl2.getThreadLoader.setEnabled(false)
  //jcl2.getSystemLoader.setEnabled(false)

  val factory = JclObjectFactory.getInstance
  val config: java.util.Map[Any, Any] = new HashMap[Any, Any]();
  config.put("ajp13Port", "-1");
  config.put("warfile", "/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/javase/org.kevoree.library.javase.webserver.jenkins/target/jenkins.war");
  val clazz = jcl2.loadClass("winstone.Launcher")
  clazz.getMethods.foreach {
    m =>
      println(m.getName)
  }

  val t = new Thread() {
    override def run() {
      setContextClassLoader(jcl2)
      clazz.getDeclaredConstructors.foreach {
        dcs =>
          if (dcs.getParameterTypes.size > 0) {
            dcs.newInstance(config)
          }
      }
    }
  }
  t.start()


 // jcl2.unload()


  // val obj = factory.create(jcl2, "winstone.Launcher");

  /*
  obj.getClass.getMethods.find(m => m.getName == "main") match {
    case Some(m) => {
      m.invoke(obj, Array[String]())
    }
    case None =>
  }*/


}