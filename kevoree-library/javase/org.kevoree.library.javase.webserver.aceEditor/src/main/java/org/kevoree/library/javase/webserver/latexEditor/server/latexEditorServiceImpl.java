package org.kevoree.library.javase.webserver.latexEditor.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.kevoree.library.javase.webserver.latexEditor.client.latexEditorService;

public class latexEditorServiceImpl extends RemoteServiceServlet implements latexEditorService {
    // Implementation of sample interface method
    public String getMessage(String msg) {
        return "Client said: \"" + msg + "\"<br>Server answered: \"Hi!\"";
    }
}