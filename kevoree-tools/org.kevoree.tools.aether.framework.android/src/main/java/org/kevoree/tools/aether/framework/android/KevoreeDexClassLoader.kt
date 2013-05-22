package org.kevoree.tools.aether.framework.android

import org.kevoree.kcl.KevoreeJarClassLoader
import dalvik.system.DexFile
import org.kevoree.log.Log

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 04/04/13
 * Time: 10:28
 * To change this template use File | Settings | File Templates.
 */

 /**
    KevoreeDexClassLoader
    sourcePathName 	Jar or APK file with "classes.dex". (May expand this to include "raw DEX" in the future.)
    outputPathName 	File that will hold the optimized form of the DEX data.
 */
class KevoreeDexClassLoader(sourcePathName: String, outputPathName: String, val jarKCL: KevoreeJarClassLoader)
{
    val dexFile = DexFile.loadDex(sourcePathName, outputPathName, 0)

    fun tryLoadClass(className: String?): Class<out Any?>? {
        Log.debug("tryLoadClass " + className+" in "+dexFile!!.getName())
        return dexFile!!.loadClass(className, jarKCL)
    }
}