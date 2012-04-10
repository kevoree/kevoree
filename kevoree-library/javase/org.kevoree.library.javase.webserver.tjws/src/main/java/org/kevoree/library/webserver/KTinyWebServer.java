package org.kevoree.library.webserver;

import org.kevoree.annotation.*;
import org.kevoree.library.javase.webserver.AbstractWebServer;
import org.kevoree.library.webserver.internal.KTinyWebServerInternalServe;

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

    public void start() {
        srv = new KTinyWebServerInternalServe();
        java.util.Properties properties = new java.util.Properties();
        properties.put("port", Integer.parseInt(getDictionary().get("port").toString()));
        properties.setProperty(Acme.Serve.Serve.ARG_NOHUP, "nohup");
        srv.arguments = properties;
        mainT = new Thread(this);
        mainT.start();

        srv.addServlet("/*",new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                System.out.println("Waiting ");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("After Wait");
                resp.getOutputStream().write("Hello".getBytes());
            }
        });

    }

    public void stop() {
       mainT.interrupt();
    }

    public void update() {
        stop();
        start();
    }

    @Override
    public void responseHandler(Object param) {

    }


    @Override
    public void run() {
        srv.serve();
    }
}
