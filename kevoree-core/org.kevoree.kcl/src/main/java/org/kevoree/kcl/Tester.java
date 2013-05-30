package org.kevoree.kcl;

import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 30/05/13
 * Time: 16:14
 * To change this template use File | Settings | File Templates.
 */
public class Tester {


    /*   Exception in thread "Thread-11" java.lang.LinkageError:
    loader constraint violation in interface itable initialization:
    when resolving method "org.slf4j.impl.StaticLoggerBinder.getLoggerFactory()Lorg/slf4j/ILoggerFactory;" the class loader (instance of org/kevoree/kcl/KevoreeJarClassLoader) of the current class, org/slf4j/impl/StaticLoggerBinder, and the class loader (instance of org/kevoree/kcl/KevoreeJarClassLoader)
     for interface org/slf4j/spi/LoggerFactoryBinder have different Class objects for the type org/slf4j/ILoggerFactory used in the signature

     */
    public static void main(String argv[]) throws Exception{
        try {

            KevoreeJarClassLoader root = new KevoreeJarClassLoader();


            KevoreeJarClassLoader cl1 = new KevoreeJarClassLoader();

            KevoreeJarClassLoader cl2 = new KevoreeJarClassLoader();

            cl1.add("/home/jed/.m2/repository/org/slf4j/slf4j-api/1.6.4/slf4j-api-1.6.4.jar");


            cl2.add("/home/jed/.m2/repository/org/slf4j/slf4j-api/1.7.5/slf4j-api-1.7.5.jar");
            cl2.add("/home/jed/.m2/repository/org/slf4j/slf4j-simple/1.7.5/slf4j-simple-1.7.5.jar");


            root.addSubClassLoader(cl1);
            root.addSubClassLoader(cl2);

            Class t =  cl1.loadClass("org.slf4j.LoggerFactory");
            Method m = t.getMethod("getLogger", new Class[] { Class.class});
            m.setAccessible(true);

            m.invoke(t, new Object[] { Tester.class});


            Class t2 =  cl2.loadClass("org.slf4j.LoggerFactory");
            Method m2 = t.getMethod("getLogger", new Class[] { Class.class});


            m2.invoke(t, new Object[] { Tester.class});









        }   catch (Exception e){
            e.printStackTrace();
        }


    }
}
