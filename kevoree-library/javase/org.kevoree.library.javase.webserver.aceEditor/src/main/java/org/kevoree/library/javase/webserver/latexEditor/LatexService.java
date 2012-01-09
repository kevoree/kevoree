package org.kevoree.library.javase.webserver.latexEditor;

import org.kevoree.library.javase.fileSystem.LockFilesService;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 21/11/11
 * Time: 23:02
 */
public class LatexService {

    private static Logger logger = LoggerFactory.getLogger(LatexService.class);

    public static boolean checkService(LatexEditor editor, KevoreeHttpRequest request, KevoreeHttpResponse response) {

        LockFilesService portService = editor.getPortByName("files", LockFilesService.class);

        boolean result = false;
        if (request.getUrl().endsWith("rawfile")) {
            if (request.getResolvedParams().containsKey("file")) {
                byte[] content = portService.getFileContent(request.getResolvedParams().get("file"),false);
                if (content.length > 0) {
                    response.setRawContent(content);
                    if (request.getResolvedParams().get("file").endsWith(".pdf")) {
//                        response.setContentType("application/pdf");
						response.getHeaders().put("Content-Type", "application/pdf");
                    }
                    if (request.getResolvedParams().get("file").endsWith(".log")) {
//                        response.setContentType("text/plain");
						response.getHeaders().put("Content-Type", "text/plain");
                    }
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
