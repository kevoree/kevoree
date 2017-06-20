package org.kevoree.resolver;

import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by leiko on 2/28/17.
 */
public class TestMavenResolver {

    @Test
    @Ignore
    public void testDefault() throws Exception {
        MavenResolver resolver = new MavenResolver.Builder().build();
        List<RemoteRepository> repos = new ArrayList<>();
        repos.add(new RemoteRepository.Builder("snapshots", "default", "https://oss.sonatype.org/content/repositories/snapshots").build());
        PreorderNodeListGenerator nlg = resolver
                .resolve("org.kevoree.library:org.kevoree.library.javaNode:5.5.0-SNAPSHOT", repos);

        for (File f : nlg.getFiles()) {
            System.out.println(f.getAbsolutePath());
        }
    }
}
