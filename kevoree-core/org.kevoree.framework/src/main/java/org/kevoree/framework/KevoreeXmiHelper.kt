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

import org.kevoree.ContainerRoot
import java.util.zip.Deflater
import java.util.zip.Inflater
import org.kevoree.loader.ModelLoader
import java.io.*
import org.kevoree.serializer.ModelSerializer
import org.kevoree.serializer.XMIModelSerializer
import org.kevoree.loader.XMIModelLoader

object KevoreeXmiHelper {

    fun save(uri: String, root: ContainerRoot) {
        //CHECK DIRECTORY CREATION
        val folderUri = if(uri.contains(File.separator)){
            uri.substring(0, uri.lastIndexOf(File.separator))
        } else {
            uri
        }
        val folder = File(folderUri)
        if (!folder.exists()){
            folder.mkdirs()
        }
        val serializer = XMIModelSerializer()
        val outputFile = File(uri);
        if(!outputFile.exists()) {
            outputFile.createNewFile()
        }
        val fop = FileOutputStream(outputFile)
        serializer.serialize(root, fop)
        fop.flush()
        fop.close()
    }

    fun saveToString(root: ContainerRoot, prettyPrint: Boolean): String {
        val serializer = XMIModelSerializer()
        val ba = ByteArrayOutputStream()
        val res = serializer.serialize(root, ba)
        ba.flush()
        val result = String(ba.toByteArray())
        ba.close()
        return result
    }

    fun loadString(model: String): ContainerRoot? {
        val loader = XMIModelLoader()
        val loadedElements = loader.loadModelFromString(model)
        if(loadedElements != null && loadedElements.size() > 0) {
            return loadedElements.get(0) as ContainerRoot;
        } else {
            return null;
        }
    }

    fun load(uri: String): ContainerRoot? {
        val loader = XMIModelLoader()
        val loadedElements = loader.loadModelFromPath(File(uri))
        if(loadedElements != null && loadedElements.size() > 0) {
            return loadedElements.get(0) as ContainerRoot;
        } else {
            return null;
        }
    }

    fun loadStream(input: InputStream): ContainerRoot? {
        val loader = XMIModelLoader()
        val loadedElements = loader.loadModelFromStream(input)
        if(loadedElements != null && loadedElements.size() > 0) {
            return loadedElements.get(0) as ContainerRoot;
        } else {
            return null;
        }

    }

    fun saveStream(output: OutputStream, root: ContainerRoot): Unit {
        val serializer = XMIModelSerializer()
        val result = serializer.serialize(root, output)
    }

    fun saveCompressedStream(output: OutputStream, root: ContainerRoot): Unit {
        val modelStream = ByteArrayOutputStream()
        saveStream(modelStream, root)
        output.write(ZipUtil.compressByteArray(modelStream.toByteArray()))
        output.flush()

    }

    fun loadCompressedStream( input: InputStream): ContainerRoot? {
        val inputS = ByteArrayInputStream(ZipUtil.uncompressByteArray(input.readBytes(input.available())))
        return loadStream(inputS)
    }


}
