package org.kevoree.library.webserver;

import org.kevoree.annotation.*;
import org.kevoree.library.javase.webserver.AbstractWebServer;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.webserver.internal.KTinyWebServerInternalServe;
import org.kevoree.library.webserver.tjws.RequestHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/04/12
 * Time: 16:28
 */

@ComponentType
public class KTinyWebServer extends AbstractWebServer implements Runnable {

    private KTinyWebServerInternalServe srv = null;
    private Thread mainT = null;
    private RequestHandler handler = null;


    public void start() {
        handler = new RequestHandler(this);
        handler.start();
        srv = new KTinyWebServerInternalServe();
        java.util.Properties properties = new java.util.Properties();
        properties.put("port", Integer.parseInt(getDictionary().get("port").toString()));
        properties.setProperty(Acme.Serve.Serve.ARG_NOHUP, "nohup");
        srv.arguments = properties;
        mainT = new Thread(this);
        mainT.start();

        srv.addServlet("/*", new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                KevoreeHttpResponse res = handler.sendAndWait((KevoreeHttpRequest) req);
                if (res.getRawContent() != null) {
                    resp.getOutputStream().write(res.getRawContent());
                } else {
                    resp.getOutputStream().write(res.getContent().getBytes());
                }
            }
        });

    }

    public void stop() {
        srv.notifyStop();
        srv.destroyAllServlets();

        handler.$bang(false);
        mainT.interrupt();
    }

    public void update() {
        stop();
        start();
    }

    @Override
    public void responseHandler(Object param) {
        System.out.println(param);

        if (param instanceof KevoreeHttpResponse) {
            handler.internalSend((KevoreeHttpResponse)param);
        }
    }

    @Override
    public void run() {
        srv.serve();
    }
}
