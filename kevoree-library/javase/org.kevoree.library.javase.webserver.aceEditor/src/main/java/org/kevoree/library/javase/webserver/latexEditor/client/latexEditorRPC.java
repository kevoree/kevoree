package org.kevoree.library.javase.webserver.latexEditor.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 24/11/11
 * Time: 19:44
 * To change this template use File | Settings | File Templates.
 */
public class latexEditorRPC {

    public static void callForSave(latexEditorFileExplorer explorer) {
        String url = GWT.getModuleBaseURL() + "save?file=" + explorer.getSelectedFilePath();
        //TODO SEND AND CONPUTE HASH

        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, URL.encode(url));
        try {
            builder.sendRequest(AceEditorWrapper.getText(), new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    Window.alert("Error while connecting to server");
                }

                public void onResponseReceived(Request request, Response response) {
                    //TODO CHECK RESULT

                }
            });

        } catch (Exception e) {
            Window.alert("Error while connecting to server");
        }
    }

    public static void callForCompile(final latexEditorFileExplorer explorer) {
        final String selectedPath = explorer.getSelectedCompileRootFilePath();
        if (selectedPath.equals("") || selectedPath.equals(null)) {
            Window.alert("Please select root file");
            return;
        }


        final String url = GWT.getModuleBaseURL() + "compile?file=" + selectedPath;
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    Window.alert("Error while connecting to server");
                }

                final JavaScriptObject window = newWindow("", null, null);

                public void onResponseReceived(Request request, Response response) {

                    if (response.getStatusCode() == 200) {
                        final String url = GWT.getModuleBaseURL() + "compileresult?uuid=" + response.getText();
                        final RequestBuilder builder2 = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
                        final boolean[] compileresult = {false};
                        Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
                            @Override
                            public boolean execute() {
                                if (compileresult[0]) {
                                    return false;
                                }
                                try {
                                    builder2.sendRequest(null, new RequestCallback() {
                                        public void onError(Request request, Throwable exception) {
                                            Window.alert("Error while connecting to server");
                                        }

                                        public void onResponseReceived(Request request, Response response) {
                                            if (response.getStatusCode() == 200 && !response.getText().equals("waiting")) {
                                                compileresult[0] = true;
                                                String[] resultCompile = response.getText().split(";");
                                                if (resultCompile.length == 2) {
                                                    if (resultCompile[0].equals("true")) {
                                                        String pdfpath = GWT.getModuleBaseURL() + "flatfile?file=" + selectedPath.replace(".tex", ".pdf");
                                                        setWindowTarget(window, pdfpath);
                                                    } else {
                                                        String pdfpath = GWT.getModuleBaseURL() + "flatfile?file=" + selectedPath.replace(".tex", ".log");
                                                        setWindowTarget(window, pdfpath);
                                                    }
                                                }
                                            }


                                        }
                                    });
                                } catch (Exception e) {
                                    Window.alert("Error while connecting to server");
                                }
                                return true;
                            }
                        }, 1500);

                    }
                }
            });
        } catch (Exception e) {
            Window.alert("Error while connecting to server");
        }
    }


    public static native JavaScriptObject newWindow(String url, String
            name, String features)/*-{
        var window = $wnd.open(url, name, features);
        return window;
    }-*/;

    public static native void setWindowTarget(JavaScriptObject window,
                                              String target)/*-{
        window.location = target;
    }-*/;


}
