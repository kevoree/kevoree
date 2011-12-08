package org.kevoree.library.javase.webserver.latexEditor.client;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 08/12/11
 * Time: 10:51
 * To change this template use File | Settings | File Templates.
 */
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("latexEditorService")
public interface latexEditorService extends RemoteService {
    // Sample interface method of remote interface
    String getMessage(String msg);

    /**
     * Utility/Convenience class.
     * Use latexEditorService.App.getInstance() to access static instance of latexEditorServiceAsync
     */
    public static class App {
        private static latexEditorServiceAsync ourInstance = GWT.create(latexEditorService.class);

        public static synchronized latexEditorServiceAsync getInstance() {
            return ourInstance;
        }
    }
}
