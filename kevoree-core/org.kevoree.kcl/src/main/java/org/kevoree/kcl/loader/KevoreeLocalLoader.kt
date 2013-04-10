package org.kevoree.kcl.loader

import java.lang.Class
import org.slf4j.LoggerFactory
import java.util.concurrent.Semaphore
import java.io.InputStream
import java.io.ByteArrayInputStream
import java.util.concurrent.Callable
import org.kevoree.kcl.KevoreeLazyJarResources
import org.kevoree.kcl.KevoreeJarClassLoader
import org.kevoree.kcl.KCLScheduler

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 19/03/12
 * Time: 14:53
 */

class KevoreeLocalLoader(val classpathResources: KevoreeLazyJarResources, val kcl: KevoreeJarClassLoader): ProxyClassLoader() {

    KevoreeLocalLoader(classpathResources: KevoreeLazyJarResources, kcl: KevoreeJarClassLoader){
        order = 1
    }

    private val logger = LoggerFactory.getLogger(this.javaClass)!!

    public override fun loadResource(name: String?): InputStream? {
        if(name != null){
            val arr = classpathResources.getResource(name)
            if (arr != null) {
                return ByteArrayInputStream(arr)
            }
        }
        return null
    }

    public override fun loadClass(className: String?, resolveIt: Boolean): Class<out Any?>? {
        var result = kcl.getLoadedClass(className!!)
        if (result == null) {
            val bytes = kcl.loadClassBytes(className)
            if (bytes != null) {
                acquireLock(className!!)
                result = kcl.getLoadedClass(className)
                if (result == null) {
                    result = kcl.internal_defineClass(className, bytes)
                }
                releaseLock(className!!)
            }
        }
        return result
    }

    inner class AcquireLockCallable(val className: String): Callable<Semaphore> {
        override fun call(): Semaphore? {
            return if (locked.containsKey(className)) {
                val tuple = locked.get(className)!!
                tuple.inc()
                //locked.put(className, SemaCounter(tuple._1, (tuple._2 + 1)))
                tuple.sema
            } else {
                val obj = Semaphore(0)
                locked.put(className, SemaCounter(obj, 1))
                null //don't block first thread
            }
        }
    }

    fun acquireLock(className: String) {
        val call = AcquireLockCallable(className)
        try {
            val obj: Semaphore? = KCLScheduler.getScheduler().submit(call).get()
            if (obj != null){
                logger.debug("Lock KCL to avoid concurrency {}", className)
                obj.acquire()
            }
        } catch(ie: java.lang.InterruptedException) {
        }
        catch(e: Exception){
            logger.error("Error while sync {} KCL thread : {}",className, Thread.currentThread().getName(), e)
        }
    }

    inner class ReleaseLockCallable(val className: String): Runnable {
        override fun run() {
            if (locked.containsKey(className)) {
                val lobj = locked.get(className)!!
                if(lobj.counter == 1){
                    locked.remove(className)
                } else {
                    lobj.sema.release()
                }
            }
        }
    }

    fun releaseLock(className: String) {
        try {
            val call = ReleaseLockCallable(className)
            KCLScheduler.getScheduler().submit(call).get()
        } catch(ie: java.lang.InterruptedException) {
        }
        catch (e: Exception) {
            logger.error("Error while sync {} KCL thread : {}", className,Thread.currentThread().getName(), e)
        }
    }

    private val locked = java.util.HashMap<String, SemaCounter>()

    data class SemaCounter(val sema : Semaphore, var counter : Int) {
        fun inc(){
            counter = counter +1
        }

    }

}
