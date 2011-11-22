package org.kevoree.library.javase.webserver.latexEditor;

import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 21/11/11
 * Time: 23:02
 * To change this template use File | Settings | File Templates.
 */
public class LatexService {

    private static Logger logger = LoggerFactory.getLogger(LatexService.class);

    //TO CHANGE
    private static String baseURL = "/Users/duke/Documents/PapersDev/JSS 2011/articles/2011/JSS 2011";

    private static Set<String> getFlatFiles(File base, String relativePath) {
        Set<String> files = new HashSet<String>();
        if (base.exists()) {
            if (base.isDirectory()) {
                File[] childs = base.listFiles();
                for (int i = 0; i < childs.length; i++) {
                    files.addAll(getFlatFiles(childs[i], relativePath + "/" + base.getName()));
                }
            } else {
                files.add(relativePath + "/" + base.getName());
            }
        }
        return files;
    }

    public static boolean checkService(LatexEditor editor, KevoreeHttpRequest request, KevoreeHttpResponse response) {
        boolean result = false;
        if (request.getUrl().endsWith("flatfiles")) {
            Set<String> flatFiles = getFlatFiles(new File(baseURL), "/");
            StringBuilder csvResult = new StringBuilder();
            for (String ff : flatFiles) {
                if (csvResult.length() != 0) {
                    csvResult.append(";");
                }
                csvResult.append(ff);
            }
            response.setContent(csvResult.toString());
            return true;
        }
        if (request.getUrl().endsWith("flatfile")) {
            if (request.getResolvedParams().containsKey("file")) {
                File f = new File(baseURL + request.getResolvedParams().get("file"));
                if (f.exists()) {
                    try {
                        response.setContent(new String(FileServiceHelper.convertStream(new FileInputStream(f))));
                    } catch (Exception e) {
                        logger.error("Error while getting file ",e);
                    }
                }
            }
            return false;
        }
        return result;
    }

}
