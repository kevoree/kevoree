package org.kevoree.resolver.util;

import org.kevoree.resolver.api.MavenArtefact;
import org.kevoree.resolver.api.MavenVersionResult;

import java.io.*;
import java.net.URL;

/**
 * Created by duke on 16/05/13.
 */
public class MavenVersionResolver {

    private static final String buildMavenTag = "<buildNumber>";
    private static final String buildEndMavenTag = "</buildNumber>";

    private static final String timestampMavenTag = "<timestamp>";
    private static final String timestampEndMavenTag = "</timestamp>";

    private static final String lastUpdatedMavenTag = "<lastUpdated>";
    private static final String lastUpdatedEndMavenTag = "</lastUpdated>";

    public static final String metaFile = "maven-metadata.xml";
    private static final String localmetaFile = "maven-metadata-local.xml";

    public MavenVersionResult resolveVersion(MavenArtefact artefact, String basePath, boolean localDeploy) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(basePath);
        String sep = File.separator;
        if(basePath.startsWith("http")){
            sep = "/";
        }
        if (!basePath.endsWith(sep)) {
            builder.append(sep);
        }
        if(basePath.startsWith("http")){
            builder.append(artefact.getGroup().replace(".","/"));
        } else {
            builder.append(artefact.getGroup().replace(".",File.separator));
        }
        builder.append(sep);
        builder.append(artefact.getName());
        builder.append(sep);
        builder.append(artefact.getVersion());
        builder.append(sep);
        if(localDeploy){
            builder.append(localmetaFile);
        } else {
            builder.append(metaFile);
        }
        URL metadataURL = new URL("file:///"+builder.toString());
        if(basePath.startsWith("http")){
            metadataURL = new URL(builder.toString());
        }
        InputStream in = metadataURL.openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String result, line = reader.readLine();
        result = line;
        while ((line = reader.readLine()) != null) {
            result += line;
        }
        MavenVersionResult versionResult = new MavenVersionResult();
        if (result.contains(lastUpdatedMavenTag) && result.contains(lastUpdatedEndMavenTag)) {
            versionResult.setTimestamp(result.substring(result.indexOf(lastUpdatedMavenTag) + lastUpdatedMavenTag.length(), result.indexOf(lastUpdatedEndMavenTag)));
        }
        if (result.contains(timestampMavenTag) && result.contains(timestampEndMavenTag)) {
            versionResult.setTimestamp(result.substring(result.indexOf(timestampMavenTag) + timestampMavenTag.length(), result.indexOf(timestampEndMavenTag)));
        }
        if (result.contains(buildMavenTag) && result.contains(buildEndMavenTag)) {
            versionResult.setBuildNumber(result.substring(result.indexOf(buildMavenTag) + buildMavenTag.length(), result.indexOf(buildEndMavenTag)));
        }
        versionResult.setUrl_origin(basePath);
        return versionResult;
    }

}
