package org.kevoree.library.javase.webserver.latexEditor.client;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 08/12/11
 * Time: 10:51
 * To change this template use File | Settings | File Templates.
 */
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;
import java.util.Set;

public interface latexEditorServiceAsync {

    void saveFile(String fileName, String content, AsyncCallback<Boolean> async);

    void getFlatFiles(AsyncCallback<Set<String>> async);

    void getFileContent(String fileName, Boolean lock, AsyncCallback<String> async);

    void compile(String fileName, AsyncCallback<String> async) throws Exception;

    void compileresult(String uuid, AsyncCallback<String[]> async);
}
