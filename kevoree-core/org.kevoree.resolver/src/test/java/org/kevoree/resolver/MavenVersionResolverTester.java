package org.kevoree.resolver;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.resolver.api.MavenArtefact;
import org.kevoree.resolver.api.MavenVersionResult;
import org.kevoree.resolver.util.MavenVersionResolver;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/06/13
 * Time: 14:02
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class MavenVersionResolverTester {

    @Test
    public void testfoundMaxVersion() throws IOException {
        MavenResolver resolver = new MavenResolver();
        File result = resolver.resolve("org.kevoree","org.kevoree.core","RELEASE","jar", Arrays.asList("http://maven.kevoree.org/release"));
        Assert.assertTrue("RELEASE", result.getAbsolutePath().contains("ALPHA"));


        File result2 = resolver.resolve("org.kevoree","org.kevoree.core","LATEST","jar", Arrays.asList("http://maven.kevoree.org/snapshots"));
        System.out.println(result2.getCanonicalPath());
        Assert.assertTrue("SNAPSHOT", result2.getAbsolutePath().contains("SNAPSHOT"));

        /*

        artifact = new MavenArtefact();
        artifact.setGroup("org.kevoree");
        artifact.setName("org.kevoree.core");
       // artifact = resolver.foundMaxVersion(artifact, "http://maven.kevoree.org/snapshots", false, true);

        System.out.println(artifact.getVersion());
        Assert.assertEquals(null, artifact.getVersion());

        artifact = new MavenArtefact();
        artifact.setGroup("org.kevoree");
        artifact.setName("org.kevoree.core");
      //  artifact = resolver.foundMaxVersion(artifact, "http://maven.kevoree.org/release", false, false);

        System.out.println(artifact.getVersion());
        Assert.assertEquals("2.0.0-ALPHA", artifact.getVersion());

        artifact = new MavenArtefact();
        artifact.setGroup("org.kevoree");
        artifact.setName("org.kevoree.core");
      //  artifact = resolver.foundMaxVersion(artifact, "http://maven.kevoree.org/snapshots", false, false);

        System.out.println(artifact.getVersion());
        Assert.assertEquals("2.0.0-SNAPSHOT", artifact.getVersion());

        // local resolution => is it possible ?
        /*artifact = new MavenArtefact();
        artifact.setGroup("org.kevoree");
        artifact.setName("org.kevoree.core");
        artifact = resolver.foundMaxVersion(artifact, System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository", true, true);

        System.out.println(artifact.getVersion());
        Assert.assertEquals("2.0.0-ALPHA", artifact.getVersion());

        artifact = new MavenArtefact();
        artifact.setGroup("org.kevoree");
        artifact.setName("org.kevoree.core");
        artifact = resolver.foundMaxVersion(artifact, System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository", true, true);

        System.out.println(artifact.getVersion());
        Assert.assertEquals(null, artifact.getVersion());

        artifact = new MavenArtefact();
        artifact.setGroup("org.kevoree");
        artifact.setName("org.kevoree.core");
        artifact = resolver.foundMaxVersion(artifact, System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository", true, false);

        System.out.println(artifact.getVersion());
        Assert.assertEquals("2.0.0-ALPHA", artifact.getVersion());

        artifact = new MavenArtefact();
        artifact.setGroup("org.kevoree");
        artifact.setName("org.kevoree.core");
        artifact = resolver.foundMaxVersion(artifact, System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository", true, false);

        System.out.println(artifact.getVersion());
        Assert.assertEquals("2.0.0-SNAPSHOT", artifact.getVersion());*/
    }

}
