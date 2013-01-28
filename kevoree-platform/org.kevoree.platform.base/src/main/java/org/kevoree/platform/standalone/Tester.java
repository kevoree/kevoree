package org.kevoree.platform.standalone;

import org.kevoree.kcl.KevoreeJarClassLoader;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 1/28/13
 * Time: 9:05 AM
 */
public class Tester {

    public static void main(String[] args){
        KevoreeJarClassLoader jar = new KevoreeJarClassLoader();
        Class kevRoot = jar.loadClass("org.kevoree.ContainerRoot");
        Class tuple2 = jar.loadClass("jet.Tuple2");
    }




}
