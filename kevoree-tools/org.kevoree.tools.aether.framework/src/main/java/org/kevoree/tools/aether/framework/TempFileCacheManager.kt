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
package org.kevoree.tools.aether.framework

import org.slf4j.LoggerFactory
import org.sonatype.aether.artifact.Artifact
import org.kevoree.framework.FileNIOHelper
import java.util.Random
import java.io.File
import java.io.FileInputStream

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 08/03/12
 * Time: 10:28
 */

trait TempFileCacheManager {

    fun getFileExtension(jarArtifact: Artifact): String {
        val posPoint = jarArtifact.getFile()!!.getName().lastIndexOf('.')
        if (0 < posPoint && posPoint <= jarArtifact.getFile()!!.getName().length() - 2 ) {
            return jarArtifact.getFile()!!.getName().substring(posPoint);
        }
        return ""
    }

    fun installInCache(jarArtifact: Artifact): File? {
        val logger = LoggerFactory.getLogger(this.javaClass)!!
        try {
            val random = Random()
            val lastTempFile = File.createTempFile(jarArtifact.getArtifactId() + random.nextInt() + "", getFileExtension(jarArtifact))
            lastTempFile.deleteOnExit()
            val jarStream = FileInputStream(jarArtifact.getFile()!!);
            FileNIOHelper.copyFile(jarStream, lastTempFile)
            jarStream.close()
            lastTempFile.setLastModified(jarArtifact.getFile()!!.lastModified())
            lastTempFile.setExecutable(jarArtifact.getFile()!!.canExecute())
            lastTempFile.setReadable(jarArtifact.getFile()!!.canRead())
            lastTempFile.setWritable(jarArtifact.getFile()!!.canWrite())
            logger.debug("Cache File for " + jarArtifact.getArtifactId() + " - " + lastTempFile.getAbsolutePath())
            return lastTempFile
        } catch(e: Exception) {
            logger.debug("Error while cahcing file")
            if (jarArtifact != null) {
                return jarArtifact.getFile()
            } else {
                return null
            }
        }
    }


}
