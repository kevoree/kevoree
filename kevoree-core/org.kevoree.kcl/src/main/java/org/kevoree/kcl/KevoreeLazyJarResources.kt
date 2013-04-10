package org.kevoree.kcl

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

import java.io.*
import java.util.jar.JarInputStream
import java.net.URL
import org.slf4j.LoggerFactory
import java.util.ArrayList
import java.lang.ref.WeakReference
import java.util.HashMap
import org.kevoree.kcl.exception.KclException


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/01/12
 * Time: 19:13
 */

class KevoreeLazyJarResources {

    public fun getResource(name:String): ByteArray? {
        return getJarEntryContents(name);
    }


    val jarEntryContents = HashMap<String, ByteArray>()

    val jarContentURL = HashMap<String, URL>()
    private val logger = LoggerFactory.getLogger(this.javaClass)!!
    private var parentKCL: WeakReference<KevoreeJarClassLoader>? = null

    fun setParentKCL(kcl: KevoreeJarClassLoader) {
        parentKCL = WeakReference(kcl)
    }

    var lastLoadedJars = ArrayList<URL>()

    fun getLoadedURLs(): List<URL> {
        return  lastLoadedJars
    }

    var lazyload = true

    fun setLazyLoad(_lazyload: Boolean) {
        lazyload = _lazyload
    }

    fun getLastLoadedJar(): String {
        return if (lastLoadedJars.size > 0) {
            lastLoadedJars.get(0).toString()
        } else {
            "streamKCL"
        }
    }

    fun loadJar(jarStream: InputStream?) {
        loadJar(jarStream, null)
    }

    fun loadJar(url: URL?) {
        var inS: InputStream? = null;
        try {
            inS = url!!.openStream()
            lastLoadedJars.add(url)
            loadJar(inS, url)
        } catch(e: IOException) {
            throw KclException(e)
        } finally {
            if (inS != null)
                try {
                    inS?.close()
                } catch(e: IOException) {
                    throw KclException(e)
                }
        }
    }

    fun loadJar(jarFile: String?) {
        var fis: FileInputStream? = null
        try {
            val f = File(jarFile!!)
            fis = FileInputStream(jarFile)
            val url = URL("file:" + f.getAbsolutePath())
            lastLoadedJars.add(url)
            loadJar(fis, url)
        } catch(e: IOException) {
            throw KclException(e)
        } finally {
            if (fis != null)
                try {
                    fis?.close()
                } catch(e: IOException) {
                    throw KclException(e)
                }
        }
    }

    private val detectedResourcesURL = java.util.HashMap<String, MutableList<URL>>()
    private val detectedResources = java.util.HashMap<URL, ByteArray>()

    fun getResourceURLS(name: String): List<URL> {
        return if (containResource(name)) {
            detectedResourcesURL.get(name)!!
        } else {
            ArrayList<URL>()
        }
    }

    fun containResource(name: String): Boolean {
        return if (detectedResourcesURL.get(name) != null) {
            !detectedResourcesURL.get(name)?.isEmpty()!!
        } else {
            false
        }
    }

    fun getResourceURL(name: String): URL? {
        return if (containResource(name)) {
            detectedResourcesURL.get(name)?.get(0)
        } else {
            null
        }
    }


