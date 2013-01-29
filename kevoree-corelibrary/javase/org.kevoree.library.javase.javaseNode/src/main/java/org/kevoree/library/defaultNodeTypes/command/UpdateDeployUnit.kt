package org.kevoree.library.defaultNodeTypes.command

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

import org.kevoree.DeployUnit
import org.slf4j.LoggerFactory
import java.util.Random
import org.kevoree.kcl.KevoreeJarClassLoader

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 26/01/12
 * Time: 16:35
 */

class UpdateDeployUnit(val du: DeployUnit, val bs: org.kevoree.api.Bootstraper): EndAwareCommand {

    var logger = LoggerFactory.getLogger(this.javaClass)!!
    var lastKCL: KevoreeJarClassLoader? = null
    var random = Random()

    override fun undo() {
        if(lastKCL != null){
            bs.getKevoreeClassLoaderHandler().removeDeployUnitClassLoader(du)
            bs.getKevoreeClassLoaderHandler().attachKCL(du, lastKCL)
        }
    }

    override fun execute(): Boolean {
        try {
            lastKCL = bs.getKevoreeClassLoaderHandler().getKevoreeClassLoader(du)
            bs.getKevoreeClassLoaderHandler().removeDeployUnitClassLoader(du)
            bs.getKevoreeClassLoaderHandler().installDeployUnit(du)
            return true
        } catch (e: Exception) {
            logger.debug("error ", e);return false
        }
    }

    override fun doEnd() {
        lastKCL = null
    }
}