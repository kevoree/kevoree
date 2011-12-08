package org.kevoree.library.javase.webserver.latexEditor.client;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 08/12/11
 * Time: 10:51
 * To change this template use File | Settings | File Templates.
 */
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface latexEditorServiceAsync {
    void getMessage(String msg, AsyncCallback<String> async);
}
