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

import java.io.*;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 07/03/12
 * Time: 09:50
 */
public class BuildSub implements Runnable{

    private Context ctx;
    private ClassLoader parentCL;
    private String name;
    private TinyClusterKCLDexClassLoader clusterKCL;

    public BuildSub(Context ctx,ClassLoader parentCL,String name,TinyClusterKCLDexClassLoader clusterKCL){
        this.ctx = ctx;
        this.parentCL = parentCL;
        this.name = name;
        this.clusterKCL = clusterKCL;
    }

    @Override
    public void run() {
        BufferedOutputStream dexWriter = null;
        InputStream st  = null;
        try
        {
            st  = this.getClass().getClassLoader().getResourceAsStream("boot/"+name+"/classes.dex");

            String cleanName = "";
            if(name.contains("scala.library.android")){
                 cleanName = name.replaceAll(File.separator, "_").replaceAll(":", "_")+".dex";
            } else {
                 cleanName = System.currentTimeMillis() + name.replaceAll(File.separator, "_").replaceAll(":", "_")+".dex";
            }

            File dexInternalStoragePath = new File(ctx.getDir("dex", Context.MODE_WORLD_WRITEABLE), cleanName);
            dexWriter = new BufferedOutputStream(new FileOutputStream(dexInternalStoragePath));
            byte[] b = new byte[1024*16];
            int len = 0;
            while ((len = st.read(b)) != -1)
                dexWriter.write(b, 0, len);

            dexWriter.flush();
            dexWriter.close();
            st.close();
            File dexOptStoragePath = ctx.getDir("odex" + System.currentTimeMillis(), Context.MODE_WORLD_WRITEABLE);
            dexOptStoragePath.mkdirs();
            TinyKCLDexClassLoader c = new TinyKCLDexClassLoader(dexInternalStoragePath.getAbsolutePath(), dexOptStoragePath.getAbsolutePath(), null, parentCL,clusterKCL);
            clusterKCL.addKCL(c);

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        }

        finally
        {
            try {
                if (dexWriter != null) {
                    dexWriter.close();
                }
                if (st != null) {
                    st.close();
                }
            } catch (IOException e) {
              //ignore
            }
        }

    }
}
