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

import android.util.Log;
import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

import java.io.IOException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 29/02/12
 * Time: 18:19
 */
public class TinyKCLDexClassLoader extends ClassLoader {

    private ClassLoader clusterCL = null;
    private DexFile df = null;

    public TinyKCLDexClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent, ClassLoader clusterCL) throws IOException {
        super(parent);
        this.clusterCL = clusterCL;
        df = DexFile.loadDex(dexPath, preparePath(dexPath,optimizedDirectory),0);
    }

    public String preparePath(String dexPath, String optimizedDirectory) {
        StringBuilder newStr = new StringBuilder(80);
        newStr.append(optimizedDirectory);
        if (!optimizedDirectory.endsWith("/")) {
            newStr.append("/");
        }
        String sourceFileName;
        int lastSlash = dexPath.lastIndexOf("/");
        if (lastSlash < 0)
            sourceFileName = dexPath;
        else
            sourceFileName = dexPath.substring(lastSlash + 1);
        int lastDot = sourceFileName.lastIndexOf(".");
        if (lastDot < 0)
            newStr.append(sourceFileName);
        else
            newStr.append(sourceFileName, 0, lastDot);
        newStr.append(".dex");
        return newStr.toString();
    }


    public Class internalLoad(String clazzName) throws ClassNotFoundException {
        return super.loadClass(clazzName);
    }

    @Override
    public Class<?> loadClass(String s) throws ClassNotFoundException {
        return clusterCL.loadClass(s);
    }

    @Override
    protected URL findResource(String name) {
        try {
            return super.findResource(name);
        } catch (Exception e) {
            Log.i("KCL not find resources ", name);
        }
        return null;
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        return df.loadClass(className,this);
    }
}
