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
package org.kevoree.api

import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import java.io.File
import org.kevoree.DeployUnit
import org.kevoree.ContainerRoot
import org.kevoree.kcl.KevoreeJarClassLoader
import org.kevoree.Instance

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 31/12/11
 * Time: 13:10
 */

trait BootstrapService {

    public fun get(du : DeployUnit) : KevoreeJarClassLoader?;

    public fun installDeployUnit(du : DeployUnit) : KevoreeJarClassLoader?;

    public fun removeDeployUnit(du : DeployUnit)

    public fun manualAttach(du : DeployUnit, kcl : KevoreeJarClassLoader);

    public fun recursiveInstallDeployUnit(du : DeployUnit) : KevoreeJarClassLoader?;

    public fun setOffline(offline:Boolean)

    public fun clear()

    public fun createInstance(instance : Instance) : Any?

}