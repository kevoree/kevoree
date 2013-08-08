package org.kevoree.kcl

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory


/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 20/07/12
 * Time: 16:01
 */

/*
object KCLScheduler {

    var _scheduler: ExecutorService? = null
    val lock = Object()
    fun getScheduler(): ExecutorService {
        if(_scheduler == null){
            synchronized(lock, {
                _scheduler = Executors.newSingleThreadExecutor(KCLSchedulerThreadFactory())
            })
        }
        return _scheduler!!
    }

}
   */