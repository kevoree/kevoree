package org.kevoree.bootstrap.kernel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by duke on 8/8/14.
 */
public class ExExecutors {

    public static ExecutorService newFixedThreadPool(int nThreads) {

        System.err.println("WTF !!! I saw u :-) :-)");

        return Executors.newFixedThreadPool(nThreads);
    };

}
