package org.kevoree.kcl;

import org.junit.Test;
import org.kevoree.log.Log;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 19/08/13
 * Time: 11:47
 */
public class SimpleTest {

    @Test
    public void simpleTest() {
        System.out.println("Perform simple KCL Test");
        KevoreeJarClassLoader jar = new KevoreeJarClassLoader();
        jar.add(this.getClass().getClassLoader().getResourceAsStream("org.kevoree.kcl.jar"));
        Class resolvedClass = jar.loadClass("org.kevoree.kcl.KevoreeJarClassLoader");
        assert (resolvedClass.getClassLoader().equals(jar));

        Class resolvedLogClass = jar.loadClass(Log.class.getName());
        assert (!resolvedLogClass.getClassLoader().equals(jar)); // Log class should be resolved from the System ClassLoader nor the new KCL (no binding)

    }

    @Test
    public void linkedTest() {

        KevoreeJarClassLoader systemEnabledKCL = new KevoreeJarClassLoader();

        System.out.println("Perform simple KCL Test");
        KevoreeJarClassLoader jar = new KevoreeJarClassLoader();
        jar.isolateFromSystem();
        jar.add(this.getClass().getClassLoader().getResourceAsStream("org.kevoree.kcl.jar"));
        jar.addChild(systemEnabledKCL);

        KevoreeJarClassLoader jarLog = new KevoreeJarClassLoader();
        jarLog.add(this.getClass().getClassLoader().getResourceAsStream("org.kevoree.log.jar"));
        jarLog.isolateFromSystem();
        jarLog.addChild(systemEnabledKCL);

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
