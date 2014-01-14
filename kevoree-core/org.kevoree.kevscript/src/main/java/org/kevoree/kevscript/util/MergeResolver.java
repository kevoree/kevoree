package org.kevoree.kevscript.util;

import org.kevoree.ContainerRoot;
import org.kevoree.Repository;
import org.kevoree.compare.DefaultModelCompare;
import org.kevoree.loader.JSONModelLoader;
import org.kevoree.log.Log;
import org.kevoree.resolver.MavenResolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 17:11
 */
public class MergeResolver {

    private static MavenResolver resolver = new MavenResolver();
    private static JSONModelLoader loader = new JSONModelLoader();
    private static DefaultModelCompare compare = new DefaultModelCompare();

    public static void merge(ContainerRoot model, String type, String url) {
        if (type.equals("mvn")) {
            Set<String> urls = new HashSet<>();
            for (Repository repo : model.getRepositories()) {
                urls.add(repo.getUrl());
            }
            String request = type.trim() + ":" + url.trim();
            File resolved = resolver.resolve(request, urls);
            if (resolved != null && resolved.exists()) {
                try {
                    JarFile jar = new JarFile(new File(resolved.getAbsolutePath()));
                    JarEntry entry = jar.getJarEntry("KEV-INF/lib.json");
                    if (entry != null) {
                        ContainerRoot remoteModel = (ContainerRoot) loader.loadModelFromStream(jar.getInputStream(entry)).get(0);
                        compare.merge(model, remoteModel).applyOn(model);
                    }
                } catch (IOException e) {
                    Log.error("Bad JAR file ", e);
                }
            } else {
                Log.warn("Not resolved typeDef {}", request);
            }
        }
    }

}
