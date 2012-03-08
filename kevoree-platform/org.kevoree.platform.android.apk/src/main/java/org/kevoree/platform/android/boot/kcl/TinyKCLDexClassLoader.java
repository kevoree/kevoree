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
package org.kevoree.platform.android.boot.kcl;

import dalvik.system.DexClassLoader;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 29/02/12
 * Time: 18:19
 */
public class TinyKCLDexClassLoader extends DexClassLoader {

    private  ClassLoader clusterCL = null;

    public TinyKCLDexClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent, ClassLoader clusterCL) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
        this.clusterCL = clusterCL;
    }

    public Class internalLoad(String clazzName) throws ClassNotFoundException {
      return super.loadClass(clazzName);
    }

    @Override
    public Class<?> loadClass(String s) throws ClassNotFoundException {
        return clusterCL.loadClass(s);
    }
}
