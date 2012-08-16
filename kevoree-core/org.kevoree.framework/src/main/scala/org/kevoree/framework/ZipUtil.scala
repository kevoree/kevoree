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
package org.kevoree.framework

import java.io.ByteArrayOutputStream
import java.util.zip.{Inflater, Deflater}
import org.slf4j.LoggerFactory

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 13/04/12
 * Time: 13:39
 */

object ZipUtil {

  private val logger = LoggerFactory.getLogger(this.getClass.getName)

    def compressByteArray(input: Array[Byte]): Array[Byte] = {
      val compressor  = new Deflater()
      compressor.setLevel(Deflater.BEST_COMPRESSION)
      compressor.setInput(input)
      compressor.finish()
      val bos = new ByteArrayOutputStream(input.length)
      val buf = new Array[Byte](1024)
      while (!compressor.finished) {
        val count = compressor.deflate(buf)
        bos.write(buf, 0, count)
      }
      try {
        bos.close()
        bos.flush()
      }
      catch {
        case _ => {
          None
        }
      }
        bos.toByteArray
    }

    def uncompressByteArray(compressedData: Array[Byte]): Array[Byte] = {
      val decompressor = new Inflater()
      decompressor.setInput(compressedData)
      val bos = new ByteArrayOutputStream(compressedData.length)
      val buf: Array[Byte] = new Array[Byte](1024)
      while (!decompressor.finished) {
        try {
          val count: Int = decompressor.inflate(buf)
          bos.write(buf, 0, count)
        }
        catch {
          case _@ex => {
            logger.debug("Unable to inflate stream", ex)
            None
          }
        }
      }
      try {
        bos.close()
        bos.flush()
      }
      catch {
        case _ => {
          None
        }
      }
      bos.toByteArray
    }

}