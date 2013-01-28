package org.kevoree.framework.deploy;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 1/28/13
 * Time: 10:24 AM
 */
public class WorkerThreadFactory implements ThreadFactory {

    AtomicInteger threadNumber = new AtomicInteger(1);

    private String id = "";

    public WorkerThreadFactory(String _id){
        id = _id;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        SecurityManager s = System.getSecurityManager();
        ThreadGroup group = null;
        if (s != null) {
            group = s.getThreadGroup();
        } else {
            group = Thread.currentThread().getThreadGroup();
        }
        Thread t = new Thread(group, runnable, "Kevoree_Deploy_" + id + "_Worker_" + threadNumber.getAndIncrement());
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}
