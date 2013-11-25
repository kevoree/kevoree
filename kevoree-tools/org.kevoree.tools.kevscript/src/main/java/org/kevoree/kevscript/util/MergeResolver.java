package org.kevoree.kevscript.util;

import org.kevoree.ContainerRoot;
import org.kevoree.Repository;
import org.kevoree.resolver.MavenResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 17:11
 */
public class MergeResolver {

    private static MavenResolver resolver = new MavenResolver();

    public static void merge(ContainerRoot model, String type, String url) {
        if (type.equals("mvn")) {
            List<String> urls = new ArrayList<String>();
            for (Repository repo : model.getRepositories()) {
                urls.add(repo.getUrl());
            }

        }
    }

}
