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
import java.util.concurrent.{Semaphore, Callable}
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 19/03/12
 * Time: 14:53
 */

class KevoreeLocalLoader(classpathResources: KevoreeLazyJarResources, kcl: KevoreeJarClassLoader) extends ProxyClassLoader /*with DaemonActor*/ {

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

  class AcquireLockCallable(className: String) extends Callable[Semaphore] {
    def call(): Semaphore = {
      if (locked.containsKey(className)) {
        val tuple = locked.get(className)
        locked.put(className,(tuple._1,(tuple._2+1)))
        tuple._1
      } else {
        val obj = new Semaphore(0)
        locked.put(className, (obj,1))
        null //don't block first thread
      }
    }
  }


  def acquireLock(className: String) {
    val call = new AcquireLockCallable(className)
    try {
      val obj: Semaphore = KCLScheduler.getScheduler.submit(call).get()
      if (obj != null){
        logger.debug("Lock KCL to avoid concurrency {}",className)
        obj.acquire()
      }
    } catch {
      case ie: java.lang.InterruptedException =>
      case _@e => {
        logger.error("Error while sync " + className + " KCL thread : {}", Thread.currentThread().getName, e)
      }
    }
  }

  class ReleaseLockCallable(className: String) extends Runnable {
    def run() {
      if (locked.containsKey(className)) {
        val lobj = locked.get(className)
        if(lobj == 1){
          locked.remove(className)
        } else {
          lobj._1.release()
        }
      }
    }
  }

  private val logger = LoggerFactory.getLogger(this.getClass)

  def releaseLock(className: String) {
    try {
      val call = new ReleaseLockCallable(className)
      KCLScheduler.getScheduler.submit(call).get()
    } catch {
      case ie: java.lang.InterruptedException =>
      case _@e => {
        logger.error("Error while sync " + className + " KCL thread : {}", Thread.currentThread().getName, e)
      }
    }

  }

  private val locked = new java.util.HashMap[String, Tuple2[Semaphore,Int]]

}
