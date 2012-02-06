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
package org.kevoree.api.service.core.classloading;

import org.kevoree.DeployUnit;
import org.kevoree.extra.jcl.KevoreeJarClassLoader;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 04/02/12
 * Time: 18:24
 * To change this template use File | Settings | File Templates.
 */
public interface KevoreeClassLoaderHandler {

    public KevoreeJarClassLoader installDeployUnit(DeployUnit du);

    public KevoreeJarClassLoader installDeployUnit(DeployUnit du ,File srcFile);

    public KevoreeJarClassLoader getKevoreeClassLoader(DeployUnit du);

    public void removeDeployUnitClassLoader(DeployUnit du);

    public File getCacheFile(DeployUnit du);

}
