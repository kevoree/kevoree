package org.kevoree.kcl;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 30/05/13
 * Time: 16:14
 * To change this template use File | Settings | File Templates.
 */
public class Tester {



    public static void main(String argv[]) throws Exception{

        KevoreeJarClassLoader kcl = new KevoreeJarClassLoader();
        kcl.add("/home/jed/.m2/repository/org/slf4j/slf4j-api/1.7.5/slf4j-api-1.7.5.jar");

        kcl.loadClass("org.slf4j.LoggerFactory");

        System.out.println("ici") ;
        kcl.add("/home/jed/.m2/repository/org/slf4j/slf4j-api/1.6.5/slf4j-api-1.6.5.jar");


        kcl.loadClass("org.slf4j.LoggerFactory");

    }
}
