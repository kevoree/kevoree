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
import org.kevoree.api.PrimitiveCommand
import org.kevoree.library.defaultNodeTypes.context.KevoreeDeployManager

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 26/01/12
 * Time: 16:35
 */

class AddDeployUnit(val du: DeployUnit, val bs: org.kevoree.api.Bootstraper): PrimitiveCommand {

    var logger = LoggerFactory.getLogger(this.javaClass)!!

    override fun undo() {
        bs.getKevoreeClassLoaderHandler().removeDeployUnitClassLoader(du)
        KevoreeDeployManager.clearRef(du.javaClass.getName(), CommandHelper.buildKEY(du))
    }

    override fun execute(): Boolean {
        try {
            if (bs.getKevoreeClassLoaderHandler().getKevoreeClassLoader(du) == null) {
                val new_kcl = bs.getKevoreeClassLoaderHandler().installDeployUnit(du)
                if (new_kcl != null) {
                    KevoreeDeployManager.putRef(du.javaClass.getName(), CommandHelper.buildKEY(du), new_kcl)
                    return true
                } else {
                    return false
                }
            } else {
                return true
            }
        } catch(e: Exception) {
            logger.debug("error ", e); return false
        }
    }
}