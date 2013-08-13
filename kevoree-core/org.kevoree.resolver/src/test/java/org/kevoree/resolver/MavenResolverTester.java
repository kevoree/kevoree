package org.kevoree.resolver;

import org.junit.Assert;
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
public class MavenResolverTester {

    @Test
    public void testLocalResolution() {
        MavenResolver resolver = new MavenResolver();

        List<String> l = new ArrayList<String>();
        l.add("http://oss.sonatype.org/content/groups/public");

        File resolved2 = resolver.resolve("org.kevoree.corelibrary.model", "org.kevoree.library.model.bootstrap", "2.0.0-SNAPSHOT", "jar", l);
        System.out.println(resolved2);
        Assert.assertNotSame(null, resolved2);
    }

    @Test
    public void testCentralResolution() {
        MavenResolver resolver = new MavenResolver();
        List<String> l = new ArrayList<String>();
        l.add("http://repo1.maven.org/maven2/");
        File resolved2 = resolver.resolve("io.tesla.maven", "maven-model", "3.1.2", "jar", l);
        System.out.println(resolved2);
        Assert.assertNotSame(null, resolved2);
    }

    @Test
    public void testRemoteResolution() {
        MavenResolver resolver = new MavenResolver();

        List<String> l = new ArrayList<String>();
        l.add("http://maven.kevoree.org/release");
        l.add("http://maven.kevoree.org/snapshots");
        l.add("http://oss.sonatype.org/content/groups/public");

        File resolved2 = resolver.resolve("org.kevoree.corelibrary.model", "org.kevoree.library.model.bootstrap", "2.0.0-ALPHA", "jar", l);
        System.out.println(resolved2);
        Assert.assertNotSame(null, resolved2);
        Assert.assertEquals(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository" + File.separator + "org.kevoree.corelibrary.model".replace(".", File.separator) + File.separator + "org.kevoree.library.model.bootstrap" + File.separator + "2.0.0-ALPHA" + File.separator + "org.kevoree.library.model.bootstrap-2.0.0-ALPHA.jar", resolved2.getAbsolutePath());

        resolved2 = resolver.resolve("org.kevoree.corelibrary.model", "org.kevoree.library.model.bootstrap", "RELEASE", "jar", l);
        System.out.println(resolved2);
        Assert.assertNotSame(null, resolved2);
        Assert.assertEquals(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository" + File.separator + "org.kevoree.corelibrary.model".replace(".", File.separator) + File.separator + "org.kevoree.library.model.bootstrap" + File.separator + "2.0.0-ALPHA" + File.separator + "org.kevoree.library.model.bootstrap-2.0.0-ALPHA.jar", resolved2.getAbsolutePath());

        resolved2 = resolver.resolve("org.kevoree.corelibrary.model", "org.kevoree.library.model.bootstrap", "LATEST", "jar", l);
        System.out.println(resolved2);
        Assert.assertNotSame(null, resolved2);
    }


    @Test
    public void testRemoteResolution2() {
        MavenResolver resolver = new MavenResolver();
        List<String> l = new ArrayList<String>();
        l.add("http://oss.sonatype.org/content/groups/public");
        File resolved = resolver.resolve("org.kevoree.tools", "org.kevoree.tools.marShell.pack", "2.0.0-SNAPSHOT", "jar", l);
        System.out.println(resolved.getAbsolutePath());
        Assert.assertNotSame(null, resolved);


    }

    @Test
    public void testremoteResolution3() {
        List<String> repos = new ArrayList<String>();
        repos.add("http://maven.kevoree.org/daum/snapshots");
        MavenResolver resolver = new MavenResolver();
        File f = resolver.resolve("org.daum.common.followermodel", "org.daum.common.followermodel", "2.0.0-SNAPSHOT", "jar", repos);
        System.out.println(f.getAbsolutePath());
        Assert.assertNotSame(null, f);
    }

    @Test
    public void testRemoteResolution4() {
        List<String> repos = new ArrayList<String>();
        repos.add("https://oss.sonatype.org/content/groups/public/");
        MavenResolver resolver = new MavenResolver();
        File f = resolver.resolve("org.kevoree.tools", "org.kevoree.tools.marShell.pack", "2.0.4", "jar", repos);
        System.out.println(f.getAbsolutePath());
        Assert.assertNotSame(null, f);
    }



}
