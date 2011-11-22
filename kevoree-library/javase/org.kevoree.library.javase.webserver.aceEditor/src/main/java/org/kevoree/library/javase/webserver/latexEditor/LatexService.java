package org.kevoree.library.javase.webserver.latexEditor;

import org.kevoree.library.javase.fileSystem.FilesService;
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


    public static boolean checkService(LatexEditor editor, KevoreeHttpRequest request, KevoreeHttpResponse response) {

        FilesService portService = editor.getPortByName("files", FilesService.class);

        boolean result = false;
        if (request.getUrl().endsWith("flatfiles")) {
            Set<String> flatFiles = portService.getFilesPath();
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

                System.out.println(request.getResolvedParams().get("file"));

                byte[] content = portService.getFileContent(request.getResolvedParams().get("file"));
                if (content.length > 0) {
                    response.setRawContent(content);
                    return true;
                } else {
                    logger.debug("No file exist = {}", request.getResolvedParams().get("file"));
                }
            } else {
                logger.debug("No file parameter detected");
            }
            return false;
        }
        return result;
    }

}
