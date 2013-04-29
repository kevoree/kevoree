package org.kevoree.framework.port

import java.util.concurrent.ThreadFactory

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 29/04/13
 * Time: 17:18
 */

class PausablePortThreadPoolFactory(val tg : ThreadGroup) : ThreadFactory {

    public override fun newThread(r: Runnable): Thread {
        return Thread(tg,r)
    }

}