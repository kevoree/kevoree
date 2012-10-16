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
package org.kevoree.tools.nativeN;



import org.kevoree.tools.nativeN.api.NativeEventPort;
import org.kevoree.tools.nativeN.utils.FileManager;
import org.kevoree.tools.nativeN.utils.SystemHelper;

import java.io.*;
import java.util.EventObject;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 01/08/12
 * Time: 16:27
 *
 */
public class NativeJNI extends AbstractNativeJNI implements NativeEventPort {

    protected native int init(int key,int port_event);
    protected native boolean register();
    protected native int start(int key,String path);
    public native int stop(int key);
    protected native int update(int key);
    protected native int create_input(int key,String name);
    protected native int create_output(int key,String name);
    protected native int enqueue(int key,int port,String msg);


    private NativeManager handler=null;

    public NativeJNI(NativeManager obj)
    {
        super(obj);
        this.handler =obj;
    }

    public void dispatchEvent(String queue,String evt)
    {
        handler.fireEvent(this,queue,evt);
    }

    public String configureCL()
    {
        try
        {
            File folder = new File(System.getProperty("java.io.tmpdir") + File.separator + "native");
            if (folder.exists())
            {
                FileManager.deleteOldFile(folder);
            }
            folder.mkdirs();
            // todo
            String r = ""+new Random().nextInt(800);
            String absolutePath = FileManager.copyFileFromStream(SystemHelper.getPath("native.so"), folder.getAbsolutePath(),"libnative"+r+""+ SystemHelper.getExtension());
            System.out.println("Loading "+absolutePath);
            System.load(absolutePath);

            return absolutePath;
        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }


    @Override
    public String getMessage() {
        return "todo";
    }
}
