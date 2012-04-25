package org.kevoree.library.webserver;

import org.kevoree.api.service.core.classloading.KevoreeClassLoaderHandler;
import org.kevoree.kcl.KevoreeJarClassLoader;
import org.xeustechnologies.jcl.JarClassLoader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/04/12
 * Time: 16:58
 * To change this template use File | Settings | File Templates.
 */
public class Tester {

    public static void main(String[] args) throws InterruptedException, IOException {
        /*KTinyWebServer server = new KTinyWebServer();
        server.getDictionary().put("port","8080");
        server.start();   */

      /*
        String path = "jar:file:/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/javase/org.kevoree.library.javase.webserver.tjws/target/org.kevoree.library.javase.webserver.tjws-1.7.0-SNAPSHOT.jar!/Acme/Resource/mime.properties";
        URL url = new URL(path);
        System.out.println(url.openStream().available());
        System.out.println(url.getFile());
        File f = new File(url.getFile());
        System.out.println(f.exists());
        URL url2 = Tester.class.getClassLoader().getResource("javax/servlet/LocalStrings_ja.properties");
        File file2 = new File(url2.toString());
        System.out.println(file2.exists());
        System.out.println(url2);
                                */
        String path3 = "file:/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/javase/org.kevoree.library.javase.webserver.tjws/target/org.kevoree.library.javase.webserver.tjws-1.7.0-SNAPSHOT.jar";
        URL url3 = new URL(path3);
        JarClassLoader cl = new JarClassLoader();
        cl.add(url3);
        System.out.println(cl.getResource("javax/servlet/LocalStrings_ja.properties"));
             /*
        KevoreeJarClassLoader kcl = new KevoreeJarClassLoader();
        kcl.setLazyLoad(true);
        kcl.add(url3.openStream());
        System.out.println("kcl="+kcl.getResource("Acme/Resource/mime.properties"));
             */


    }

}
