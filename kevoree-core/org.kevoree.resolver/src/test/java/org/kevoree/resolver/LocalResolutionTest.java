package org.kevoree.resolver;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 21/05/13
 * Time: 14:57
 */
public class LocalResolutionTest {

    @Test
    public void testLocalResolution() {
        MavenResolver resolver = new MavenResolver();
        //File resolved = resolver.resolve("org.kevoree.bootstrap", "org.kevoree.bootstrap", "1.0-SNAPSHOT", "jar", new ArrayList<String>());
        //System.out.println(resolved);

        List<String> l = new ArrayList<String>();
        l.add("http://maven.kevoree.org/release");
        l.add("http://maven.kevoree.org/snapshots");

        File resolved2 = resolver.resolve("org.kevoree.corelibrary.model","org.kevoree.library.model.bootstrap", "2.0.0-SNAPSHOT", "jar", l);
        System.out.println(resolved2);


    }

}
