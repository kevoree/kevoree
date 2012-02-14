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
package org.kevoree.tools.aether.framework.android

import dalvik.system.DexClassLoader
import android.content.Context
import java.io.{BufferedOutputStream, FileOutputStream, File, InputStream}
import org.kevoree.kcl.{KevoreeResourcesLoader, KevoreeJarClassLoader}
import org.slf4j.LoggerFactory
import java.lang.Class

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 05/02/12
 * Time: 17:57
 */

class AndroidKevoreeJarClassLoader(gkey: String, ctx: android.content.Context, parent: ClassLoader) extends KevoreeJarClassLoader {

  val logger = LoggerFactory.getLogger(this.getClass)

  private var odexPaths = List[File]()

  /* Constructor */
  addSpecialLoaders(new KevoreeResourcesLoader(".class") {
    def doLoad(key: String, stream: InputStream) {
      //logger.debug("Ignore class => "+key)
      //NOOP
    }
  })
  addSpecialLoaders(new KevoreeResourcesLoader(".dex") {
    def doLoad(key: String, stream: InputStream) {
      declareLocalDexClassLoader(stream, gkey + "_" + key)
    }
  })
  /* End Constructor */

  private var subDexClassLoader: List[DexClassLoader] = List()

  def declareLocalDexClassLoader(dexStream: InputStream, idName: String) {
    logger.debug("Begin declare subClassLoader " + idName)
    val cleanName = System.currentTimeMillis() + idName.replaceAll(File.separator, "_").replaceAll(":", "_")
    val dexInternalStoragePath = new File(ctx.getDir("dex", Context.MODE_WORLD_WRITEABLE), cleanName)
    logger.debug("File Create " + dexInternalStoragePath.getAbsolutePath)
    val dexWriter = new BufferedOutputStream(new FileOutputStream(dexInternalStoragePath))
    val b = new Array[Byte](2048)
    var len = 0;
    while (len != -1) {
      len = dexStream.read(b);
      if (len > 0) {
        dexWriter.write(b, 0, len);
      }
    }
    dexWriter.flush()
    dexWriter.close()
    val dexOptStoragePath = ctx.getDir("odex" + System.currentTimeMillis(), Context.MODE_WORLD_WRITEABLE)
    odexPaths = odexPaths ++ List(dexOptStoragePath)
    dexOptStoragePath.mkdirs()
    val newDexCL = new DexClassLoader(dexInternalStoragePath.getAbsolutePath, dexOptStoragePath.getAbsolutePath, null, parent)
    subDexClassLoader = subDexClassLoader ++ List(newDexCL)
  }

  override def callSuperConcreteLoader(className: String, resolveIt: Boolean): Class[_] = {
    logger.debug("Try to load " + className)
    subDexClassLoader.foreach {
      subCL =>
        try {
          return subCL.loadClass(className)
        } catch {
          case nf: ClassNotFoundException =>
        }
    }
    throw new ClassNotFoundException(className)
  }

  override def unload() {
    subDexClassLoader = null
    super.unload()
    odexPaths.foreach {
      odexp =>
        clearAll(odexp)
    }
    odexPaths = List()
  }


  def clearAll(f : File) {
    def deleteFile(dfile: File): Unit = {
      if (dfile.isDirectory) {
        val subfiles = dfile.listFiles
        if (subfiles != null)
          subfiles.foreach {
            f => deleteFile(f)
          }
      }
      dfile.delete
    }
    deleteFile(f)
  }

}
