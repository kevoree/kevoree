package org.kevoree.tools.aether.framework.android

import dalvik.system.DexClassLoader
import org.kevoree.extra.jcl.KevoreeJarClassLoader
import android.content.Context
import java.io.{BufferedOutputStream, FileOutputStream, File, InputStream}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 05/02/12
 * Time: 17:57
 */

class AndroidKevoreeJarClassLoader(ctx: android.content.Context, parent: ClassLoader) extends KevoreeJarClassLoader {

  private var subDexClassLoader: List[DexClassLoader] = List()

  def declareLocalDexClassLoader(dexStream: InputStream, idName: String) {
    val cleanName = idName.replace(File.separator, "_")
    val dexInternalStoragePath = new File(ctx.getDir("dex", Context.MODE_PRIVATE), cleanName)
    val dexWriter = new BufferedOutputStream(new FileOutputStream(dexInternalStoragePath))
    val b = new Array[Byte](8*1014)
    var len = 0;
    while (dexStream.available() > 0) {
      len = dexStream.read(b);
      if (len > 0) {
        dexWriter.write(b, 0, len);
      }
    }
    dexWriter.close()
    val dexOptStoragePath = new File(ctx.getDir("odex", Context.MODE_PRIVATE), cleanName)
    val newDexCL = new DexClassLoader(dexInternalStoragePath.getAbsolutePath,dexOptStoragePath.getAbsolutePath,null,parent)

  }

  override def callSuperConcreteLoader(className: String, resolveIt: Boolean) = {
    //CALL LOCAL DEX CLASS LOADER
  }


}
