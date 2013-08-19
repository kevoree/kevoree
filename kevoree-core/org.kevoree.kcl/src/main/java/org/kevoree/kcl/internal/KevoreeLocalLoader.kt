package org.kevoree.kcl.internal;

import java.io.InputStream
import java.io.ByteArrayInputStream
import java.util.concurrent.Callable
import org.kevoree.kcl.KevoreeJarClassLoader
import org.kevoree.log.Log
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.CountDownLatch

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

    public override fun loadResource(name: String?): InputStream? {
        if(name != null){
            val arr = classpathResources.getResource(name)
            if (arr != null) {
                return ByteArrayInputStream(arr)
            }
        }
        return null
    }

    val lock = Object()

    //TODO CLASS LOAD OPTIMISATION FOR JAVA 7
    //http://docs.oracle.com/javase/7/docs/technotes/guides/lang/cl-mt.html
    public override fun loadClass(className: String?, resolveIt: Boolean): Class<out Any?>? {
        var result = kcl.getLoadedClass(className!!)
        if (result == null) {
            val bytes = kcl.loadClassBytes(className)
            if (bytes != null) {
                synchronized(lock, {
                    result = kcl.getLoadedClass(className)
                    if (result == null) {
                        result = kcl.internal_defineClass(className, bytes)
                    }
                })

                /*
                acquireLock(className!!)
                result = kcl.getLoadedClass(className)
                if (result == null) {
                    result = kcl.internal_defineClass(className, bytes)
                }
                releaseLock(className!!)  */
            }
        }
        return result
    }

        /*

    inner class AcquireLockCallable(val className: String): Callable<CountDownLatch> {
        override fun call(): CountDownLatch? {
            if (locked.containsKey(className)) {
                return locked.get(className)!!
            } else {
                val newLock = CountDownLatch(1)
                locked.put(className, newLock)
                return null
            }
        }
    }

    fun acquireLock(className: String) {
        val call = AcquireLockCallable(className)
        try {
            KCLScheduler.getScheduler().submit(call).get()?.await()
        } catch(e: Throwable){
            if(Log.ERROR){
                Log.error("Error while sync " + className + " KCL thread : " + Thread.currentThread().getName(), e)
            }
        }
    }

    inner class ReleaseLockCallable(val className: String): Callable<CountDownLatch> {
        override fun call(): CountDownLatch? {
            if (locked.containsKey(className)) {
                val lobj = locked.get(className)!!
                locked.remove(className)
                return lobj
            }
            return null
        }
    }

    fun releaseLock(className: String) {
        try {
            val call = ReleaseLockCallable(className)
            val awaiter = KCLScheduler.getScheduler().submit(call).get()
            awaiter?.countDown()
        } catch(ie: java.lang.InterruptedException) {
        }
        catch (e: Throwable) {
            if(Log.ERROR){
                Log.error("Error while sync " + className + " KCL thread : " + Thread.currentThread().getName(), e)
            }
        }
    }

    private val locked = java.util.HashMap<String, CountDownLatch>()
     */
}
