package org.kevoree.library.javase.webserver.latexEditor.server;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 08/12/11
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.kevoree.library.javase.webserver.latexEditor.client.latexEditorService;

public class latexEditorServiceImpl extends RemoteServiceServlet implements latexEditorService {
    // Implementation of sample interface method
    public String getMessage(String msg) {
        System.out.println(msg.toString());
        
        return "Client said: \"" + msg + "\"<br>Server answered: \"Hi!\"";
    }
}
