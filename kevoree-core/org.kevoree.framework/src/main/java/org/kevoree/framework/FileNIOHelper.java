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

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 05/12/11
 * Time: 22:06
 * To change this template use File | Settings | File Templates.
 */
public class FileNIOHelper {

    private static Logger logger = LoggerFactory.getLogger(FileNIOHelper.class);

    public static File resolveBundleJar(Bundle bundle, File bundleWorkingDir) {
        String versionDef = "version0.0";
        try {
            File bundleRevisionCounter = new File(bundleWorkingDir.getAbsolutePath() + File.separator + "bundle" + bundle.getBundleId() + File.separator + "refresh.counter");
            if (bundleRevisionCounter.exists()) {
                FileReader fr = new FileReader(bundleRevisionCounter);
                char vers = (char) fr.read();
                versionDef = "version"+vers + "." + vers;
                fr.close();
            } /*else {
                logger.warn("revision file does not exist");
            }  */
            File jarFile = new File(bundleWorkingDir.getAbsolutePath() + File.separator + "bundle" + bundle.getBundleId() + File.separator + versionDef + File.separator + "bundle.jar");
            if (jarFile.exists()) {
                return jarFile;
            } else {
                logger.warn("File not found {}",jarFile.getAbsolutePath());
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
}
