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

import org.xeustechnologies.jcl.JarClassLoader
import java.io.{ByteArrayInputStream, InputStream}
import java.lang.String
import java.net.URL

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/01/12
 * Time: 18:57
 * To change this template use File | Settings | File Templates.
 */

class KevoreeJarClassLoader extends JarClassLoader(null.asInstanceOf[ClassLoader]) {



  classpathResources =  new KevoreeLazyJarResources




  override def getResourceAsStream(name  : String) : InputStream = {
    val res = this.classpathResources.getResource(name)
    if(res != null){
      new ByteArrayInputStream(res)
    } else {
      null
    }
  }

  override def getResource(p1: String): URL = {
    classpathResources.asInstanceOf[KevoreeLazyJarResources].getContentURL(p1)
  }

  def unload() {
    import scala.collection.JavaConversions._
    (this.getLoadedClasses.keySet().toList++List()).foreach{ lc =>
      unloadClass(lc)
    }
  }


}