    fun loadJar(jarStream: InputStream?, baseurl: URL?) {
        var bis: BufferedInputStream? = null
        var jis: JarInputStream? = null
        try {
            bis = BufferedInputStream(jarStream!!)
            jis = JarInputStream(bis)
            var jarEntry = jis!!.getNextJarEntry()
            while (jarEntry != null) {
                if (!jarEntry!!.isDirectory()) {
                    var filtered = false
                    if (parentKCL?.get() != null) {
                        val extentionSelected = parentKCL!!.get()!!.getSpecialLoaders().find{ r -> jarEntry!!.getName().endsWith(r.getExtension()!!) }
                        if(extentionSelected != null){
                            extentionSelected!!.doLoad(jarEntry?.getName(), jis)
                            filtered = true
                        }
                    }
                    if (!filtered) {
                        if (jarContentURL.containsKey(jarEntry?.getName())) {
                            continue
                        } else {
                            if (baseurl != null && lazyload) {
                                if (jarEntry?.getName()?.endsWith(".class")!!) {
                                    jarContentURL.put(jarEntry?.getName()!!, URL("jar:" + baseurl + "!/" + jarEntry!!.getName()))
                                } else {
                                    if (!detectedResourcesURL.containsKey(jarEntry?.getName())) {
                                        detectedResourcesURL.put(jarEntry!!.getName(), ArrayList<URL>())
                                    }
                                    val tempL = detectedResourcesURL.get(jarEntry?.getName())
                                    tempL?.add(URL("jar:" + baseurl + "!/" + jarEntry!!.getName()))
                                }
                            } else {
                                if (!jarEntry!!.getName().endsWith(".class") && baseurl != null) {
                                    //IF URL OK , DON'T COPY RESOURCES

                                    var rurl = detectedResourcesURL.get(jarEntry!!.getName())
                                    if (rurl == null) {
                                        rurl = ArrayList<URL>()
                                    }
                                    detectedResourcesURL.put(jarEntry!!.getName(), rurl!!)
                                    rurl?.add(URL("jar:" + baseurl + "!/" + jarEntry!!.getName()))
                                } else {

                                    val b = ByteArray(2048)
                                    val out = ByteArrayOutputStream()
                                    var len = 0
                                    while (len != -1) {

                                        //while (jis.available() > 0) {
                                        len = jis?.read(b)!!
                                        if (len > 0) {
                                            out.write(b, 0, len)
                                        }
                                    }
                                    out.flush()
                                    out.close()
                                    val key_url = "file:kclstream:" + jarStream.hashCode() + jarEntry!!.getName()
                                    if (jarEntry!!.getName().endsWith(".class")) {
                                        jarContentURL.put(jarEntry!!.getName(), URL(key_url))
                                    } else {
                                        var rurl = detectedResourcesURL.get(jarEntry!!.getName())
                                        if (rurl == null) {
                                            rurl = ArrayList<URL>()
                                            detectedResourcesURL.put(jarEntry!!.getName(), rurl!!)
                                        }
                                        rurl?.add(URL(key_url))
                                    }
                                    if (jarEntry!!.getName().endsWith(".jar")) {
                                        if (baseurl != null) {
                                            val subRUL = URL("jar:" + baseurl + "!/" + jarEntry!!.getName())
                                            lastLoadedJars.add(subRUL)
                                        }

                                        //  println("subParentURL="+baseurl +jarEntry.getName())
                                        logger.debug("KCL Found sub Jar => {}", jarEntry!!.getName())
                                        loadJar(ByteArrayInputStream(out.toByteArray()))
                                    } else {
                                        if (jarEntry!!.getName().endsWith(".class")) {

                                            (jarEntryContents as MutableMap<String?,ByteArray?>).put(jarEntry!!.getName(), out.toByteArray())
                                        } else {
                                            detectedResources.put(URL(key_url), out.toByteArray())
                                        }
                                    }

                                }


                            }
                        }
                    }
                }
                jarEntry = jis?.getNextJarEntry()
            }
        }
        catch(e: IOException) {
            KclException(e)
        }
        catch(e: NullPointerException) {
            e.printStackTrace()
        } finally {
            if (jis != null)
                try {
                    jis?.close();
                } catch(e: Exception) {
                    throw KclException(e);
                }
            if (bis != null)
                try {
                    bis?.close();
                } catch(e: Exception) {
                    throw KclException(e)
                }
        }
    }


    protected fun getJarEntryContents(name: String?): ByteArray? {
        return if (jarContentURL.containsKey(name)) {
            if (jarEntryContents!!.containsKey(name)) {
                jarEntryContents?.get(name)!!
            } else {
                if (jarContentURL.get(name) != null) {
                    var stream: InputStream? = null
                    try {
                        val b = ByteArray(2048)
                        val out = ByteArrayOutputStream();
                        var len = 0;
                        stream = jarContentURL.get(name)?.openStream()
                        while (stream?.available()!! > 0) {
                            len = stream?.read(b)!!
                            if (len > 0) {
                                out.write(b, 0, len);
                            }
                        }
                        out.flush()
                        out.close()
                        (jarEntryContents as MutableMap<String?,ByteArray?>).put(name, out.toByteArray())
                        out.toByteArray()
                    } finally {
                        if (stream != null) {
                            stream?.close()
                        }
                    }
                } else {
                    null
                }
            }
        } else {
            null
        }
    }

    fun getResourceContent(resUrl: URL): ByteArray? {
        return if (detectedResources.containsKey(resUrl)) {
            detectedResources.get(resUrl)
        } else {
            if (!resUrl.toString().startsWith("file:kclstream:")) {
                var stream: InputStream? = null
                try {
                    val b = ByteArray(2048)
                    val out = ByteArrayOutputStream()
                    var len = 0;
                    stream = resUrl.openStream()
                    while (stream?.available()!! > 0) {
                        len = stream?.read(b)!!
                        if (len > 0) {
                            out.write(b, 0, len);
                        }
                    }
                    out.flush()
                    out.close()
                    detectedResources.put(resUrl, out.toByteArray())
                    out.toByteArray()
                } catch(e: Exception) {
                    logger.debug("Error while copying {} ",resUrl, e)
                    null
                } finally {
                    if (stream != null) {
                        stream?.close()
                    }
                }
            } else {
                null
            }
        }
    }

}