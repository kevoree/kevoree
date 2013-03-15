package org.kevoree.kcl

import org.slf4j.LoggerFactory
import java.io.*
import java.util.concurrent.ConcurrentHashMap
import java.net.URL
import java.util.HashMap
import java.util.ArrayList
import java.lang.ref.WeakReference
import java.util.Collections
import java.util.Comparator
import org.kevoree.kcl.loader.KevoreeResourcesLoader
import org.kevoree.kcl.loader.KevoreeLocalLoader
import org.kevoree.kcl.loader.ProxyClassLoader

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/01/12
 * Time: 18:57
 */

open class KevoreeJarClassLoader(): ClassLoader() {

    protected var classpathResources: KevoreeLazyJarResources? = null
    protected var local_loader: KevoreeLocalLoader? = null
    protected val loaders: ArrayList<ProxyClassLoader> = ArrayList<ProxyClassLoader>()

    private final val systemLoader: ProxyClassLoader = SystemLoader()
    private final val parentLoader: ProxyClassLoader = ParentLoader()
    private final val currentLoader: ProxyClassLoader = CurrentLoader()

    KevoreeJarClassLoader(){
        classpathResources = KevoreeLazyJarResources()
        local_loader = KevoreeLocalLoader(classpathResources!!, this)
        (classpathResources as KevoreeLazyJarResources).setParentKCL(this)
        loaders.add(systemLoader)
        loaders.add(parentLoader)
        loaders.add(currentLoader)
        loaders.add(local_loader!!)
        Collections.sort(loaders)

    }

    private val nativeMap = HashMap<String, String>()

    fun addNativeMapping(name: String, url: String) {
        nativeMap.put(name, url)
    }

    override fun findLibrary(p1: String?): String? {
        return if (nativeMap.containsKey(p1)) {
            nativeMap.get(p1)
        } else {
            super.findLibrary(p1)
        }
    }

    fun getLoadedURLs(): List<URL> {
        return (classpathResources as KevoreeLazyJarResources).getLoadedURLs()
    }

    fun getLinkedLoadedURLs(): List<URL> {
        val resultURL = ArrayList<URL>()
        val alreadyPassed = ArrayList<ClassLoader>()
        internal_getAllLoadedURLs(resultURL, alreadyPassed)
        return resultURL
    }

    fun internal_getAllLoadedURLs(res: MutableList<URL>, cls: MutableList<ClassLoader>): Unit {
        cls.add(this)
        res.addAll((classpathResources as KevoreeLazyJarResources).getLoadedURLs())
        for(l in subClassLoaders){
            if (l is KevoreeJarClassLoader && !cls.contains(l)) {
                (l as KevoreeJarClassLoader).internal_getAllLoadedURLs(res, cls)
            }
        }
    }

    var specialloaders = ArrayList<KevoreeResourcesLoader>()

    protected fun addSpecialLoaders(l: KevoreeResourcesLoader) {
        specialloaders.add(l)
    }

    fun getSpecialLoaders(): ArrayList<KevoreeResourcesLoader> {
        return specialloaders
    }

    private var locked = false

    fun lockLinks() {
        locked = true
    }

    protected var subClassLoaders: ArrayList<ClassLoader> = ArrayList<ClassLoader>()


    private val logger = LoggerFactory.getLogger(this.javaClass)!!

    fun cleanupLinks(c: ClassLoader) {
        // CHECK USED
        subClassLoaders.remove(c)
        subWeakClassLoaders.removeAll(subWeakClassLoaders.filter{ scl -> scl.get() == c })
    }

    protected var subWeakClassLoaders: ArrayList<WeakReference<ClassLoader>> = ArrayList<WeakReference<ClassLoader>>()

    open fun setLazyLoad(lazyload: Boolean) {
        (classpathResources as KevoreeLazyJarResources).setLazyLoad(lazyload)
    }

    fun getSubClassLoaders(): List<ClassLoader> {
        return subClassLoaders
    }


