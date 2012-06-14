package org.kevoree.library.javase.vertx;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 14/06/12
 * Time: 16:26
 */
public class HelloVertx {

    public static void main(String[] args){
        Vertx vertxServer = Vertx.newVertx();
        vertxServer.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest req) {
                String file = req.path.equals("/") ? "index.html" : req.path;
                req.response.sendFile("webroot/" + file);
            }
        }).listen(8080);


    }


}
