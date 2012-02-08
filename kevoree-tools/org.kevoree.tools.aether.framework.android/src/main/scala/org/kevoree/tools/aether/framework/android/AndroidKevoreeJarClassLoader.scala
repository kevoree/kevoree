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

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 05/02/12
 * Time: 17:57
 */

class AndroidKevoreeJarClassLoader(gkey : String,ctx: android.content.Context, parent: ClassLoader) extends KevoreeJarClassLoader {

  /* Constructor */
  addSpecialLoaders(new KevoreeResourcesLoader("class"){
    def doLoad(key: String, stream: InputStream) {
      //NOOP
    }
  })
  addSpecialLoaders(new KevoreeResourcesLoader("dex"){
    def doLoad(key: String, stream: InputStream) {
      declareLocalDexClassLoader(stream,gkey+"_"+key)
    }
  })
  /* End Constructor */

  private var subDexClassLoader: List[DexClassLoader] = List()

  def declareLocalDexClassLoader(dexStream: InputStream, idName: String) {
    val cleanName = idName.replace(File.separator, "_")
    val dexInternalStoragePath = new File(ctx.getDir("dex", Context.MODE_PRIVATE), cleanName)
    val dexWriter = new BufferedOutputStream(new FileOutputStream(dexInternalStoragePath))
    val b = new Array[Byte](8 * 1014)
    var len = 0;
    while (dexStream.available() > 0) {
      len = dexStream.read(b);
      if (len > 0) {
        dexWriter.write(b, 0, len);
      }
    }
    dexWriter.close()
    val dexOptStoragePath = new File(ctx.getDir("odex", Context.MODE_PRIVATE), cleanName)
    val newDexCL = new DexClassLoader(dexInternalStoragePath.getAbsolutePath, dexOptStoragePath.getAbsolutePath, null, parent)
    subDexClassLoader = subDexClassLoader ++ List(newDexCL)
  }

  override def callSuperConcreteLoader(className: String, resolveIt: Boolean) : Class[_] = {
    subClassLoaders.foreach {
      subCL =>
        try {
          return subCL.loadClass(className)
        } catch {
          case nf: ClassNotFoundException =>
        }
    }
    throw new ClassNotFoundException(className)
  }





}