    fun addSubClassLoader(cl: ClassLoader) {
        if (!locked) {
            if (!subClassLoaders.contains(cl)) {
                if (!subWeakClassLoaders.any{ scl -> scl.get() != null && scl.get() == cl }) {
                    subClassLoaders.add(cl)
                }
            }
        }
    }


    fun addWeakClassLoader(wcl: ClassLoader) {
        if (!locked) {
            if (!subClassLoaders.contains(wcl)) {
                if (!subWeakClassLoaders.any{ scl -> scl.get() != null && scl.get() == wcl }) {
                    subWeakClassLoaders.add(WeakReference<ClassLoader>(wcl))
                }
            }
        }
    }


    protected fun callSuperConcreteLoader(className: String, resolveIt: Boolean): Class<out Any?>? {
        var clazz: Class<out Any?>? = null
        for (l in loaders) {
            if (l.isEnabled()) {
                clazz = l.loadClass(className, resolveIt);
                if (clazz != null)
                    break;
            }
        }
        return clazz;
        //return super<JarClassLoader>.loadClass(className, resolveIt)
    }


    override fun loadClass(className: String?, resolveIt: Boolean): Class<out Any?>? {
        val result = internal_loadClass(className!!, resolveIt)
        if (result == null) {
            throw ClassNotFoundException(className)
        }
        return result
    }

    private val scoreMap = ConcurrentHashMap<Int, Int>()

    fun getScore(kcl: ClassLoader): Int {
        return if (scoreMap.containsKey(kcl.hashCode())) {
            scoreMap.get(kcl.hashCode())!!
        } else {
            0
        }
    }

    fun incScore(kcl: ClassLoader): Int {
        scoreMap.put(kcl.hashCode(), getScore(kcl) + 1)
        return scoreMap.get(kcl.hashCode())!!
    }

    fun internal_loadClass(className: String, resolveIt: Boolean): Class<out Any ?>? {
        var result: Class<out Any?>? = null
        result = callSuperConcreteLoader(className, resolveIt)
        if (result != null) {
            return result
        }
        if (resolveIt) {
            val sortedL = subClassLoaders.sort(object : Comparator<ClassLoader> {
                public override fun equals(p0: Any?): Boolean {
                    throw UnsupportedOperationException()
                }
                public override fun compare(p0: ClassLoader, p1: ClassLoader): Int {
                    if(getScore(p0) == getScore(p1)){
                        return 0
                    }
                    if(getScore(p0) > getScore(p1)){
                        return 1
                    }
                    return -1
                }


            })
            for(subCL in sortedL) {
                var result: Class<out Any?>? = null
                if(subCL is KevoreeJarClassLoader){
                    result = (subCL as KevoreeJarClassLoader).internal_loadClass(className, false)
                } else {
                    try {
                        subCL.loadClass(className)
                    } catch(nf: ClassNotFoundException) {
                        null
                    }
                }
                if (result != null) {
                    incScore(subCL)
                    return result
                }
            }
            val filteredLL = subWeakClassLoaders.filter{ p -> p.get() != null }.sort(object : Comparator<WeakReference<ClassLoader>>{
                public override fun compare(p0: WeakReference<ClassLoader>, p1: WeakReference<ClassLoader>): Int {
                    if(getScore(p0.get()!!) == getScore(p1.get()!!)){
                        return 0
                    }
                    if(getScore(p0.get()!!) > getScore(p1.get()!!)){
                        return 1
                    }
                    return -1
                }
                public override fun equals(p0: Any?): Boolean {
                    throw UnsupportedOperationException()
                }

            })
            for(subCL in filteredLL) {
                try {
                    if(subCL.get() != null) {

                        var result: Class<out Any?>? = null
                        if(subCL.get() is KevoreeJarClassLoader){
                            result = (subCL.get() as KevoreeJarClassLoader).internal_loadClass(className, false)
                        } else {
                            try {
                                subCL.get()!!.loadClass(className)
                            } catch(nf: ClassNotFoundException) {
                                null
                            }
                        }
                        if (result != null) {
                            incScore(subCL.get()!!)
                            return result
                        }
                    }
                } catch(nf: ClassNotFoundException) {
                }
            }
        }
        return result
    }

