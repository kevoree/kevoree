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

import android.content.Context;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 29/02/12
 * Time: 18:18
 */
public class TinyKCL {

    private TinyClusterKCLDexClassLoader clusterKCL = new TinyClusterKCLDexClassLoader();
    private int waitTime =25;
    private  ExecutorService pool;

    public TinyClusterKCLDexClassLoader getClusterKCL() {
        return clusterKCL;
    }

    public void start(/*Activity act,*/Context ctx,ClassLoader parentCL)
    {
        //INIT BOOT SEQUENCE
       // pool = Executors.newFixedThreadPool(10);
        new BuildSub(ctx,parentCL,"scala.library.android.actor",clusterKCL).run();
        new BuildSub(ctx,parentCL,"scala.library.android.base",clusterKCL).run();
        new BuildSub(ctx,parentCL,"scala.library.android.collection.base",clusterKCL).run();
        new BuildSub(ctx,parentCL,"scala.library.android.collection.immutable",clusterKCL).run();
        new BuildSub(ctx,parentCL,"scala.library.android.collection.mutable",clusterKCL).run();
        new BuildSub(ctx,parentCL,"scala.library.android.collection.parallel",clusterKCL).run();
        new BuildSub(ctx,parentCL,"scala.library.android.runtime",clusterKCL).run();
        new BuildSub(ctx,parentCL,"scala.library.android.util",clusterKCL).run();
        new BuildSub(ctx,parentCL,"org.kevoree.platform.android.core",clusterKCL).run();
        new BuildSub(ctx,parentCL,"org.kevoree.tools.aether.framework.android",clusterKCL).run();
    }

    /*
    public  void waitTinyKCL(){
        try
        {
            Thread.sleep(waitTime);
            pool.shutdown();
            pool.awaitTermination(waitTime, TimeUnit.SECONDS);
        } catch (InterruptedException ignored)
        {
            Log.e("TinyKCL ExecutorService", ignored.getMessage());
        }
    }   */

    public void stop(){

    }
}
