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
package org.kevoree.kcl

import org.xeustechnologies.jcl.ProxyClassLoader
import java.lang.Class
import java.io.{ByteArrayInputStream, InputStream}
import actors.{OutputChannel, DaemonActor}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 19/03/12
 * Time: 14:53
 */

class KevoreeLocalLoader(classpathResources: KevoreeLazyJarResources, kcl: KevoreeJarClassLoader) extends ProxyClassLoader with DaemonActor {

  order = 1

  def loadClass(className: String, resolveIt: Boolean): Class[_] = {
    var result = kcl.getLoadedClass(className)
    if (result == null) {
      val bytes = kcl.loadClassBytes(className)
      if (bytes != null) {
        acquireLock(className)
        result = kcl.getLoadedClass(className)
        if (result == null) {
          result = kcl.internal_defineClass(className, bytes)
        }
        releaseLock(className)
      }
    }
    result
  }

  def loadResource(name: String): InputStream = {
    val arr = classpathResources.getResource(name)
    if (arr != null) {
      return new ByteArrayInputStream(arr)
    }
    null
  }

  case class LockClassLoad(className: String)

  case class UnlockClassLoad(className: String)

  def acquireLock(className: String) {
    (this !? LockClassLoad(className))
  }

  def releaseLock(className: String) {
    (this ! UnlockClassLoad(className))
  }

  case class KILL()

  def killActor() {
    this ! KILL()
  }


  private val locked = new java.util.HashMap[String,List[OutputChannel[Any]]]
  def act() {
    loop {
      react {
        case LockClassLoad(cName) => {
          if(locked.containsKey(cName)){
           // println("Conflict locked "+cName+"-"+locked.keySet().size()+"-"+locked.get(cName).size)
            locked.put(cName,locked.get(cName)++List(sender))
          } else {
            locked.put(cName,List())
            reply(true)
          }
        }
        case UnlockClassLoad(cName) => {
          if(locked.containsKey(cName)){
           // println("Unlock "+cName)
            locked.get(cName).foreach(l => l ! true)
            locked.remove(cName)
            
          }
        }
        case KILL() => exit()
        case _ => println("RTFM")
      }
    }
  }


}