    fun getLoadedClass(className: String): Class<out Any?>? {
        return findLoadedClass(className)
    }

    open fun internal_defineClass(className: String, bytes: ByteArray): Class<out Any?>? {
        if (className.contains(".")) {
            val packageName = className.substring(0, className.lastIndexOf('.'))
            if (getPackage(packageName) == null) {
                definePackage(packageName, null, null, null, null, null, null, null)
            }
        }
        return defineClass(className, bytes, 0, bytes.size)
    }

    override fun loadClass(className: String?): Class<out Any?>? {
        return loadClass(className, true)
    }


    override fun getResourceAsStream(name: String?): InputStream? {
        var resolved = internal_getResourceAsStream(name)
        if (resolved != null) {
            return resolved
        }
        for(sub in subClassLoaders) {
            resolved = if (sub is KevoreeJarClassLoader) {
                (sub as KevoreeJarClassLoader).internal_getResourceAsStream(name)
            } else {
                sub.getResourceAsStream(name)
            }
            if (resolved != null) {
                return resolved
            }
        }
        for( subOpt in subWeakClassLoaders) {
            if (subOpt.get() != null) {
                val sub = subOpt.get()
                resolved = if (sub is KevoreeJarClassLoader) {
                    (sub as KevoreeJarClassLoader).internal_getResourceAsStream(name)
                } else {
                    sub?.getResourceAsStream(name)
                }
                if (resolved != null) {
                    return resolved
                }
            }
        }
        return resolved
    }


    fun internal_getResourceAsStream(name: String?): InputStream? {
        if (name?.endsWith(".class")!!) {
            val res = if(name != null){this.classpathResources!!.getResource(name) } else {null}
            if (res != null) {
                return ByteArrayInputStream(res)
            }
        }
        val url = (this.classpathResources as KevoreeLazyJarResources).getResourceURL(name!!)
        return if (url != null) {
            if (url.toString().startsWith("file:kclstream:")) {
                ByteArrayInputStream((this.classpathResources as KevoreeLazyJarResources).getResourceContent(url)!!)
            } else {
                url.openStream()
            }
        } else {
            //STRANGE ERROR
            null
        }
    }

    override fun getResource(s: String?): URL? {
        return findResource(s)
    }

    fun internal_getResource(s: String): URL? {
        return if ( (classpathResources as KevoreeLazyJarResources).containResource(s)) {
            if ( (classpathResources as KevoreeLazyJarResources).getResourceURL(s).toString().startsWith("file:kclstream:")) {
                val cleanName = if (s.contains("/")) {
                    s.substring(s.lastIndexOf("/") + 1)
                } else {
                    s
                }
                val tFile = File.createTempFile("dummy_kcl_temp", cleanName)
                tFile.deleteOnExit()
                val tWriter = FileOutputStream(tFile)
                tWriter.write((classpathResources as KevoreeLazyJarResources).getResourceContent((classpathResources as KevoreeLazyJarResources).getResourceURL(s)!!)!!)
                tWriter.close()
                URL("file:///" + tFile.getAbsolutePath())
            } else {
                //SIMPLY RETURN URL
                (classpathResources as KevoreeLazyJarResources).getResourceURL(s)
            }
        } else {
            //logger.debug("getResource not found null=>" + s + " in " + classpathResources.asInstanceOf[KevoreeLazyJarResources].getClass)
            //logger.debug("getResource not found null=>" + s + " in " + classpathResources.asInstanceOf[KevoreeLazyJarResources].getLastLoadedJar)
            null
        }
    }

    open fun unload() {
    }

