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

import android.content.Context;
import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 29/02/12
 * Time: 18:18
 */
public class TinyKCL {

    private TinyClusterKCLDexClassLoader clusterKCL = new TinyClusterKCLDexClassLoader();

    public TinyClusterKCLDexClassLoader getClusterKCL() {
        return clusterKCL;
    }

    public void start(/*Activity act,*/Context ctx,ClassLoader parentCL){
        try {
            buildSub(ctx,parentCL,"scala.library.android.actor");
            buildSub(ctx,parentCL,"scala.library.android.base");
            buildSub(ctx,parentCL,"scala.library.android.collection.base");
            buildSub(ctx,parentCL,"scala.library.android.collection.immutable");
            buildSub(ctx,parentCL,"scala.library.android.collection.mutable");
            buildSub(ctx,parentCL,"scala.library.android.collection.parallel");
            buildSub(ctx,parentCL,"scala.library.android.runtime");
            buildSub(ctx,parentCL,"scala.library.android.util");
            buildSub(ctx,parentCL,"org.kevoree.platform.android.core");
            buildSub(ctx,parentCL,"org.kevoree.tools.aether.framework.android");

            //INIT BOOT SEQUENCE


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop(){

    }

    public void buildSub(Context ctx,ClassLoader parentCL,String name) throws IOException {

        InputStream st = this.getClass().getClassLoader().getResourceAsStream("boot/"+name+"/classes.dex");
        String cleanName = System.currentTimeMillis() + name.replaceAll(File.separator, "_").replaceAll(":", "_")+".dex";

        File dexInternalStoragePath = new File(ctx.getDir("dex", Context.MODE_WORLD_WRITEABLE), cleanName);
        BufferedOutputStream dexWriter = new BufferedOutputStream(new FileOutputStream(dexInternalStoragePath));
        byte[] b = new byte[1024*16];
        int len = 0;
        while (len != -1) {
            len = st.read(b);
            if (len > 0) {
                dexWriter.write(b, 0, len);
            }
        }
        dexWriter.flush();
        dexWriter.close();
        File dexOptStoragePath = ctx.getDir("odex" + System.currentTimeMillis(), Context.MODE_WORLD_WRITEABLE);
        dexOptStoragePath.mkdirs();
        TinyKCLDexClassLoader c = new TinyKCLDexClassLoader(dexInternalStoragePath.getAbsolutePath(), dexOptStoragePath.getAbsolutePath(), null, parentCL,clusterKCL);
        clusterKCL.addKCL(c);
    }


}
