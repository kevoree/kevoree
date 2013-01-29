package org.kevoree.library.defaultNodeTypes.context

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

import org.slf4j.LoggerFactory
import org.kevoree.DeployUnit
import org.kevoree.framework.AbstractNodeType
import java.util.ArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ConcurrentHashMap

object KevoreeDeployManager {


    val internalMap = ConcurrentHashMap<String, Any>()
    val logger = LoggerFactory.getLogger(this.javaClass)!!

    fun getRef(clazzName: String, name: String): Any? {
        return internalMap.get(clazzName + "/" + name)
    }

    fun putRef(clazzName: String, name: String, ref: Any) {
        internalMap.put(clazzName + "/" + name, ref)
    }

    fun clearRef(clazzName: String, name: String) {
        internalMap.remove(clazzName + "/" + name)
    }

    fun clearAll(nodeType: AbstractNodeType) {
        for( o in internalMap.values()){
            if(o is DeployUnit){
                val old_du =o  as DeployUnit
                //CLEANUP KCL CONTEXT
                if (nodeType.getBootStrapperService()!!.getKevoreeClassLoaderHandler().getKevoreeClassLoader(old_du) != null) {
                    logger.debug("Force cleanup unitName {}", old_du.getUnitName())
                }
            }
        }
    }

}


