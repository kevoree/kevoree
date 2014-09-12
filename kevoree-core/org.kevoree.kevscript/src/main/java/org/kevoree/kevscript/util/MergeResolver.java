package org.kevoree.kevscript.util;

import org.kevoree.ContainerRoot;
import org.kevoree.Repository;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.compare.ModelCompare;
import org.kevoree.modeling.api.json.JSONModelLoader;
import org.kevoree.resolver.MavenResolver;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
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
    private static KevoreeFactory factory = new DefaultKevoreeFactory();
    private static JSONModelLoader loader = new JSONModelLoader(factory);
    private static ModelCompare compare = new ModelCompare(factory);

    public static void merge(ContainerRoot model, String type, String url) throws Exception {
        if (type.equals("mvn")) {
            Set<String> urls = new HashSet<String>();
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
                        try {
                            ContainerRoot remoteModel = (ContainerRoot) loader.loadModelFromStream(jar.getInputStream(entry)).get(0);
                            compare.merge(model, remoteModel).applyOn(model);
                        } catch (Exception e) {
                            Log.warn("Error while Merging", e);
                            throw new Exception("KevScript error while merging " + resolved.getAbsolutePath() + " resolved from " + url);
                        }

                    }
                } catch (IOException e) {
                    Log.error("Bad JAR file, resolved from {}", url, e);
                }
            } else {
                Log.warn("Not resolved typeDef {}", request);
            }
        }
    }

}
