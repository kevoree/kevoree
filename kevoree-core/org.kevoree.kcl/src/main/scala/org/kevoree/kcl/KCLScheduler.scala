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
package org.kevoree.kcl

import java.util.concurrent.{ThreadFactory, Executors}

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 20/07/12
 * Time: 16:01
 */

object KCLScheduler {

  lazy val scheduler = {
    val pool = Executors.newSingleThreadExecutor(new ThreadFactory() {
      val s = System.getSecurityManager
      val group = if (s != null) {
        s.getThreadGroup
      } else {
        Thread.currentThread().getThreadGroup
      }

      def newThread(p1: Runnable) = {
        val t = new Thread(group, p1, "KCL_Scheduler")
        if (!t.isDaemon) {
          t.setDaemon(true)
        }
        if (t.getPriority != Thread.NORM_PRIORITY) {
          t.setPriority(Thread.NORM_PRIORITY)
        }
        t
      }
    })
    /*  Runtime.getRuntime.addShutdownHook(new Thread(){
  override def run() {
    pool.shutdownNow()
  }
})    */
    pool
  }

  def getScheduler = {
    scheduler
  }

}
