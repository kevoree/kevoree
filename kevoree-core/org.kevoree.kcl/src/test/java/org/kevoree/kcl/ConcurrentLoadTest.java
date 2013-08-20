package org.kevoree.kcl;

import org.junit.Test;
import org.kevoree.log.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 19/08/13
 * Time: 11:46
 */
public class ConcurrentLoadTest implements Runnable {

    @Test
    public void testConcurrentLoad() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*4);
        for (int i = 0; i < Runtime.getRuntime().availableProcessors()*4; i++) {
            pool.submit(new ConcurrentLoadTest());
        }
        pool.awaitTermination(1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        System.out.println("Perform simple KCL Test");
        KevoreeJarClassLoader jar = new KevoreeJarClassLoader();
        jar.add(this.getClass().getClassLoader().getResourceAsStream("org.kevoree.kcl.jar"));

        KevoreeJarClassLoader jarLog = new KevoreeJarClassLoader();
        jarLog.add(this.getClass().getClassLoader().getResourceAsStream("org.kevoree.log.jar"));

        jar.addSubClassLoader(jarLog);

        Class resolvedClass = jar.loadClass("org.kevoree.kcl.KevoreeJarClassLoader");
        assert (resolvedClass.getClassLoader().equals(jar));

        Class resolvedLogClass = jarLog.loadClass(Log.class.getName());  //std resolution of class
        assert (resolvedLogClass.getClassLoader().equals(jarLog));

        Class resolvedLogClassTransitive = jar.loadClass(Log.class.getName());
        assert (resolvedLogClassTransitive.getClassLoader().equals(jarLog)); // Log class should be resolved from the new KCL
        //TEst the transitive link
    }
}
