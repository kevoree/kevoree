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
package org.kevoree.tools.accesscontrol.framework.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 03/10/12
 * Time: 17:21
 * To change this template use File | Settings | File Templates.
 */
public class FileManager {


    public static byte[] load(InputStream reader) throws IOException {
        int c;
        ArrayList<Byte> tab = new ArrayList<Byte>();
        while((c = reader.read()) != -1) {
            tab.add((byte)c);
        }
        if (reader!=null)
            reader.close();
        return toByteArray(tab);
    }

    public static String copyFileFromStream( InputStream inputStream , String path, String targetName,boolean replace) throws IOException {

        if (inputStream != null) {
            File copy = new File(path + File.separator + targetName);
            copy.mkdirs();
            if(replace)
            {
                if(copy.exists()){
                    if(!copy.delete()){
                       throw new IOException("delete file "+copy.getPath());
                    }
                    if(!copy.createNewFile()){
                        throw new IOException("createNewFile file "+copy.getPath());
                    }
                }
            }
            //copy.deleteOnExit();
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
    public static byte[] toByteArray(List<Byte> in) {
        final int n = in.size();
        byte ret[] = new byte[n];
        for (int i = 0; i < n; i++) {
            ret[i] = in.get(i);
        }
        return ret;
    }

    public  static byte[] load(String pathfile)
    {
        File file = new File(pathfile);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        DataInputStream dis = null;
        ArrayList<Byte> tab = new ArrayList<Byte>();
        try
        {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            dis = new DataInputStream(bis);
            while (dis.available() != 0)
            {
                tab.add((byte)dis.read());
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                fis.close();
                bis.close();
                dis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return  toByteArray(tab);
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



    public static String copyFileFromStream(String inputFile, String path, String targetName) throws IOException {
        InputStream inputStream = FileManager.class.getClassLoader().getResourceAsStream(inputFile);
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

}
