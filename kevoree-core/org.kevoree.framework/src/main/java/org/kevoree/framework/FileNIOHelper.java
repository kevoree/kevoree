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
