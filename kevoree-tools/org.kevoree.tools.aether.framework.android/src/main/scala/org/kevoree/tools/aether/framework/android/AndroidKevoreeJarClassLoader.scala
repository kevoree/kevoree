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

import android.content.Context
import java.io.{BufferedOutputStream, FileOutputStream, File, InputStream}
import org.kevoree.kcl.{KevoreeResourcesLoader, KevoreeJarClassLoader}
import org.slf4j.LoggerFactory
import java.lang.Class
import dalvik.system.{DexFile, DexClassLoader}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 05/02/12
 * Time: 17:57
 */

class AndroidKevoreeJarClassLoader(gkey: String, ctx: android.content.Context, parent: ClassLoader) extends KevoreeJarClassLoader {

  val logger = LoggerFactory.getLogger(this.getClass)

  private var odexPaths = List[File]()

  val selfPointer = this


  override def setLazyLoad(lazyload: Boolean) {
    super.setLazyLoad(true)
  }

  private class KevoreeDexClassLoader(c1:String,c2:String,c3:String,jarKCL : KevoreeJarClassLoader) {

    val dexFile = DexFile.loadDex(c1,c2,0)

    /*def internalLoad(name:String) : Class[_] = {
      super[ClassLoader].loadClass(name)
    }

    override def loadClass(p1: String) : Class[_] = {
      selfPointer.loadClass(p1)
    }

    override def findClass(className : String) : Class[_] = {
       dexFile.loadClass(className,jarKCL)
    }

     */

    def tryLoadClass(className : String) : Class[_] = {
      dexFile.loadClass(className,jarKCL)
    }


  }





  /* Constructor */
  addSpecialLoaders(new KevoreeResourcesLoader(".jar") {
    def doLoad(key: String, stream: InputStream) {
      //logger.debug("Ignore class => "+key)
      //NOOP
    }
  })
  addSpecialLoaders(new KevoreeResourcesLoader(".class") {
    def doLoad(key: String, stream: InputStream) {
      //logger.debug("Ignore class => "+key)
      //NOOP
    }
  })
  addSpecialLoaders(new KevoreeResourcesLoader(".dex") {
    def doLoad(key: String, stream: InputStream) {
      //logger.debug("Found DEX file => "+key)
      declareLocalDexClassLoader(stream, gkey + "_" + key)
    }
  })
  /* End Constructor */

  private var subDexClassLoader: List[KevoreeDexClassLoader] = List()

  def declareLocalDexClassLoader(dexStream: InputStream, idName: String) {
    logger.debug("Begin declare subClassLoader v2 " + idName)
    //val cleanName = System.currentTimeMillis() + idName.replaceAll(File.separator, "_").replaceAll(":", "_")

    val cleanName : String = (if(idName.contains("SNAPSHOT")){
      System.currentTimeMillis() + idName.replaceAll(File.separator, "_").replaceAll(":", "_")
    } else {
      idName.replaceAll(File.separator, "_").replaceAll(":", "_")
    })
        /*
    val dexInternalStoragePath = new File(ctx.getDir("dex", Context.MODE_WORLD_WRITEABLE), cleanName)
    if (!dexInternalStoragePath.exists()){
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
    }        */

    val dexOptStoragePath = ctx.getDir("kevCache", Context.MODE_WORLD_WRITEABLE)

    val outDex = new File(dexOptStoragePath,cleanName+".dex")

    //logger.info("create ODEX dir = "+dexOptStoragePath.mkdirs()+"-"+dexOptStoragePath.isDirectory)
    odexPaths = odexPaths ++ List(dexOptStoragePath)
    dexOptStoragePath.mkdirs()
    val newDexCL = new KevoreeDexClassLoader(getLoadedURLs.get(0).getPath/*dexInternalStoragePath.getAbsolutePath*/, outDex.getAbsolutePath/*dexOptStoragePath.getAbsolutePath*/, null, this)
    subDexClassLoader = subDexClassLoader ++ List(newDexCL)
  }

  override def internal_defineClass(className: String, bytes: Array[Byte]) : Class[_] = {
    subDexClassLoader.foreach {
      subCL =>
        try {
          val classLoaded = subCL.tryLoadClass(className)
          if(classLoaded != null){
            return classLoaded
          }

          //return subCL.internalLoad(className)
        } catch {
          case nf: ClassNotFoundException =>
        }
    }
    null
  }

  override def loadClassBytes( className : String) : Array[Byte] = {
      "dummy".getBytes
  }




            /*
  override def callSuperConcreteLoader(className: String, resolveIt: Boolean): Class[_] = {
    //logger.debug("Try to load " + className)
    subDexClassLoader.foreach {
      subCL =>
        try {
          return subCL.internalLoad(className)
        } catch {
          case nf: ClassNotFoundException =>
        }
    }
    null
    //throw new ClassNotFoundException(className)
  }   */



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
