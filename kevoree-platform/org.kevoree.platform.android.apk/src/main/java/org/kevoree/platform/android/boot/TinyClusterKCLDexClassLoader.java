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
package org.kevoree.platform.android.boot;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 29/02/12
 * Time: 18:33
 */
public class TinyClusterKCLDexClassLoader extends ClassLoader {

    private List<TinyKCLDexClassLoader> subs = new ArrayList<TinyKCLDexClassLoader>();

    public void addKCL(TinyKCLDexClassLoader sub){
        subs.add(sub);
    }

    @Override
    public Class<?> loadClass(String s) throws ClassNotFoundException {
        //Log.i("ClusterCL","Try to resolve Class "+s);
        for(TinyKCLDexClassLoader sub : subs){
            try {
                Class clazz = sub.internalLoad(s);
                if(clazz != null){
                    return clazz;
                }
            }   catch (Exception e){
              //SILENTLY IGNORE
            }
        }
        //Log.i("ClusterCL","Not resolved in cluster "+s);
        throw new ClassNotFoundException(s);
    }
}
