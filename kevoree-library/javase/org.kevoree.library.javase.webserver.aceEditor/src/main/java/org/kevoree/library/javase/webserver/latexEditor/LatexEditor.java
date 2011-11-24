package org.kevoree.library.javase.webserver.latexEditor;

import org.kevoree.annotation.*;
import org.kevoree.library.javase.fileSystem.FilesService;
import org.kevoree.library.javase.webserver.AbstractPage;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 17/10/11
 * Time: 09:29
 * To change this template use File | Settings | File Templates.
 */
@ComponentType
@MessageTypes({
        @MessageType(name = "COMPILE",
                elems = {@MsgElem(name = "id", className = UUID.class), @MsgElem(name = "file",
                        className = String.class)}),
        @MessageType(name = "COMPILE_CALLBACK",
                elems = {@MsgElem(name = "id", className = UUID.class), @MsgElem(name = "path",
                        className = String.class), @MsgElem(name = "log", className = String.class), @MsgElem(
                        name = "success", className = boolean.class)})
})
@Requires({
        @RequiredPort(name = "saveFile", type = PortType.MESSAGE, optional = true),
        @RequiredPort(name = "files", type = PortType.SERVICE, className = FilesService.class),
        @RequiredPort(name = "compile", type = PortType.MESSAGE, optional = true,messageType = "COMPILE")
})
@Provides({
   @ProvidedPort(name = "comileCallback", type = PortType.MESSAGE, messageType = "COMPILE_CALLBACK")
})
public class LatexEditor extends AbstractPage {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Port(name = "comileCallback")
    public void compileCallback(Object o){
        logger.debug("Compilation complete ;-) "+o);
    }

    @Override
    public KevoreeHttpResponse process(KevoreeHttpRequest request, KevoreeHttpResponse response) {

        if (LatexService.checkService(this, request, response)) {
            return response;
        }
        if (FileServiceHelper.checkStaticFile("latexEditor.html", this, request, response)) {
            return response;
        }
        response.setContent("Bad request");

        return response;
    }


}

