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
package org.kevoree.framework.osgi

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

import org.slf4j.LoggerFactory
import java.util
import util.concurrent.Semaphore

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 19/12/11
 * Time: 10:34
 */

trait KevoreeInstanceFactory {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val instancesCache = new util.HashMap[String, org.kevoree.framework.osgi.KevoreeInstanceActivator]()
  private val sem = new Semaphore(1)

  def registerInstance(instanceName: String, nodeName: String): org.kevoree.framework.osgi.KevoreeInstanceActivator = {
    var newInstance: org.kevoree.framework.osgi.KevoreeInstanceActivator =     null
    try{
      sem.acquire()
      logger.debug("RegisterInstance -->TypeCache " + this.getClass.getName + " has " + instancesCache.keySet().size() + " instances")
      newInstance = createInstanceActivator
      newInstance.setInstanceName(instanceName)
      newInstance.setNodeName(nodeName)
      instancesCache.put(instanceName, newInstance)
    }catch {
      case e: Exception => logger.error("RegisterInstance ",e)
    }finally {
      sem.release()
    }
    newInstance
  }

  def remove(instanceName: String): org.kevoree.framework.osgi.KevoreeInstanceActivator = {
    var removed: org.kevoree.framework.osgi.KevoreeInstanceActivator = null
    try
    {
      sem.acquire()
      logger.debug("Remove -->TypeCache " + this.getClass.getName + " has " + instancesCache.keySet().size() + " instances")
      if (instancesCache.containsKey(instanceName)) {
        removed = instancesCache.get(instanceName)
        instancesCache.remove(instanceName)
      } else {
        removed = null
      }
    }catch {
      case e: Exception => logger.error("Remove -->TypeCache  ",e)
    } finally {
      sem.release()
    }
    removed
  }

  def createInstanceActivator: org.kevoree.framework.osgi.KevoreeInstanceActivator


}