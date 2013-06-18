package org.kevoree.resolver.util;

import org.kevoree.resolver.api.MavenArtefact;

import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 18/06/13
 * Time: 17:23
 */
public class AsyncVersionFiller implements Callable<String> {

    private MavenVersionResolver versionResolver = null;
    private MavenArtefact artefact = null;
    private String url = null;

    public AsyncVersionFiller(MavenVersionResolver r, MavenArtefact a, String u) {
        this.versionResolver = r;
        this.artefact = a;
        this.url = u;
    }

    @Override
    public String call() {
        return versionResolver.foundRelevantVersion(artefact, url, false);
    }
}
