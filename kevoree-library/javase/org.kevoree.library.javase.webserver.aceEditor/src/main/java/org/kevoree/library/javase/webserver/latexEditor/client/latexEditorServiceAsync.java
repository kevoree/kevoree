package org.kevoree.library.javase.webserver.latexEditor.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface latexEditorServiceAsync {
    void getMessage(String msg, AsyncCallback<String> async);
}
