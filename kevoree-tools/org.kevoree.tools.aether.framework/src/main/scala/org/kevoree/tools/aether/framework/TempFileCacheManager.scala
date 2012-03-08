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
import util.Random
import java.io.{FileInputStream, File}
import org.kevoree.framework.FileNIOHelper

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 08/03/12
 * Time: 10:28
 */

trait TempFileCacheManager {

  private val random = new Random
  private val logger = LoggerFactory.getLogger(this.getClass)

  def installInCache(jarArtifact: Artifact): File = {
    try {
      val lastTempFile = File.createTempFile(random.nextInt() + "", ".jar")
      lastTempFile.deleteOnExit()
      val jarStream = new FileInputStream(jarArtifact.getFile);
      FileNIOHelper.copyFile(jarStream, lastTempFile)
      jarStream.close()
      logger.debug("Cache File for " + jarArtifact.getArtifactId + " - " + lastTempFile.getAbsolutePath)
      lastTempFile
    } catch {
      case _@e => {
        logger.debug("Error while cahcing file")
        if (jarArtifact != null) {
          jarArtifact.getFile
        } else {
          null
        }
      }
    }
  }


}
