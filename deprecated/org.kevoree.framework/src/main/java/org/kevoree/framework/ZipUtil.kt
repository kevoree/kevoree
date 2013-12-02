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

package org.kevoree.framework

import java.io.ByteArrayOutputStream
import java.util.zip.Inflater
import java.util.zip.Deflater

object ZipUtil {

    fun compressByteArray(input: ByteArray): ByteArray {
        val compressor = Deflater()
        compressor.setLevel(Deflater.BEST_COMPRESSION)
        compressor.setInput(input)
        compressor.finish()
        val bos = ByteArrayOutputStream(input.size)
        val buf = ByteArray(1024)
        while (!compressor.finished()) {
            val count = compressor.deflate(buf)
            bos.write(buf, 0, count)
        }
        try {
            bos.close()
            bos.flush()
        }
        catch(e: Exception) {
            null
        }
        return bos.toByteArray()
    }

    fun uncompressByteArray(compressedData: ByteArray): ByteArray {
        val decompressor = Inflater()
        decompressor.setInput(compressedData)
        val bos = ByteArrayOutputStream(compressedData.size)
        val buf = ByteArray(1024)
        while (!decompressor.finished()) {
            try {
                val count: Int = decompressor.inflate(buf)
                bos.write(buf, 0, count)
            }
            catch (e: Exception) {
                //return null
            }
        }
        try {
            bos.close()
            bos.flush()
        }
        catch (e: Exception) {
            //return null
        }
        return bos.toByteArray()
    }

}