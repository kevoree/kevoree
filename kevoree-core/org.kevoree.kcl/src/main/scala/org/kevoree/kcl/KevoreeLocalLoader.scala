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
package org.kevoree.kcl

import org.xeustechnologies.jcl.ProxyClassLoader
import java.lang.Class
import java.io.{ByteArrayInputStream, InputStream}
import actors.{OutputChannel, DaemonActor}
import java.util.concurrent.Callable
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 19/03/12
 * Time: 14:53
 */

class KevoreeLocalLoader(classpathResources: KevoreeLazyJarResources, kcl: KevoreeJarClassLoader) extends ProxyClassLoader /*with DaemonActor*/ {

  order = 1

  /*
class LoadClassCallable(className: String) extends Callable[Class[_]] {
 def call(): Class[_] = {
   var result = kcl.getLoadedClass(className)
   if (result == null) {
     val bytes = kcl.loadClassBytes(className)
     if (bytes != null) {
       result = kcl.internal_defineClass(className, bytes)
     }
   }
   result
 }
}

def loadClass(className: String, resolveIt: Boolean): Class[_] = {
 var result = kcl.getLoadedClass(className)
 if (result == null) {
   val bytes = kcl.loadClassBytes(className)
   if (bytes != null) {
     val call = new LoadClassCallable(className)
     println("Before" +className)
     result = KCLScheduler.getScheduler.submit[Class[_]](call).get()
     println("After "+result)
   }
 }
 result
}   */


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

  class AcquireLockCallable(className: String) extends Callable[Object] {
    def call(): Object = {
      if (locked.containsKey(className)) {
        var obj = locked.get(className)
        if (obj == null){
          obj = new Object()
          locked.put(className, obj)
        }

        obj
      } else {
        locked.put(className, null)
        null
      }
    }
  }

  var lock = new Object

  def acquireLock(className: String) {
    val call = new AcquireLockCallable(className)
    var obj: Object = null
    while ( {
      try {
        //logger.info("Will wait "+Thread.currentThread().getName)
        obj = KCLScheduler.getScheduler.submit(call).get()
      } catch {
        case ie : java.lang.InterruptedException =>
        case _ @ e => {
          obj = null
          logger.error("Error while sync "+className+" KCL thread : {}" ,Thread.currentThread().getName,e)
        }
      }
      obj != null
    }) {
      try {
        logger.debug("KCL sync lock for {}",className)
        obj.synchronized{
          obj.wait()
        }
      } catch {
        case _ @ e => logger.error("Error while sync KCL",e)
      }
    }
  }

  class ReleaseLockCallable(className: String) extends Callable[Object] {
    def call(): Object = {
      if (locked.containsKey(className)) {
        val lobj = locked.get(className)
        locked.remove(className)
        lobj
      } else {
        null
      }
    }
  }

  private val logger = LoggerFactory.getLogger(this.getClass)

  def releaseLock(className: String) {
    val call = new ReleaseLockCallable(className)
    val obj = KCLScheduler.getScheduler.submit(call).get()
    if (obj != null) {
      try {
        obj.synchronized{
          obj.notifyAll()
        }
      } catch {
        case ie : java.lang.InterruptedException =>
        case _ @ e => logger.error("Error while sync KCL",e)
      }

    }
  }


  /*
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
}       */


  private val locked = new java.util.HashMap[String, Object]
  /*
def act() {
 loop {
   react {
     case LockClassLoad(cName) => {
       if (locked.containsKey(cName)) {
         // println("Conflict locked "+cName+"-"+locked.keySet().size()+"-"+locked.get(cName).size)
         locked.put(cName, locked.get(cName) ++ List(sender))
       } else {
         locked.put(cName, List())
         reply(true)
       }
     }
     case UnlockClassLoad(cName) => {
       if (locked.containsKey(cName)) {
         // println("Unlock "+cName)
         locked.get(cName).foreach(l => l ! true)
         locked.remove(cName)

       }
     }
     case KILL() => exit()
     case _ => println("RTFM")
   }
 }
}   */


}
