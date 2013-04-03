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
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.tools.aether.framework.android

import android.content.Context
import org.kevoree.kcl.KevoreeJarClassLoader
import org.slf4j.LoggerFactory
import java.lang.Class
import org.kevoree.kcl.loader.KevoreeResourcesLoader
import java.util.ArrayList
import java.io.File
import dalvik.system.DexFile
import java.io.InputStream
import java.io.FileOutputStream
import java.io.BufferedOutputStream

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 05/02/12
 * Time: 17:57
 */

class AndroidKevoreeJarClassLoader(gkey: String, val ctx: android.content.Context, val _parent: ClassLoader): KevoreeJarClassLoader() {

    val logger = LoggerFactory.getLogger(this.javaClass)!!

    private var odexPaths = ArrayList<File>()

    val selfPointer = this

    public override fun loadClassBytes(className: String): ByteArray {
        return "dummy".getBytes()
    }

    override fun setLazyLoad(lazyload: Boolean) {
        super.setLazyLoad(true)
    }

    private class KevoreeDexClassLoader(c1: String, c2: String, c3: String?, val jarKCL: KevoreeJarClassLoader) {

        val dexFile = DexFile.loadDex(c1, c2, 0)
        fun tryLoadClass(className: String?): Class<out Any?>? {
            return dexFile!!.loadClass(className, jarKCL)
        }
    }


    AndroidKevoreeJarClassLoader(){
        /* Constructor */
        addSpecialLoaders(object : KevoreeResourcesLoader(".jar") {
            override fun doLoad(key: String?, stream: InputStream?) {
                //logger.debug("Ignore class => "+key)
                //NOOP
            }
        })
        addSpecialLoaders(object : KevoreeResourcesLoader(".class") {
            override fun doLoad(key: String?, stream: InputStream?) {
                //logger.debug("Ignore class => "+key)
                //NOOP
            }
        })
        addSpecialLoaders(object : KevoreeResourcesLoader(".dex") {
            override fun doLoad(key: String?, stream: InputStream?) {
                //logger.debug("Found DEX file => "+key)
                declareLocalDexClassLoader(stream, gkey + "_" + key)
            }
        })
    }



    private var subDexClassLoader = ArrayList<KevoreeDexClassLoader>()

    fun declareLocalDexClassLoader(dexStream: InputStream?, idName: String) {
        logger.debug("Begin declare subClassLoader v2 " + idName)
        val cleanName: String = (if (idName.contains("SNAPSHOT")) {
            System.currentTimeMillis().toString()+idName.replaceAll(File.separator, "_").replaceAll(":", "_")
        } else {
            idName.replaceAll(File.separator, "_").replaceAll(":", "_")
        })
        val dexOptStoragePath = ctx.getDir("kevCache", Context.MODE_WORLD_WRITEABLE)
        val outDex = File(dexOptStoragePath, cleanName+".dex")
        odexPaths.add(dexOptStoragePath!!)
        dexOptStoragePath!!.mkdirs()

        var newDexCL: KevoreeDexClassLoader? = null
        if (idName.contains("classes.dex")) {
            newDexCL = KevoreeDexClassLoader(getLoadedURLs().get(0).getPath()!!, outDex.getAbsolutePath(), null, this)
        } else {
            val dexInternalStoragePath = File(ctx.getDir("dex", Context.MODE_WORLD_WRITEABLE), cleanName)
            dexInternalStoragePath.deleteOnExit()
            if (!dexInternalStoragePath.exists()) {
                logger.debug("File Create " + dexInternalStoragePath.getAbsolutePath())
                val dexWriter = BufferedOutputStream(FileOutputStream(dexInternalStoragePath))
                val b = ByteArray(2048)
                var len = 0;
                while (len != -1) {
                    len = dexStream!!.read(b);
                    if (len > 0) {
                        dexWriter.write(b, 0, len);
                    }
                }
                dexWriter.flush()
                dexWriter.close()
            }
            newDexCL = KevoreeDexClassLoader(dexInternalStoragePath.getAbsolutePath(), outDex.getAbsolutePath(), null, this)
        }
        subDexClassLoader.add(newDexCL!!)
    }


    override fun internal_defineClass(className: String, bytes: ByteArray): Class<out Any?>? {
        for(subCL in subDexClassLoader) {
            try {
                val classLoaded = subCL.tryLoadClass(className)
                if (classLoaded != null) {
                    return classLoaded
                }
            } catch(nf : ClassNotFoundException) {
            }
        }
        return null
    }

    override fun unload() {
        subDexClassLoader.clear()
        super.unload()
        for(odexp in odexPaths) {
            clearAll(odexp)
        }
        odexPaths.clear()
    }


    fun clearAll(f: File) {

        deleteFile(f)
    }

    fun deleteFile(dfile: File): Unit {
        if (dfile.isDirectory()) {
            val subfiles = dfile.listFiles()
            if (subfiles != null){
                for(f in subfiles){
                    deleteFile(f)
                }
            }
        }
        dfile.delete()
    }


}
