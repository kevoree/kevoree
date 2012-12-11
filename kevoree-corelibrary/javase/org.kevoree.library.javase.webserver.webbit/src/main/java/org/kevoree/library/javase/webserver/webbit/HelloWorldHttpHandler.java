package org.kevoree.library.javase.webserver.webbit;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 11/12/12
 */
public class HelloWorldHttpHandler implements HttpHandler {
    @Override
    public void handleHttpRequest(HttpRequest httpRequest, HttpResponse httpResponse, HttpControl httpControl) throws Exception {

        File tf = File.createTempFile("mmmmmmmmmmmmmmm","oooooooooooo");
        tf.delete();
        httpResponse.header("Content-type", "text/html")
                .content("<html><body>Hello world!</body></html>")
                .end();
    }
}
