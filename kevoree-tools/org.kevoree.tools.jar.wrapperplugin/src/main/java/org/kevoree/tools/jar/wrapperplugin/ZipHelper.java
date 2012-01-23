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
package org.kevoree.tools.jar.wrapperplugin;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 15/12/11
 * Time: 10:59
 * To change this template use File | Settings | File Templates.
 */
public class ZipHelper {

    public static void createParentDirs(File file) throws IOException {
        File parent = file.getCanonicalFile().getParentFile();
        if (parent == null) {
          /*
           * The given directory is a filesystem root. All zero of its ancestors
           * exist. This doesn't mean that the root itself exists -- consider x:\ on
           * a Windows machine without such a drive -- or even that the caller can
           * create it, but this method makes no such guarantees even for non-root
           * files.
           */
          return;
        }
        parent.mkdirs();
        if (!parent.isDirectory()) {
          throw new IOException("Unable to create parent directories of " + file);
        }
      }

    public static void unzipToTempDir(File inputWar, File outputDir) {
        try {
            FileInputStream inputWarST = new FileInputStream(inputWar);
            ZipInputStream zis = new ZipInputStream(inputWarST);
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    new File(outputDir.getAbsolutePath() + File.separator + entry.getName()).mkdirs();
                } else {
                    File targetFile = new File(outputDir + File.separator + entry.getName());
                    createParentDirs(targetFile);
                    if (!targetFile.exists()) {
                        targetFile.createNewFile();
                    }

                    BufferedOutputStream outputEntry = new BufferedOutputStream(new FileOutputStream(targetFile));
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while (zis.available() > 0) {
                        len = zis.read(buffer);
                        if (len > 0) {
                            outputEntry.write(buffer, 0, len);
                        }
                    }
                    outputEntry.flush();
                    outputEntry.close();
                }
            }
            zis.close();
            inputWarST.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
