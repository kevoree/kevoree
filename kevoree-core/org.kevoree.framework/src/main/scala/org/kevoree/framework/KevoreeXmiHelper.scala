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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.framework

import org.kevoree.ContainerRoot
import java.util.zip.{Deflater, Inflater}
import org.slf4j.LoggerFactory
import java.io._
import org.kevoree.loader.ContainerRootLoader
import org.kevoree.serializer.ModelSerializer
<<<<<<< HEAD
=======
import xml.PrettyPrinter
>>>>>>> commit

object KevoreeXmiHelper {

  val logger = LoggerFactory.getLogger(this.getClass)

  def save(uri: String, root: ContainerRoot) = {
    val serializer = new ModelSerializer
    val result = serializer.serialize(root)
    val pp = new PrettyPrinter(3000,1)
    val fileWrite = new FileWriter(uri)
    fileWrite.append(pp.format(serializer.serialize(root)))

  }


  def load(uri: String): ContainerRoot = {
    logger.debug("load model from => " + uri)
    val localModel = ContainerRootLoader.loadModel(new File(uri));
    localModel match {
      case Some(m) => m
      case None => println("Model not loaded!"); null
    }

  }

  def loadStream(input: InputStream): ContainerRoot = {
    val localModel = ContainerRootLoader.loadModel(input);
    localModel match {
      case Some(m) => m
      case None => println("Model not loaded!"); null
    }

  }

  def saveStream(output: OutputStream, root: ContainerRoot) : Unit = {
    val serializer = new ModelSerializer
    val result = serializer.serialize(root)
    val pr = new PrintWriter(output)
    pr.print(result)
    pr.flush()
  }

  def saveCompressedStream(output: OutputStream, root: ContainerRoot) : Unit = {
    val modelStream = new ByteArrayOutputStream()
    saveStream(modelStream, root)
    val compressor = new Deflater()
    compressor.setLevel(Deflater.BEST_COMPRESSION)
    compressor.setInput(modelStream.toByteArray)
    compressor.finish()
    val buf = new Array[Byte](1024)
    while (!compressor.finished()) {
      val count = compressor.deflate(buf)
      output.write(buf, 0, count)
    }
    output.flush()
  }

  def loadCompressedStream(input: InputStream): ContainerRoot = {
    val decompressor = new Inflater()
    val inputData: Array[Byte] = Stream.continually(input.read).takeWhile(-1 !=).map(_.toByte).toArray
    decompressor.setInput(inputData)
    val bos = new ByteArrayOutputStream(inputData.length)
    val buf = new Array[Byte](1024)
    while (!decompressor.finished()) {
      val count = decompressor.inflate(buf)
      bos.write(buf, 0, count)
    }
    val inputS = new ByteArrayInputStream(bos.toByteArray)
    loadStream(inputS)
  }


}
