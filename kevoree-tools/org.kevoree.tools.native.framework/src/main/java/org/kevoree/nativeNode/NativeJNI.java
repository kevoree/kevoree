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
package org.kevoree.nativeNode;

import java.io.*;
import java.util.EventObject;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 01/08/12
 * Time: 16:27
 * To change this template use File | Settings | File Templates.
 */
public class NativeJNI extends EventObject implements NativePortEvent{

    public native int init(int key,int port_event);
    public native boolean register();
    public native int start(int key,String path);
    public native int stop(int key);
    public native int update(int key);
    public native int create_input(int key,String name);
    public native int create_output(int key,String name);
    public native int enqueue(int key,int port,String msg);

    private Handler handler=null;

    public NativeJNI(Handler obj)
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
                deleteOldFile(folder);
            }
            folder.mkdirs();


            String r = ""+new Random().nextInt(800);
            String absolutePath = copyFileFromStream(getPath("native.so"), folder.getAbsolutePath(),"libnative"+r+""+ getExtension());

            System.load(absolutePath);

            return absolutePath;
        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }



    /* Utility fonctions */
    public static void deleteOldFile(File folder) {
        if (folder.isDirectory()) {
            for (File f : folder.listFiles()) {
                if (f.isFile()) {
                    f.delete();
                } else {
                    deleteOldFile(f);
                }
            }
        }
        folder.delete();
    }

    public static String getExtension() {
        if (System.getProperty("os.name").toLowerCase().contains("nux")) {
            return ".so";
        }
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            return ".dynlib";
        }
        return null;
    }

    public static String getPath(String lib) {
        if (System.getProperty("os.name").toLowerCase().contains("nux")) {
            if (is64()) {
                return "nix64/"+lib;
            } else {
                return "nix32/"+lib;
            }
        }
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            return "osx/"+lib;
        }
        return null;
    }

    public static boolean is64() {
        String os = System.getProperty("os.arch").toLowerCase();
        return (os.contains("64"));
    }

    public static String copyFileFromStream(String inputFile, String path, String targetName) throws IOException {
        InputStream inputStream = NativeJNI.class.getClassLoader().getResourceAsStream(inputFile);
        if (inputStream != null) {
            File copy = new File(path + File.separator + targetName);
            copy.deleteOnExit();
            OutputStream outputStream = new FileOutputStream(copy);
            byte[] bytes = new byte[1024];
            int length = inputStream.read(bytes);

            while (length > -1) {
                outputStream.write(bytes, 0, length);
                length = inputStream.read(bytes);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();

            return copy.getAbsolutePath();
        }
        return null;
    }

    @Override
    public String getMessage() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