    override fun findResource(s: String?): URL? {
        var urlInternal: URL? = internal_getResource(s!!)
        return if (urlInternal == null) {
            for(sub in  subClassLoaders) {
                urlInternal = if (sub is KevoreeJarClassLoader) {
                    (sub as KevoreeJarClassLoader).internal_getResource(s)
                } else {
                    sub.getResource(s)
                }
                if (urlInternal != null) {
                    return urlInternal
                }
            }
            for(subOpt in subWeakClassLoaders) {
                if (subOpt.get() != null) {
                    val sub = subOpt.get()
                    urlInternal = if (sub is KevoreeJarClassLoader) {
                        (sub as KevoreeJarClassLoader).internal_getResource(s)
                    } else {
                        sub?.getResource(s)
                    }
                    if (urlInternal != null) {
                        return urlInternal
                    }
                }
            }
            null
        } else {
            urlInternal
        }
    }


    fun internal_findResources(p1: String): java.util.ArrayList<URL> {
        return if ( (classpathResources as KevoreeLazyJarResources).containResource(p1)) {
            val urls = (classpathResources as KevoreeLazyJarResources).getResourceURLS(p1)
            val resolvedUrl = ArrayList<URL>()
            for(u in urls) {
                if (u.toString().startsWith("file:kclstream:")) {
                    val cleanName = if (p1.contains("/")) {
                        p1.substring(p1.lastIndexOf("/") + 1)
                    } else {
                        p1
                    }
                    val tFile = File.createTempFile("dummy_kcl_temp", cleanName)
                    tFile.deleteOnExit()
                    val tWriter = FileOutputStream(tFile)
                    tWriter.write((classpathResources as KevoreeLazyJarResources).getResourceContent(u)!!)
                    tWriter.close()
                    resolvedUrl.add(URL("file:///" + tFile.getAbsolutePath()))
                } else {
                    resolvedUrl.add(u)
                }
            }
            resolvedUrl
        } else {
            ArrayList<URL>()
        }
    }

    override fun findResources(p1: String?): java.util.Enumeration<URL> {
        val selfRes: MutableList<URL> = internal_findResources(p1!!)
        //Then call on all
        for( sub in subClassLoaders) {
            val subEnum = if (sub is KevoreeJarClassLoader) {
                Collections.enumeration((sub as KevoreeJarClassLoader).internal_findResources(p1))
            } else {
                sub.getResources(p1)
            }
            while (subEnum.hasMoreElements()) {
                val subElem = subEnum.nextElement();
                if (!selfRes.contains(subElem)) {
                    selfRes.add(subElem as URL)
                }
            }
        }
        for(subOpt in subWeakClassLoaders) {
            if (subOpt.get() != null) {
                val sub = subOpt.get()
                val subEnum = if (sub is KevoreeJarClassLoader) {
                    Collections.enumeration((sub as KevoreeJarClassLoader).internal_findResources(p1))
                } else {
                    sub?.getResources(p1)
                }
                while (subEnum!!.hasMoreElements()) {
                    val subElem = subEnum.nextElement();
                    if (!selfRes.contains(subElem)) {
                        selfRes.add(subElem as URL)
                    }
                }
            }
        }
        return Collections.enumeration(selfRes)
    }

    fun cleanJarURL(j: String) : String {
        return if (j.contains(File.separator)) {
            j.substring(j.lastIndexOf(File.separator) + 1)
        } else {
            j
        }
    }


    fun getKCLDump(): String {
        val buffer = StringBuffer()
        buffer.append("\tJar=" + cleanJarURL((classpathResources as KevoreeLazyJarResources).getLastLoadedJar()) + "_" + hashCode() + "\n")
        for(s in subClassLoaders) {
            buffer.append("\t\tl->" + cleanJarURL(((s as KevoreeJarClassLoader).classpathResources as KevoreeLazyJarResources).getLastLoadedJar()) + "_" + s.hashCode() + "\n")
        }
        for(s in subWeakClassLoaders) {
            if (s.get() != null) {
                buffer.append("\t\tw~>" + cleanJarURL(((s.get() as KevoreeJarClassLoader).classpathResources as KevoreeLazyJarResources).getLastLoadedJar() + "_" + s.get()!!.hashCode()) + "\n")
            }
        }
        return buffer.toString()
    }


