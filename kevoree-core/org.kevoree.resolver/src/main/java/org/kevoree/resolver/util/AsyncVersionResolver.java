package org.kevoree.resolver.util;

import org.kevoree.resolver.api.MavenArtefact;
import org.kevoree.resolver.api.MavenVersionResult;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 18/06/13
 * Time: 15:13
 */
public class AsyncVersionResolver implements Callable<MavenVersionResult> {

    private MavenArtefact artefact = null;
    private String url = null;
    private MavenVersionResolver versionResolver = null;

    public AsyncVersionResolver(MavenArtefact a, String u, MavenVersionResolver vr) {
        this.artefact = a;
        this.url = u;
        this.versionResolver = vr;
    }

    @Override
    public MavenVersionResult call() throws Exception {
        try {
            return versionResolver.resolveVersion(this.artefact, this.url, false);
        } catch (IOException e) {
            //not found remotely, ignore
            return null;
        }
    }

}
