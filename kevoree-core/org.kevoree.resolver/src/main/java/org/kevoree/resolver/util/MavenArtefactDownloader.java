package org.kevoree.resolver.util;

import org.kevoree.resolver.api.MavenArtefact;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 21/05/13
 * Time: 12:13
 */
public class MavenArtefactDownloader {

    private static String urlSep = "/";

    public boolean download(File targetFile, String url, MavenArtefact artefact, String extension, String preresolvedVersion, boolean metafile) {
        try {
            //BUILD HTTP URL
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(url);
            urlBuilder.append(urlSep);
            urlBuilder.append(artefact.getGroup());
            urlBuilder.append(urlSep);
            urlBuilder.append(artefact.getName());
            urlBuilder.append(urlSep);
            urlBuilder.append(artefact.getVersion());
            urlBuilder.append(urlSep);

            if (metafile) {
                urlBuilder.append(MavenVersionResolver.metaFile);
            } else {
                urlBuilder.append(artefact.getName());
                urlBuilder.append("-");
                if (preresolvedVersion != null) {
                    urlBuilder.append(preresolvedVersion);
                } else {
                    urlBuilder.append(artefact.getVersion());
                }
                urlBuilder.append(".");
                urlBuilder.append(extension);
            }

            //DOWNLOAD FILE
            URL artefactURL = new URL(urlBuilder.toString());
            ReadableByteChannel rbc = Channels.newChannel(artefactURL.openStream());
            FileOutputStream fos = new FileOutputStream(targetFile);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            fos.close();
            return true;
        } catch (Throwable t) {
            //TODO CLEANUP FILE IF CORRUPTED
            return false;
        }
    }

}
