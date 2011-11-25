package org.kevoree.library.javase.webserver.latexEditor;

import org.kevoree.framework.MessagePort;
import org.kevoree.framework.message.StdKevoreeMessage;
import org.kevoree.library.javase.fileSystem.FilesService;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

        if (request.getUrl().endsWith("save")) {

            if (request.getResolvedParams().containsKey("file") && request.getRawBody() != null) {
                boolean saveResult = portService.saveFile(request.getResolvedParams().get("file"), request.getRawBody());
                if (!saveResult) {
                    logger.debug("Error while saving file = {}", request.getResolvedParams().get("file"));
                }
            } else {
                logger.debug("No file parameter detected");
            }
            return false;
        }
        if (request.getUrl().endsWith("compile")) {
            //REQUEST ABSOLUTE PATH
            if (request.getResolvedParams().containsKey("file") && editor.isPortBinded("compile")) {
                String absolutePath = portService.getAbsolutePath(request.getResolvedParams().get("file"));
                StdKevoreeMessage message = new StdKevoreeMessage();
                message.putValue("file", absolutePath);
                //CREATE TEMP UUID
                UUID compileID = UUID.randomUUID();
                message.putValue("id", compileID);
                editor.getPortByName("compile", MessagePort.class).process(message);
                result = true;
            }
        }
        if (request.getUrl().endsWith("flatfiles")) {
            Set<String> extensions = new HashSet<String>();
            extensions.add("tex");
            extensions.add("cls");
            extensions.add("txt");
            extensions.add("sty");
            extensions.add("bst");
            extensions.add("bib");
            Set<String> flatFiles = portService.getFilteredFilesPath(extensions);
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