    fun printDump() {
        logger.debug("KCL : " + (classpathResources as KevoreeLazyJarResources).getLastLoadedJar())
        for(s in subClassLoaders) {
            logger.debug("    l->" + ((s as KevoreeJarClassLoader).classpathResources as KevoreeLazyJarResources).getLastLoadedJar() + "_" + s.hashCode())
        }
        for(s in subWeakClassLoaders) {
            if (s.get() != null){
                logger.debug("    w~>" + ((s.get() as KevoreeJarClassLoader).classpathResources as KevoreeLazyJarResources).getLastLoadedJar() + "_" + s.get()!!.hashCode())
            }
        }
    }

    override fun toString(): String {
        return cleanJarURL((classpathResources as KevoreeLazyJarResources).getLastLoadedJar()).toString() + hashCode()
    }


    public fun add(resourceName: String) {
        classpathResources?.loadJar(resourceName);
    }

    /**
     * Loads classes from InputStream
     *
     * @param jarStream
     */
    public fun add(jarStream: InputStream) {
        classpathResources?.loadJar(jarStream);
    }


    public fun add(url: URL): Unit {
        classpathResources?.loadJar(url);
    }

    /**
     * Reads the class bytes from different local and remote resources using
     * ClasspathResources
     *
     * @param className
     * @return byte[]
     */
    public open fun loadClassBytes(className: String): ByteArray? {
        val className2 = formatClassName(className);
        return classpathResources?.getResource(className2);
    }

    fun formatClassName(className: String): String {
        var classNameT = className.replace('/', '~');
        classNameT = classNameT.replace('.', '/') + ".class";
        classNameT = classNameT.replace('~', '/');
        return classNameT;
    }

    inner class CurrentLoader: ProxyClassLoader() {
        CurrentLoader() {
            order = 2;
        }
        override fun loadClass(className: String?, resolveIt: Boolean): Class<out Any?>? {
            var result: Class<out Any?>? = null
            try {
                result = getClass().getClassLoader()!!.loadClass(className)
            } catch (e: ClassNotFoundException) {
                return null;
            }
            return result;
        }

        override fun loadResource(name: String?): InputStream? {
            val isS = getClass().getClassLoader()!!.getResourceAsStream(name)
            if (isS != null) {
                return isS
            }
            return null;
        }
    }

    class ThreadContextLoader: ProxyClassLoader() {

        ThreadContextLoader(){
            order = 4;
        }

        override fun loadClass(className: String?, resolveIt: Boolean): Class<out Any?>? {
            var result: Class<out Any?>? = null;
            try {
                result = Thread.currentThread().getContextClassLoader()!!.loadClass(className)
            } catch (e: ClassNotFoundException) {
                return null
            }
            return result
        }

        override fun loadResource(name: String?): InputStream? {
            val isS = Thread.currentThread()!!.getContextClassLoader()!!.getResourceAsStream(name);
            if (isS != null) {
                return isS;
            }
            return null;
        }

    }

    fun callCLfindSystemClass(className: String?): Class<out Any?>? {
        return findSystemClass(className)
    }


    inner class SystemLoader: ProxyClassLoader() {
        SystemLoader() {
            order = 5;
        }
        override fun loadClass(className: String?, resolveIt: Boolean): Class<out Any?>? {
            var result: Class<out Any?>? = null;
            try {
                result = callCLfindSystemClass(className);
            } catch (e: ClassNotFoundException) {
                return null;
            }
            return result;
        }

        override fun loadResource(name: String?): InputStream? {
            val isS = null//getSystemResourceAsStream(name);
            if (isS != null) {
                return isS;
            }
            return null;

        }
    }

    inner class ParentLoader: ProxyClassLoader() {

        public ParentLoader() {
            order = 3;
        }

        override fun loadClass(className: String?, resolveIt: Boolean): Class<out Any?>? {
            var result: Class<out Any?>? = null;

            try {
                result = getParent()!!.loadClass(className);
            } catch (e: ClassNotFoundException) {
                return null;
            }
            return result;
        }

        override fun loadResource(name: String?): InputStream? {

            val isS = getParent()!!.getResourceAsStream(name);

            if (isS != null) {
                return isS;
            }
            return null;
        }

    }


}