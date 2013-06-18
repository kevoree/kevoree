package org.kevoree.resolver.util;

import org.kevoree.resolver.api.MavenArtefact;
import org.kevoree.resolver.api.MavenVersionResult;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by duke on 16/05/13.
 */
public class MavenVersionResolver {

    private static final String buildLatestTag = "<latest>";
    private static final String buildEndLatestTag = "</latest>";

    private static final String buildReleaseTag = "<release>";
    private static final String buildEndreleaseTag = "</release>";

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
        if (basePath.startsWith("http")) {
            sep = "/";
        }
        if (!basePath.endsWith(sep)) {
            builder.append(sep);
        }
        if (basePath.startsWith("http")) {
            builder.append(artefact.getGroup().replace(".", "/"));
        } else {
            builder.append(artefact.getGroup().replace(".", File.separator));
        }
        builder.append(sep);
        builder.append(artefact.getName());
        builder.append(sep);
        builder.append(artefact.getVersion());
        builder.append(sep);
        if (localDeploy) {
            builder.append(localmetaFile);
        } else {
            builder.append(metaFile);
        }
        URL metadataURL = new URL("file:///" + builder.toString());
        if (basePath.startsWith("http")) {
            metadataURL = new URL(builder.toString());
        }
        URLConnection c = metadataURL.openConnection();
        //c.setConnectTimeout(500);
        InputStream in = c.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder resultBuilder = new StringBuilder();
        String line = reader.readLine();
        resultBuilder.append(line);
        while ((line = reader.readLine()) != null) {
            resultBuilder.append(line);
        }
        String result = resultBuilder.toString();
        in.close();
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
        versionResult.setNotDeployed(localDeploy);
        return versionResult;
    }


    public String foundRelevantVersion(MavenArtefact artefact, String basePath, boolean localDeploy) {
        String askedVersion = artefact.getVersion().toLowerCase();
        Boolean release = false;
        Boolean lastest = false;
        if (askedVersion.contains("release")) {
            release = true;
        }
        if (askedVersion.contains("latest")) {
            lastest = true;
        }
        if (!release && !lastest) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(basePath);
        String sep = File.separator;
        if (basePath.startsWith("http")) {
            sep = "/";
        }
        if (!basePath.endsWith(sep)) {
            builder.append(sep);
        }
        if (basePath.startsWith("http")) {
            builder.append(artefact.getGroup().replace(".", "/"));
        } else {
            builder.append(artefact.getGroup().replace(".", File.separator));
        }
        builder.append(sep);
        builder.append(artefact.getName());
        builder.append(sep);

        if (localDeploy) {
            builder.append(localmetaFile);
        } else {
            builder.append(metaFile);
        }
        try {
            URL metadataURL = new URL("file:///" + builder.toString());
            if (basePath.startsWith("http")) {
                metadataURL = new URL(builder.toString());
            }
            URLConnection c = metadataURL.openConnection();
            InputStream in = c.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder resultBuilder = new StringBuilder();
            String line = reader.readLine();
            resultBuilder.append(line);
            while ((line = reader.readLine()) != null) {
                resultBuilder.append(line);
            }
            String result = resultBuilder.toString();
            in.close();
            if (release) {
                if (result.contains(buildReleaseTag) && result.contains(buildEndreleaseTag)) {
                    return result.substring(result.indexOf(buildReleaseTag) + buildReleaseTag.length(), result.indexOf(buildEndreleaseTag));
                }
            }
            if (lastest) {
                if (result.contains(buildLatestTag) && result.contains(buildEndLatestTag)) {
                    return result.substring(result.indexOf(buildLatestTag) + buildLatestTag.length(), result.indexOf(buildEndLatestTag));
                }
            }
        } catch (MalformedURLException ignored) {
        } catch (IOException ignored) {
        }
        return null;
    }


}
