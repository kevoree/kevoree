package org.kevoree.library.javase.webserver.latexEditor.client;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 24/11/11
 * Time: 11:28
 * To change this template use File | Settings | File Templates.
 */
public class AceEditorWrapper {


    public static native String getText() /*-{
        return $wnd.aceEditor.getSession().getValue();
    }-*/;

    public static native void setText(String text) /*-{
        $wnd.aceEditor.getSession().setValue(text);
    }-*/;

    public static native void addOnChangeHandler(AceEditorCallback callback) /*-{
        $wnd.aceEditor.getSession().on("change", function (e) {
            callback.@org.kevoree.library.javase.webserver.latexEditor.client.AceEditorCallback::invokeAceCallback(Lcom/google/gwt/core/client/JavaScriptObject;)(e);
        });
    }-*/;

    public static native void setReadOnly(boolean readOnly) /*-{
        $wnd.aceEditor.setReadOnly(readOnly);
    }-*/;

}
