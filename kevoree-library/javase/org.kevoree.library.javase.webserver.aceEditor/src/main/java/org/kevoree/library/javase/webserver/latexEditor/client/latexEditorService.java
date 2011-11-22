package org.kevoree.library.javase.webserver.latexEditor.client;

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
