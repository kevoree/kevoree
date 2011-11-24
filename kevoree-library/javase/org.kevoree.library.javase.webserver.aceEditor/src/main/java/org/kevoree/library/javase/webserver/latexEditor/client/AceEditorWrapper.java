package org.kevoree.library.javase.webserver.latexEditor.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 24/11/11
 * Time: 11:28
 * To change this template use File | Settings | File Templates.
 */
public class AceEditorWrapper {

    public static native void setText(String text) /*-{
        $wnd.aceEditor.getSession().setValue(text);
    }-*/;


}
