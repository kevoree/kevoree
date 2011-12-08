package org.kevoree.library.javase.webserver.latexEditor;

import org.kevoree.framework.MessagePort;
import org.kevoree.framework.message.StdKevoreeMessage;
import org.kevoree.library.javase.fileSystem.LockFilesService;
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

        LockFilesService portService = editor.getPortByName("files", LockFilesService.class);

        boolean result = false;
        if (request.getUrl().endsWith("compileresult")) {
            if (request.getResolvedParams().containsKey("uuid")) {
                if (editor.waitingID.contains(request.getResolvedParams().get("uuid"))) {
                    response.setContent("waiting");
                    result = true;
                } else {
                    response.setContent(editor.compileResult.get(request.getResolvedParams().get("uuid")) + ";" + editor.compileLog.get(request.getResolvedParams().get("uuid")));
                    editor.compileResult.remove(request.getResolvedParams().get("uuid"));
                    editor.compileLog.remove(request.getResolvedParams().get("uuid"));
                    result = true;
                }
            }
        }
        if (request.getUrl().endsWith("save")) {
            if (request.getResolvedParams().containsKey("file") && request.getRawBody() != null) {
                boolean saveResult = portService.saveFile(request.getResolvedParams().get("file"), request.getRawBody(),true);
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
                editor.waitingID.add(compileID.toString());
                editor.getPortByName("compile", MessagePort.class).process(message);
                response.setContent(compileID.toString());
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

                boolean lock=false;
                if("true".equals(request.getResolvedParams().get("lock"))){
                    lock = true;
                }
                byte[] content = portService.getFileContent(request.getResolvedParams().get("file"),lock);
                if (content.length > 0) {
                    response.setRawContent(content);
                    if (request.getResolvedParams().get("file").endsWith(".pdf")) {
                        response.setContentType("application/pdf");
                    }
                    if (request.getResolvedParams().get("file").endsWith(".log")) {
                        response.setContentType("text/plain");
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
