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
package org.kevoree.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 05/12/11
 * Time: 22:06
 * To change this template use File | Settings | File Templates.
 */
public class FileNIOHelper {

    private static Logger logger = LoggerFactory.getLogger(FileNIOHelper.class);

    public static File resolveBundleJar(Long bundleID, File bundleWorkingDir) {
        String versionDef = "version0.0";
        try {
            File bundleRevisionCounter = new File(bundleWorkingDir.getAbsolutePath() + File.separator + "bundle" + bundleID + File.separator + "refresh.counter");
            if (bundleRevisionCounter.exists()) {
                FileReader fr = new FileReader(bundleRevisionCounter);
                char vers = (char) fr.read();
                versionDef = "version" + vers + "." + vers;
                fr.close();
            } /*else {
                logger.warn("revision file does not exist");
            }  */
            File jarFile = new File(bundleWorkingDir.getAbsolutePath() + File.separator + "bundle" + bundleID + File.separator + versionDef + File.separator + "bundle.jar");
            if (jarFile.exists()) {
                return jarFile;
            } else {
                logger.warn("File not found {}", jarFile.getAbsolutePath());
                return null;
            }
        } catch (Exception e) {
            logger.warn("Error while trying to get jar cache", e);
            return null;
        }
    }

    public static void copyFile(InputStream sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        ReadableByteChannel source = null;
        FileChannel destination = null;
        try {
            source = Channels.newChannel(sourceFile);
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, sourceFile.available());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static void unzipToTempDir(File inputWar, File outputDir, List<String> inclusions, List<String> exclusions) {
        try {
            FileInputStream inputWarST = new FileInputStream(inputWar);
            ZipInputStream zis = new ZipInputStream(inputWarST);
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    new File(outputDir.getAbsolutePath() + File.separator + entry.getName()).mkdirs();
                } else {
                    File targetFile = new File(outputDir + File.separator + entry.getName());
                    boolean filtered = false;
                    for (String ex : exclusions) {
                        filtered = filtered || targetFile.getName().endsWith(ex.trim());
                    }
                    for (String in : inclusions) {
                       // logger.debug("Check for incluseion => "+targetFile.getName()+"-"+in.trim()+"="+targetFile.getName().trim().equals(in.trim()));
                        if(targetFile.getName().endsWith(in.trim())||targetFile.getName().equals(in.trim())){
                            filtered = false;
                        }
                    }
                    if (!filtered) {
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
            }
            zis.close();
            inputWarST.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createParentDirs(File file) throws IOException {
        File parent = file.getCanonicalFile().getParentFile();
        if (parent == null) {
            return;
        }
        parent.mkdirs();
        if (!parent.isDirectory()) {
            throw new IOException("Unable to create parent directories of " + file);
        }
    }

}
