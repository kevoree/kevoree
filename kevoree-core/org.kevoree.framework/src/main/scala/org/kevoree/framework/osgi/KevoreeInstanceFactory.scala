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
package org.kevoree.framework.osgi

import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 19/12/11
 * Time: 10:34
 * To change this template use File | Settings | File Templates.
 */

trait KevoreeInstanceFactory {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val instancesCache = new java.util.HashMap[String, org.kevoree.framework.osgi.KevoreeInstanceActivator]()

  def registerInstance(instanceName: String, nodeName: String): org.kevoree.framework.osgi.KevoreeInstanceActivator = {
    val newInstance = createInstanceActivator
    newInstance.setInstanceName(instanceName)
    newInstance.setNodeName(nodeName)
    instancesCache.put(instanceName, newInstance)
    logger.debug("TypeCache " + this.getClass.getName + " has " + instancesCache.keySet().size() + " instances")
    newInstance
  }

  def remove(instanceName: String): org.kevoree.framework.osgi.KevoreeInstanceActivator = {
    var removed: org.kevoree.framework.osgi.KevoreeInstanceActivator = null
    if (instancesCache.containsKey(instanceName)) {
      removed = instancesCache.get(instanceName)
      instancesCache.remove(instanceName)
    }
    logger.debug("TypeCache " + this.getClass.getName + " has " + instancesCache.keySet().size() + " instances")
    removed
  }

  def createInstanceActivator: org.kevoree.framework.osgi.KevoreeInstanceActivator


}