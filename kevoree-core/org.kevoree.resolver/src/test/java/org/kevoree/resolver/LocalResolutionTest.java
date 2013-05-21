package org.kevoree.resolver;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

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
        File resolved = resolver.resolve("org.kevoree.bootstrap", "org.kevoree.bootstrap", "1.0-SNAPSHOT", "jar", new ArrayList<String>());
        System.out.println(resolved);
    }

}
