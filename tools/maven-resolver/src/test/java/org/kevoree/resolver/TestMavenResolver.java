package org.kevoree.resolver;

import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * Created by leiko on 2/28/17.
 */
public class TestMavenResolver {

    @Test
		@Ignore
    public void testDefault() throws IOException {
        ConfigurableMavenResolverSystem resolver = MavenResolver.get();
        File javaNodeJar = resolver
                .resolve("org.kevoree.library.java:org.kevoree.library.java.javaNode:5.5.0-SNAPSHOT")
                .withoutTransitivity()
                .asSingleFile();

        JarFile jar = new JarFile(javaNodeJar);
        JarEntry mainClass = jar.getJarEntry("org/kevoree/library/JavaNode.class");
        Assert.assertNotNull(mainClass);
    }
}
